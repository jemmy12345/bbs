package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsPostFollowupRecord;

public interface BbsPostFollowupRecordMapper
{
    int insertBbsPostFollowupRecord(BbsPostFollowupRecord record);

    BbsPostFollowupRecord selectLatestByPostId(Long postId);

    List<BbsPostFollowupRecord> selectListByPostId(Long postId);
}
