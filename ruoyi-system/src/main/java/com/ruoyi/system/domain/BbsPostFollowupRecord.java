package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 帖子闭环处理记录 bbs_post_followup_record
 */
public class BbsPostFollowupRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long postId;
    private String followupStatus;
    private String processNote;
    private String ownerUserId;
    private String ownerUserName;
    private String handledBy;
    private String handledByName;
    private java.util.Date handleTime;
    private String delFlag;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getPostId()
    {
        return postId;
    }

    public void setPostId(Long postId)
    {
        this.postId = postId;
    }

    public String getFollowupStatus()
    {
        return followupStatus;
    }

    public void setFollowupStatus(String followupStatus)
    {
        this.followupStatus = followupStatus;
    }

    public String getProcessNote()
    {
        return processNote;
    }

    public void setProcessNote(String processNote)
    {
        this.processNote = processNote;
    }

    public String getOwnerUserId()
    {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId)
    {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerUserName()
    {
        return ownerUserName;
    }

    public void setOwnerUserName(String ownerUserName)
    {
        this.ownerUserName = ownerUserName;
    }

    public String getHandledBy()
    {
        return handledBy;
    }

    public void setHandledBy(String handledBy)
    {
        this.handledBy = handledBy;
    }

    public String getHandledByName()
    {
        return handledByName;
    }

    public void setHandledByName(String handledByName)
    {
        this.handledByName = handledByName;
    }

    public java.util.Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(java.util.Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
