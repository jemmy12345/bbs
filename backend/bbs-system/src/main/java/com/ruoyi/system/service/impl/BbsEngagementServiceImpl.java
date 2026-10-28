package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.BbsDemandForm;
import com.ruoyi.system.domain.BbsDemandSubmission;
import com.ruoyi.system.domain.BbsTopicActivity;
import com.ruoyi.system.domain.BbsVote;
import com.ruoyi.system.domain.BbsVoteOption;
import com.ruoyi.system.mapper.BbsEngagementMapper;
import com.ruoyi.system.service.IBbsEngagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BbsEngagementServiceImpl implements IBbsEngagementService
{
    @Autowired
    private BbsEngagementMapper bbsEngagementMapper;

    @Override
    @Transactional
    public Long createVote(BbsVote vote)
    {
        if (vote.getVoteType() == null || vote.getVoteType().isEmpty())
        {
            vote.setVoteType("single");
        }
        vote.setStatus("open");
        bbsEngagementMapper.insertVote(vote);
        if (vote.getOptions() != null)
        {
            int sort = 1;
            for (BbsVoteOption option : vote.getOptions())
            {
                if (option.getOptionText() == null || option.getOptionText().trim().isEmpty())
                {
                    continue;
                }
                option.setVoteId(vote.getVoteId());
                option.setSortOrder(sort++);
                bbsEngagementMapper.insertVoteOption(option);
            }
        }
        return vote.getVoteId();
    }

    @Override
    public List<BbsVote> listVotes(String status)
    {
        return bbsEngagementMapper.selectVoteList(status);
    }

    @Override
    public BbsVote getVoteDetail(Long voteId)
    {
        BbsVote vote = bbsEngagementMapper.selectVoteById(voteId);
        if (vote != null)
        {
            vote.setOptions(bbsEngagementMapper.selectVoteOptions(voteId));
        }
        return vote;
    }

    @Override
    @Transactional
    public void submitVote(Long voteId, List<Long> optionIds, String userId)
    {
        if (optionIds == null || optionIds.isEmpty())
        {
            throw new ServiceException("No option selected");
        }
        if (bbsEngagementMapper.hasVoted(voteId, userId) > 0)
        {
            throw new ServiceException("You already voted");
        }
        for (Long optionId : optionIds)
        {
            bbsEngagementMapper.insertVoteRecord(voteId, optionId, userId);
            bbsEngagementMapper.increaseVoteOptionCount(optionId);
        }
    }

    @Override
    public Long createDemandForm(BbsDemandForm form)
    {
        form.setStatus("open");
        bbsEngagementMapper.insertDemandForm(form);
        return form.getFormId();
    }

    @Override
    public List<BbsDemandForm> listDemandForms(String status)
    {
        return bbsEngagementMapper.selectDemandFormList(status);
    }

    @Override
    public void submitDemand(Long formId, String userId, String nickName, String submissionContent)
    {
        BbsDemandSubmission submission = new BbsDemandSubmission();
        submission.setFormId(formId);
        submission.setUserId(userId);
        submission.setUserNickName(nickName);
        submission.setSubmissionContent(submissionContent);
        bbsEngagementMapper.insertDemandSubmission(submission);
    }

    @Override
    public Long createTopicActivity(BbsTopicActivity activity)
    {
        activity.setStatus("open");
        bbsEngagementMapper.insertTopicActivity(activity);
        return activity.getActivityId();
    }

    @Override
    public List<BbsTopicActivity> listTopicActivities(String status)
    {
        return bbsEngagementMapper.selectTopicActivityList(status);
    }

    @Override
    @Transactional
    public void joinTopicActivity(Long activityId, String userId, String nickName, String remark)
    {
        int affected = bbsEngagementMapper.joinTopicActivity(activityId, userId, nickName, remark);
        if (affected > 0)
        {
            bbsEngagementMapper.increaseTopicJoinCount(activityId);
        }
    }
}
