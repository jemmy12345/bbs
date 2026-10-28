package com.ruoyi.framework.aspectj;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.core.domain.entity.SysUser;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.AdminLog;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.HttpMethod;
import com.ruoyi.common.filter.PropertyPreExcludeFilter;
import com.ruoyi.common.utils.AdminLogContext;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.domain.BbsAdminLog;

/**
 * 管理员操作日志记录处理切面
 * 
 * 拦截标注了 @AdminLog 注解的方法，自动采集管理员操作信息，
 * 通过 AsyncManager 异步写入 bbs_admin_log 表。
 * 
 * @author simonyang
 * @date 2026-04-10
 */
@Aspect
@Component
public class AdminLogAspect
{
    private static final Logger log = LoggerFactory.getLogger(AdminLogAspect.class);

    /** 排除敏感属性字段 */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    /** 计算操作消耗时间 */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("AdminLog Cost Time");

    /** 参数最大长度限制 */
    private static final int PARAM_MAX_LENGTH = 2000;

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(adminLog)")
    public void doBefore(JoinPoint joinPoint, AdminLog adminLog)
    {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(adminLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, AdminLog adminLog, Object jsonResult)
    {
        handleLog(joinPoint, adminLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     * 
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "@annotation(adminLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, AdminLog adminLog, Exception e)
    {
        handleLog(joinPoint, adminLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, AdminLog adminLog, final Exception e, Object jsonResult)
    {
        try
        {
            // 获取当前的用户
            LoginUser loginUser = SecurityUtils.getLoginUser();

            // *========构建管理员操作日志=========*//
            BbsAdminLog bbsAdminLog = new BbsAdminLog();

            // 设置操作状态：1成功 0失败
            bbsAdminLog.setStatus(1L);

            // 请求的IP地址
            String ip = IpUtils.getIpAddr();
            bbsAdminLog.setIpAddress(ip);

            // 请求URL
            HttpServletRequest request = ServletUtils.getRequest();
            bbsAdminLog.setRequestUrl(StringUtils.substring(request.getRequestURI(), 0, 255));

            // 请求方式
            bbsAdminLog.setRequestMethod(request.getMethod());

            // User-Agent
            bbsAdminLog.setUserAgent(StringUtils.substring(request.getHeader("User-Agent"), 0, 500));

            // 设置管理员信息
            if (loginUser != null)
            {
                SysUser user = loginUser.getUser();
                if(user != null){
                    bbsAdminLog.setAdminName(user.getNickName());
                    // adminId 使用用户ID的hashCode作为Long值（因为userId是String类型）
                    try
                    {
                        bbsAdminLog.setAdminId(user.getUserId());
                    }
                    catch (Exception ex)
                    {
                        bbsAdminLog.setAdminId(user.getUserId());
                    }
                }

            }

            // 设置异常信息
            if (e != null)
            {
                bbsAdminLog.setStatus(0L);
                bbsAdminLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }

            // 设置注解上的参数
            bbsAdminLog.setOperationModule(adminLog.module());
            bbsAdminLog.setOperationType(adminLog.operationType().getCode());
            // 操作描述优先级：AdminLogContext动态描述 > 注解静态描述 > 操作类型默认描述
            String contextDesc = AdminLogContext.getDescription();
            String desc;
            if (StringUtils.isNotEmpty(contextDesc))
            {
                desc = contextDesc;
            }
            else if (StringUtils.isNotEmpty(adminLog.description()))
            {
                desc = adminLog.description();
            }
            else
            {
                desc = adminLog.operationType().getDescription();
            }
            bbsAdminLog.setOperationDesc(desc);

            // 是否需要保存request参数
            if (adminLog.isSaveRequestData())
            {
                setRequestValue(joinPoint, bbsAdminLog, adminLog.excludeParamNames());
            }

            // 是否需要保存response结果
            if (adminLog.isSaveResponseData() && StringUtils.isNotNull(jsonResult))
            {
                bbsAdminLog.setResponseResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000));
            }

            // 设置消耗时间
            Long startTime = TIME_THREADLOCAL.get();
            if (startTime != null)
            {
                bbsAdminLog.setExecutionTime(System.currentTimeMillis() - startTime);
            }

            // 设置操作时间
            bbsAdminLog.setCreatedTime(new Date());

            // 通过异步任务管理器保存到数据库
            AsyncManager.me().execute(AsyncFactory.recordAdminLog(bbsAdminLog));
        }
        catch (Exception exp)
        {
            // 记录本地异常日志
            log.error("管理员操作日志记录异常:{}", exp.getMessage());
            exp.printStackTrace();
        }
        finally
        {
            TIME_THREADLOCAL.remove();
            AdminLogContext.clear();
        }
    }

    /**
     * 获取请求的参数，放到adminLog中
     */
    private void setRequestValue(JoinPoint joinPoint, BbsAdminLog adminLog, String[] excludeParamNames) throws Exception
    {
        String requestMethod = adminLog.getRequestMethod();
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        if (StringUtils.isEmpty(paramsMap) 
            && StringUtils.equalsAny(requestMethod, HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name()))
        {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            adminLog.setRequestParams(params);
        }
        else
        {
            adminLog.setRequestParams(StringUtils.substring(
                JSON.toJSONString(paramsMap, excludePropertyPreFilter(excludeParamNames)), 0, PARAM_MAX_LENGTH));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames)
    {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0)
        {
            for (Object o : paramsArray)
            {
                if (StringUtils.isNotNull(o) && !isFilterObject(o))
                {
                    try
                    {
                        String jsonObj = JSON.toJSONString(o, excludePropertyPreFilter(excludeParamNames));
                        params.append(jsonObj).append(" ");
                        if (params.length() >= PARAM_MAX_LENGTH)
                        {
                            return StringUtils.substring(params.toString(), 0, PARAM_MAX_LENGTH);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("管理员日志请求参数拼装异常 msg:{}", e.getMessage());
                    }
                }
            }
        }
        return params.toString();
    }

    /**
     * 忽略敏感属性
     */
    public PropertyPreExcludeFilter excludePropertyPreFilter(String[] excludeParamNames)
    {
        return new PropertyPreExcludeFilter().addExcludes(ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
    }

    /**
     * 判断是否需要过滤的对象
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o)
    {
        Class<?> clazz = o.getClass();
        if (clazz.isArray())
        {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        else if (Collection.class.isAssignableFrom(clazz))
        {
            Collection collection = (Collection) o;
            for (Object value : collection)
            {
                return value instanceof MultipartFile;
            }
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            Map map = (Map) o;
            for (Object value : map.entrySet())
            {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
