package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.List;

public class BbsVote extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long voteId;
    private String title;
    private String description;
    private String voteType;
    private String startTime;
    private String endTime;
    private String status;
    private String creatorUserId;
    private String creatorNickName;

    private List<BbsVoteOption> options;

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVoteType() { return voteType; }
    public void setVoteType(String voteType) { this.voteType = voteType; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(String creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorNickName() { return creatorNickName; }
    public void setCreatorNickName(String creatorNickName) { this.creatorNickName = creatorNickName; }

    public List<BbsVoteOption> getOptions() { return options; }
    public void setOptions(List<BbsVoteOption> options) { this.options = options; }
}
