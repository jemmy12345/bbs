package com.ruoyi.web.controller.bbs;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsIdeaProposal;
import com.ruoyi.system.service.IBbsIdeaService;
import com.ruoyi.system.service.IBbsIdeaStageReviewerConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bbs/idea")
@Api(tags = "Innovation Proposal")
public class BbsIdeaController extends BaseController
{
    @Autowired
    private IBbsIdeaService bbsIdeaService;

    @Autowired
    private IBbsIdeaStageReviewerConfigService reviewerConfigService;

    private boolean isAdminUser()
    {
        try
        {
            return SecurityUtils.isAdmin()
                    || SecurityUtils.hasRole("admin");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private Set<String> currentRoleKeys()
    {
        return SecurityUtils.getLoginUser().getUser().getRoles().stream()
                .map(role -> role.getRoleKey())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    private boolean canReviewAny()
    {
        return reviewerConfigService.canReviewAnyStage(SecurityUtils.getUserId(), currentRoleKeys(), isAdminUser());
    }

    private boolean canReviewStage(String stage)
    {
        return reviewerConfigService.canReviewStageByUserId(stage, SecurityUtils.getUserId(), currentRoleKeys(), isAdminUser());
    }

    private boolean canManageFlow()
    {
        return isAdminUser() || canReviewStage("L3");
    }

    @ApiOperation("保存草稿")
    @PostMapping("/draft")
    public AjaxResult saveDraft(@RequestBody BbsIdeaProposal idea)
    {
        String userId = SecurityUtils.getUserId();
        String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
        return success(bbsIdeaService.saveOrSubmitIdea(idea, userId, nickName, false));
    }

    @ApiOperation("更新草稿")
    @PutMapping("/{ideaId}/draft")
    public AjaxResult updateDraft(@PathVariable Long ideaId, @RequestBody BbsIdeaProposal idea)
    {
        idea.setIdeaId(ideaId);
        bbsIdeaService.updateDraft(idea, SecurityUtils.getUserId());
        return success();
    }

    @ApiOperation("提交提案")
    @PostMapping
    public AjaxResult submitIdea(@RequestBody BbsIdeaProposal idea)
    {
        String userId = SecurityUtils.getUserId();
        String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
        return success(bbsIdeaService.saveOrSubmitIdea(idea, userId, nickName, true));
    }

    @ApiOperation("提案列表")
    @GetMapping("/list")
    public TableDataInfo listIdeas(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String isPublicized,
                                   @RequestParam(required = false, defaultValue = "false") Boolean mine,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        String userId = SecurityUtils.getUserId();
        boolean isAdmin = isAdminUser();
        boolean canReviewAny = Boolean.TRUE.equals(mine) ? false : canReviewAny();
        List<BbsIdeaProposal> list = bbsIdeaService.listIdeas(status, category, keyword, isPublicized, userId,
                isAdmin, canReviewAny, mine);
        return getDataTable(list);
    }

    @ApiOperation("提案详情")
    @GetMapping("/{ideaId}")
    public AjaxResult detail(@PathVariable Long ideaId)
    {
        return success(bbsIdeaService.getIdeaDetail(ideaId, SecurityUtils.getUserId(), isAdminUser(), canReviewAny()));
    }

    @ApiOperation("评审提案")
    @PostMapping("/{ideaId}/review")
    public AjaxResult review(@PathVariable Long ideaId, @RequestBody Map<String, Object> body)
    {
        String stage = body.get("stage") == null ? null : String.valueOf(body.get("stage"));
        if (!canReviewStage(stage))
        {
            return AjaxResult.error("无该评审环节处理权限");
        }
        String action = body.get("action") == null ? "pass" : String.valueOf(body.get("action"));
        Integer score = body.get("score") == null ? null : Integer.parseInt(String.valueOf(body.get("score")));
        String comment = body.get("comment") == null ? null : String.valueOf(body.get("comment"));
        String rejectReason = body.get("rejectReason") == null ? null : String.valueOf(body.get("rejectReason"));
        bbsIdeaService.reviewIdea(ideaId, stage, action, score, comment, rejectReason,
                SecurityUtils.getUserId(), SecurityUtils.getLoginUser().getUser().getNickName());
        return success();
    }

    @ApiOperation("更新进度")
    @PostMapping("/{ideaId}/progress")
    public AjaxResult addProgress(@PathVariable Long ideaId, @RequestBody Map<String, Object> body)
    {
        // 权限由service层校验（仅提案人且终审通过后）
        String statusEnum = body.get("status") == null ? null : String.valueOf(body.get("status"));
        String stage = body.get("stage") == null ? "进度更新" : String.valueOf(body.get("stage"));
        String desc = body.get("desc") == null ? null : String.valueOf(body.get("desc"));
        String attachmentUrls = body.get("attachmentUrls") == null ? null : String.valueOf(body.get("attachmentUrls"));
        Integer percent = body.get("percent") == null ? 0 : Integer.parseInt(String.valueOf(body.get("percent")));
        bbsIdeaService.addProgress(ideaId, statusEnum, stage, desc, attachmentUrls, percent,
                SecurityUtils.getUserId(), SecurityUtils.getLoginUser().getUser().getNickName());
        return success();
    }

    @ApiOperation("重新提交（驳回后修改）")
    @PostMapping("/{ideaId}/resubmit")
    public AjaxResult resubmit(@PathVariable Long ideaId, @RequestBody BbsIdeaProposal idea)
    {
        idea.setIdeaId(ideaId);
        bbsIdeaService.resubmitIdea(idea, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName());
        return success();
    }

    @ApiOperation("更新提案状态")
    @PostMapping("/{ideaId}/status")
    public AjaxResult updateStatus(@PathVariable Long ideaId, @RequestBody Map<String, Object> body)
    {
        if (!canManageFlow())
        {
            return AjaxResult.error("无状态管理权限");
        }
        String status = body.get("status") == null ? null : String.valueOf(body.get("status"));
        if (StringUtils.isBlank(status))
        {
            return AjaxResult.error("状态不能为空");
        }
        bbsIdeaService.updateIdeaStatus(ideaId, status);
        return success();
    }

    @ApiOperation("成果公示")
    @PostMapping("/{ideaId}/publish")
    public AjaxResult publish(@PathVariable Long ideaId, @RequestBody Map<String, Object> body)
    {
        String resultSummary = body.get("resultSummary") == null ? null : String.valueOf(body.get("resultSummary"));
        Integer incentivePoints = body.get("incentivePoints") == null ? 0 : Integer.parseInt(String.valueOf(body.get("incentivePoints")));
        String honorBadge = body.get("honorBadge") == null ? null : String.valueOf(body.get("honorBadge"));
        bbsIdeaService.publishResult(ideaId, resultSummary, incentivePoints, honorBadge, SecurityUtils.getUserId());
        return success();
    }

    @ApiOperation("成果公示墙")
    @GetMapping("/wall")
    public TableDataInfo wall(@RequestParam(required = false, defaultValue = "latest") String sortBy,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "12") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        return getDataTable(bbsIdeaService.listPublicizedWall(sortBy, category, keyword, SecurityUtils.getUserId()));
    }

    @ApiOperation("公示提案点赞")
    @PostMapping("/{ideaId}/like")
    public AjaxResult like(@PathVariable Long ideaId)
    {
        return success(bbsIdeaService.likePublicizedIdea(ideaId, SecurityUtils.getUserId()));
    }

    @ApiOperation("公示提案分享")
    @PostMapping("/{ideaId}/share")
    public AjaxResult share(@PathVariable Long ideaId)
    {
        return success(bbsIdeaService.sharePublicizedIdea(ideaId, SecurityUtils.getUserId()));
    }
}
