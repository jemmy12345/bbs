package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 管理员操作日志对象 bbs_admin_log
 * 
 * @author simonyang
 * @date 2026-04-10
 */
public class BbsAdminLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long logId;

    /** 管理员ID */
    private String adminId;

    /** 管理员名称 */
    private String adminName;

    /** 操作类型：ADD/UPDATE/DELETE/QUERY/EXPORT/LOGIN/LOGOUT等 */
    private String operationType;

    /** 操作模块：用户管理/订单管理/商品管理等 */
    private String operationModule;

    /** 操作描述 */
    private String operationDesc;

    /** 请求URL */
    private String requestUrl;

    /** 请求方法：GET/POST/PUT/DELETE */
    private String requestMethod;

    /** 请求参数（JSON格式） */
    private String requestParams;

    /** 响应结果 */
    private String responseResult;

    /** 操作IP地址 */
    private String ipAddress;

    /** 浏览器User-Agent */
    private String userAgent;

    /** 执行耗时（毫秒） */
    private Long executionTime;

    /** 操作状态：1成功 0失败 */
    private Long status;

    /** 错误信息 */
    private String errorMsg;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    /** 关键词（非数据库字段，仅用于查询条件） */
    private transient String keyword;

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }
    public void setAdminId(String adminId)
    {
        this.adminId = adminId;
    }

    public String getAdminId()
    {
        return adminId;
    }
    public void setAdminName(String adminName) 
    {
        this.adminName = adminName;
    }

    public String getAdminName() 
    {
        return adminName;
    }
    public void setOperationType(String operationType) 
    {
        this.operationType = operationType;
    }

    public String getOperationType() 
    {
        return operationType;
    }
    public void setOperationModule(String operationModule) 
    {
        this.operationModule = operationModule;
    }

    public String getOperationModule() 
    {
        return operationModule;
    }
    public void setOperationDesc(String operationDesc) 
    {
        this.operationDesc = operationDesc;
    }

    public String getOperationDesc() 
    {
        return operationDesc;
    }
    public void setRequestUrl(String requestUrl) 
    {
        this.requestUrl = requestUrl;
    }

    public String getRequestUrl() 
    {
        return requestUrl;
    }
    public void setRequestMethod(String requestMethod) 
    {
        this.requestMethod = requestMethod;
    }

    public String getRequestMethod() 
    {
        return requestMethod;
    }
    public void setRequestParams(String requestParams) 
    {
        this.requestParams = requestParams;
    }

    public String getRequestParams() 
    {
        return requestParams;
    }
    public void setResponseResult(String responseResult) 
    {
        this.responseResult = responseResult;
    }

    public String getResponseResult() 
    {
        return responseResult;
    }
    public void setIpAddress(String ipAddress) 
    {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() 
    {
        return ipAddress;
    }
    public void setUserAgent(String userAgent) 
    {
        this.userAgent = userAgent;
    }

    public String getUserAgent() 
    {
        return userAgent;
    }
    public void setExecutionTime(Long executionTime) 
    {
        this.executionTime = executionTime;
    }

    public Long getExecutionTime() 
    {
        return executionTime;
    }
    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }
    public void setErrorMsg(String errorMsg) 
    {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() 
    {
        return errorMsg;
    }
    public void setCreatedTime(Date createdTime) 
    {
        this.createdTime = createdTime;
    }

    public Date getCreatedTime() 
    {
        return createdTime;
    }

    public void setKeyword(String keyword) 
    {
        this.keyword = keyword;
    }

    public String getKeyword() 
    {
        return keyword;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("adminId", getAdminId())
            .append("adminName", getAdminName())
            .append("operationType", getOperationType())
            .append("operationModule", getOperationModule())
            .append("operationDesc", getOperationDesc())
            .append("requestUrl", getRequestUrl())
            .append("requestMethod", getRequestMethod())
            .append("requestParams", getRequestParams())
            .append("responseResult", getResponseResult())
            .append("ipAddress", getIpAddress())
            .append("userAgent", getUserAgent())
            .append("executionTime", getExecutionTime())
            .append("status", getStatus())
            .append("errorMsg", getErrorMsg())
            .append("createdTime", getCreatedTime())
            .toString();
    }
}
