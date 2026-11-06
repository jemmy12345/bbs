package com.ruoyi.system.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.BbsPointRecord;
import com.ruoyi.system.domain.BbsPost;
import com.ruoyi.system.mapper.BbsCommentMapper;
import com.ruoyi.system.mapper.BbsLikeMapper;
import com.ruoyi.system.mapper.BbsPostMapper;
import com.ruoyi.system.mapper.BbsCollectMapper;
import com.ruoyi.system.mapper.BbsPointRecordMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsUserCenterService;

/**
 * 个人中心服务实现类
 * 
 * @author Smart BBS
 */
@Service
public class BbsUserCenterServiceImpl implements IBbsUserCenterService
{
    @Autowired
    private BbsPostMapper bbsPostMapper;

    @Autowired
    private BbsLikeMapper bbsLikeMapper;

    @Autowired
    private BbsCommentMapper bbsCommentMapper;

    @Autowired
    private BbsCollectMapper bbsCollectMapper;

    @Autowired
    private BbsPointRecordMapper bbsPointRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 获取个人中心统计信息
     */
    @Override
    public Map<String, Object> getUserCenterStats(String userId)
    {
        Map<String, Object> stats = new HashMap<>();
        
        // 发布的帖子数
        int postCount = bbsPostMapper.countUserPosts(userId);
        stats.put("postCount", postCount);
        
        // 收到的回复数（用户发布的帖子收到的评论数）
        int commentCount = bbsCommentMapper.countUserReceivedComments(userId);
        stats.put("commentCount", commentCount);
        
        // 收到的点赞数（包括帖子和评论）
        int likeCount = bbsLikeMapper.countUserReceivedLikes(userId);
        stats.put("likeCount", likeCount);
        
        // 收到的收藏数（用户发布的帖子被收藏的次数）
        // 注意：这里需要统计用户发布的帖子被收藏的总数
        List<BbsPost> userPosts = bbsPostMapper.selectUserPostList(userId);
        int collectCount = 0;
        for (BbsPost post : userPosts)
        {
            collectCount += post.getCollectCount() != null ? post.getCollectCount() : 0;
        }
        stats.put("collectCount", collectCount);

        SysUser user = sysUserMapper.selectUserById(userId);
        stats.put("bbsPoints", user == null || user.getBbsPoints() == null ? 0 : user.getBbsPoints());
        
        return stats;
    }

    /**
     * 查询用户发布的帖子列表
     */
    @Override
    public List<BbsPost> selectUserPostList(String userId)
    {
        return bbsPostMapper.selectUserPostList(userId);
    }

    /**
     * 查询用户收藏的帖子列表
     */
    @Override
    public List<BbsPost> selectUserCollectList(String userId)
    {
        return bbsCollectMapper.selectCollectPostList(userId);
    }

    /**
     * 查询用户点赞的帖子列表
     */
    @Override
    public List<BbsPost> selectUserLikeList(String userId)
    {
        return bbsLikeMapper.selectLikePostList(userId);
    }

    /**
     * 查询用户积分流水
     */
    @Override
    public List<BbsPointRecord> selectUserPointRecords(String userId)
    {
        return bbsPointRecordMapper.selectUserPointRecords(userId);
    }
}
