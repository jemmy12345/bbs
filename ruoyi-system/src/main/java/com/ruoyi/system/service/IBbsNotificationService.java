package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BbsNotification;

/**
 * 通知 服务层
 * 
 * @author ruoyi
 */
public interface IBbsNotificationService
{
    /**
     * 查询通知信息
     * 
     * @param notificationId 通知ID
     * @return 通知信息
     */
    public BbsNotification selectBbsNotificationById(Long notificationId);

    /**
     * 查询通知列表
     * 
     * @param bbsNotification 通知信息
     * @return 通知集合
     */
    public List<BbsNotification> selectBbsNotificationList(BbsNotification bbsNotification);

    /**
     * 新增通知
     * 
     * @param bbsNotification 通知信息
     * @return 结果
     */
    public int insertBbsNotification(BbsNotification bbsNotification);

    /**
     * 修改通知
     * 
     * @param bbsNotification 通知信息
     * @return 结果
     */
    public int updateBbsNotification(BbsNotification bbsNotification);

    /**
     * 删除通知信息
     * 
     * @param notificationIds 需要删除的通知ID
     * @return 结果
     */
    public int deleteBbsNotificationByIds(Long[] notificationIds);

    /**
     * 标记为已读
     * 
     * @param notificationId 通知ID
     * @return 结果
     */
    public int markAsRead(Long notificationId);

    /**
     * 全部标记为已读
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int markAllAsRead(String userId);

    /**
     * 统计未读通知数
     * 
     * @param userId 用户ID
     * @return 未读通知数
     */
    public int countUnread(String userId);

    /**
     * 创建评论通知
     * 
     * @param postId 帖子ID
     * @param commentId 评论ID
     * @param fromUserId 评论用户ID
     * @param postOwnerId 帖子作者ID
     * @return 结果
     */
    public int createCommentNotification(Long postId, Long commentId, String fromUserId, String isAnonymous, String postOwnerId);

    /**
     * 创建回复通知
     * 
     * @param postId 帖子ID
     * @param commentId 评论ID
     * @param fromUserId 回复用户ID
     * @param replyUserId 被回复用户ID
     * @return 结果
     */
    public int createReplyNotification(Long postId, Long commentId, String fromUserId, String isAnonymous, String replyUserId);

    /**
     * 创建点赞通知
     * 
     * @param postId 帖子ID
     * @param fromUserId 点赞用户ID
     * @param postOwnerId 帖子作者ID
     * @return 结果
     */
    public int createLikeNotification(Long postId, String fromUserId, String postOwnerId);

    /**
     * 创建收藏通知
     * 
     * @param postId 帖子ID
     * @param fromUserId 收藏用户ID
     * @param postOwnerId 帖子作者ID
     * @return 结果
     */
    public int createCollectNotification(Long postId, String fromUserId, String postOwnerId);
}
