package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsDemandForm;
import com.ruoyi.system.domain.BbsTopicActivity;
import com.ruoyi.system.domain.BbsVote;

import java.util.List;

public interface IBbsEngagementService
{
    Long createVote(BbsVote vote);

    List<BbsVote> listVotes(String status);

    BbsVote getVoteDetail(Long voteId);

    void submitVote(Long voteId, List<Long> optionIds, String userId);

    Long createDemandForm(BbsDemandForm form);

    List<BbsDemandForm> listDemandForms(String status);

    void submitDemand(Long formId, String userId, String nickName, String submissionContent);

    Long createTopicActivity(BbsTopicActivity activity);

    List<BbsTopicActivity> listTopicActivities(String status);

    void joinTopicActivity(Long activityId, String userId, String nickName, String remark);
}
