package com.ruoyi.common.enums;

/**
 * 管理员操作类型枚举
 * 
 * @author simonyang
 * @date 2026-04-10
 */
public enum AdminOperationType
{
    /** 其他 */
    OTHER("OTHER", "其他"),

    /** 新增 */
    ADD("ADD", "新增"),

    /** 修改 */
    UPDATE("UPDATE", "修改"),

    /** 删除 */
    DELETE("DELETE", "删除"),

    /** 查询 */
    QUERY("QUERY", "查询"),

    /** 导出 */
    EXPORT("EXPORT", "导出"),

    /** 导入 */
    IMPORT("IMPORT", "导入"),

    /** 审核通过 */
    APPROVE("APPROVE", "审核通过"),

    /** 审核驳回 */
    REJECT("REJECT", "审核驳回"),

    /** 下架 */
    OFFLINE("OFFLINE", "下架"),

    /** 上架 */
    ONLINE("ONLINE", "上架"),

    /** 置顶 */
    TOP("TOP", "置顶"),

    /** 登录 */
    LOGIN("LOGIN", "登录"),

    /** 登出 */
    LOGOUT("LOGOUT", "登出");

    private final String code;
    private final String description;

    AdminOperationType(String code, String description)
    {
        this.code = code;
        this.description = description;
    }

    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static AdminOperationType getByCode(String code)
    {
        for (AdminOperationType type : values())
        {
            if (type.getCode().equals(code))
            {
                return type;
            }
        }
        return OTHER;
    }
}
