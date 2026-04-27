package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsCheckin;

import java.util.Map;

/**
 * 每日签到 Service 接口
 */
public interface IBbsCheckinService
{
    /**
     * 执行签到，返回签到结果（today、streak、pointsEarned、alreadyChecked）
     */
    Map<String, Object> doCheckin(String userId);

    /**
     * 查询用户今日是否已签到及当前连续天数
     */
    Map<String, Object> getTodayStatus(String userId);
}
