package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.BbsPost;

/**
 * 帖子表 数据层
 * 
 * @author ruoyi
 */
public interface BbsPostMapper
{
    /**
     * 查询帖子信息
     * 
     * @param postId 帖子ID
     * @return 帖子信息
     */
    public BbsPost selectBbsPostById(Long postId);

    /**
     * 查询帖子列表
     * 
     * @param bbsPost 帖子信息
     * @return 帖子集合
     */
    public List<BbsPost> selectBbsPostList(BbsPost bbsPost);

    /**
     * 查询匿名帖子
     * @param hashCode
     * @return
     */
    public List<BbsPost> selectBbsPostListByHashCode(String hashCode);

    /**
     * 新增帖子
     * 
     * @param bbsPost 帖子信息
     * @return 结果
     */
    public int insertBbsPost(BbsPost bbsPost);

    /**
     * 修改帖子
     * 
     * @param bbsPost 帖子信息
     * @return 结果
     */
    public int updateBbsPost(BbsPost bbsPost);

    /**
     * 删除帖子
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int deleteBbsPostById(Long postId);

    /**
     * 批量删除帖子
     * 
     * @param postIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBbsPostByIds(Long[] postIds);

    /**
     * 真正删除草稿（设置del_flag='2'）
     * 
     * @param postIds 需要删除的帖子ID数组
     * @return 结果
     */
    public int realDeleteBbsPostByIds(Long[] postIds);

    /**
     * 增加浏览数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int incrementViewCount(Long postId);

    /**
     * 增加点赞数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int incrementLikeCount(Long postId);

    /**
     * 减少点赞数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int decrementLikeCount(Long postId);

    /**
     * 增加评论数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int incrementCommentCount(Long postId);

    /**
     * 减少评论数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int decrementCommentCount(Long postId);

    /**
     * 增加收藏数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int incrementCollectCount(Long postId);

    /**
     * 减少收藏数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int decrementCollectCount(Long postId);

    /**
     * 更新最后回复信息
     * 
     * @param bbsPost 帖子信息
     * @return 结果
     */
    public int updateLastReply(BbsPost bbsPost);

    /**
     * 查询热门文章列表
     * 
     * @param limit 限制数量
     * @return 帖子集合
     */
    public List<BbsPost> selectHotPostList(Integer limit);

    /**
     * 查询用户发布的帖子列表
     * 
     * @param userId 用户ID
     * @return 帖子集合
     */
    public List<BbsPost> selectUserPostList(String userId);

    /**
     * 统计用户发布的帖子数量
     * 
     * @param userId 用户ID
     * @return 帖子数量
     */
    public int countUserPosts(String userId);

    /**
     * 运营看板：各状态帖子数量
     */
    public List<Map<String, Object>> selectPostStatusStats();

    /**
     * 运营看板：各类型帖子数量
     */
    public List<Map<String, Object>> selectPostTypeStats();

    /**
     * 运营看板：最近7天每日发帖趋势
     */
    public List<Map<String, Object>> selectPostDailyTrend();

    /**
     * 运营看板：活跃用户排行
     */
    public List<Map<String, Object>> selectTopActiveUsers(int limit);

    /**
     * 运营看板：总体汇总数据
     */
    public Map<String, Object> selectPostSummary();
}
