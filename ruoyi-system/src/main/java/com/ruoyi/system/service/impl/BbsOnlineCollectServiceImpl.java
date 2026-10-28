package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsCollectActivity;
import com.ruoyi.system.domain.BbsCollectDemandTicket;
import com.ruoyi.system.domain.BbsNotification;
import com.ruoyi.system.domain.BbsCollectSurveyQuestion;
import com.ruoyi.system.domain.BbsCollectSurveyResponse;
import com.ruoyi.system.domain.BbsCollectTopicContent;
import com.ruoyi.system.domain.BbsDemandForm;
import com.ruoyi.system.domain.BbsTopicActivity;
import com.ruoyi.system.domain.BbsVote;
import com.ruoyi.system.domain.BbsVoteOption;
import com.ruoyi.system.mapper.BbsNotificationMapper;
import com.ruoyi.system.mapper.BbsOnlineCollectMapper;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsOnlineCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BbsOnlineCollectServiceImpl implements IBbsOnlineCollectService
{
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_ENDED = "ENDED";
    private static final String STATUS_DELETED = "DELETED";

    @Autowired
    private BbsOnlineCollectMapper mapper;

    @Autowired
    private BbsNotificationMapper bbsNotificationMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    @Transactional
    public Long createVoteActivity(BbsCollectActivity activity, String voteType, String description, List<String> options,
                                   String userId, String nickName)
    {
        validateCreator(activity, userId, nickName);
        if (options == null || options.size() < 2)
        {
            throw new ServiceException("投票至少需要 2 个选项");
        }

        BbsVote vote = new BbsVote();
        vote.setTitle(activity.getTitle());
        vote.setDescription(description);
        vote.setVoteType(StringUtils.isBlank(voteType) ? "single" : voteType);
        vote.setStartTime(activity.getStartTime());
        vote.setEndTime(activity.getEndTime());
        vote.setStatus("open");
        vote.setCreatorUserId(userId);
        vote.setCreatorNickName(nickName);
        mapper.insertVote(vote);

        int sort = 1;
        for (String text : options)
        {
            if (StringUtils.isBlank(text))
            {
                continue;
            }
            BbsVoteOption option = new BbsVoteOption();
            option.setVoteId(vote.getVoteId());
            option.setOptionText(text.trim());
            option.setSortOrder(sort++);
            mapper.insertVoteOption(option);
        }

        activity.setActivityType("vote");
        activity.setStatus(STATUS_PUBLISHED);
        activity.setBizId(vote.getVoteId());
        activity.setParticipantCount(0);
        mapper.insertCollectActivity(activity);

        sendVoteCreatedNotification(activity, userId, nickName);
        return activity.getActivityId();
    }

    @Override
    @Transactional
    public Long createSurveyActivity(BbsCollectActivity activity, List<Map<String, Object>> questions, String userId, String nickName)
    {
        validateCreator(activity, userId, nickName);
        if (questions == null || questions.isEmpty())
        {
            throw new ServiceException("问卷至少需要一个题目");
        }

        activity.setActivityType("survey");
        activity.setStatus(STATUS_PUBLISHED);
        activity.setParticipantCount(0);
        mapper.insertCollectActivity(activity);

        int no = 1;
        for (Map<String, Object> q : questions)
        {
            BbsCollectSurveyQuestion question = new BbsCollectSurveyQuestion();
            question.setActivityId(activity.getActivityId());
            question.setQuestionNo(no++);
            question.setQuestionType(str(q.get("questionType"), "single"));
            question.setQuestionTitle(str(q.get("questionTitle"), null));
            question.setOptionJson(str(q.get("optionJson"), null));
            question.setJumpLogicJson(str(q.get("jumpLogicJson"), null));
            question.setRequiredFlag(str(q.get("requiredFlag"), "1"));
            if (StringUtils.isBlank(question.getQuestionTitle()))
            {
                throw new ServiceException("问卷题目不能为空");
            }
            mapper.insertSurveyQuestion(question);
        }
        return activity.getActivityId();
    }

    @Override
    @Transactional
    public Long createDemandActivity(BbsCollectActivity activity, String formSchema, String description,
                                     String userId, String nickName)
    {
        validateCreator(activity, userId, nickName);

        BbsDemandForm form = new BbsDemandForm();
        form.setTitle(activity.getTitle());
        form.setDescription(description);
        form.setFormSchema(StringUtils.isBlank(formSchema) ? "[]" : formSchema);
        form.setStatus("open");
        form.setCreatorUserId(userId);
        form.setCreatorNickName(nickName);
        mapper.insertDemandForm(form);

        activity.setActivityType("demand");
        activity.setStatus(STATUS_PUBLISHED);
        activity.setBizId(form.getFormId());
        activity.setParticipantCount(0);
        mapper.insertCollectActivity(activity);
        return activity.getActivityId();
    }

    @Override
    @Transactional
    public Long createTopicActivity(BbsCollectActivity activity, String content, String userId, String nickName)
    {
        validateCreator(activity, userId, nickName);

        BbsTopicActivity topic = new BbsTopicActivity();
        topic.setTitle(activity.getTitle());
        topic.setContent(StringUtils.isBlank(content) ? "" : content);
        topic.setStartTime(activity.getStartTime());
        topic.setEndTime(activity.getEndTime());
        topic.setStatus("open");
        topic.setJoinCount(0);
        topic.setCreatorUserId(userId);
        topic.setCreatorNickName(nickName);
        mapper.insertTopicActivity(topic);

        activity.setActivityType("topic");
        activity.setStatus(STATUS_PUBLISHED);
        activity.setBizId(topic.getActivityId());
        activity.setParticipantCount(0);
        mapper.insertCollectActivity(activity);
        return activity.getActivityId();
    }

    @Override
    public List<BbsCollectActivity> listActivities(String activityType, String status, String keyword,
                                                   String currentUserId, boolean adminView)
    {
        return mapper.selectCollectActivityList(activityType, status, keyword, currentUserId, adminView ? 1 : 0);
    }

    @Override
    public BbsCollectActivity getActivityDetail(Long activityId, String currentUserId, boolean adminView)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!canParticipate(activity, currentUserId, null, adminView))
        {
            throw new ServiceException("无权限查看该活动");
        }
        activity.setHasParticipated(mapper.hasParticipation(activityId, currentUserId, "SUBMITTED") > 0 ? "1" : "0");
        return activity;
    }

    @Override
    public Map<String, Object> getVoteParticipateMeta(Long activityId, String currentUserId, boolean adminView)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!"vote".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是投票");
        }
        if (!canParticipate(activity, currentUserId, null, adminView))
        {
            throw new ServiceException("无权限查看该活动");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("vote", mapper.selectVoteById(activity.getBizId()));
        data.put("options", mapper.selectVoteOptions(activity.getBizId()));
        return data;
    }

    @Override
    public void endActivity(Long activityId, String operatorUserId, boolean admin)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if ("vote".equals(activity.getActivityType()))
        {
            requireVoteCreator(activity, operatorUserId);
        }
        else
        {
            requireOwnerOrAdmin(activity, operatorUserId, admin);
        }
        BbsCollectActivity update = new BbsCollectActivity();
        update.setActivityId(activityId);
        update.setStatus(STATUS_ENDED);
        mapper.updateCollectActivity(update);
    }

    @Override
    public void deleteActivity(Long activityId, String operatorUserId, boolean admin)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if ("vote".equals(activity.getActivityType()))
        {
            requireVoteCreator(activity, operatorUserId);
        }
        else
        {
            requireOwnerOrAdmin(activity, operatorUserId, admin);
        }
        mapper.deleteCollectActivity(activityId);
    }

    @Override
    @Transactional
    public void submitVote(Long activityId, List<Long> optionIds, String userId, String nickName, Long deptId)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!"vote".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是投票");
        }
        verifyOpenAndScope(activity, userId, deptId, false);

        if (optionIds == null || optionIds.isEmpty())
        {
            throw new ServiceException("请至少选择一项");
        }

        if ("1".equals(activity.getLimitOnceFlag()) && mapper.hasParticipation(activityId, userId, "SUBMITTED") > 0)
        {
            throw new ServiceException("你已参与该投票");
        }
        Long voteId = activity.getBizId();
        if (voteId == null)
        {
            throw new ServiceException("投票活动配置异常");
        }

        BbsVote vote = mapper.selectVoteById(voteId);
        if (vote == null)
        {
            throw new ServiceException("投票不存在");
        }
        if ("single".equalsIgnoreCase(vote.getVoteType()) && optionIds.size() > 1)
        {
            throw new ServiceException("单选投票仅能选择一个选项");
        }
        if (mapper.hasVotedInVote(voteId, userId) > 0 && "1".equals(activity.getLimitOnceFlag()))
        {
            throw new ServiceException("你已投票");
        }

        for (Long optionId : optionIds)
        {
            mapper.insertVoteRecord(voteId, optionId, userId);
            mapper.increaseVoteOptionCount(optionId);
        }

        mapper.upsertParticipation(activityId, activity.getActivityType(), userId, nickName, "SUBMITTED", nowString());
        mapper.increaseParticipantCount(activityId);
    }

    @Override
    @Transactional
    public void saveSurveyDraft(Long activityId, String answersJson, String userId, String nickName, Long deptId)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!"survey".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是调研问卷");
        }
        verifyOpenAndScope(activity, userId, deptId, false);

        BbsCollectSurveyResponse response = new BbsCollectSurveyResponse();
        response.setActivityId(activityId);
        response.setUserId(userId);
        response.setUserNickName(nickName);
        response.setAnswersJson(StringUtils.isBlank(answersJson) ? "{}" : answersJson);
        response.setSubmitStatus("DRAFT");
        response.setSubmitTime(null);
        mapper.upsertSurveyResponse(response);
        mapper.upsertParticipation(activityId, activity.getActivityType(), userId, nickName, "DRAFT", null);
    }

    @Override
    @Transactional
    public void submitSurvey(Long activityId, String answersJson, String userId, String nickName, Long deptId)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!"survey".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是调研问卷");
        }
        verifyOpenAndScope(activity, userId, deptId, false);

        if (StringUtils.isBlank(answersJson))
        {
            throw new ServiceException("问卷答案不能为空");
        }

        BbsCollectSurveyResponse existed = mapper.selectSurveyResponseByUser(activityId, userId);
        if ("1".equals(activity.getLimitOnceFlag()) && existed != null && "SUBMITTED".equals(existed.getSubmitStatus()))
        {
            throw new ServiceException("你已提交该问卷");
        }

        BbsCollectSurveyResponse response = new BbsCollectSurveyResponse();
        response.setActivityId(activityId);
        response.setUserId(userId);
        response.setUserNickName(nickName);
        response.setAnswersJson(answersJson);
        response.setSubmitStatus("SUBMITTED");
        response.setSubmitTime(nowString());
        mapper.upsertSurveyResponse(response);
        mapper.upsertParticipation(activityId, activity.getActivityType(), userId, nickName, "SUBMITTED", nowString());
        mapper.increaseParticipantCount(activityId);
    }

    @Override
    @Transactional
    public Long submitDemand(Long activityId, BbsCollectDemandTicket ticket, String userId, String nickName, Long deptId)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!"demand".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是需求征集");
        }
        verifyOpenAndScope(activity, userId, deptId, false);

        if (ticket == null || StringUtils.isBlank(ticket.getDemandName())
                || StringUtils.isBlank(ticket.getDemandType())
                || StringUtils.isBlank(ticket.getDemandDetail()))
        {
            throw new ServiceException("需求名称、类型、详情为必填项");
        }

        if (StringUtils.isBlank(ticket.getPriority()))
        {
            ticket.setPriority("P3");
        }

        ticket.setActivityId(activityId);
        ticket.setFormId(activity.getBizId());
        ticket.setSubmitUserId(userId);
        ticket.setSubmitNickName(nickName);
        ticket.setStatus("TODO");

        if (StringUtils.isBlank(ticket.getAssigneeUserId()))
        {
            ticket.setAssigneeUserId(activity.getCreatorUserId());
            ticket.setAssigneeNickName(activity.getCreatorNickName());
        }

        mapper.insertDemandTicket(ticket);
        mapper.upsertParticipation(activityId, activity.getActivityType(), userId, nickName, "SUBMITTED", nowString());
        mapper.increaseParticipantCount(activityId);
        return ticket.getTicketId();
    }

    @Override
    public List<BbsCollectDemandTicket> listDemandTickets(Long activityId, String status, String keyword,
                                                           String currentUserId, boolean adminView)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!adminView && !currentUserId.equals(activity.getCreatorUserId()))
        {
            List<BbsCollectDemandTicket> all = mapper.selectDemandTicketList(activityId, status, keyword);
            List<BbsCollectDemandTicket> own = new ArrayList<>();
            for (BbsCollectDemandTicket ticket : all)
            {
                if (currentUserId.equals(ticket.getSubmitUserId()) || currentUserId.equals(ticket.getAssigneeUserId()))
                {
                    own.add(ticket);
                }
            }
            return own;
        }
        return mapper.selectDemandTicketList(activityId, status, keyword);
    }

    @Override
    public void assignDemandTicket(Long ticketId, String assigneeUserId, String assigneeNickName,
                                   String operatorUserId, boolean admin)
    {
        BbsCollectDemandTicket ticket = mustTicket(ticketId);
        BbsCollectActivity activity = mustActivity(ticket.getActivityId());
        if (!admin && !operatorUserId.equals(activity.getCreatorUserId()))
        {
            throw new ServiceException("无分配权限");
        }
        mapper.updateDemandTicketAssign(ticketId, assigneeUserId, assigneeNickName);
    }

    @Override
    public void updateDemandTicketStatus(Long ticketId, String status, String comment,
                                         String operatorUserId, boolean admin)
    {
        BbsCollectDemandTicket ticket = mustTicket(ticketId);
        if (!admin && !operatorUserId.equals(ticket.getAssigneeUserId()) && !operatorUserId.equals(ticket.getSubmitUserId()))
        {
            throw new ServiceException("无状态更新权限");
        }
        if (!"TODO".equals(status) && !"PROCESSING".equals(status) && !"DONE".equals(status) && !"REJECTED".equals(status))
        {
            throw new ServiceException("不支持的状态");
        }
        mapper.updateDemandTicketStatus(ticketId, status, comment);
    }

    @Override
    @Transactional
    public void joinTopic(Long activityId, String userId, String nickName, String remark, Long deptId)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!"topic".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是专题");
        }
        verifyOpenAndScope(activity, userId, deptId, false);

        int affected = mapper.joinTopicActivity(activity.getBizId(), userId, nickName, remark);
        if (affected > 0)
        {
            mapper.increaseTopicJoinCount(activity.getBizId());
            mapper.upsertParticipation(activityId, activity.getActivityType(), userId, nickName, "SUBMITTED", nowString());
            mapper.increaseParticipantCount(activityId);
        }
    }

    @Override
    public void addTopicContent(Long activityId, BbsCollectTopicContent content, String userId, String nickName, Long deptId)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if (!"topic".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是专题");
        }
        verifyOpenAndScope(activity, userId, deptId, false);

        if (content == null || StringUtils.isBlank(content.getContentType()) || content.getContentId() == null)
        {
            throw new ServiceException("关联内容类型和ID必填");
        }

        content.setActivityId(activityId);
        content.setAuthorUserId(userId);
        content.setAuthorNickName(nickName);
        if (content.getScore() == null)
        {
            content.setScore(0);
        }
        if (content.getLikeCount() == null)
        {
            content.setLikeCount(0);
        }
        if (content.getCommentCount() == null)
        {
            content.setCommentCount(0);
        }
        if (content.getSortOrder() == null)
        {
            content.setSortOrder(0);
        }
        mapper.insertTopicContent(content);
    }

    @Override
    public Map<String, Object> topicDetail(Long activityId, String currentUserId, boolean adminView)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        Map<String, Object> data = new HashMap<>();
        data.put("activity", activity);
        data.put("topic", mapper.selectTopicActivityById(activity.getBizId()));
        data.put("contentList", mapper.selectTopicContentList(activityId));
        data.put("rankList", mapper.selectTopicRank(activityId));
        return data;
    }

    @Override
    public Map<String, Object> getActivityStats(Long activityId, String currentUserId, boolean adminView)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        if ("vote".equals(activity.getActivityType()) && !StringUtils.equals(currentUserId, activity.getCreatorUserId()))
        {
            throw new ServiceException("仅投票创建人可查看投票数据");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("activity", activity);

        if ("vote".equals(activity.getActivityType()))
        {
            data.put("vote", mapper.selectVoteById(activity.getBizId()));
            data.put("options", mapper.selectVoteOptions(activity.getBizId()));
            data.put("participantCount", mapper.selectVoteParticipantCount(activity.getBizId()));
        }
        else if ("survey".equals(activity.getActivityType()))
        {
            data.put("questions", mapper.selectSurveyQuestions(activityId));
            data.put("submittedCount", mapper.countSurveySubmitted(activityId));
            data.put("responses", mapper.selectSurveySubmittedList(activityId));
        }
        else if ("demand".equals(activity.getActivityType()))
        {
            data.put("tickets", mapper.selectDemandTicketList(activityId, null, null));
        }
        else if ("topic".equals(activity.getActivityType()))
        {
            data.put("topic", mapper.selectTopicActivityById(activity.getBizId()));
            data.put("contentList", mapper.selectTopicContentList(activityId));
            data.put("rankList", mapper.selectTopicRank(activityId));
        }
        return data;
    }

    @Override
    public void archiveTopic(Long activityId, String summaryText, String operatorUserId, boolean admin)
    {
        BbsCollectActivity activity = mustActivity(activityId);
        requireOwnerOrAdmin(activity, operatorUserId, admin);
        if (!"topic".equals(activity.getActivityType()))
        {
            throw new ServiceException("活动类型不是专题");
        }
        mapper.archiveTopicActivity(activity.getBizId(), summaryText);
        BbsCollectActivity update = new BbsCollectActivity();
        update.setActivityId(activityId);
        update.setStatus(STATUS_ENDED);
        mapper.updateCollectActivity(update);
    }

    private void validateCreator(BbsCollectActivity activity, String userId, String nickName)
    {
        if (activity == null || StringUtils.isBlank(activity.getTitle()))
        {
            throw new ServiceException("活动标题不能为空");
        }
        activity.setCreatorUserId(userId);
        activity.setCreatorNickName(nickName);
        if (StringUtils.isBlank(activity.getScopeType()))
        {
            activity.setScopeType("all");
        }
        if (StringUtils.isBlank(activity.getAnonymousFlag()))
        {
            activity.setAnonymousFlag("0");
        }
        if (StringUtils.isBlank(activity.getRealtimeFlag()))
        {
            activity.setRealtimeFlag("1");
        }
        if (StringUtils.isBlank(activity.getLimitOnceFlag()))
        {
            activity.setLimitOnceFlag("1");
        }
    }

    private void verifyOpenAndScope(BbsCollectActivity activity, String userId, Long deptId, boolean admin)
    {
        if (STATUS_ENDED.equals(activity.getStatus()) || STATUS_DELETED.equals(activity.getStatus()))
        {
            throw new ServiceException("活动已结束");
        }
        if (!admin && !canParticipate(activity, userId, deptId, false))
        {
            throw new ServiceException("不在活动参与范围内");
        }
    }

    private boolean canParticipate(BbsCollectActivity activity, String userId, Long deptId, boolean admin)
    {
        if (admin)
        {
            return true;
        }
        if (activity == null || StringUtils.isBlank(userId))
        {
            return false;
        }
        if (userId.equals(activity.getCreatorUserId()))
        {
            return true;
        }
        if ("all".equals(activity.getScopeType()))
        {
            return true;
        }
        if ("users".equals(activity.getScopeType()))
        {
            return inScope(userId, activity.getScopeValue());
        }
        if ("dept".equals(activity.getScopeType()))
        {
            return inDeptScope(activity.getScopeValue(), userId, deptId);
        }
        if ("org".equals(activity.getScopeType()))
        {
            return inOrgScope(activity.getScopeValue(), userId, deptId);
        }
        return false;
    }

    private boolean inDeptScope(String scopeValue, String userId, Long deptId)
    {
        if (StringUtils.isBlank(scopeValue))
        {
            return false;
        }
        Set<Long> selectedDeptIds = parseDeptIds(scopeValue, false);
        if (selectedDeptIds.isEmpty())
        {
            return false;
        }
        Long currentDeptId = deptId != null ? deptId : currentDeptId(userId);
        if (currentDeptId == null)
        {
            return false;
        }
        return matchDeptOrAncestor(currentDeptId, selectedDeptIds);
    }

    private boolean inOrgScope(String scopeValue, String userId, Long deptId)
    {
        if (StringUtils.isBlank(scopeValue))
        {
            return false;
        }
        Set<String> userIds = parseUserIds(scopeValue, true);
        if (userIds.contains(userId))
        {
            return true;
        }
        Set<Long> deptIds = parseDeptIds(scopeValue, true);
        if (deptIds.isEmpty())
        {
            return false;
        }
        Long currentDeptId = deptId != null ? deptId : currentDeptId(userId);
        return currentDeptId != null && matchDeptOrAncestor(currentDeptId, deptIds);
    }

    private Long currentDeptId(String userId)
    {
        if (StringUtils.isBlank(userId))
        {
            return null;
        }
        SysUser user = sysUserMapper.selectUserById(userId);
        return user == null ? null : user.getDeptId();
    }

    private boolean matchDeptOrAncestor(Long currentDeptId, Set<Long> selectedDeptIds)
    {
        if (currentDeptId == null || selectedDeptIds == null || selectedDeptIds.isEmpty())
        {
            return false;
        }
        if (selectedDeptIds.contains(currentDeptId))
        {
            return true;
        }
        SysDept dept = sysDeptMapper.selectDeptById(currentDeptId);
        if (dept == null || StringUtils.isBlank(dept.getAncestors()))
        {
            return false;
        }
        String[] ancestors = dept.getAncestors().split(",");
        for (String ancestor : ancestors)
        {
            if (StringUtils.isBlank(ancestor))
            {
                continue;
            }
            try
            {
                if (selectedDeptIds.contains(Long.valueOf(ancestor.trim())))
                {
                    return true;
                }
            }
            catch (Exception ignored)
            {
            }
        }
        return false;
    }

    private boolean inScope(String key, String csv)
    {
        if (StringUtils.isBlank(csv) || StringUtils.isBlank(key))
        {
            return false;
        }
        String[] arr = csv.split(",");
        for (String item : arr)
        {
            if (key.equals(item == null ? null : item.trim()))
            {
                return true;
            }
        }
        return false;
    }

    private BbsCollectActivity mustActivity(Long activityId)
    {
        BbsCollectActivity activity = mapper.selectCollectActivityById(activityId);
        if (activity == null)
        {
            throw new ServiceException("活动不存在");
        }
        return activity;
    }

    private BbsCollectDemandTicket mustTicket(Long ticketId)
    {
        BbsCollectDemandTicket ticket = mapper.selectDemandTicketById(ticketId);
        if (ticket == null)
        {
            throw new ServiceException("工单不存在");
        }
        return ticket;
    }

    private void requireOwnerOrAdmin(BbsCollectActivity activity, String userId, boolean admin)
    {
        if (!admin && !userId.equals(activity.getCreatorUserId()))
        {
            throw new ServiceException("仅创建人或管理员可操作");
        }
    }

    private void requireVoteCreator(BbsCollectActivity activity, String userId)
    {
        if (!StringUtils.equals(userId, activity.getCreatorUserId()))
        {
            throw new ServiceException("仅投票创建人可操作");
        }
    }

    private void sendVoteCreatedNotification(BbsCollectActivity activity, String creatorUserId, String creatorNickName)
    {
        Set<String> recipients = resolveScopeUsers(activity);
        recipients.remove(creatorUserId);
        for (String recipient : recipients)
        {
            BbsNotification notification = new BbsNotification();
            notification.setUserId(recipient);
            notification.setType("5");
            notification.setTitle("新投票通知");
            notification.setContent("您有一条新的投票待参与：《" + activity.getTitle() + "》");
            notification.setTargetType("5");
            notification.setTargetId(activity.getActivityId());
            notification.setFromUserId(creatorUserId);
            notification.setFromNickName(StringUtils.isBlank(creatorNickName) ? "系统" : creatorNickName);
            notification.setFromAvatar("");
            notification.setIsRead("0");
            bbsNotificationMapper.insertBbsNotification(notification);
        }
    }

    private Set<String> resolveScopeUsers(BbsCollectActivity activity)
    {
        Set<String> users = new HashSet<>();
        if (activity == null)
        {
            return users;
        }
        if ("all".equals(activity.getScopeType()))
        {
            SysUser query = new SysUser();
            query.setStatus("0");
            List<SysUser> userList = sysUserMapper.selectUserList(query);
            for (SysUser user : userList)
            {
                if (StringUtils.isNotBlank(user.getUserId()))
                {
                    users.add(user.getUserId());
                }
            }
            return users;
        }
        if ("users".equals(activity.getScopeType()))
        {
            users.addAll(parseUserIds(activity.getScopeValue(), false));
            return users;
        }

        Set<Long> deptIds = new HashSet<>();
        if ("dept".equals(activity.getScopeType()))
        {
            deptIds.addAll(parseDeptIds(activity.getScopeValue(), false));
        }
        else if ("org".equals(activity.getScopeType()))
        {
            users.addAll(parseUserIds(activity.getScopeValue(), true));
            deptIds.addAll(parseDeptIds(activity.getScopeValue(), true));
        }

        for (Long deptId : deptIds)
        {
            if (deptId == null)
            {
                continue;
            }
            SysUser query = new SysUser();
            query.setDeptId(deptId);
            query.setStatus("0");
            List<SysUser> inDept = sysUserMapper.selectUserList(query);
            for (SysUser user : inDept)
            {
                if (StringUtils.isNotBlank(user.getUserId()))
                {
                    users.add(user.getUserId());
                }
            }
        }
        return users;
    }

    private Set<Long> parseDeptIds(String scopeValue, boolean orgPrefixed)
    {
        Set<Long> deptIds = new HashSet<>();
        if (StringUtils.isBlank(scopeValue))
        {
            return deptIds;
        }
        String[] arr = scopeValue.split(",");
        for (String item : arr)
        {
            if (StringUtils.isBlank(item))
            {
                continue;
            }
            String text = item.trim();
            if (orgPrefixed)
            {
                if (!text.startsWith("D_"))
                {
                    continue;
                }
                text = text.substring(2);
            }
            try
            {
                deptIds.add(Long.valueOf(text));
            }
            catch (Exception ignored)
            {
            }
        }
        return deptIds;
    }

    private Set<String> parseUserIds(String scopeValue, boolean orgPrefixed)
    {
        Set<String> userIds = new HashSet<>();
        if (StringUtils.isBlank(scopeValue))
        {
            return userIds;
        }
        String[] arr = scopeValue.split(",");
        for (String item : arr)
        {
            if (StringUtils.isBlank(item))
            {
                continue;
            }
            String text = item.trim();
            if (orgPrefixed)
            {
                if (!text.startsWith("U_"))
                {
                    continue;
                }
                text = text.substring(2);
            }
            if (StringUtils.isNotBlank(text))
            {
                userIds.add(text);
            }
        }
        return userIds;
    }

    private String str(Object value, String defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        String text = String.valueOf(value);
        return StringUtils.isBlank(text) ? defaultValue : text;
    }

    private String nowString()
    {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
