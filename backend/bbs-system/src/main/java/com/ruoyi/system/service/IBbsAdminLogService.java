package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsAdminLog;

import java.util.List;

/**
 * 管理员操作日志Service接口
 * 
 * @author simonyang
 * @date 2026-04-10
 */
public interface IBbsAdminLogService 
{
    /**
     * 查询管理员操作日志
     * 
     * @param logId 管理员操作日志主键
     * @return 管理员操作日志
     */
    public BbsAdminLog selectBbsAdminLogByLogId(Long logId);

    /**
     * 查询管理员操作日志列表
     * 
     * @param bbsAdminLog 管理员操作日志
     * @return 管理员操作日志集合
     */
    public List<BbsAdminLog> selectBbsAdminLogList(BbsAdminLog bbsAdminLog);

    /**
     * 新增管理员操作日志
     * 
     * @param bbsAdminLog 管理员操作日志
     * @return 结果
     */
    public int insertBbsAdminLog(BbsAdminLog bbsAdminLog);

    /**
     * 修改管理员操作日志
     * 
     * @param bbsAdminLog 管理员操作日志
     * @return 结果
     */
    public int updateBbsAdminLog(BbsAdminLog bbsAdminLog);

    /**
     * 批量删除管理员操作日志
     * 
     * @param logIds 需要删除的管理员操作日志主键集合
     * @return 结果
     */
    public int deleteBbsAdminLogByLogIds(Long[] logIds);

    /**
     * 删除管理员操作日志信息
     * 
     * @param logId 管理员操作日志主键
     * @return 结果
     */
    public int deleteBbsAdminLogByLogId(Long logId);
}
