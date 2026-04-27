package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Forum point record entity.
 */
public class BbsPointRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private String userId;

    private String actionType;

    private String bizType;

    private Long bizId;

    private Integer pointChange;

    private String remark;

    private String delFlag;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getActionType()
    {
        return actionType;
    }

    public void setActionType(String actionType)
    {
        this.actionType = actionType;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public Integer getPointChange()
    {
        return pointChange;
    }

    public void setPointChange(Integer pointChange)
    {
        this.pointChange = pointChange;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
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
