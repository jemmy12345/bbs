package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.BbsPost;

/**
 * 个人中心服务接口
 * 
 * @author ruoyi
 */
public interface IBbsUserCenterService
{
    /**
     * 获取个人中心统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getUserCenterStats(String userId);

    /**
     * 查询用户发布的帖子列表
     * 
     * @param userId 用户ID
     * @return 帖子集合
     */
    public List<BbsPost> selectUserPostList(String userId);

    /**
     * 查询用户收藏的帖子列表
     * 
     * @param userId 用户ID
     * @return 帖子集合
     */
    public List<BbsPost> selectUserCollectList(String userId);

    /**
     * 查询用户点赞的帖子列表
     * 
     * @param userId 用户ID
     * @return 帖子集合
     */
    public List<BbsPost> selectUserLikeList(String userId);
}
