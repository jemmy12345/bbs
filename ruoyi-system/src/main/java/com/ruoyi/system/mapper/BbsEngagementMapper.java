package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BbsDemandForm;
import com.ruoyi.system.domain.BbsDemandSubmission;
import com.ruoyi.system.domain.BbsTopicActivity;
import com.ruoyi.system.domain.BbsVote;
import com.ruoyi.system.domain.BbsVoteOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BbsEngagementMapper
{
    int insertVote(BbsVote vote);

    int insertVoteOption(BbsVoteOption option);

    List<BbsVote> selectVoteList(@Param("status") String status);

    BbsVote selectVoteById(@Param("voteId") Long voteId);

    List<BbsVoteOption> selectVoteOptions(@Param("voteId") Long voteId);

    int hasVoted(@Param("voteId") Long voteId, @Param("userId") String userId);

    int insertVoteRecord(@Param("voteId") Long voteId, @Param("optionId") Long optionId, @Param("userId") String userId);

    int increaseVoteOptionCount(@Param("optionId") Long optionId);

    int insertDemandForm(BbsDemandForm form);

    List<BbsDemandForm> selectDemandFormList(@Param("status") String status);

    BbsDemandForm selectDemandFormById(@Param("formId") Long formId);

    int insertDemandSubmission(BbsDemandSubmission submission);

    int insertTopicActivity(BbsTopicActivity activity);

    List<BbsTopicActivity> selectTopicActivityList(@Param("status") String status);

    int joinTopicActivity(@Param("activityId") Long activityId, @Param("userId") String userId,
                          @Param("userNickName") String userNickName, @Param("remark") String remark);

    int increaseTopicJoinCount(@Param("activityId") Long activityId);
}
