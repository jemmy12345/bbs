package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsCollectActivity;
import com.ruoyi.system.domain.BbsCollectDemandTicket;
import com.ruoyi.system.domain.BbsCollectTopicContent;

import java.util.List;
import java.util.Map;

public interface IBbsOnlineCollectService
{
    Long createVoteActivity(BbsCollectActivity activity, String voteType, String description, List<String> options,
                            String userId, String nickName);

    Long createSurveyActivity(BbsCollectActivity activity, List<Map<String, Object>> questions, String userId, String nickName);

    Long createDemandActivity(BbsCollectActivity activity, String formSchema, String description,
                              String userId, String nickName);

    Long createTopicActivity(BbsCollectActivity activity, String content, String userId, String nickName);

    List<BbsCollectActivity> listActivities(String activityType, String status, String keyword,
                                            String currentUserId, boolean adminView);

    BbsCollectActivity getActivityDetail(Long activityId, String currentUserId, boolean adminView);

    Map<String, Object> getVoteParticipateMeta(Long activityId, String currentUserId, boolean adminView);

    void endActivity(Long activityId, String operatorUserId, boolean admin);

    void deleteActivity(Long activityId, String operatorUserId, boolean admin);

    void submitVote(Long activityId, List<Long> optionIds, String userId, String nickName, Long deptId);

    void saveSurveyDraft(Long activityId, String answersJson, String userId, String nickName, Long deptId);

    void submitSurvey(Long activityId, String answersJson, String userId, String nickName, Long deptId);

    Long submitDemand(Long activityId, BbsCollectDemandTicket ticket, String userId, String nickName, Long deptId);

    List<BbsCollectDemandTicket> listDemandTickets(Long activityId, String status, String keyword,
                                                   String currentUserId, boolean adminView);

    void assignDemandTicket(Long ticketId, String assigneeUserId, String assigneeNickName,
                            String operatorUserId, boolean admin);

    void updateDemandTicketStatus(Long ticketId, String status, String comment,
                                  String operatorUserId, boolean admin);

    void joinTopic(Long activityId, String userId, String nickName, String remark, Long deptId);

    void addTopicContent(Long activityId, BbsCollectTopicContent content, String userId, String nickName, Long deptId);

    Map<String, Object> topicDetail(Long activityId, String currentUserId, boolean adminView);

    Map<String, Object> getActivityStats(Long activityId, String currentUserId, boolean adminView);

    void archiveTopic(Long activityId, String summaryText, String operatorUserId, boolean admin);
}
