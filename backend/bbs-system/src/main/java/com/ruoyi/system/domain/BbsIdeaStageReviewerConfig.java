package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class BbsIdeaStageReviewerConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;

    @Excel(name = "环节编码")
    private String stageCode;

    @Excel(name = "环节名称")
    private String stageName;

    @Excel(name = "角色标识")
    private String roleKeys;

    /** 指定处理人用户ID，逗号分隔，与roleKeys二选一或叠加使用 */
    @Excel(name = "处理人用户ID")
    private String reviewerUserIds;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public String getStageCode()
    {
        return stageCode;
    }

    public void setStageCode(String stageCode)
    {
        this.stageCode = stageCode;
    }

    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }

    public String getRoleKeys()
    {
        return roleKeys;
    }

    public void setRoleKeys(String roleKeys)
    {
        this.roleKeys = roleKeys;
    }

    public String getReviewerUserIds()
    {
        return reviewerUserIds;
    }

    public void setReviewerUserIds(String reviewerUserIds)
    {
        this.reviewerUserIds = reviewerUserIds;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}