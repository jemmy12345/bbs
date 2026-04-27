package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.Map;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsPointRecord;
import com.ruoyi.system.mapper.BbsPointRecordMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Forum points service implementation.
 */
@Service
public class BbsPointServiceImpl implements IBbsPointService
{
    private static final int POST_PUBLISH_POINTS = 5;

    private static final int COMMENT_PUBLISH_POINTS = 2;

    @Autowired
    private BbsPointRecordMapper bbsPointRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public void awardPostPublishPoints(String userId, Long postId)
    {
        awardPoints(userId, "POST_PUBLISH", "post", postId, POST_PUBLISH_POINTS, "发布帖子奖励积分");
    }

    @Override
    @Transactional
    public void awardCommentPublishPoints(String userId, Long commentId)
    {
        awardPoints(userId, "COMMENT_PUBLISH", "comment", commentId, COMMENT_PUBLISH_POINTS, "发布评论奖励积分");
    }

    @Override
    public List<Map<String, Object>> getTopPointUsers(Integer limit)
    {
        if (limit == null || limit <= 0)
        {
            limit = 5;
        }
        return sysUserMapper.selectTopPointUsers(limit);
    }

    protected void awardPoints(String userId, String actionType, String bizType, Long bizId, Integer points, String remark)
    {
        if (StringUtils.isEmpty(userId) || bizId == null || points == null || points <= 0)
        {
            return;
        }

        BbsPointRecord record = new BbsPointRecord();
        record.setUserId(userId);
        record.setActionType(actionType);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setPointChange(points);
        record.setRemark(remark);
        record.setDelFlag("0");
        record.setCreateBy(userId);
        record.setUpdateBy(userId);

        int inserted = bbsPointRecordMapper.insertIgnore(record);
        if (inserted > 0)
        {
            sysUserMapper.incrementBbsPoints(userId, points);
        }
    }
}
