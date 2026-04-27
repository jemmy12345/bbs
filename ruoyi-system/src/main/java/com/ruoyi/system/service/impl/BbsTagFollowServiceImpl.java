package com.ruoyi.system.service.impl;

import com.ruoyi.system.mapper.BbsTagFollowMapper;
import com.ruoyi.system.service.IBbsTagFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签订阅 Service 实现
 */
@Service
public class BbsTagFollowServiceImpl implements IBbsTagFollowService
{
    @Autowired
    private BbsTagFollowMapper bbsTagFollowMapper;

    @Override
    public boolean toggleFollow(String userId, Long tagId)
    {
        if (bbsTagFollowMapper.countByUserAndTag(userId, tagId) > 0)
        {
            bbsTagFollowMapper.delete(userId, tagId);
            return false;
        }
        else
        {
            bbsTagFollowMapper.insert(userId, tagId);
            return true;
        }
    }

    @Override
    public boolean isFollowing(String userId, Long tagId)
    {
        return bbsTagFollowMapper.countByUserAndTag(userId, tagId) > 0;
    }

    @Override
    public List<Long> getFollowedTagIds(String userId)
    {
        return bbsTagFollowMapper.selectTagIdsByUser(userId);
    }

    @Override
    public List<String> getFollowerUserIds(Long tagId)
    {
        return bbsTagFollowMapper.selectUserIdsByTag(tagId);
    }
}
