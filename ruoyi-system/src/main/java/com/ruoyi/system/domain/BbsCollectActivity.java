package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsCollectActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long activityId;
    private String title;
    private String activityType;
    private String status;
    private String creatorUserId;
    private String creatorNickName;
    private String scopeType;
    private String scopeValue;
    private String startTime;
    private String endTime;
    private String anonymousFlag;
    private String realtimeFlag;
    private String limitOnceFlag;
    private Long bizId;
    private Integer participantCount;
    private String bannerUrl;
    private String ruleText;
    private String remark;
    private String hasParticipated;

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(String creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorNickName() { return creatorNickName; }
    public void setCreatorNickName(String creatorNickName) { this.creatorNickName = creatorNickName; }

    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }

    public String getScopeValue() { return scopeValue; }
    public void setScopeValue(String scopeValue) { this.scopeValue = scopeValue; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getAnonymousFlag() { return anonymousFlag; }
    public void setAnonymousFlag(String anonymousFlag) { this.anonymousFlag = anonymousFlag; }

    public String getRealtimeFlag() { return realtimeFlag; }
    public void setRealtimeFlag(String realtimeFlag) { this.realtimeFlag = realtimeFlag; }

    public String getLimitOnceFlag() { return limitOnceFlag; }
    public void setLimitOnceFlag(String limitOnceFlag) { this.limitOnceFlag = limitOnceFlag; }

    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }

    public Integer getParticipantCount() { return participantCount; }
    public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getHasParticipated() { return hasParticipated; }
    public void setHasParticipated(String hasParticipated) { this.hasParticipated = hasParticipated; }
}
