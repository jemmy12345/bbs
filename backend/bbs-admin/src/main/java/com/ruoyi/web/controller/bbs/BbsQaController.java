package com.ruoyi.web.controller.bbs;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.BbsQaQuestion;
import com.ruoyi.system.service.IBbsQaService;
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bbs/qa")
@Api(tags = "Enterprise Q&A")
public class BbsQaController extends BaseController
{
    @Autowired
    private IBbsQaService bbsQaService;

    // ============ 问题 CRUD ============

    @ApiOperation("发起悬赏提问")
    @PostMapping("/question")
    public AjaxResult createQuestion(@RequestBody BbsQaQuestion question)
    {
        String userId   = SecurityUtils.getUserId();
        String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
        String avatar   = SecurityUtils.getLoginUser().getUser().getAvatar();
        String deptName = SecurityUtils.getLoginUser().getUser().getDept() == null ? null
                : SecurityUtils.getLoginUser().getUser().getDept().getDeptName();
        Long questionId = bbsQaService.createQuestion(question, userId, nickName, avatar, deptName);
        return success(questionId);
    }

    @ApiOperation("问题列表（带分页）")
    @GetMapping("/question/list")
    public TableDataInfo listQuestions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        List<BbsQaQuestion> list = bbsQaService.listQuestions(status, category, keyword, orderBy);
        return getDataTable(list);
    }

    @ApiOperation("问题详情")
    @GetMapping("/question/{questionId}")
    public AjaxResult getQuestion(@PathVariable Long questionId)
    {
        String userId = null;
        try { userId = SecurityUtils.getUserId(); } catch (Exception ignored) {}
        return success(bbsQaService.getQuestionDetail(questionId, userId));
    }

    @ApiOperation("回答问题")
    @PostMapping("/question/{questionId}/answer")
    public AjaxResult answerQuestion(@PathVariable Long questionId, @RequestBody Map<String, Object> body)
    {
        String content       = body.get("content") == null ? null : String.valueOf(body.get("content"));
        Long parentAnswerId  = body.get("parentAnswerId") == null ? null : Long.valueOf(body.get("parentAnswerId").toString());
        String userId        = SecurityUtils.getUserId();
        String nickName      = SecurityUtils.getLoginUser().getUser().getNickName();
        String avatar        = SecurityUtils.getLoginUser().getUser().getAvatar();
        String deptName      = SecurityUtils.getLoginUser().getUser().getDept() == null ? null
                : SecurityUtils.getLoginUser().getUser().getDept().getDeptName();
        return success(bbsQaService.answerQuestion(questionId, content, parentAnswerId, userId, nickName, avatar, deptName));
    }

    @ApiOperation("采纳最佳答案")
    @PostMapping("/question/{questionId}/accept/{answerId}")
    public AjaxResult acceptBest(@PathVariable Long questionId, @PathVariable Long answerId)
    {
        bbsQaService.acceptBestAnswer(questionId, answerId, SecurityUtils.getUserId());
        return success();
    }

    @ApiOperation("撤销问题（退回积分）")
    @DeleteMapping("/question/{questionId}")
    public AjaxResult cancelQuestion(@PathVariable Long questionId)
    {
        bbsQaService.cancelQuestion(questionId, SecurityUtils.getUserId());
        return success();
    }

    @ApiOperation("将已解决的问题归档")
    @PostMapping("/question/{questionId}/archive")
    public AjaxResult archive(@PathVariable Long questionId)
    {
        bbsQaService.archiveQuestion(questionId, SecurityUtils.getUserId());
        return success();
    }

    // ============ 回答操作 ============

    @ApiOperation("点赞回答")
    @PostMapping("/answer/{answerId}/like")
    public AjaxResult likeAnswer(@PathVariable Long answerId)
    {
        bbsQaService.toggleLikeAnswer(answerId, SecurityUtils.getUserId());
        return success();
    }

    // ============ 知识库 ============

    @ApiOperation("已归档问题列表（知识库）")
    @GetMapping("/archive")
    public TableDataInfo archiveList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "12") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        List<BbsQaQuestion> list = bbsQaService.listArchivedQuestions(category, keyword, orderBy);
        return getDataTable(list);
    }

    // ============ 管理员接口 ============

    @Autowired
    private ISysConfigService sysConfigService;

    private boolean isAdmin()
    {
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        return SecurityUtils.isConfigAdmin(adminConfig, SecurityUtils.getLoginUser().getUser());
    }

    @ApiOperation("管理员强制采纳")
    @PostMapping("/admin/question/{questionId}/accept/{answerId}")
    public AjaxResult adminAccept(@PathVariable Long questionId, @PathVariable Long answerId)
    {
        if (!isAdmin())
        {
            return error("无权限操作");
        }
        bbsQaService.adminAcceptBestAnswer(questionId, answerId);
        return success();
    }

    @ApiOperation("管理员审核问题")
    @PostMapping("/admin/question/{questionId}/audit")
    public AjaxResult auditQuestion(@PathVariable Long questionId, @RequestBody Map<String, Object> body)
    {
        if (!isAdmin())
        {
            return error("无权限操作");
        }
        String auditStatus = String.valueOf(body.get("auditStatus"));
        String auditReason = body.get("auditReason") == null ? null : String.valueOf(body.get("auditReason"));
        bbsQaService.auditQuestion(questionId, auditStatus, auditReason);
        return success();
    }
}