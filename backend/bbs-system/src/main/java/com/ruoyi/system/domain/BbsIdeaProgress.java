package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BbsIdeaProgress extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long progressId;
    private Long ideaId;
    private String statusEnum;
    private String updaterUserId;
    private String updaterNickName;
    private String progressStage;
    private String progressDesc;
    private String attachmentUrls;
    private Integer progressPercent;

    public Long getProgressId() { return progressId; }
    public void setProgressId(Long progressId) { this.progressId = progressId; }

    public Long getIdeaId() { return ideaId; }
    public void setIdeaId(Long ideaId) { this.ideaId = ideaId; }

    public String getStatusEnum() { return statusEnum; }
    public void setStatusEnum(String statusEnum) { this.statusEnum = statusEnum; }

    public String getUpdaterUserId() { return updaterUserId; }
    public void setUpdaterUserId(String updaterUserId) { this.updaterUserId = updaterUserId; }

    public String getUpdaterNickName() { return updaterNickName; }
    public void setUpdaterNickName(String updaterNickName) { this.updaterNickName = updaterNickName; }

    public String getProgressStage() { return progressStage; }
    public void setProgressStage(String progressStage) { this.progressStage = progressStage; }

    public String getProgressDesc() { return progressDesc; }
    public void setProgressDesc(String progressDesc) { this.progressDesc = progressDesc; }

    public String getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(String attachmentUrls) { this.attachmentUrls = attachmentUrls; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
}
