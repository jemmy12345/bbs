package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsTopicActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long activityId;
    private String title;
    private String content;
    private String startTime;
    private String endTime;
    private String status;
    private Integer joinCount;
    private String creatorUserId;
    private String creatorNickName;

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getJoinCount() { return joinCount; }
    public void setJoinCount(Integer joinCount) { this.joinCount = joinCount; }

    public String getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(String creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorNickName() { return creatorNickName; }
    public void setCreatorNickName(String creatorNickName) { this.creatorNickName = creatorNickName; }
}
