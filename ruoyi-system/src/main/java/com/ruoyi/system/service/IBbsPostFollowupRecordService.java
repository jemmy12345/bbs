package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BbsPostFollowupRecord;

public interface IBbsPostFollowupRecordService
{
    int insertBbsPostFollowupRecord(BbsPostFollowupRecord record);

    BbsPostFollowupRecord selectLatestByPostId(Long postId);

    List<BbsPostFollowupRecord> selectListByPostId(Long postId);
}
