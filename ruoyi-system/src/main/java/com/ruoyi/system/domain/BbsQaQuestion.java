package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;
import java.util.List;

public class BbsQaQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long questionId;
    private String title;
    private String content;
    /** 分类：tech/product/hr/admin/other */
    private String category;
    private Integer rewardPoints;
    private Integer frozenPoints;
    /** 状态：open/solved/archived/cancelled */
    private String status;
    private Long bestAnswerId;
    private String askUserId;
    private String askNickName;
    private String askAvatar;
    private String askDeptName;
    /** 可见范围：all/dept/specific */
    private String visibilityScope;
    private String visibilityExtra;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiryTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;
    private Integer viewCount;
    private Integer answerCount;
    /** 审核状态：pending/approved/rejected */
    private String auditStatus;
    private String auditReason;
    private String tags;
    private String delFlag;

    /** 非持久化字段 */
    private List<BbsQaAnswer> answers;
    /** 当前用户是否已点赞最佳答案（前端展示用） */
    private Boolean isCurrentUserAsker;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }

    public Integer getFrozenPoints() { return frozenPoints; }
    public void setFrozenPoints(Integer frozenPoints) { this.frozenPoints = frozenPoints; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getBestAnswerId() { return bestAnswerId; }
    public void setBestAnswerId(Long bestAnswerId) { this.bestAnswerId = bestAnswerId; }

    public String getAskUserId() { return askUserId; }
    public void setAskUserId(String askUserId) { this.askUserId = askUserId; }

    public String getAskNickName() { return askNickName; }
    public void setAskNickName(String askNickName) { this.askNickName = askNickName; }

    public String getAskAvatar() { return askAvatar; }
    public void setAskAvatar(String askAvatar) { this.askAvatar = askAvatar; }

    public String getAskDeptName() { return askDeptName; }
    public void setAskDeptName(String askDeptName) { this.askDeptName = askDeptName; }

    public String getVisibilityScope() { return visibilityScope; }
    public void setVisibilityScope(String visibilityScope) { this.visibilityScope = visibilityScope; }

    public String getVisibilityExtra() { return visibilityExtra; }
    public void setVisibilityExtra(String visibilityExtra) { this.visibilityExtra = visibilityExtra; }

    public Date getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Date expiryTime) { this.expiryTime = expiryTime; }

    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }

    public Date getArchiveTime() { return archiveTime; }
    public void setArchiveTime(Date archiveTime) { this.archiveTime = archiveTime; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getAnswerCount() { return answerCount; }
    public void setAnswerCount(Integer answerCount) { this.answerCount = answerCount; }

    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public List<BbsQaAnswer> getAnswers() { return answers; }
    public void setAnswers(List<BbsQaAnswer> answers) { this.answers = answers; }

    public Boolean getIsCurrentUserAsker() { return isCurrentUserAsker; }
    public void setIsCurrentUserAsker(Boolean isCurrentUserAsker) { this.isCurrentUserAsker = isCurrentUserAsker; }
}
