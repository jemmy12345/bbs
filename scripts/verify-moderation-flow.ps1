$ErrorActionPreference = 'Stop'
$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$base = 'http://localhost:9082'
$mysql = 'D:\LenovoSoftstore\Install\MySQL\MySQL Server 8.0\bin\mysql.exe'
$redis = 'D:\LenovoSoftstore\Install\Redis\Redis\redis-cli.exe'
$stamp = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$marker = "流程验证-$stamp"
$users = @('admin', 'zhangsan', 'l1reviewer', 'l2reviewer', 'l3reviewer')

function Invoke-Db($sql) {
  & $mysql -uroot -p123456 bbsdb --default-character-set=utf8mb4 -N -e $sql
}

function Login($username) {
  $body = @{
    username = $username
    password = '123456'
    code = ''
    uuid = ''
    loginType = 'password'
  } | ConvertTo-Json
  $res = Invoke-RestMethod -Method Post -Uri "$base/login" -Body $body -ContentType 'application/json;charset=UTF-8'
  if ($res.code -ne 200) {
    throw "登录失败：$username"
  }
  return $res.token
}

function Headers($token) {
  return @{ Authorization = "Bearer $token" }
}

function Json($obj) {
  return ($obj | ConvertTo-Json -Depth 8 -Compress)
}

function Precheck($token, $scene, $title, $content) {
  $body = Json @{ scene = $scene; title = $title; content = $content }
  return Invoke-RestMethod -Method Post -Uri "$base/bbs/moderation/precheck" -Headers (Headers $token) -Body $body -ContentType 'application/json;charset=UTF-8'
}

try {
  Invoke-Db "UPDATE sys_config SET config_value='false' WHERE config_key='sys.account.captchaEnabled';" | Out-Null
  & $redis DEL sys_config:sys.account.captchaEnabled | Out-Null

  $tokens = @{}
  foreach ($u in $users) {
    $tokens[$u] = Login $u
  }
  "登录验证：$($users -join '、') 均成功"

  $safeTitle = "$marker-评论载体"
  $safeContent = '<p>这是用于流程验证的普通意见反馈内容，描述清楚背景、问题和建议。</p>'
  $safePre = Precheck $tokens['zhangsan'] 'post' $safeTitle $safeContent
  $safePost = @{
    categoryId = 1
    title = $safeTitle
    content = $safeContent
    summary = '普通意见反馈内容'
    postType = 'share'
    isAnonymous = '0'
    status = '0'
    moderationToken = $safePre.data.moderationToken
    tags = @()
  }
  $safeRes = Invoke-RestMethod -Method Post -Uri "$base/bbs/post" -Headers (Headers $tokens['zhangsan']) -Body (Json $safePost) -ContentType 'application/json;charset=UTF-8'
  $postId = (Invoke-Db "SELECT post_id FROM bbs_post WHERE title='$safeTitle' ORDER BY post_id DESC LIMIT 1;")
  "普通载体帖：code=$($safeRes.code)，postId=$postId"

  $normalComment = @{
    postId = [int]$postId
    content = '<p>这是用于回复流程验证的普通评论。</p>'
    images = ''
    parentId = 0
    isAnonymous = '0'
  }
  $normalCommentRes = Invoke-RestMethod -Method Post -Uri "$base/bbs/comment" -Headers (Headers $tokens['zhangsan']) -Body (Json $normalComment) -ContentType 'application/json;charset=UTF-8'
  $parentCommentId = (Invoke-Db "SELECT comment_id FROM bbs_comment WHERE post_id=$postId AND content LIKE '%回复流程验证%' ORDER BY comment_id DESC LIMIT 1;")
  "普通父评论：code=$($normalCommentRes.code)，commentId=$parentCommentId"

  foreach ($u in $users) {
    $token = $tokens[$u]
    for ($i = 1; $i -le 3; $i++) {
      $riskTitle = "$marker-$u-发帖-$i"
      $sw = [Diagnostics.Stopwatch]::StartNew()
      $pre = Precheck $token 'post' $riskTitle '<p>我要打人。</p>'
      $sw.Stop()
      "预检发帖 $u 第$i 次：action=$($pre.data.action)，source=$($pre.data.source)，耗时=$($sw.ElapsedMilliseconds)ms"
      if ($pre.data.action -ne 'review' -or $pre.data.source -ne 'local-rule') {
        throw "发帖预检未稳定命中本地规则：$u 第$i 次"
      }

      $riskPost = @{
        categoryId = 1
        title = $riskTitle
        content = '<p>我要打人。</p>'
        summary = '流程验证'
        postType = 'share'
        isAnonymous = '0'
        status = '0'
        moderationToken = $pre.data.moderationToken
        tags = @()
      }
      $submitWatch = [Diagnostics.Stopwatch]::StartNew()
      $submit = Invoke-RestMethod -Method Post -Uri "$base/bbs/post" -Headers (Headers $token) -Body (Json $riskPost) -ContentType 'application/json;charset=UTF-8'
      $submitWatch.Stop()
      $status = (Invoke-Db "SELECT status FROM bbs_post WHERE title='$riskTitle' ORDER BY post_id DESC LIMIT 1;")
      "提交发帖 $u 第$i 次：code=$($submit.code)，status=$status，耗时=$($submitWatch.ElapsedMilliseconds)ms"
      if ($status -ne '2') {
        throw "风险发帖没有进入待审核：$u 第$i 次"
      }
    }

    for ($i = 1; $i -le 3; $i++) {
      $commentPre = Precheck $token 'comment' $safeTitle '<p>我要打人。</p>'
      "预检评论 $u 第$i 次：action=$($commentPre.data.action)，source=$($commentPre.data.source)"
      if ($commentPre.data.action -ne 'block' -or $commentPre.data.source -ne 'local-rule') {
        throw "评论预检未稳定拦截：$u 第$i 次"
      }

      $comment = @{
        postId = [int]$postId
        content = '<p>我要打人。</p>'
        images = ''
        parentId = 0
        isAnonymous = '0'
        moderationToken = $commentPre.data.moderationToken
      }
      $commentSubmit = Invoke-RestMethod -Method Post -Uri "$base/bbs/comment" -Headers (Headers $token) -Body (Json $comment) -ContentType 'application/json;charset=UTF-8'
      "提交评论 $u 第$i 次：code=$($commentSubmit.code)，msg=$($commentSubmit.msg)"
      if ($commentSubmit.code -ne 500) {
        throw "风险评论没有被拦截：$u 第$i 次"
      }
    }

    for ($i = 1; $i -le 3; $i++) {
      $replyPre = Precheck $token 'reply' $safeTitle '<p>我要打人。</p>'
      "预检回复 $u 第$i 次：action=$($replyPre.data.action)，source=$($replyPre.data.source)"
      if ($replyPre.data.action -ne 'block' -or $replyPre.data.source -ne 'local-rule') {
        throw "回复预检未稳定拦截：$u 第$i 次"
      }

      $reply = @{
        postId = [int]$postId
        content = '<p>我要打人。</p>'
        images = ''
        parentId = [int]$parentCommentId
        replyUserId = 'zhangsan'
        isAnonymous = '0'
        moderationToken = $replyPre.data.moderationToken
      }
      $replySubmit = Invoke-RestMethod -Method Post -Uri "$base/bbs/comment" -Headers (Headers $token) -Body (Json $reply) -ContentType 'application/json;charset=UTF-8'
      "提交回复 $u 第$i 次：code=$($replySubmit.code)，msg=$($replySubmit.msg)"
      if ($replySubmit.code -ne 500) {
        throw "风险回复没有被拦截：$u 第$i 次"
      }
    }
  }
}
finally {
  Invoke-Db "DELETE FROM bbs_notification WHERE target_type='1' AND target_id IN (SELECT post_id FROM bbs_post WHERE title LIKE '$marker%'); DELETE FROM bbs_point_record WHERE (biz_type='post' AND biz_id IN (SELECT post_id FROM bbs_post WHERE title LIKE '$marker%')) OR (biz_type='comment' AND biz_id IN (SELECT comment_id FROM bbs_comment WHERE post_id IN (SELECT post_id FROM bbs_post WHERE title LIKE '$marker%'))); DELETE FROM bbs_comment WHERE post_id IN (SELECT post_id FROM bbs_post WHERE title LIKE '$marker%'); DELETE FROM bbs_post WHERE title LIKE '$marker%'; UPDATE sys_config SET config_value='true' WHERE config_key='sys.account.captchaEnabled';" | Out-Null
  & $redis DEL sys_config:sys.account.captchaEnabled | Out-Null
}
