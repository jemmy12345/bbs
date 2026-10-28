package com.ruoyi.common.utils;

/**
 * 管理员操作日志上下文工具类
 * 
 * 基于 ThreadLocal 实现，允许在 Controller 业务逻辑中动态设置操作详情描述。
 * AdminLogAspect 切面会在方法执行后自动读取此上下文中的描述信息，
 * 优先使用动态设置的描述，如果未设置则回退到注解上的静态描述。
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @AdminLog(module = "帖子管理", operationType = AdminOperationType.DELETE)
 * public AjaxResult remove(@PathVariable Long[] postIds) {
 *     BbsPost post = bbsPostService.selectBbsPostById(postIds[0]);
 *     AdminLogContext.setDescription("删除帖子：《" + post.getTitle() + "》");
 *     // ... 业务逻辑 ...
 * }
 * }
 * </pre>
 * 
 * @author simonyang
 * @date 2026-04-10
 */
public class AdminLogContext
{
    /** 操作描述 ThreadLocal */
    private static final ThreadLocal<String> DESCRIPTION = new ThreadLocal<>();

    /**
     * 设置操作描述（会覆盖注解上的静态描述）
     * 
     * @param description 动态操作描述
     */
    public static void setDescription(String description)
    {
        DESCRIPTION.set(description);
    }

    /**
     * 获取操作描述
     * 
     * @return 操作描述，如果未设置则返回null
     */
    public static String getDescription()
    {
        return DESCRIPTION.get();
    }

    /**
     * 清除上下文（由切面在 finally 中自动调用，防止内存泄漏）
     */
    public static void clear()
    {
        DESCRIPTION.remove();
    }
}
