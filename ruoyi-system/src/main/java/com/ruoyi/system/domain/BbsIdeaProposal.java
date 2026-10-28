package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

public class BbsIdeaProposal extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long ideaId;
    private String title;
    private String category;
    private String content;
    private String backgroundText;
    private String planText;
    private String expectedValue;
    private String visibilityScope;
    private String attachmentUrls;
    private String relatedCase;
    private String teamName;
    private String isDraft;
    private String reviewStage;
    private String rejectReason;
    private java.util.Date projectTime;
    private java.util.Date landingTime;
    private String isPublicized;
    private Integer incentivePoints;
    private String honorBadge;
    private Integer likeCount;
    private Integer shareCount;
    private java.util.Date reviewDeadline;
    private String delFlag;
    private String bizValue;
    private String status;
    private BigDecimal avgScore;
    private String resultSummary;
    private String submitUserId;
    private String submitNickName;

    private List<BbsIdeaReview> reviews;
    private List<BbsIdeaProgress> progressList;

    public Long getIdeaId() { return ideaId; }
    public void setIdeaId(Long ideaId) { this.ideaId = ideaId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getBackgroundText() { return backgroundText; }
    public void setBackgroundText(String backgroundText) { this.backgroundText = backgroundText; }

    public String getPlanText() { return planText; }
    public void setPlanText(String planText) { this.planText = planText; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public String getVisibilityScope() { return visibilityScope; }
    public void setVisibilityScope(String visibilityScope) { this.visibilityScope = visibilityScope; }

    public String getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(String attachmentUrls) { this.attachmentUrls = attachmentUrls; }

    public String getRelatedCase() { return relatedCase; }
    public void setRelatedCase(String relatedCase) { this.relatedCase = relatedCase; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getIsDraft() { return isDraft; }
    public void setIsDraft(String isDraft) { this.isDraft = isDraft; }

    public String getReviewStage() { return reviewStage; }
    public void setReviewStage(String reviewStage) { this.reviewStage = reviewStage; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public java.util.Date getProjectTime() { return projectTime; }
    public void setProjectTime(java.util.Date projectTime) { this.projectTime = projectTime; }

    public java.util.Date getLandingTime() { return landingTime; }
    public void setLandingTime(java.util.Date landingTime) { this.landingTime = landingTime; }

    public String getIsPublicized() { return isPublicized; }
    public void setIsPublicized(String isPublicized) { this.isPublicized = isPublicized; }

    public Integer getIncentivePoints() { return incentivePoints; }
    public void setIncentivePoints(Integer incentivePoints) { this.incentivePoints = incentivePoints; }

    public String getHonorBadge() { return honorBadge; }
    public void setHonorBadge(String honorBadge) { this.honorBadge = honorBadge; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getShareCount() { return shareCount; }
    public void setShareCount(Integer shareCount) { this.shareCount = shareCount; }

    public java.util.Date getReviewDeadline() { return reviewDeadline; }
    public void setReviewDeadline(java.util.Date reviewDeadline) { this.reviewDeadline = reviewDeadline; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getBizValue() { return bizValue; }
    public void setBizValue(String bizValue) { this.bizValue = bizValue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAvgScore() { return avgScore; }
    public void setAvgScore(BigDecimal avgScore) { this.avgScore = avgScore; }

    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }

    public String getSubmitUserId() { return submitUserId; }
    public void setSubmitUserId(String submitUserId) { this.submitUserId = submitUserId; }

    public String getSubmitNickName() { return submitNickName; }
    public void setSubmitNickName(String submitNickName) { this.submitNickName = submitNickName; }

    public List<BbsIdeaReview> getReviews() { return reviews; }
    public void setReviews(List<BbsIdeaReview> reviews) { this.reviews = reviews; }

    public List<BbsIdeaProgress> getProgressList() { return progressList; }
    public void setProgressList(List<BbsIdeaProgress> progressList) { this.progressList = progressList; }
}
