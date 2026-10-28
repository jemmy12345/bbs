package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsIdeaReview extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long reviewId;
    private Long ideaId;
    private String stage;
    private String action;
    private String reviewerUserId;
    private String reviewerNickName;
    private Integer score;
    private String comment;
    private String rejectReason;
    private String isVisibleToAll;
    private java.util.Date reviewTime;

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getIdeaId() { return ideaId; }
    public void setIdeaId(Long ideaId) { this.ideaId = ideaId; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(String reviewerUserId) { this.reviewerUserId = reviewerUserId; }

    public String getReviewerNickName() { return reviewerNickName; }
    public void setReviewerNickName(String reviewerNickName) { this.reviewerNickName = reviewerNickName; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getIsVisibleToAll() { return isVisibleToAll; }
    public void setIsVisibleToAll(String isVisibleToAll) { this.isVisibleToAll = isVisibleToAll; }

    public java.util.Date getReviewTime() { return reviewTime; }
    public void setReviewTime(java.util.Date reviewTime) { this.reviewTime = reviewTime; }
}
