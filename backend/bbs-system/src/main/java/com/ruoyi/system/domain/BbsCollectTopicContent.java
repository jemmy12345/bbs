package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsCollectTopicContent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long activityId;
    private String contentType;
    private Long contentId;
    private String contentTitle;
    private String authorUserId;
    private String authorNickName;
    private Integer score;
    private Integer likeCount;
    private Integer commentCount;
    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }

    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }

    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }

    public String getAuthorNickName() { return authorNickName; }
    public void setAuthorNickName(String authorNickName) { this.authorNickName = authorNickName; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
