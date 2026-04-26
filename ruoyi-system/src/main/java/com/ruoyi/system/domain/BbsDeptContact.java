package com.ruoyi.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 部门接口人表 bbs_dept_contact
 * 
 * @author ruoyi
 */
public class BbsDeptContact extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接口人ID */
    private Long contactId;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 部门接口人名称 */
    private String contactName;

    /** 部门接口人ID（企业微信用户ID） */
    private String contactUserId;

    /** 状态（0正常 1停用） */
    private String status;

    public Long getContactId()
    {
        return contactId;
    }

    public void setContactId(Long contactId)
    {
        this.contactId = contactId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    @NotBlank(message = "部门名称不能为空")
    @Size(min = 0, max = 100, message = "部门名称不能超过100个字符")
    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    @NotBlank(message = "部门接口人名称不能为空")
    @Size(min = 0, max = 50, message = "部门接口人名称不能超过50个字符")
    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    @NotBlank(message = "部门接口人ID不能为空")
    @Size(min = 0, max = 64, message = "部门接口人ID不能超过64个字符")
    public String getContactUserId()
    {
        return contactUserId;
    }

    public void setContactUserId(String contactUserId)
    {
        this.contactUserId = contactUserId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("contactId", getContactId())
            .append("deptId", getDeptId())
            .append("deptName", getDeptName())
            .append("contactName", getContactName())
            .append("contactUserId", getContactUserId())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
