package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.BbsPostFollowupRecord;
import com.ruoyi.system.mapper.BbsPostFollowupRecordMapper;
import com.ruoyi.system.service.IBbsPostFollowupRecordService;

@Service
public class BbsPostFollowupRecordServiceImpl implements IBbsPostFollowupRecordService
{
    @Autowired
    private BbsPostFollowupRecordMapper bbsPostFollowupRecordMapper;

    @Override
    public int insertBbsPostFollowupRecord(BbsPostFollowupRecord record)
    {
        return bbsPostFollowupRecordMapper.insertBbsPostFollowupRecord(record);
    }

    @Override
    public BbsPostFollowupRecord selectLatestByPostId(Long postId)
    {
        return bbsPostFollowupRecordMapper.selectLatestByPostId(postId);
    }

    @Override
    public List<BbsPostFollowupRecord> selectListByPostId(Long postId)
    {
        return bbsPostFollowupRecordMapper.selectListByPostId(postId);
    }
}
