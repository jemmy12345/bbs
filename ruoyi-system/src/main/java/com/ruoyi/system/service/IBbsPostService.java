package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BbsPost;

/**
 * 帖子 服务层
 * 
 * @author ruoyi
 */
public interface IBbsPostService
{
    /**
     * 查询帖子信息
     * 
     * @param postId 帖子ID
     * @return 帖子信息
     */
    public BbsPost selectBbsPostById(Long postId);

    /**
     * 查询帖子信息（包含用户点赞/收藏状态）
     * 
     * @param postId 帖子ID
     * @param userId 用户ID（可为空，如果为空则不查询用户状态）
     * @return 帖子信息
     */
    public BbsPost selectBbsPostById(Long postId, String userId);

    /**
     * 查询帖子列表
     * 
     * @param bbsPost 帖子信息
     * @return 帖子集合
     */
    public List<BbsPost> selectBbsPostList(BbsPost bbsPost);

    /**
     * 查询个人匿名帖子
     * @param hashCode
     * @return
     */
    public List<BbsPost> listAnonymousByKey(String hashCode);
    /**
     * 新增帖子
     * 
     * @param bbsPost 帖子信息
     * @return 结果
     */
    public Long insertBbsPost(BbsPost bbsPost);

    /**
     * 修改帖子
     * 
     * @param bbsPost 帖子信息
     * @return 结果
     */
    public int updateBbsPost(BbsPost bbsPost);

    /**
     * 批量删除帖子信息
     * 
     * @param postIds 需要删除的帖子ID
     * @return 结果
     */
    public int deleteBbsPostByIds(Long[] postIds);

    /**
     * 删除帖子信息
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int deleteBbsPostById(Long postId);

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
     * 点赞/取消点赞
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 结果
     */
    public int toggleLike(Long postId, String userId);

    /**
     * 收藏/取消收藏
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 结果
     */
    public int toggleCollect(Long postId, String userId);

    /**
     * 查询热门文章列表
     * 
     * @param limit 限制数量
     * @return 帖子集合
     */
    public List<BbsPost> selectHotPostList(Integer limit);

    /**
     * 批量审核通过文章
     * 
     * @param postIds 需要审核通过的帖子ID数组
     * @return 结果
     */
    public int approvePosts(Long[] postIds);

    /**
     * 批量审核不通过文章
     * 
     * @param postIds 需要审核不通过的帖子ID数组
     * @param auditReason 审核不通过原因
     * @return 结果
     */
    public int rejectPosts(Long[] postIds, String auditReason);

    /**
     * 置顶/取消置顶
     * 
     * @param postId 帖子ID
     * @return 结果（1置顶成功，-1取消置顶）
     */
    public int toggleTop(Long postId);

    /**
     * 下架/上架帖子
     * 
     * @param postIds 帖子ID数组
     * @return 结果（成功数量）
     */
    public int toggleOffline(Long[] postIds);

    /**
     * 根据用户ID查询帖子列表
     * @param postId
     * @return
     */
    public int delByPersonal(Long postId);
}
