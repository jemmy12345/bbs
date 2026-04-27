package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BbsCheckin;
import org.apache.ibatis.annotations.Param;
import java.util.Date;

/**
 * 每日签到 Mapper
 */
public interface BbsCheckinMapper
{
    /**
     * 查询用户某日的签到记录（存在则返回，否则 null）
     */
    BbsCheckin selectByUserAndDate(@Param("userId") String userId, @Param("date") Date date);

    /**
     * 查询用户最近一次签到记录（按日期倒序第一条）
     */
    BbsCheckin selectLatestByUser(@Param("userId") String userId);

    /**
     * 插入签到记录
     */
    int insert(BbsCheckin checkin);
}
