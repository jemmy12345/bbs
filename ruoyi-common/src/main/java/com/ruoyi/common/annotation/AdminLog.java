package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.ruoyi.common.enums.AdminOperationType;

/**
 * 自定义管理员操作日志记录注解
 * 
 * 用于标注管理员接口方法，通过AOP切面自动记录操作日志到 bbs_admin_log 表
 * 
 * @author simonyang
 * @date 2026-04-10
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminLog
{
    /**
     * 操作模块（如：帖子管理、评论管理、用户管理等）
     */
    String module() default "";

    /**
     * 操作类型
     */
    AdminOperationType operationType() default AdminOperationType.OTHER;

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数
     */
    String[] excludeParamNames() default {};
}
