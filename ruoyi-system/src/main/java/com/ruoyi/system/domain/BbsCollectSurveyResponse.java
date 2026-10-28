package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsCollectSurveyResponse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long responseId;
    private Long activityId;
    private String userId;
    private String userNickName;
    private String answersJson;
    private String submitStatus;
    private String submitTime;

    public Long getResponseId() { return responseId; }
    public void setResponseId(Long responseId) { this.responseId = responseId; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserNickName() { return userNickName; }
    public void setUserNickName(String userNickName) { this.userNickName = userNickName; }

    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }

    public String getSubmitStatus() { return submitStatus; }
    public void setSubmitStatus(String submitStatus) { this.submitStatus = submitStatus; }

    public String getSubmitTime() { return submitTime; }
    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }
}
