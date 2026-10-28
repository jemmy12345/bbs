package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsDemandSubmission extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long submissionId;
    private Long formId;
    private String userId;
    private String userNickName;
    private String submissionContent;

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserNickName() { return userNickName; }
    public void setUserNickName(String userNickName) { this.userNickName = userNickName; }

    public String getSubmissionContent() { return submissionContent; }
    public void setSubmissionContent(String submissionContent) { this.submissionContent = submissionContent; }
}
