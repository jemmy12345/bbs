package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsCollectDemandTicket extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long ticketId;
    private Long activityId;
    private Long formId;
    private String submitUserId;
    private String submitNickName;
    private String demandName;
    private String demandType;
    private String demandDetail;
    private String priority;
    private String attachmentUrls;
    private String contactPerson;
    private String customFieldJson;
    private String status;
    private String assigneeUserId;
    private String assigneeNickName;
    private String processComment;

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public String getSubmitUserId() { return submitUserId; }
    public void setSubmitUserId(String submitUserId) { this.submitUserId = submitUserId; }

    public String getSubmitNickName() { return submitNickName; }
    public void setSubmitNickName(String submitNickName) { this.submitNickName = submitNickName; }

    public String getDemandName() { return demandName; }
    public void setDemandName(String demandName) { this.demandName = demandName; }

    public String getDemandType() { return demandType; }
    public void setDemandType(String demandType) { this.demandType = demandType; }

    public String getDemandDetail() { return demandDetail; }
    public void setDemandDetail(String demandDetail) { this.demandDetail = demandDetail; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(String attachmentUrls) { this.attachmentUrls = attachmentUrls; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getCustomFieldJson() { return customFieldJson; }
    public void setCustomFieldJson(String customFieldJson) { this.customFieldJson = customFieldJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(String assigneeUserId) { this.assigneeUserId = assigneeUserId; }

    public String getAssigneeNickName() { return assigneeNickName; }
    public void setAssigneeNickName(String assigneeNickName) { this.assigneeNickName = assigneeNickName; }

    public String getProcessComment() { return processComment; }
    public void setProcessComment(String processComment) { this.processComment = processComment; }
}
