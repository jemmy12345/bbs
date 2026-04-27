package com.ruoyi.web.controller.bbs;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.wx.WeChatApi;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.system.domain.BbsPost;
import com.ruoyi.system.domain.BbsTag;
import com.ruoyi.system.domain.BbsDeptContact;
import com.ruoyi.system.domain.BbsNotification;
import com.ruoyi.system.domain.BbsPostFollowupRecord;
import com.ruoyi.system.domain.ai.BbsAiModerationResult;
import com.ruoyi.system.mapper.BbsTagMapper;
import com.ruoyi.system.service.IBbsAiService;
import com.ruoyi.system.service.IBbsNotificationService;
import com.ruoyi.system.service.IBbsPostService;
import com.ruoyi.system.service.IBbsPostFollowupRecordService;
import com.ruoyi.system.service.IBbsSensitiveWordService;
import com.ruoyi.system.service.IBbsDeptContactService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.text.Convert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.AdminLog;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.AdminOperationType;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.AdminLogContext;
import com.ruoyi.common.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 帖子 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/bbs/post")
@Api(tags = "帖子")
public class BbsPostController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(BbsPostController.class);
    private static final String FOLLOWUP_PREFIX = "[FOLLOWUP]";

    @Value("${msg.url}")
    private String redirectUrl;
    @Value("${msg.detailUrl}")
    private String detailUrl;

    @Value("${weCom.agentid}")
    private String agentid;

    @Value("${weCom.homePage}")
    private String homePage;

    @Autowired
    private IBbsPostService bbsPostService;

    @Autowired
    private IBbsSensitiveWordService bbsSensitiveWordService;

    @Autowired
    private IBbsDeptContactService bbsDeptContactService;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private IBbsAiService bbsAiService;

    @Autowired
    private IBbsNotificationService bbsNotificationService;

    @Autowired
    private IBbsPostFollowupRecordService bbsPostFollowupRecordService;

    @Autowired
    private BbsTagMapper bbsTagMapper;

    /**
     * 获取运营看板统计数据
     */
    @ApiOperation("获取运营看板统计数据")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        return AjaxResult.success(bbsPostService.getPostStats());
    }

    /**
     * 查询帖子列表
     */
    @ApiOperation("查询帖子列表")
    @GetMapping("/list")
    public TableDataInfo list(BbsPost bbsPost)
    {
        // Mapper会自动处理：如果status为空或不是"4"，则排除草稿状态
        startPage();
        List<BbsPost> list = bbsPostService.selectBbsPostList(bbsPost);
        return getDataTable(list);
    }

    /**
     * 查询帖子列表
     */
    @ApiOperation("查询帖子列表 - 匿名")
    @GetMapping("/listAnonymousByKey/{hashCode}")
    public TableDataInfo listAnonymousByKey(@PathVariable String hashCode)
    {
        // Mapper会自动处理：如果status为空或不是"4"，则排除草稿状态
        startPage();
        List<BbsPost> list = bbsPostService.listAnonymousByKey(hashCode);
        return getDataTable(list);
    }



    /**
     * 查询当前用户的草稿列表
     */
    @ApiOperation("查询当前用户的草稿列表")
    @GetMapping("/drafts")
    public TableDataInfo listDrafts()
    {
        startPage();
        BbsPost queryPost = new BbsPost();
        String userId = SecurityUtils.getUserId();
        queryPost.setUserId(userId);
        queryPost.setStatus("4"); // 草稿状态
        // 确保status是字符串"4"
        if (!"4".equals(queryPost.getStatus()))
        {
            queryPost.setStatus("4");
        }
        List<BbsPost> list = bbsPostService.selectBbsPostList(queryPost);
        return getDataTable(list);
    }

    /**
     * 根据帖子编号获取详细信息
     */
    @ApiOperation("根据帖子编号获取详细信息")
    @GetMapping(value = "/{postId}")
    public AjaxResult getInfo(@PathVariable Long postId)
    {
        // 获取当前登录用户ID（如果已登录）
        String userId = null;
        try
        {
            userId = SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            // 用户未登录，userId保持为null
        }
        
        // 查询帖子信息（包含用户点赞/收藏状态）
        BbsPost post = bbsPostService.selectBbsPostById(postId, userId);
        if (post != null)
        {
            // 增加浏览数
            bbsPostService.incrementViewCount(postId);
            post.setViewCount(post.getViewCount() + 1);
        }
        return success(post);
    }

    /**
     * 更新建议/意见类帖子的闭环状态
     */
    @ApiOperation("更新建议/意见类帖子的闭环状态")
    @PostMapping("/followup")
    public AjaxResult updateFollowup(@RequestBody Map<String, String> params)
    {
        String postIdText = params.get("postId");
        String followupStatus = params.get("followupStatus");
        String followupNote = params.get("followupNote");
        String ownerUserId = params.get("ownerUserId");
        String ownerUserName = params.get("ownerUserName");

        if (StringUtils.isEmpty(postIdText))
        {
            return error("帖子编号不能为空");
        }
        if (!isValidFollowupStatus(followupStatus))
        {
            return error("闭环状态不合法");
        }

        Long postId = Convert.toLong(postIdText);
        if (postId == null || postId <= 0)
        {
            return error("帖子编号不合法");
        }

        BbsPost post = bbsPostService.selectBbsPostById(postId);
        if (post == null)
        {
            return error("帖子不存在");
        }
        if (!isSuggestionFollowupPost(post))
        {
            return error("仅建议/意见类帖子支持闭环状态");
        }
        if (!"0".equals(post.getStatus()))
        {
            return error("仅已发布帖子可更新闭环状态");
        }
        if (!canManageFollowup(post))
        {
            return error("无权限更新闭环状态");
        }

        BbsDeptContact deptContact = bbsDeptContactService.selectBbsDeptContactByDeptId(post.getResponseDeptId());
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();

        BbsPostFollowupRecord record = new BbsPostFollowupRecord();
        record.setPostId(postId);
        record.setFollowupStatus(followupStatus);
        record.setProcessNote(StringUtils.isEmpty(followupNote) ? "" : StringUtils.trim(followupNote));
        record.setOwnerUserId(StringUtils.isNotEmpty(ownerUserId) ? StringUtils.trim(ownerUserId)
            : (deptContact != null ? deptContact.getContactUserId() : ""));
        record.setOwnerUserName(StringUtils.isNotEmpty(ownerUserName) ? StringUtils.trim(ownerUserName)
            : (deptContact != null ? deptContact.getContactName() : ""));
        record.setHandledBy(SecurityUtils.getUsername());
        record.setHandledByName(currentUser.getNickName());
        record.setHandleTime(new java.util.Date());
        record.setCreateBy(SecurityUtils.getUsername());
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setDelFlag("0");
        bbsPostFollowupRecordService.insertBbsPostFollowupRecord(record);

        post.setAuditReason(buildFollowupAuditReason(followupStatus, followupNote));
        bbsPostService.updateBbsPost(post);
        return AjaxResult.success("更新成功", record);
    }

    @ApiOperation("获取帖子最新闭环记录")
    @GetMapping("/followup/latest/{postId}")
    public AjaxResult getFollowupLatest(@PathVariable Long postId)
    {
        return AjaxResult.success(bbsPostFollowupRecordService.selectLatestByPostId(postId));
    }

    @ApiOperation("获取帖子闭环记录历史")
    @GetMapping("/followup/history/{postId}")
    public AjaxResult getFollowupHistory(@PathVariable Long postId)
    {
        return AjaxResult.success(bbsPostFollowupRecordService.selectListByPostId(postId));
    }

    @ApiOperation("获取热门标签")
    @GetMapping("/tags/hot")
    public AjaxResult getHotTags(Integer limit)
    {
        if (limit == null || limit <= 0)
        {
            limit = 20;
        }
        List<BbsTag> tags = bbsTagMapper.selectHotTags(limit);
        return AjaxResult.success(tags);
    }

    /**
     * AI助写帖子内容
     */
    @ApiOperation("AI助写帖子内容")
    @PostMapping("/ai/generate")
    public AjaxResult generateByAi(@RequestBody Map<String, String> params)
    {
        String keywords = params.get("keywords");
        if (StringUtils.isEmpty(keywords))
        {
            return error("请输入关键词");
        }
        String postType = params.get("postType");
        String content = bbsAiService.generatePostContent(keywords, postType);
        return AjaxResult.success("操作成功", content);
    }

    /**
     * 新增帖子
     */
    @ApiOperation("新增帖子")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BbsPost bbsPost)
    {
        // 检测敏感词
        String checkText = (bbsPost.getTitle() != null ? bbsPost.getTitle() : "") + 
                          (bbsPost.getContent() != null ? bbsPost.getContent() : "");
        List<String> sensitiveWords = bbsSensitiveWordService.checkSensitiveWords(checkText);
        if (sensitiveWords != null && !sensitiveWords.isEmpty())
        {
            return error("内容包含敏感词：" + String.join("、", sensitiveWords) + "，请修改后重新提交");
        }
        
        // 先检查是否为草稿（status="4"表示草稿），草稿不需要进行管理员判断和状态设置
        boolean isDraft = "4".equals(bbsPost.getStatus());
        
        // 如果不是草稿，才进行管理员判断和状态设置
        boolean isAdmin = false;
        boolean aiRisk = false;
        String aiRiskReason = null;
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();

        if (!isDraft)
        {
            // 检查是否为管理员，管理员发布帖子直接通过审核
            isAdmin = SecurityUtils.isConfigAdmin(adminConfig, currentUser)
                || (currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role -> Constants.SUPER_ADMIN.equals(role.getRoleKey())));
            
            if (isAdmin)
            {
                bbsPost.setStatus("0"); // 管理员直接设置为正常状态
            }
            else
            {
                try
                {
                    BbsAiModerationResult moderation = bbsAiService.moderatePost(bbsPost.getTitle(), bbsPost.getContent());
                    if (moderation != null && moderation.isRisk())
                    {
                        aiRisk = true;
                        aiRiskReason = moderation.getRiskSummary();
                        bbsPost.setStatus("2");
                        bbsPost.setAuditReason(aiRiskReason);
                    }
                    else
                    {
                        bbsPost.setStatus("0");
                        bbsPost.setAuditReason(null);
                    }
                }
                catch (Exception e)
                {
                    log.error("AI审核失败，降级为人工审核", e);
                    aiRisk = true;
                    aiRiskReason = "AI审核服务异常，请管理员人工复核";
                    bbsPost.setStatus("2");
                    bbsPost.setAuditReason(aiRiskReason);
                }
            }
        }
        bbsPost.setDelFlag("0");
        bbsPost.setViewCount(0);
        bbsPost.setLikeCount(0);
        bbsPost.setCommentCount(0);
        bbsPost.setCollectCount(0);
        // 如果没有设置匿名标识，默认为实名
        if (bbsPost.getIsAnonymous() == null)
        {
            bbsPost.setIsAnonymous("0");
            bbsPost.setUserId(SecurityUtils.getUserId());
            bbsPost.setCreateBy(SecurityUtils.getUsername());
            bbsPost.setNickName(SecurityUtils.getLoginUser().getUser().getNickName());
            bbsPost.setAvatar(SecurityUtils.getLoginUser().getUser().getAvatar());
        }else if ("1".equals(bbsPost.getIsAnonymous())){
            bbsPost.setUserId(bbsPost.getUserId());
            bbsPost.setCreateBy(bbsPost.getUserId());
            bbsPost.setNickName("匿名");
            bbsPost.setAvatar("");
//            bbsPost.setResponseDeptId(null);
//            bbsPost.setResponseDeptName("匿名");
        }else{
            bbsPost.setIsAnonymous("0");
            bbsPost.setUserId(SecurityUtils.getUserId());
            bbsPost.setCreateBy(SecurityUtils.getUsername());
            bbsPost.setNickName(SecurityUtils.getLoginUser().getUser().getNickName());
            bbsPost.setAvatar(SecurityUtils.getLoginUser().getUser().getAvatar());
        }

        String returnStr = null;
        Long postId = bbsPostService.insertBbsPost(bbsPost);
        if (postId > 0)
        {
            bbsPost.setPostId(postId);
            // 如果是草稿，返回草稿保存成功
            if (isDraft)
            {
                return success("草稿保存成功");
            }

            // AI识别风险则进入待审核并通知管理员
            if (aiRisk)
            {
                createAiRiskNotification(postId, bbsPost, aiRiskReason, currentUser, adminConfig);
                return success("AI检测到风险，已提交管理员审核");
            }
            
            // 如果帖子类型是建议或意见，并且选择了回应部门，则发送企业微信消息
            if (bbsPost.getResponseDeptId() != null &&
                ("suggestion".equals(bbsPost.getPostType()) || "opinion".equals(bbsPost.getPostType())))
            {
                try
                {
                    // 根据回应部门ID查询部门接口人
                    BbsDeptContact deptContact = bbsDeptContactService.selectBbsDeptContactByDeptId(bbsPost.getResponseDeptId());
                    if (deptContact != null && StringUtils.isNotEmpty(deptContact.getContactUserId()))
                    {
                        // 发送企业微信消息（功能待定，先留接口）
                        sendWeChatWorkMessage(deptContact, bbsPost);
                    }
                }
                catch (Exception e)
                {
                    // 发送消息失败不影响帖子发布，记录日志即可
                    log.error("发送企业微信消息失败：", e);
                }
            }
            // 直接发布的帖子，发送消息通知至管理员
            if (StringUtils.isNotEmpty(adminConfig))
            {
                String remindUserid = adminConfig.replaceAll(",", "|");
                sendWeChatMessageToAdmin(bbsPost, remindUserid);
            }
            returnStr = "发布成功";
            return success(returnStr);
        }else{
            return error("发布失败");
        }
    }

    private void sendWeChatMessageToAdmin(BbsPost bbsPost, String remindUserid) {
        try {
            String access_token = WeChatApi.getToken(agentid);

            JSONObject msgInfo = new JSONObject();
            msgInfo.put("msgtype", "textcard");
            msgInfo.put("touser", remindUserid);
//            msgInfo.put("touser", "@all");
            msgInfo.put("content", bbsPost.getContent());
            msgInfo.put("agentid", agentid);

            // 处理人处理待办
            String url =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx786a96dd52ea3edb&redirect_uri="
                            + homePage
                            + "&response_type=code&scope=snsapi_base&agentid="
                            + agentid
                            + "&state=STATE#wechat_redirect";

//            // 先去除HTML标签和样式代码，再截取前100字符
//            String plainContent = bbsPost.getContent()
//                    .replaceAll("<style[^>]*>[\\s\\S]*?</style>", "")
//                    .replaceAll("<script[^>]*>[\\s\\S]*?</script>", "")
//                    .replaceAll("<[^>]+>", "")
//                    .replaceAll("&nbsp;", " ")
//                    .replaceAll("&amp;", "&")
//                    .replaceAll("&lt;", "<")
//                    .replaceAll("&gt;", ">")
//                    .replaceAll("&quot;", "\"")
//                    .replaceAll("\\s+", " ")
//                    .trim();
//            String briefContent =
//                    plainContent.length() > 100
//                            ? plainContent.substring(0, 100) + "…"
//                            : plainContent;
//            // 是否匿名（0实名 1匿名）
//            String nickName = bbsPost.getIsAnonymous().equals("1") ?
//                    "匿名" : SecurityUtils.getLoginUser().getUser().getNickName();

            JSONObject textcard = new JSONObject();
            textcard.put("title", "BBS通知-帖子发布");
            String description = "<div class=\"normal\">有同事在BBS发布了新帖子，标题为：《" + bbsPost.getTitle() + "》，欢迎大家一起互动交流！</div>";
            textcard.put("description", description);
            textcard.put("url", url);
            msgInfo.put("textcard", textcard);

            log.info("BBS通知-帖子发布通知入参 : " + msgInfo);
            String sendUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+access_token;
            String msgResult = HttpUtils.sendPost(sendUrl, JSONObject.toJSONString(msgInfo));
            // 调用企业微信API发送消息
            log.info("BBS通知-帖子发布通知结果 : " + msgResult);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("BBS通知-帖子发布发送企业微信消息失败：", e);
        }
    }

    private void createAiRiskNotification(Long postId, BbsPost bbsPost, String riskReason, SysUser currentUser, String adminConfig)
    {
        if (StringUtils.isEmpty(adminConfig))
        {
            return;
        }
        String[] admins = adminConfig.split(",");
        for (String token : admins)
        {
            String adminUser = StringUtils.trim(token);
            if (StringUtils.isEmpty(adminUser))
            {
                continue;
            }
            BbsNotification notification = new BbsNotification();
            notification.setUserId(adminUser);
            notification.setType("5");
            notification.setTitle("AI风控待审核");
            notification.setContent("帖子《" + bbsPost.getTitle() + "》疑似风险：" + (StringUtils.isEmpty(riskReason) ? "请人工复核" : riskReason));
            notification.setTargetType("1");
            notification.setTargetId(postId);
            notification.setFromUserId(currentUser.getUserId());
            notification.setFromNickName(currentUser.getNickName());
            notification.setFromAvatar(currentUser.getAvatar());
            bbsNotificationService.insertBbsNotification(notification);
        }
        sendWeChatRiskMessageToAdmin(bbsPost, riskReason, adminConfig.replaceAll(",", "|"));
    }

    private void sendWeChatRiskMessageToAdmin(BbsPost bbsPost, String riskReason, String remindUserid)
    {
        try
        {
            String accessToken = WeChatApi.getToken(agentid);
            JSONObject msgInfo = new JSONObject();
            msgInfo.put("msgtype", "textcard");
            msgInfo.put("touser", remindUserid);
            msgInfo.put("content", bbsPost.getContent());
            msgInfo.put("agentid", agentid);

            String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx786a96dd52ea3edb&redirect_uri="
                + homePage
                + "&response_type=code&scope=snsapi_base&agentid="
                + agentid
                + "&state=STATE#wechat_redirect";

            JSONObject textcard = new JSONObject();
            textcard.put("title", "BBS通知-AI风控待审核");
            textcard.put("description", "<div class=\"normal\">帖子《" + bbsPost.getTitle() + "》被AI识别为风险内容，风险摘要："
                + (StringUtils.isEmpty(riskReason) ? "请尽快审核" : riskReason)
                + "</div>");
            textcard.put("url", url);
            msgInfo.put("textcard", textcard);

            String sendUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
            HttpUtils.sendPost(sendUrl, JSONObject.toJSONString(msgInfo));
        }
        catch (Exception e)
        {
            log.error("发送AI风险企业微信消息失败", e);
        }
    }

    /**
     * 修改帖子
     */
    @ApiOperation("修改帖子")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BbsPost bbsPost)
    {
        // 检测敏感词（如果不是草稿状态）
        if (!"4".equals(bbsPost.getStatus()))
        {
            String checkText = (bbsPost.getTitle() != null ? bbsPost.getTitle() : "") + 
                              (bbsPost.getContent() != null ? bbsPost.getContent() : "");
            List<String> sensitiveWords = bbsSensitiveWordService.checkSensitiveWords(checkText);
            if (sensitiveWords != null && !sensitiveWords.isEmpty())
            {
                return error("内容包含敏感词：" + String.join("、", sensitiveWords) + "，请修改后重新提交");
            }
        }
        
        bbsPost.setUpdateBy(SecurityUtils.getUsername());
        
        // 检查是否为草稿保存
        boolean isDraft = "4".equals(bbsPost.getStatus());
        boolean isAdmin = false;
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();
        // 方法1：检查用户角色中是否包含admin角色
        if (currentUser.getRoles() != null && !currentUser.getRoles().isEmpty())
        {
            isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> Constants.SUPER_ADMIN.equals(role.getRoleKey()));
        }
        // 方法2：如果方法1没找到，检查管理员配置
        if (!isAdmin)
        {
            String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
            isAdmin = SecurityUtils.isConfigAdmin(adminConfig, currentUser);
        }
        String returnStr = null;
        boolean aiRisk = false;
        String aiRiskReason = null;
        // 如果是从草稿发布（status从"4"变为其他状态），需要设置正确的状态
        boolean isPublishingFromDraft = false;
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        if (!isDraft && bbsPost.getPostId() != null)
        {
            // 检查原帖子是否为草稿
            BbsPost originalPost = bbsPostService.selectBbsPostById(bbsPost.getPostId());
            if (originalPost != null && "4".equals(originalPost.getStatus()))
            {
                isPublishingFromDraft = true;
                // 从草稿发布，需要设置正确的状态
                
                if (isAdmin)
                {
                    bbsPost.setStatus("0"); // 管理员直接设置为正常状态
                }
                else
                {
                    try
                    {
                        BbsAiModerationResult moderation = bbsAiService.moderatePost(bbsPost.getTitle(), bbsPost.getContent());
                        if (moderation != null && moderation.isRisk())
                        {
                            aiRisk = true;
                            aiRiskReason = moderation.getRiskSummary();
                            bbsPost.setStatus("2");
                            bbsPost.setAuditReason(aiRiskReason);
                        }
                        else
                        {
                            bbsPost.setStatus("0");
                            bbsPost.setAuditReason(null);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("AI审核失败，降级为人工审核", e);
                        aiRisk = true;
                        aiRiskReason = "AI审核服务异常，请管理员人工复核";
                        bbsPost.setStatus("2");
                        bbsPost.setAuditReason(aiRiskReason);
                    }
                }
            }
        }

        int result = bbsPostService.updateBbsPost(bbsPost);
        if (result > 0)
        {
            if (isPublishingFromDraft && aiRisk)
            {
                createAiRiskNotification(bbsPost.getPostId(), bbsPost, aiRiskReason, currentUser, adminConfig);
                return success("AI检测到风险，已提交管理员审核");
            }

            // 如果是从草稿发布，并且帖子类型是建议或意见，并且选择了回应部门，则发送企业微信消息
            if (isPublishingFromDraft && bbsPost.getResponseDeptId() != null &&
                ("suggestion".equals(bbsPost.getPostType()) || "opinion".equals(bbsPost.getPostType())))
            {
                try
                {
                    // 根据回应部门ID查询部门接口人
                    BbsDeptContact deptContact = bbsDeptContactService.selectBbsDeptContactByDeptId(bbsPost.getResponseDeptId());
                    if (deptContact != null && StringUtils.isNotEmpty(deptContact.getContactUserId()))
                    {
                        // 发送企业微信消息（功能待定，先留接口）
                        sendWeChatWorkMessage(deptContact, bbsPost);
                    }
                }
                catch (Exception e)
                {
                    // 发送消息失败不影响帖子发布，记录日志即可
                    log.error("发送企业微信消息失败：", e);
                }
            }else{
                returnStr = isDraft ? "草稿保存成功" : "发布成功";
            }
            // 如果是保存草稿
            if (isDraft)
            {
                return success("草稿保存成功");
            }else{
                return success(returnStr);
            }
        }else{
            return error("操作失败");
        }
    }

    /**
     * 管理员删除 - 删除帖子（草稿真正删除，已发布帖子下架）
     */
    @ApiOperation("管理员删除 - 删除帖子（草稿真正删除，已发布帖子下架）")
    @DeleteMapping("/{postIds}")
    @Log(title = "文章管理", businessType = BusinessType.DELETE)
    @AdminLog(module = "帖子管理", operationType = AdminOperationType.DELETE, description = "管理员删除/下架帖子")
    public AjaxResult remove(@PathVariable Long[] postIds)
    {
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        if (!SecurityUtils.isConfigAdmin(adminConfig, SecurityUtils.getLoginUser().getUser())) {
            return error("无权限操作");
        }

        // 分别处理草稿和已发布的帖子
        List<Long> draftIds = new ArrayList<>();
        List<Long> publishedIds = new ArrayList<>();
        
        // 收集帖子标题用于操作日志
        List<String> postTitles = new ArrayList<>();

        for (Long postId : postIds)
        {
            BbsPost post = bbsPostService.selectBbsPostById(postId);
            if (post != null)
            {
                postTitles.add(post.getTitle());
                // 草稿状态（status="4"）真正删除
                if ("4".equals(post.getStatus()))
                {
                    draftIds.add(postId);
                }
                else
                {
                    // 已发布的帖子下架
                    publishedIds.add(postId);
                }
            }
        }
        
        int successCount = 0;
        String message = "";
        
        // 删除草稿（真正删除）
        if (!draftIds.isEmpty())
        {
            Long[] draftArray = draftIds.toArray(new Long[0]);
            int draftResult = bbsPostService.realDeleteBbsPostByIds(draftArray);
            successCount += draftResult;
            if (draftResult > 0)
            {
                message += "删除草稿成功";
            }
        }
        
        // 下架已发布的帖子
        if (!publishedIds.isEmpty())
        {
            Long[] publishedArray = publishedIds.toArray(new Long[0]);
            // 检查第一个帖子的状态，判断是上架还是下架
            BbsPost firstPost = bbsPostService.selectBbsPostById(publishedArray[0]);
            boolean isOffline = firstPost != null && "1".equals(firstPost.getStatus());
            
            int offlineResult = bbsPostService.toggleOffline(publishedArray);
            successCount += offlineResult;
            if (offlineResult > 0)
            {
                if (!message.isEmpty())
                {
                    message += "，";
                }
                message += isOffline ? "上架成功" : "下架成功";
            }
        }
        
        if (successCount > 0)
        {
            // 设置动态操作描述
            String titles = String.join("、", postTitles);
            AdminLogContext.setDescription(message + "，帖子：《" + titles + "》");
            return success(message);
        }
        
        // 如果都是已发布的帖子且下架失败
        if (!publishedIds.isEmpty() && draftIds.isEmpty())
        {
            BbsPost firstPost = bbsPostService.selectBbsPostById(publishedIds.get(0));
            boolean isOffline = firstPost != null && "1".equals(firstPost.getStatus());
            return error(isOffline ? "上架失败" : "下架失败");
        }
        
        return error("删除失败");
    }

    /**
     * 个人中心增加删除帖子功能
     */
    @ApiOperation("个人中心增加删除帖子功能")
    @DeleteMapping("/delByPersonal/{postId}")
    @Log(title = "个人中心删除贴子", businessType = BusinessType.DELETE)
    public AjaxResult delByPersonal(@PathVariable Long postId)
    {
        bbsPostService.delByPersonal(postId);
        return AjaxResult.success();
    }

    /**
     * 点赞/取消点赞
     */
    @ApiOperation("点赞/取消点赞")
    @PostMapping("/like/{postId}")
    public AjaxResult toggleLike(@PathVariable Long postId)
    {
        String userId = SecurityUtils.getUserId();
        int result = bbsPostService.toggleLike(postId, userId);
        return success(result > 0 ? "点赞成功" : "取消点赞");
    }

    /**
     * 收藏/取消收藏
     */
    @ApiOperation("收藏/取消收藏")
    @PostMapping("/collect/{postId}")
    public AjaxResult toggleCollect(@PathVariable Long postId)
    {
        String userId = SecurityUtils.getUserId();
        int result = bbsPostService.toggleCollect(postId, userId);
        return success(result > 0 ? "收藏成功" : "取消收藏");
    }

    /**
     * 获取热门文章列表
     */
    @ApiOperation("获取热门文章列表")
    @GetMapping("/hot")
    public AjaxResult getHotPosts(Integer limit)
    {
        if (limit == null || limit <= 0)
        {
            limit = 5;
        }
        List<BbsPost> list = bbsPostService.selectHotPostList(limit);
        return success(list);
    }

    private boolean isSuggestionFollowupPost(BbsPost post)
    {
        return post != null
            && ("suggestion".equals(post.getPostType()) || "opinion".equals(post.getPostType()))
            && post.getResponseDeptId() != null;
    }

    private boolean isValidFollowupStatus(String followupStatus)
    {
        return "accepted".equals(followupStatus)
            || "processing".equals(followupStatus)
            || "feedback".equals(followupStatus)
            || "resolved".equals(followupStatus);
    }

    private boolean canManageFollowup(BbsPost post)
    {
        if (post == null)
        {
            return false;
        }

        SysUser currentUser = SecurityUtils.getLoginUser().getUser();
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        boolean isAdmin = SecurityUtils.isConfigAdmin(adminConfig, currentUser)
            || (currentUser.getRoles() != null && currentUser.getRoles().stream()
            .anyMatch(role -> Constants.SUPER_ADMIN.equals(role.getRoleKey())));
        if (isAdmin)
        {
            return true;
        }

        BbsDeptContact deptContact = bbsDeptContactService.selectBbsDeptContactByDeptId(post.getResponseDeptId());
        return deptContact != null
            && "0".equals(deptContact.getStatus())
            && StringUtils.isNotEmpty(deptContact.getContactUserId())
            && deptContact.getContactUserId().equals(SecurityUtils.getUserId());
    }

    private String buildFollowupAuditReason(String followupStatus, String followupNote)
    {
        String sanitizedNote = StringUtils.isEmpty(followupNote) ? "" : followupNote.trim();
        return FOLLOWUP_PREFIX + followupStatus + "|" + sanitizedNote;
    }

    /**
     * 获取审核开关状态
     */
    @ApiOperation("获取审核开关状态")
    @GetMapping("/audit/enabled")
    public AjaxResult getAuditEnabled()
    {
        String auditEnabled = sysConfigService.selectConfigByKey("bbs.post.audit.enabled");
        if (StringUtils.isEmpty(auditEnabled))
        {
            auditEnabled = "false"; // 默认关闭
        }
        return success(Convert.toBool(auditEnabled));
    }

    /**
     * 设置审核开关
     */
    @ApiOperation("设置审核开关")
    @PostMapping("/audit/enabled")
    @Log(title = "文章审核", businessType = BusinessType.UPDATE)
    @AdminLog(module = "帖子管理", operationType = AdminOperationType.UPDATE)
    public AjaxResult setAuditEnabled(@RequestBody Map<String, Boolean> params)
    {
        Boolean enabled = params.get("enabled");
        if (enabled == null)
        {
            return error("参数错误");
        }
        AdminLogContext.setDescription("设置审核开关：" + (enabled ? "开启" : "关闭"));
        
        // 查询或创建配置
        SysConfig queryConfig = new SysConfig();
        queryConfig.setConfigKey("bbs.post.audit.enabled");
        SysConfig existConfig = sysConfigService.selectConfigList(queryConfig).stream()
            .filter(c -> "bbs.post.audit.enabled".equals(c.getConfigKey()))
            .findFirst()
            .orElse(null);
        
        SysConfig config;
        
        if (existConfig != null)
        {
            existConfig.setConfigValue(enabled.toString());
            existConfig.setUpdateBy(SecurityUtils.getUsername());
            sysConfigService.updateConfig(existConfig);
        }
        else
        {
            config = new SysConfig();
            config.setConfigKey("bbs.post.audit.enabled");
            config.setConfigName("文章审核开关");
            config.setConfigValue(enabled.toString());
            config.setConfigType("Y");
            config.setCreateBy(SecurityUtils.getUsername());
            sysConfigService.insertConfig(config);
        }
        
        // 清除缓存
        sysConfigService.resetConfigCache();
        
        return success("设置成功");
    }

    /**
     * 批量审核通过文章
     */
    @ApiOperation("批量审核通过文章")
    @PostMapping("/approve")
    @Log(title = "文章审核", businessType = BusinessType.UPDATE)
    @AdminLog(module = "帖子管理", operationType = AdminOperationType.APPROVE, description = "批量审核通过文章")
    public AjaxResult approvePosts(@RequestBody Long[] postIds)
    {
        if (postIds == null || postIds.length == 0)
        {
            return error("请选择要审核的文章");
        }

        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        if (!SecurityUtils.isConfigAdmin(adminConfig, SecurityUtils.getLoginUser().getUser()))
        {
            return error("无权限操作");
        }

        int count = bbsPostService.approvePosts(postIds);
        List<String> approvedTitles = new ArrayList<>();
        for (Long postId : postIds) {
            BbsPost bbsPost = bbsPostService.selectBbsPostById(postId);
            if (bbsPost != null)
            {
                approvedTitles.add(bbsPost.getTitle());
            }
            try
            {
                // 根据回应部门ID查询部门接口人
                BbsDeptContact deptContact = bbsDeptContactService.selectBbsDeptContactByDeptId(bbsPost.getResponseDeptId());
                if (deptContact != null && StringUtils.isNotEmpty(deptContact.getContactUserId()))
                {
                    // 发送企业微信消息（功能待定，先留接口）
                    sendWeChatWorkMessage(deptContact, bbsPost);
                }
            }
            catch (Exception e)
            {
                // 发送消息失败不影响帖子发布，记录日志即可
                log.error("发送企业微信消息失败：", e);
            }

            // 直接发布的帖子,即发送消息通知至管理员企微账号,点击消息通知跳转到BBS首页
            String remindUserid = adminConfig.replaceAll(",", "|");
            sendWeChatMessageToAdmin(bbsPost, remindUserid);

        }
        // 设置动态操作描述
        AdminLogContext.setDescription("审核通过 " + count + " 篇文章：《" + String.join("、", approvedTitles) + "》");
        return success("成功审核通过 " + count + " 篇文章");
    }

    /**
     * 批量审核不通过文章
     */
    @ApiOperation("批量审核不通过文章")
    @PostMapping("/reject")
    @Log(title = "文章审核", businessType = BusinessType.UPDATE)
    @AdminLog(module = "帖子管理", operationType = AdminOperationType.REJECT, description = "批量审核驳回文章")
    public AjaxResult rejectPosts(@RequestBody Map<String, Object> params)
    {
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        if (!SecurityUtils.isConfigAdmin(adminConfig, SecurityUtils.getLoginUser().getUser()))
        {
            return error("无权限操作");
        }

        Long[] postIds = null;
        String auditReason = null;
        
        if (params.get("postIds") != null)
        {
            if (params.get("postIds") instanceof List)
            {
                List<?> list = (List<?>) params.get("postIds");
                // 将 List 中的元素转换为 Long 类型（处理 Integer 转 Long 的情况）
                List<Long> longList = list.stream()
                    .map(item -> {
                        if (item instanceof Number)
                        {
                            return ((Number) item).longValue();
                        }
                        else if (item instanceof String)
                        {
                            return Long.parseLong((String) item);
                        }
                        return null;
                    })
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
                postIds = longList.toArray(new Long[0]);
            }
            else if (params.get("postIds") instanceof Long[])
            {
                postIds = (Long[]) params.get("postIds");
            }
            else if (params.get("postIds") instanceof Integer[])
            {
                // 处理 Integer[] 类型
                Integer[] intArray = (Integer[]) params.get("postIds");
                postIds = new Long[intArray.length];
                for (int i = 0; i < intArray.length; i++)
                {
                    postIds[i] = intArray[i].longValue();
                }
            }
        }
        
        if (params.get("auditReason") != null)
        {
            auditReason = params.get("auditReason").toString();
        }
        
        if (postIds == null || postIds.length == 0)
        {
            return error("请选择要审核的文章");
        }
        
        int count = bbsPostService.rejectPosts(postIds, null);
        // 设置动态操作描述
        List<String> rejectTitles = new ArrayList<>();
        for (Long postId : postIds)
        {
            BbsPost post = bbsPostService.selectBbsPostById(postId);
            if (post != null)
            {
                rejectTitles.add(post.getTitle());
            }
        }
        AdminLogContext.setDescription("审核驳回 " + count + " 篇文章：《" + String.join("、", rejectTitles) + "》"
            + (auditReason != null ? "，原因：" + auditReason : ""));
        return success("成功审核不通过 " + count + " 篇文章");
    }

    /**
     * 置顶/取消置顶
     */
    @ApiOperation("置顶/取消置顶")
    @PostMapping("/top/{postId}")
    @Log(title = "文章置顶", businessType = BusinessType.UPDATE)
    @AdminLog(module = "帖子管理", operationType = AdminOperationType.UPDATE)
    public AjaxResult toggleTop(@PathVariable Long postId)
    {
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        if (!SecurityUtils.isConfigAdmin(adminConfig, SecurityUtils.getLoginUser().getUser()))
        {
            return error("无权限操作");
        }

        BbsPost post = bbsPostService.selectBbsPostById(postId);
        String title = post != null ? post.getTitle() : String.valueOf(postId);
        int result = bbsPostService.toggleTop(postId);
        String action = result > 0 ? "置顶" : "取消置顶";
        AdminLogContext.setDescription(action + "帖子：《" + title + "》");
        return success(action + "成功");
    }

    /**
     * 发送企业微信消息
     * 功能待定，先留接口
     * 
     * @param deptContact 部门接口人信息
     * @param post 帖子信息
     */
    private void sendWeChatWorkMessage(BbsDeptContact deptContact, BbsPost post)
    {
        // TODO: 实现企业微信消息发送功能
        // 1. 获取企业微信access_token
        // 2. 构建消息内容
        // 3. 调用企业微信API发送消息给部门接口人
        // 
        // 示例消息内容：
        // String message = String.format("您收到一条新的%s：\n标题：%s\n内容：%s\n发帖人：%s\n请及时查看并回复。",
        //     "suggestion".equals(post.getPostType()) ? "建议" : "意见",
        //     post.getTitle(),
        //     post.getSummary(),
        //     post.getNickName());
        //
        String accessTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=wx786a96dd52ea3edb&corpsecret=SbJ0TwcYFTtq2PWe1zbkI_0-4e7pO8k85VZpN4uWtoQ";
        String tokenInfo = HttpUtils.sendGet(accessTokenUrl);
        JSONObject tokenObject = JSONObject.parseObject(tokenInfo);
        String access_token = tokenObject.getString("access_token");
        String contactUserId = deptContact.getContactUserId();
        JSONObject msgInfo = new JSONObject();
        msgInfo.put("msgtype", "textcard");
        msgInfo.put("touser", contactUserId);
        msgInfo.put("content", post.getContent());
        msgInfo.put("agentid", 1000063);
        JSONObject textcard = new JSONObject();
        textcard.put("title", "BBS通知");
        String description = "<div class=\"normal\">您好，贵部在Smart BBS收到了一条新的建议/意见，还请前往查看并给予反馈!\r\n</div> <div class=\"highlight\">"+post.getTitle()+"</div>";
        textcard.put("description", description);
        String detailUrlInfo = detailUrl + post.getPostId();
        String redirectUrl1 = redirectUrl.replace("redirectUri", detailUrlInfo);
        textcard.put("url", redirectUrl1);
        msgInfo.put("textcard", textcard);
        String sendUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+access_token;
        String s = HttpUtils.sendPost(sendUrl, JSONObject.toJSONString(msgInfo));
        // 调用企业微信API发送消息
        log.info("发送企业微信消息 - 部门：{}，接口人：{}，帖子ID：{}, 链接地址：{}",
            deptContact.getDeptName(), deptContact.getContactName(), post.getPostId(), redirectUrl);
    }
}
