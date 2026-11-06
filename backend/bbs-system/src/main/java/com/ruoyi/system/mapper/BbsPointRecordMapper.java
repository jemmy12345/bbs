package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BbsPointRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Points record mapper.
 */
public interface BbsPointRecordMapper
{
    int insertIgnore(BbsPointRecord record);

    List<BbsPointRecord> selectUserPointRecords(@Param("userId") String userId);
}
