package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsLike;
import com.ruoyi.system.domain.BbsPost;

/**
 * 点赞表 数据层
 * 
 * @author ruoyi
 */
public interface BbsLikeMapper
{
    /**
     * 查询点赞信息
     * 
     * @param bbsLike 点赞信息
     * @return 点赞信息
     */
    public BbsLike selectBbsLike(BbsLike bbsLike);

    /**
     * 新增点赞
     * 
     * @param bbsLike 点赞信息
     * @return 结果
     */
    public int insertBbsLike(BbsLike bbsLike);

    /**
     * 删除点赞
     * 
     * @param bbsLike 点赞信息
     * @return 结果
     */
    public int deleteBbsLike(BbsLike bbsLike);

    /**
     * 统计点赞数
     * 
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 点赞数
     */
    public int countBbsLike(String targetType, Long targetId);

    /**
     * 查询用户点赞的帖子列表
     * 
     * @param userId 用户ID
     * @return 帖子集合
     */
    public List<BbsPost> selectLikePostList(String userId);

    /**
     * 统计用户收到的点赞数（包括帖子和评论）
     * 
     * @param userId 用户ID
     * @return 点赞数
     */
    public int countUserReceivedLikes(String userId);

    /**
     * 根据帖子ID集合查询点赞列表
     * 
     * @param postIds 帖子ID集合
     * @param targetType 目标类型
     * @return 点赞集合
     */
    public List<BbsLike> selectBbsLikeListByPostIds(@org.apache.ibatis.annotations.Param("postIds") List<Long> postIds, @org.apache.ibatis.annotations.Param("targetType") String targetType);
}
