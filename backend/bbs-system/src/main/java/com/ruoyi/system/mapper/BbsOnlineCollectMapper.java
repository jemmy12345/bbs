package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BbsCollectActivity;
import com.ruoyi.system.domain.BbsCollectDemandTicket;
import com.ruoyi.system.domain.BbsCollectSurveyQuestion;
import com.ruoyi.system.domain.BbsCollectSurveyResponse;
import com.ruoyi.system.domain.BbsCollectTopicContent;
import com.ruoyi.system.domain.BbsDemandForm;
import com.ruoyi.system.domain.BbsTopicActivity;
import com.ruoyi.system.domain.BbsVote;
import com.ruoyi.system.domain.BbsVoteOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BbsOnlineCollectMapper
{
    int insertCollectActivity(BbsCollectActivity activity);

    int updateCollectActivity(BbsCollectActivity activity);

    BbsCollectActivity selectCollectActivityById(@Param("activityId") Long activityId);

    List<BbsCollectActivity> selectCollectActivityList(@Param("activityType") String activityType,
                                                       @Param("status") String status,
                                                       @Param("keyword") String keyword,
                                                       @Param("currentUserId") String currentUserId,
                                                       @Param("adminView") Integer adminView);

    int deleteCollectActivity(@Param("activityId") Long activityId);

    int increaseParticipantCount(@Param("activityId") Long activityId);

    int hasParticipation(@Param("activityId") Long activityId, @Param("userId") String userId,
                         @Param("status") String status);

    int upsertParticipation(@Param("activityId") Long activityId,
                            @Param("activityType") String activityType,
                            @Param("userId") String userId,
                            @Param("userNickName") String userNickName,
                            @Param("submitStatus") String submitStatus,
                            @Param("submitTime") String submitTime);

    int insertVote(BbsVote vote);

    int insertVoteOption(BbsVoteOption option);

    BbsVote selectVoteById(@Param("voteId") Long voteId);

    List<BbsVoteOption> selectVoteOptions(@Param("voteId") Long voteId);

    int insertVoteRecord(@Param("voteId") Long voteId, @Param("optionId") Long optionId, @Param("userId") String userId);

    int increaseVoteOptionCount(@Param("optionId") Long optionId);

    int hasVotedInVote(@Param("voteId") Long voteId, @Param("userId") String userId);

    Long selectVoteParticipantCount(@Param("voteId") Long voteId);

    int insertDemandForm(BbsDemandForm form);

    BbsDemandForm selectDemandFormById(@Param("formId") Long formId);

    int insertDemandTicket(BbsCollectDemandTicket ticket);

    List<BbsCollectDemandTicket> selectDemandTicketList(@Param("activityId") Long activityId,
                                                        @Param("status") String status,
                                                        @Param("keyword") String keyword);

    BbsCollectDemandTicket selectDemandTicketById(@Param("ticketId") Long ticketId);

    int updateDemandTicketAssign(@Param("ticketId") Long ticketId,
                                 @Param("assigneeUserId") String assigneeUserId,
                                 @Param("assigneeNickName") String assigneeNickName);

    int updateDemandTicketStatus(@Param("ticketId") Long ticketId,
                                 @Param("status") String status,
                                 @Param("comment") String comment);

    int insertTopicActivity(BbsTopicActivity activity);

    BbsTopicActivity selectTopicActivityById(@Param("activityId") Long activityId);

    int joinTopicActivity(@Param("activityId") Long activityId, @Param("userId") String userId,
                          @Param("userNickName") String userNickName, @Param("remark") String remark);

    int increaseTopicJoinCount(@Param("activityId") Long activityId);

    int archiveTopicActivity(@Param("activityId") Long activityId, @Param("summaryText") String summaryText);

    int insertTopicContent(BbsCollectTopicContent content);

    List<BbsCollectTopicContent> selectTopicContentList(@Param("activityId") Long activityId);

    List<Map<String, Object>> selectTopicRank(@Param("activityId") Long activityId);

    int insertSurveyQuestion(BbsCollectSurveyQuestion question);

    List<BbsCollectSurveyQuestion> selectSurveyQuestions(@Param("activityId") Long activityId);

    int upsertSurveyResponse(BbsCollectSurveyResponse response);

    BbsCollectSurveyResponse selectSurveyResponseByUser(@Param("activityId") Long activityId, @Param("userId") String userId);

    Long countSurveySubmitted(@Param("activityId") Long activityId);

    List<BbsCollectSurveyResponse> selectSurveySubmittedList(@Param("activityId") Long activityId);
}
