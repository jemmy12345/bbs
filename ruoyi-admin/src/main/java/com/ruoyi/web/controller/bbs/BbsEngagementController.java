package com.ruoyi.web.controller.bbs;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.BbsDemandForm;
import com.ruoyi.system.domain.BbsTopicActivity;
import com.ruoyi.system.domain.BbsVote;
import com.ruoyi.system.domain.BbsVoteOption;
import com.ruoyi.system.service.IBbsEngagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bbs/engagement")
@Api(tags = "Engagement")
public class BbsEngagementController extends BaseController
{
    @Autowired
    private IBbsEngagementService bbsEngagementService;

    @ApiOperation("Create vote/survey")
    @PostMapping("/vote")
    public AjaxResult createVote(@RequestBody Map<String, Object> body)
    {
        BbsVote vote = new BbsVote();
        vote.setTitle(String.valueOf(body.get("title")));
        vote.setDescription(body.get("description") == null ? null : String.valueOf(body.get("description")));
        vote.setVoteType(body.get("voteType") == null ? "single" : String.valueOf(body.get("voteType")));
        vote.setStartTime(body.get("startTime") == null ? null : String.valueOf(body.get("startTime")));
        vote.setEndTime(body.get("endTime") == null ? null : String.valueOf(body.get("endTime")));
        vote.setCreatorUserId(SecurityUtils.getUserId());
        vote.setCreatorNickName(SecurityUtils.getLoginUser().getUser().getNickName());

        List<BbsVoteOption> options = new ArrayList<>();
        Object optionObj = body.get("options");
        if (optionObj instanceof List)
        {
            List<?> list = (List<?>) optionObj;
            for (Object item : list)
            {
                BbsVoteOption option = new BbsVoteOption();
                option.setOptionText(String.valueOf(item));
                options.add(option);
            }
        }
        vote.setOptions(options);
        return success(bbsEngagementService.createVote(vote));
    }

    @ApiOperation("List votes")
    @GetMapping("/vote/list")
    public AjaxResult listVotes(@RequestParam(required = false) String status)
    {
        return success(bbsEngagementService.listVotes(status));
    }

    @ApiOperation("Vote detail")
    @GetMapping("/vote/{voteId}")
    public AjaxResult voteDetail(@PathVariable Long voteId)
    {
        return success(bbsEngagementService.getVoteDetail(voteId));
    }

    @ApiOperation("Submit vote")
    @PostMapping("/vote/{voteId}/submit")
    public AjaxResult submitVote(@PathVariable Long voteId, @RequestBody Map<String, Object> body)
    {
        List<Long> optionIds = new ArrayList<>();
        Object optionObj = body.get("optionIds");
        if (optionObj instanceof List)
        {
            List<?> list = (List<?>) optionObj;
            for (Object item : list)
            {
                optionIds.add(Long.valueOf(String.valueOf(item)));
            }
        }
        bbsEngagementService.submitVote(voteId, optionIds, SecurityUtils.getUserId());
        return success();
    }

    @ApiOperation("Create demand form")
    @PostMapping("/demand/form")
    public AjaxResult createDemandForm(@RequestBody BbsDemandForm form)
    {
        form.setCreatorUserId(SecurityUtils.getUserId());
        form.setCreatorNickName(SecurityUtils.getLoginUser().getUser().getNickName());
        return success(bbsEngagementService.createDemandForm(form));
    }

    @ApiOperation("List demand forms")
    @GetMapping("/demand/form/list")
    public AjaxResult listDemandForm(@RequestParam(required = false) String status)
    {
        return success(bbsEngagementService.listDemandForms(status));
    }

    @ApiOperation("Submit demand")
    @PostMapping("/demand/form/{formId}/submit")
    public AjaxResult submitDemand(@PathVariable Long formId, @RequestBody Map<String, Object> body)
    {
        String content = body.get("submissionContent") == null ? "{}" : String.valueOf(body.get("submissionContent"));
        bbsEngagementService.submitDemand(formId, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), content);
        return success();
    }

    @ApiOperation("Create topic activity")
    @PostMapping("/topic")
    public AjaxResult createTopic(@RequestBody BbsTopicActivity activity)
    {
        activity.setCreatorUserId(SecurityUtils.getUserId());
        activity.setCreatorNickName(SecurityUtils.getLoginUser().getUser().getNickName());
        return success(bbsEngagementService.createTopicActivity(activity));
    }

    @ApiOperation("List topic activities")
    @GetMapping("/topic/list")
    public AjaxResult listTopic(@RequestParam(required = false) String status)
    {
        return success(bbsEngagementService.listTopicActivities(status));
    }

    @ApiOperation("Join topic activity")
    @PostMapping("/topic/{activityId}/join")
    public AjaxResult joinTopic(@PathVariable Long activityId, @RequestBody(required = false) Map<String, Object> body)
    {
        String remark = body == null ? null : (body.get("remark") == null ? null : String.valueOf(body.get("remark")));
        bbsEngagementService.joinTopicActivity(activityId, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), remark);
        return success();
    }
}
