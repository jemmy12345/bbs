package com.ruoyi.web.controller.bbs;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.BbsCollectActivity;
import com.ruoyi.system.domain.BbsCollectDemandTicket;
import com.ruoyi.system.domain.BbsCollectTopicContent;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsOnlineCollectService;
import com.ruoyi.system.service.ISysDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bbs/collect")
@Api(tags = "Online Collect")
@Deprecated  // 功能已屏蔽
public class BbsOnlineCollectController extends BaseController
{
    @Autowired
    private IBbsOnlineCollectService collectService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private SysUserMapper sysUserMapper;

    private boolean isAdminOrOperator()
    {
        try
        {
            return SecurityUtils.isAdmin()
                    || SecurityUtils.hasRole("admin")
                    || SecurityUtils.hasRole("operator");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private Long currentDeptId()
    {
        try
        {
            if (SecurityUtils.getLoginUser().getUser().getDept() == null)
            {
                return null;
            }
            return SecurityUtils.getLoginUser().getUser().getDept().getDeptId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @ApiOperation("创建在线投票")
    @PostMapping("/vote/create")
    public AjaxResult createVote(@RequestBody Map<String, Object> body)
    {
        BbsCollectActivity activity = parseActivity(body);
        String voteType = body.get("voteType") == null ? "single" : String.valueOf(body.get("voteType"));
        String description = body.get("description") == null ? null : String.valueOf(body.get("description"));
        List<String> options = new ArrayList<>();
        Object optionObj = body.get("options");
        if (optionObj instanceof List)
        {
            for (Object item : (List<?>) optionObj)
            {
                options.add(String.valueOf(item));
            }
        }
        Long activityId = collectService.createVoteActivity(activity, voteType, description, options,
                SecurityUtils.getUserId(), SecurityUtils.getLoginUser().getUser().getNickName());
        return success(activityId);
    }

    @ApiOperation("创建在线调研问卷")
    @PostMapping("/survey/create")
    public AjaxResult createSurvey(@RequestBody Map<String, Object> body)
    {
        BbsCollectActivity activity = parseActivity(body);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = body.get("questions") instanceof List
                ? (List<Map<String, Object>>) body.get("questions") : new ArrayList<>();
        Long activityId = collectService.createSurveyActivity(activity, questions,
                SecurityUtils.getUserId(), SecurityUtils.getLoginUser().getUser().getNickName());
        return success(activityId);
    }

    @ApiOperation("创建需求征集表单")
    @PostMapping("/demand/create")
    public AjaxResult createDemand(@RequestBody Map<String, Object> body)
    {
        BbsCollectActivity activity = parseActivity(body);
        String formSchema = body.get("formSchema") == null ? "[]" : String.valueOf(body.get("formSchema"));
        String description = body.get("description") == null ? null : String.valueOf(body.get("description"));
        Long activityId = collectService.createDemandActivity(activity, formSchema, description,
                SecurityUtils.getUserId(), SecurityUtils.getLoginUser().getUser().getNickName());
        return success(activityId);
    }

    @ApiOperation("创建专题话题活动")
    @PostMapping("/topic/create")
    public AjaxResult createTopic(@RequestBody Map<String, Object> body)
    {
        BbsCollectActivity activity = parseActivity(body);
        String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
        Long activityId = collectService.createTopicActivity(activity, content,
                SecurityUtils.getUserId(), SecurityUtils.getLoginUser().getUser().getNickName());
        return success(activityId);
    }

    @ApiOperation("活动管理列表")
    @GetMapping("/activity/list")
    public TableDataInfo listActivity(@RequestParam(required = false) String activityType,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        return getDataTable(collectService.listActivities(activityType, status, keyword,
                SecurityUtils.getUserId(), isAdminOrOperator()));
    }

    @ApiOperation("活动详情")
    @GetMapping("/activity/{activityId}")
    public AjaxResult activityDetail(@PathVariable Long activityId)
    {
        return success(collectService.getActivityDetail(activityId, SecurityUtils.getUserId(), isAdminOrOperator()));
    }

    @ApiOperation("结束活动")
    @PostMapping("/activity/{activityId}/end")
    public AjaxResult endActivity(@PathVariable Long activityId)
    {
        collectService.endActivity(activityId, SecurityUtils.getUserId(), isAdminOrOperator());
        return success();
    }

    @ApiOperation("删除活动")
    @DeleteMapping("/activity/{activityId}")
    public AjaxResult deleteActivity(@PathVariable Long activityId)
    {
        collectService.deleteActivity(activityId, SecurityUtils.getUserId(), isAdminOrOperator());
        return success();
    }

    @ApiOperation("参与投票")
    @PostMapping("/vote/{activityId}/submit")
    public AjaxResult submitVote(@PathVariable Long activityId, @RequestBody Map<String, Object> body)
    {
        List<Long> optionIds = new ArrayList<>();
        if (body.get("optionIds") instanceof List)
        {
            for (Object item : (List<?>) body.get("optionIds"))
            {
                optionIds.add(Long.valueOf(String.valueOf(item)));
            }
        }
        collectService.submitVote(activityId, optionIds, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), currentDeptId());
        return success();
    }

    @ApiOperation("保存问卷草稿")
    @PostMapping("/survey/{activityId}/draft")
    public AjaxResult saveSurveyDraft(@PathVariable Long activityId, @RequestBody Map<String, Object> body)
    {
        String answersJson = body.get("answersJson") == null ? "{}" : String.valueOf(body.get("answersJson"));
        collectService.saveSurveyDraft(activityId, answersJson, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), currentDeptId());
        return success();
    }

    @ApiOperation("提交问卷")
    @PostMapping("/survey/{activityId}/submit")
    public AjaxResult submitSurvey(@PathVariable Long activityId, @RequestBody Map<String, Object> body)
    {
        String answersJson = body.get("answersJson") == null ? "{}" : String.valueOf(body.get("answersJson"));
        collectService.submitSurvey(activityId, answersJson, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), currentDeptId());
        return success();
    }

    @ApiOperation("提交需求")
    @PostMapping("/demand/{activityId}/submit")
    public AjaxResult submitDemand(@PathVariable Long activityId, @RequestBody BbsCollectDemandTicket ticket)
    {
        return success(collectService.submitDemand(activityId, ticket, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), currentDeptId()));
    }

    @ApiOperation("需求工单列表")
    @GetMapping("/demand/{activityId}/ticket/list")
    public TableDataInfo listDemandTicket(@PathVariable Long activityId,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        return getDataTable(collectService.listDemandTickets(activityId, status, keyword,
                SecurityUtils.getUserId(), isAdminOrOperator()));
    }

    @ApiOperation("分配需求负责人")
    @PostMapping("/demand/ticket/{ticketId}/assign")
    public AjaxResult assignDemand(@PathVariable Long ticketId, @RequestBody Map<String, Object> body)
    {
        String assigneeUserId = body.get("assigneeUserId") == null ? null : String.valueOf(body.get("assigneeUserId"));
        String assigneeNickName = body.get("assigneeNickName") == null ? null : String.valueOf(body.get("assigneeNickName"));
        if (StringUtils.isBlank(assigneeUserId))
        {
            return AjaxResult.error("负责人不能为空");
        }
        collectService.assignDemandTicket(ticketId, assigneeUserId, assigneeNickName,
                SecurityUtils.getUserId(), isAdminOrOperator());
        return success();
    }

    @ApiOperation("更新需求状态")
    @PostMapping("/demand/ticket/{ticketId}/status")
    public AjaxResult updateDemandStatus(@PathVariable Long ticketId, @RequestBody Map<String, Object> body)
    {
        String status = body.get("status") == null ? null : String.valueOf(body.get("status"));
        String comment = body.get("comment") == null ? null : String.valueOf(body.get("comment"));
        if (StringUtils.isBlank(status))
        {
            return AjaxResult.error("状态不能为空");
        }
        collectService.updateDemandTicketStatus(ticketId, status, comment,
                SecurityUtils.getUserId(), isAdminOrOperator());
        return success();
    }

    @ApiOperation("参与专题活动")
    @PostMapping("/topic/{activityId}/join")
    public AjaxResult joinTopic(@PathVariable Long activityId, @RequestBody(required = false) Map<String, Object> body)
    {
        String remark = body == null ? null : (body.get("remark") == null ? null : String.valueOf(body.get("remark")));
        collectService.joinTopic(activityId, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), remark, currentDeptId());
        return success();
    }

    @ApiOperation("专题活动关联内容")
    @PostMapping("/topic/{activityId}/content")
    public AjaxResult addTopicContent(@PathVariable Long activityId, @RequestBody BbsCollectTopicContent content)
    {
        collectService.addTopicContent(activityId, content, SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), currentDeptId());
        return success();
    }

    @ApiOperation("专题活动详情")
    @GetMapping("/topic/{activityId}")
    public AjaxResult topicDetail(@PathVariable Long activityId)
    {
        return success(collectService.topicDetail(activityId, SecurityUtils.getUserId(), isAdminOrOperator()));
    }

    @ApiOperation("活动数据统计")
    @GetMapping("/activity/{activityId}/stats")
    public AjaxResult activityStats(@PathVariable Long activityId)
    {
        return success(collectService.getActivityStats(activityId, SecurityUtils.getUserId(), isAdminOrOperator()));
    }

    @ApiOperation("投票参与页元数据")
    @GetMapping("/vote/{activityId}/meta")
    public AjaxResult voteParticipateMeta(@PathVariable Long activityId)
    {
        return success(collectService.getVoteParticipateMeta(activityId, SecurityUtils.getUserId(), isAdminOrOperator()));
    }

    @ApiOperation("投票范围组织树（部门+人员）")
    @GetMapping("/vote/target/tree")
    public AjaxResult voteTargetTree()
    {
        SysDept queryDept = new SysDept();
        queryDept.setStatus("0");
        List<SysDept> depts = deptService.selectDeptListNoDataScope(queryDept);

        SysUser queryUser = new SysUser();
        queryUser.setStatus("0");
        List<SysUser> users = sysUserMapper.selectUserList(queryUser);

        Map<Long, List<SysUser>> usersByDept = users.stream()
                .filter(u -> u.getDeptId() != null)
                .collect(Collectors.groupingBy(SysUser::getDeptId));

        Map<Long, Map<String, Object>> deptNodeMap = new HashMap<>();
        for (SysDept dept : depts)
        {
            Map<String, Object> node = new HashMap<>();
            node.put("id", "D_" + dept.getDeptId());
            node.put("label", dept.getDeptName());
            node.put("type", "dept");
            node.put("children", new ArrayList<>());
            node.put("deptId", dept.getDeptId());
            node.put("parentId", dept.getParentId());
            deptNodeMap.put(dept.getDeptId(), node);
        }

        for (Map.Entry<Long, List<SysUser>> entry : usersByDept.entrySet())
        {
            Map<String, Object> deptNode = deptNodeMap.get(entry.getKey());
            if (deptNode == null)
            {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) deptNode.get("children");
            for (SysUser user : entry.getValue())
            {
                if (StringUtils.isBlank(user.getUserId()))
                {
                    continue;
                }
                Map<String, Object> userNode = new HashMap<>();
                userNode.put("id", "U_" + user.getUserId());
                userNode.put("label", StringUtils.isBlank(user.getNickName()) ? user.getUserName() : user.getNickName());
                userNode.put("type", "user");
                userNode.put("userId", user.getUserId());
                children.add(userNode);
            }
            children.sort(Comparator.comparing(i -> String.valueOf(i.get("label"))));
        }

        List<Map<String, Object>> roots = new ArrayList<>();
        for (SysDept dept : depts)
        {
            Map<String, Object> node = deptNodeMap.get(dept.getDeptId());
            if (node == null)
            {
                continue;
            }
            Long parentId = dept.getParentId();
            if (parentId != null && deptNodeMap.containsKey(parentId))
            {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> parentChildren = (List<Map<String, Object>>) deptNodeMap.get(parentId).get("children");
                parentChildren.add(node);
            }
            else
            {
                roots.add(node);
            }
        }

        for (Map<String, Object> root : roots)
        {
            sortDeptTree(root);
        }
        roots.sort(Comparator.comparing(i -> String.valueOf(i.get("label"))));
        return success(roots);
    }

    @ApiOperation("专题活动归档")
    @PostMapping("/topic/{activityId}/archive")
    public AjaxResult archiveTopic(@PathVariable Long activityId, @RequestBody Map<String, Object> body)
    {
        String summaryText = body.get("summaryText") == null ? null : String.valueOf(body.get("summaryText"));
        collectService.archiveTopic(activityId, summaryText, SecurityUtils.getUserId(), isAdminOrOperator());
        return success();
    }

    private BbsCollectActivity parseActivity(Map<String, Object> body)
    {
        BbsCollectActivity activity = new BbsCollectActivity();
        activity.setTitle(String.valueOf(body.get("title")));
        activity.setScopeType(body.get("scopeType") == null ? "all" : String.valueOf(body.get("scopeType")));
        activity.setScopeValue(body.get("scopeValue") == null ? null : String.valueOf(body.get("scopeValue")));
        activity.setStartTime(body.get("startTime") == null ? null : String.valueOf(body.get("startTime")));
        activity.setEndTime(body.get("endTime") == null ? null : String.valueOf(body.get("endTime")));
        activity.setAnonymousFlag(body.get("anonymousFlag") == null ? "0" : String.valueOf(body.get("anonymousFlag")));
        activity.setRealtimeFlag(body.get("realtimeFlag") == null ? "1" : String.valueOf(body.get("realtimeFlag")));
        activity.setLimitOnceFlag(body.get("limitOnceFlag") == null ? "1" : String.valueOf(body.get("limitOnceFlag")));
        activity.setBannerUrl(body.get("bannerUrl") == null ? null : String.valueOf(body.get("bannerUrl")));
        activity.setRuleText(body.get("ruleText") == null ? null : String.valueOf(body.get("ruleText")));
        activity.setRemark(body.get("remark") == null ? null : String.valueOf(body.get("remark")));
        return activity;
    }

    @SuppressWarnings("unchecked")
    private void sortDeptTree(Map<String, Object> node)
    {
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children == null || children.isEmpty())
        {
            return;
        }
        children.sort((a, b) -> {
            String aType = String.valueOf(a.get("type"));
            String bType = String.valueOf(b.get("type"));
            if (!aType.equals(bType))
            {
                return "dept".equals(aType) ? -1 : 1;
            }
            return String.valueOf(a.get("label")).compareTo(String.valueOf(b.get("label")));
        });
        for (Map<String, Object> child : children)
        {
            if ("dept".equals(String.valueOf(child.get("type"))))
            {
                sortDeptTree(child);
            }
        }
    }
}
