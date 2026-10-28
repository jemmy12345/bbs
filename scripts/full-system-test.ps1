param(
    [switch]$SkipBuild,
    [switch]$NoStart,
    [switch]$KeepRunning,
    [switch]$SkipMutatingTests
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir "..")
$BackendJar = Join-Path $ProjectRoot "deploy\bbs-admin.jar"
$ReportDir = Join-Path $ProjectRoot "runtime\test-reports"
$ReportPath = Join-Path $ReportDir ("full-system-test-{0}.json" -f (Get-Date -Format "yyyyMMdd-HHmmss"))
$BackendLog = Join-Path $ReportDir "backend-test.out.log"
$BackendErr = Join-Path $ReportDir "backend-test.err.log"
$FrontendLog = Join-Path $ReportDir "frontend-test.out.log"
$FrontendErr = Join-Path $ReportDir "frontend-test.err.log"

$JavaExe = "D:\LenovoSoftstore\Install\Eclipse Adoptium\jdk-17\bin\java.exe"
$MavenExe = "D:\LenovoSoftstore\Install\apache-maven-3.6.3\bin\mvn.cmd"
$RedisExe = "D:\LenovoSoftstore\Install\Redis\Redis\redis-server.exe"
$MysqlExe = "D:\LenovoSoftstore\Install\MySQL\bin\mysql.exe"

$BackendUrl = "http://localhost:9082"
$FrontendUrl = "http://localhost/bbs/"
$Results = New-Object System.Collections.Generic.List[object]
$StartedProcesses = New-Object System.Collections.Generic.List[object]

New-Item -ItemType Directory -Force -Path $ReportDir | Out-Null

function Add-Result {
    param(
        [string]$Name,
        [ValidateSet("PASS", "FAIL", "SKIP", "INFO")]
        [string]$Status,
        [string]$Detail = "",
        [object]$Data = $null
    )
    $safeData = $null
    if ($null -ne $Data) {
        try {
            $safeData = ($Data | ConvertTo-Json -Depth 20 -Compress)
            $safeData = $safeData -replace '"token"\s*:\s*"[^"]+"', '"token":"***"'
            $safeData = $safeData -replace '"img"\s*:\s*"[^"]+"', '"img":"***"'
            if ($safeData.Length -gt 4000) {
                $safeData = $safeData.Substring(0, 4000) + "...(truncated)"
            }
        }
        catch {
            $safeData = "[unserializable]"
        }
    }
    $item = [ordered]@{
        name = $Name
        status = $Status
        detail = $Detail
        time = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
        data = $safeData
    }
    $Results.Add([pscustomobject]$item) | Out-Null
    $prefix = switch ($Status) {
        "PASS" { "[PASS]" }
        "FAIL" { "[FAIL]" }
        "SKIP" { "[SKIP]" }
        default { "[INFO]" }
    }
    Write-Host "$prefix $Name $Detail"
}

function Wait-Port {
    param([int]$Port, [int]$TimeoutSeconds = 90)
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [string]$Token = $null,
        [int]$TimeoutSec = 60
    )
    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    $params = @{
        Method = $Method
        Uri = $Url
        Headers = $headers
        TimeoutSec = $TimeoutSec
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $params["ContentType"] = "application/json;charset=utf-8"
        $params["Body"] = ($Body | ConvertTo-Json -Depth 20)
    }
    $response = Invoke-WebRequest @params
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }
    try {
        return $response.Content | ConvertFrom-Json
    }
    catch {
        return [pscustomobject]@{
            code = $response.StatusCode
            raw = $response.Content
        }
    }
}

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [string]$Token = $null,
        [scriptblock]$Assert = $null,
        [int]$TimeoutSec = 60
    )
    try {
        $json = Invoke-Json -Method $Method -Url $Url -Body $Body -Token $Token -TimeoutSec $TimeoutSec
        if ($Assert) {
            & $Assert $json
        }
        Add-Result $Name "PASS" "" $json
        return $json
    }
    catch {
        Add-Result $Name "FAIL" $_.Exception.Message
        return $null
    }
}

function Get-Token {
    param([string]$Username)
    $body = @{
        username = $Username
        loginType = "sso"
    }
    $json = Test-Endpoint "登录获取令牌：$Username" "POST" "$BackendUrl/login" $body $null {
        param($r)
        if ($r.code -ne 200 -or [string]::IsNullOrWhiteSpace($r.token)) {
            throw "登录失败，返回 code=$($r.code)，msg=$($r.msg)"
        }
    }
    if ($json) { return $json.token }
    return $null
}

function Start-IfNeeded {
    if ($NoStart) {
        Add-Result "启动服务" "SKIP" "使用当前已运行服务"
        return
    }

    if (-not (Get-NetTCPConnection -LocalPort 6379 -State Listen -ErrorAction SilentlyContinue)) {
        if (Test-Path $RedisExe) {
            $p = Start-Process -FilePath $RedisExe -WorkingDirectory (Split-Path $RedisExe) -WindowStyle Hidden -PassThru
            $StartedProcesses.Add($p) | Out-Null
            Add-Result "启动 Redis" "INFO" "pid=$($p.Id)"
        }
        else {
            Add-Result "启动 Redis" "FAIL" "未找到 $RedisExe"
        }
    }

    if (-not (Get-NetTCPConnection -LocalPort 9082 -State Listen -ErrorAction SilentlyContinue)) {
        if (-not (Test-Path $BackendJar)) {
            Add-Result "启动后端" "FAIL" "未找到运行包 $BackendJar"
        }
        else {
            $java = if (Test-Path $JavaExe) { $JavaExe } else { "java" }
            $p = Start-Process -FilePath $java -ArgumentList @("-jar", $BackendJar) -WorkingDirectory (Join-Path $ProjectRoot "deploy") -RedirectStandardOutput $BackendLog -RedirectStandardError $BackendErr -WindowStyle Hidden -PassThru
            $StartedProcesses.Add($p) | Out-Null
            Add-Result "启动后端" "INFO" "pid=$($p.Id)"
        }
    }

    if (-not (Get-NetTCPConnection -LocalPort 80 -State Listen -ErrorAction SilentlyContinue)) {
        $frontendDir = Join-Path $ProjectRoot "frontend"
        $p = Start-Process -FilePath "cmd.exe" -ArgumentList @("/c", "npm run dev -- --port 80") -WorkingDirectory $frontendDir -RedirectStandardOutput $FrontendLog -RedirectStandardError $FrontendErr -WindowStyle Hidden -PassThru
        $StartedProcesses.Add($p) | Out-Null
        Add-Result "启动前端" "INFO" "pid=$($p.Id)"
    }
}

function Reset-TestDataWindow {
    $mysql = $null
    if (Test-Path $MysqlExe) {
        $mysql = $MysqlExe
    }
    else {
        $cmd = Get-Command mysql -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($cmd) { $mysql = $cmd.Source }
    }
    if (-not $mysql) {
        Add-Result "测试数据窗口清理" "SKIP" "未找到 mysql 命令"
        return
    }
    try {
        & $mysql -uroot -p123456 -D bbsdb -e "update bbs_idea_proposal set create_time = date_sub(create_time, interval 1 day) where submit_user_id in ('2','zhangsan','sam') and title like '自动测试提案 %' and is_draft = '0' and del_flag = '0' and date(create_time) = curdate();" 2>$null | Out-Null
        Add-Result "测试数据窗口清理" "PASS" "已避开历史自动测试提案的每日投稿限制"
    }
    catch {
        Add-Result "测试数据窗口清理" "FAIL" $_.Exception.Message
    }
}

try {
    Add-Result "测试开始" "INFO" "project=$ProjectRoot"

    if (-not $SkipBuild) {
        try {
            Push-Location $ProjectRoot
            & $MavenExe -pl backend/bbs-admin -am -DskipTests compile | Out-Null
            Add-Result "后端编译" "PASS"
        }
        catch {
            Add-Result "后端编译" "FAIL" $_.Exception.Message
        }
        finally {
            Pop-Location
        }
    }
    else {
        Add-Result "后端编译" "SKIP" "已按参数跳过"
    }

    Start-IfNeeded

    if (Wait-Port 6379 30) { Add-Result "Redis 端口" "PASS" "6379" } else { Add-Result "Redis 端口" "FAIL" "6379 未监听" }
    if (Wait-Port 9082 120) { Add-Result "后端端口" "PASS" "9082" } else { Add-Result "后端端口" "FAIL" "9082 未监听" }
    if (Wait-Port 80 120) { Add-Result "前端端口" "PASS" "80" } else { Add-Result "前端端口" "FAIL" "80 未监听" }

        Test-Endpoint "后端首页" "GET" "$BackendUrl/" $null $null {
        param($r)
        if ($null -eq $r -or $r.code -ne 200 -or $r.raw -notmatch "后台服务|Smart BBS") { throw "后端首页返回异常" }
    } | Out-Null

    try {
        $front = Invoke-WebRequest -Uri $FrontendUrl -UseBasicParsing -TimeoutSec 30
        if ($front.StatusCode -eq 200 -and $front.Content -match "<!DOCTYPE html|<html") {
            Add-Result "前端首页" "PASS" $FrontendUrl
        }
        else {
            Add-Result "前端首页" "FAIL" "状态码 $($front.StatusCode)，内容不像 HTML"
        }
    }
    catch {
        Add-Result "前端首页" "FAIL" $_.Exception.Message
    }

    Test-Endpoint "验证码接口" "GET" "$BackendUrl/captchaImage" $null $null {
        param($r)
        if ($r.code -ne 200) { throw "验证码接口返回 code=$($r.code)" }
    } | Out-Null

    $tokens = @{}
    foreach ($account in @("admin", "zhangsan", "l1reviewer", "l2reviewer", "l3reviewer")) {
        $token = Get-Token $account
        if ($token) { $tokens[$account] = $token }
    }

    $adminToken = $tokens["admin"]
    $userToken = $tokens["zhangsan"]

    if ($adminToken) {
        Test-Endpoint "当前用户信息" "GET" "$BackendUrl/getInfo" $null $adminToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.user) { throw "未返回用户信息" }
        } | Out-Null
        Test-Endpoint "后台路由信息" "GET" "$BackendUrl/getRouters" $null $adminToken {
            param($r)
            if ($r.code -ne 200) { throw "路由接口 code=$($r.code)" }
        } | Out-Null
        Test-Endpoint "帖子分类列表" "GET" "$BackendUrl/bbs/category/list?pageNum=1&pageSize=20" $null $adminToken {
            param($r)
            if ($null -eq $r.rows -and $null -eq $r.data) { throw "未返回列表数据" }
        } | Out-Null
        Test-Endpoint "帖子列表" "GET" "$BackendUrl/bbs/post/list?pageNum=1&pageSize=10" $null $adminToken {
            param($r)
            if ($null -eq $r.rows) { throw "未返回 rows" }
        } | Out-Null
        Test-Endpoint "评论管理列表" "GET" "$BackendUrl/bbs/commentManage/list?pageNum=1&pageSize=10" $null $adminToken {
            param($r)
            if ($null -eq $r.rows -and $null -eq $r.data) { throw "未返回列表数据" }
        } | Out-Null
        Test-Endpoint "敏感词列表" "GET" "$BackendUrl/bbs/sensitive/list?pageNum=1&pageSize=10" $null $adminToken {
            param($r)
            if ($null -eq $r.rows) { throw "未返回 rows" }
        } | Out-Null
        Test-Endpoint "提案列表" "GET" "$BackendUrl/bbs/idea/list?pageNum=1&pageSize=10" $null $adminToken {
            param($r)
            if ($null -eq $r.rows) { throw "未返回 rows" }
        } | Out-Null
        Test-Endpoint "提案评审配置" "GET" "$BackendUrl/bbs/ideaStageConfig/list?pageNum=1&pageSize=10" $null $adminToken {
            param($r)
            if ($null -eq $r.rows) { throw "未返回 rows" }
        } | Out-Null
        Test-Endpoint "账号详情：字符串用户ID" "GET" "$BackendUrl/system/user/l1reviewer" $null $adminToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.data -or $r.data.userId -ne "l1reviewer") {
                throw "字符串用户ID详情返回异常"
            }
        } | Out-Null
        Test-Endpoint "账号详情：数字字符串用户ID" "GET" "$BackendUrl/system/user/2" $null $adminToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.data -or [string]$r.data.userId -ne "2") {
                throw "数字字符串用户ID详情返回异常"
            }
        } | Out-Null
        Test-Endpoint "评审候选人：初审只含初审员" "GET" "$BackendUrl/bbs/ideaStageConfig/stage/L1/reviewerOptions" $null $adminToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.data) { throw "初审候选人接口异常" }
            $ids = @($r.data | ForEach-Object { [string]$_.userId })
            if ($ids -notcontains "l1reviewer") { throw "初审候选人未包含 l1reviewer" }
            if ($ids -contains "zhangsan" -or $ids -contains "admin") { throw "初审候选人包含非初审员账号" }
        } | Out-Null
        Test-Endpoint "评审候选人：复审只含复审员" "GET" "$BackendUrl/bbs/ideaStageConfig/stage/L2/reviewerOptions" $null $adminToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.data) { throw "复审候选人接口异常" }
            $ids = @($r.data | ForEach-Object { [string]$_.userId })
            if ($ids -notcontains "l2reviewer") { throw "复审候选人未包含 l2reviewer" }
            if ($ids -contains "l1reviewer" -or $ids -contains "zhangsan") { throw "复审候选人包含非复审员账号" }
        } | Out-Null
        Test-Endpoint "评审候选人：终审只含终审员" "GET" "$BackendUrl/bbs/ideaStageConfig/stage/L3/reviewerOptions" $null $adminToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.data) { throw "终审候选人接口异常" }
            $ids = @($r.data | ForEach-Object { [string]$_.userId })
            if ($ids -notcontains "l3reviewer") { throw "终审候选人未包含 l3reviewer" }
            if ($ids -contains "l2reviewer" -or $ids -contains "zhangsan") { throw "终审候选人包含非终审员账号" }
        } | Out-Null
        if (-not $SkipMutatingTests) {
            Test-Endpoint "评审配置保存：角色由环节自动固定" "PUT" "$BackendUrl/bbs/ideaStageConfig" @{
                configId = 1
                stageCode = "L1"
                stageName = "初审"
                roleKeys = "common"
                reviewerUserIds = "l1reviewer"
                status = "0"
                remark = "初审环节配置"
            } $adminToken {
                param($r)
                if ($r.code -ne 200) { throw "评审配置保存失败：$($r.msg)" }
            } | Out-Null
        }
        Test-Endpoint "提案公示墙" "GET" "$BackendUrl/bbs/idea/wall?pageNum=1&pageSize=10" $null $adminToken {
            param($r)
            if ($null -eq $r.rows) { throw "未返回 rows" }
        } | Out-Null
        Test-Endpoint "通知未读数量" "GET" "$BackendUrl/bbs/notification/unread/count" $null $adminToken {
            param($r)
            if ($r.code -ne 200) { throw "通知接口 code=$($r.code)" }
        } | Out-Null
        Test-Endpoint "用户中心统计" "GET" "$BackendUrl/bbs/usercenter/stats" $null $adminToken {
            param($r)
            if ($r.code -ne 200) { throw "用户中心统计 code=$($r.code)" }
        } | Out-Null
        Test-Endpoint "风控预检：普通内容" "POST" "$BackendUrl/bbs/moderation/precheck" @{ scene = "comment"; title = "测试"; content = "这是一条正常测试内容" } $adminToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.data.action) { throw "普通内容预检返回异常" }
        } | Out-Null
        Test-Endpoint "风控预检：违规内容" "POST" "$BackendUrl/bbs/moderation/precheck" @{ scene = "comment"; title = "测试"; content = "我要打人" } $adminToken {
            param($r)
            if ($r.code -ne 200 -or $r.data.action -eq "pass") { throw "违规内容未被风控标记" }
        } | Out-Null
    }
    else {
        Add-Result "鉴权接口组" "SKIP" "未拿到 admin token"
    }

    if (-not $SkipMutatingTests -and $userToken) {
        Reset-TestDataWindow
        $stamp = Get-Date -Format "MMddHHmmss"
        $ideaTitle = "自动测试提案 $stamp"
        $idea = @{
            title = $ideaTitle
            category = "process"
            content = "自动测试提案内容 $stamp"
            backgroundText = "当前流程存在重复录入问题"
            planText = "增加流程提醒和状态跟踪"
            expectedValue = "提升处理效率"
            visibilityScope = "all"
            attachmentUrls = ""
            relatedCase = ""
            teamName = "自动测试组"
            bizValue = "提升协作效率"
        }
        $ideaResp = Test-Endpoint "提交提案" "POST" "$BackendUrl/bbs/idea" $idea $userToken {
            param($r)
            if ($r.code -ne 200 -or $null -eq $r.data) { throw "提交提案失败" }
        }
        $ideaId = $null
        if ($ideaResp -and $ideaResp.data) {
            if ($ideaResp.data.ideaId) { $ideaId = $ideaResp.data.ideaId } else { $ideaId = [int64]$ideaResp.data }
            Test-Endpoint "提案详情：提交人可见" "GET" "$BackendUrl/bbs/idea/$ideaId" $null $userToken {
                param($r)
                if ($r.code -ne 200 -or $null -eq $r.data) { throw "提交人无法查看提案详情" }
            } | Out-Null
            $reviewFlow = @(
                @{ user = "l1reviewer"; stage = "L1"; score = 8 },
                @{ user = "l2reviewer"; stage = "L2"; score = 8 },
                @{ user = "l3reviewer"; stage = "L3"; score = 9 }
            )
            foreach ($step in $reviewFlow) {
                $token = $tokens[$step.user]
                if (-not $token) {
                    Add-Result "提案评审：$($step.stage)" "SKIP" "未拿到 $($step.user) token"
                    continue
                }
                Test-Endpoint "提案评审：$($step.stage)" "POST" "$BackendUrl/bbs/idea/$ideaId/review" @{
                    stage = $step.stage
                    action = "pass"
                    score = $step.score
                    comment = "自动测试通过 $($step.stage)"
                } $token {
                    param($r)
                    if ($r.code -ne 200) { throw "评审失败：$($r.msg)" }
                } 120 | Out-Null
            }
            Test-Endpoint "提案终审后详情" "GET" "$BackendUrl/bbs/idea/$ideaId" $null $adminToken {
                param($r)
                if ($r.code -ne 200 -or $null -eq $r.data.status) { throw "终审后详情异常" }
            } | Out-Null
        }

        $postList = Test-Endpoint "准备评论目标帖子" "GET" "$BackendUrl/bbs/post/list?pageNum=1&pageSize=1" $null $userToken {
            param($r)
            if ($null -eq $r.rows) { throw "帖子列表异常" }
        }
        if ($postList -and $postList.rows.Count -gt 0) {
            $postId = $postList.rows[0].postId
            $commentContent = "自动测试评论 $stamp"
            $commentResp = Test-Endpoint "发表评论" "POST" "$BackendUrl/bbs/comment" @{
                postId = $postId
                parentId = 0
                content = $commentContent
                isAnonymous = "0"
            } $userToken {
                param($r)
                if ($r.code -ne 200) { throw "发表评论失败：$($r.msg)" }
            } 120
            $commentId = $null
            if ($commentResp -and $commentResp.data -and $commentResp.data.commentId) {
                $commentId = $commentResp.data.commentId
            }
            if (-not $commentId) {
                $comments = Test-Endpoint "回查刚发布评论" "GET" "$BackendUrl/bbs/comment/list?postId=$postId&pageNum=1&pageSize=50" $null $userToken {
                    param($r)
                    if ($null -eq $r.rows -and $null -eq $r.data) { throw "评论列表未返回列表数据" }
                }
                if ($comments) {
                    $commentItems = if ($comments.rows) { $comments.rows } else { $comments.data }
                    $hit = @($commentItems | Where-Object { $_.content -eq $commentContent } | Sort-Object commentId -Descending | Select-Object -First 1)
                    if ($hit.Count -gt 0) {
                        $commentId = $hit[0].commentId
                    }
                }
            }
            if ($commentId) {
                Test-Endpoint "删除本人评论" "DELETE" "$BackendUrl/bbs/comment/delByPersonal/$commentId" $null $userToken {
                    param($r)
                    if ($r.code -ne 200) { throw "删除本人评论失败：$($r.msg)" }
                } | Out-Null
            }
            else {
                Add-Result "删除本人评论" "SKIP" "未定位到刚发布的评论 ID"
            }
        }
        else {
            Add-Result "发表评论" "SKIP" "没有可用帖子"
        }
    }
    elseif ($SkipMutatingTests) {
        Add-Result "写入类业务测试" "SKIP" "已按参数跳过"
    }
    else {
        Add-Result "写入类业务测试" "SKIP" "未拿到 zhangsan token"
    }
}
finally {
    $summary = [ordered]@{
        generatedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
        projectRoot = "$ProjectRoot"
        backendUrl = $BackendUrl
        frontendUrl = $FrontendUrl
        total = $Results.Count
        pass = @($Results | Where-Object status -eq "PASS").Count
        fail = @($Results | Where-Object status -eq "FAIL").Count
        skip = @($Results | Where-Object status -eq "SKIP").Count
        info = @($Results | Where-Object status -eq "INFO").Count
        results = $Results
    }
    $summary | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $ReportPath -Encoding UTF8
    Write-Host ""
    Write-Host "Report: $ReportPath"
    Write-Host ("Summary: total={0}, pass={1}, fail={2}, skip={3}, info={4}" -f $summary.total, $summary.pass, $summary.fail, $summary.skip, $summary.info)

    if (-not $KeepRunning) {
        foreach ($p in $StartedProcesses) {
            try {
                if ($p -and -not $p.HasExited) {
                    Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
                }
            }
            catch {}
        }
    }

    if ($summary.fail -gt 0) {
        exit 1
    }
}
