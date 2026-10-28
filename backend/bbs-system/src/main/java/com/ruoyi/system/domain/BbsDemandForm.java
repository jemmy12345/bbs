package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsDemandForm extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long formId;
    private String title;
    private String description;
    private String formSchema;
    private String status;
    private String creatorUserId;
    private String creatorNickName;

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFormSchema() { return formSchema; }
    public void setFormSchema(String formSchema) { this.formSchema = formSchema; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(String creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorNickName() { return creatorNickName; }
    public void setCreatorNickName(String creatorNickName) { this.creatorNickName = creatorNickName; }
}
