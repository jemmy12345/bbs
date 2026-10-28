package com.ruoyi.system.service.impl;

import java.util.List;

import com.ruoyi.system.domain.BbsAdminLog;
import com.ruoyi.system.mapper.BbsAdminLogMapper;
import com.ruoyi.system.service.IBbsAdminLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 管理员操作日志Service业务层处理
 * 
 * @author simonyang
 * @date 2026-04-10
 */
@Service
public class BbsAdminLogServiceImpl implements IBbsAdminLogService
{
    @Autowired
    private BbsAdminLogMapper bbsAdminLogMapper;

    /**
     * 查询管理员操作日志
     * 
     * @param logId 管理员操作日志主键
     * @return 管理员操作日志
     */
    @Override
    public BbsAdminLog selectBbsAdminLogByLogId(Long logId)
    {
        return bbsAdminLogMapper.selectBbsAdminLogByLogId(logId);
    }

    /**
     * 查询管理员操作日志列表
     * 
     * @param bbsAdminLog 管理员操作日志
     * @return 管理员操作日志
     */
    @Override
    public List<BbsAdminLog> selectBbsAdminLogList(BbsAdminLog bbsAdminLog)
    {
        return bbsAdminLogMapper.selectBbsAdminLogList(bbsAdminLog);
    }

    /**
     * 新增管理员操作日志
     * 
     * @param bbsAdminLog 管理员操作日志
     * @return 结果
     */
    @Override
    public int insertBbsAdminLog(BbsAdminLog bbsAdminLog)
    {
        return bbsAdminLogMapper.insertBbsAdminLog(bbsAdminLog);
    }

    /**
     * 修改管理员操作日志
     * 
     * @param bbsAdminLog 管理员操作日志
     * @return 结果
     */
    @Override
    public int updateBbsAdminLog(BbsAdminLog bbsAdminLog)
    {
        return bbsAdminLogMapper.updateBbsAdminLog(bbsAdminLog);
    }

    /**
     * 批量删除管理员操作日志
     * 
     * @param logIds 需要删除的管理员操作日志主键
     * @return 结果
     */
    @Override
    public int deleteBbsAdminLogByLogIds(Long[] logIds)
    {
        return bbsAdminLogMapper.deleteBbsAdminLogByLogIds(logIds);
    }

    /**
     * 删除管理员操作日志信息
     * 
     * @param logId 管理员操作日志主键
     * @return 结果
     */
    @Override
    public int deleteBbsAdminLogByLogId(Long logId)
    {
        return bbsAdminLogMapper.deleteBbsAdminLogByLogId(logId);
    }
}
