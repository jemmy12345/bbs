package com.ruoyi.system.mapper;

import java.util.List;

import com.ruoyi.system.domain.BbsAdminLog;
import org.springframework.stereotype.Repository;

/**
 * 管理员操作日志Mapper接口
 * 
 * @author simonyang
 * @date 2026-04-10
 */
@Repository
public interface BbsAdminLogMapper 
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
     * 批量新增管理员操作日志
     *
     * @param bbsAdminLogList 管理员操作日志
     * @return 结果
     */
    public int batchInsert(List<BbsAdminLog> bbsAdminLogList);

    /**
     * 修改管理员操作日志
     * 
     * @param bbsAdminLog 管理员操作日志
     * @return 结果
     */
    public int updateBbsAdminLog(BbsAdminLog bbsAdminLog);

    /**
     * 删除管理员操作日志
     * 
     * @param logId 管理员操作日志主键
     * @return 结果
     */
    public int deleteBbsAdminLogByLogId(Long logId);

    /**
     * 批量删除管理员操作日志
     * 
     * @param logIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBbsAdminLogByLogIds(Long[] logIds);
}
