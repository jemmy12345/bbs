package com.ruoyi.system.service.impl;

import java.util.List;

import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.BbsNotification;
import com.ruoyi.system.domain.BbsPost;
import com.ruoyi.system.mapper.BbsNotificationMapper;
import com.ruoyi.system.mapper.BbsPostMapper;
import com.ruoyi.system.service.IBbsNotificationService;

/**
 * 通知 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class BbsNotificationServiceImpl implements IBbsNotificationService
{
    @Autowired
    private BbsNotificationMapper bbsNotificationMapper;

    @Autowired
    private BbsPostMapper bbsPostMapper;

    @Override
    public BbsNotification selectBbsNotificationById(Long notificationId)
    {
        return bbsNotificationMapper.selectBbsNotificationById(notificationId);
    }

    @Override
    public List<BbsNotification> selectBbsNotificationList(BbsNotification bbsNotification)
    {
        return bbsNotificationMapper.selectBbsNotificationList(bbsNotification);
    }

    @Override
    public int insertBbsNotification(BbsNotification bbsNotification)
    {
        return bbsNotificationMapper.insertBbsNotification(bbsNotification);
    }

    @Override
    public int updateBbsNotification(BbsNotification bbsNotification)
    {
        return bbsNotificationMapper.updateBbsNotification(bbsNotification);
    }

    @Override
    public int deleteBbsNotificationByIds(Long[] notificationIds)
    {
        return bbsNotificationMapper.deleteBbsNotificationByIds(notificationIds);
    }

    @Override
    public int markAsRead(Long notificationId)
    {
        return bbsNotificationMapper.markAsRead(notificationId);
    }

    @Override
    public int markAllAsRead(String userId)
    {
        return bbsNotificationMapper.markAllAsRead(userId);
    }

    @Override
    public int countUnread(String userId)
    {
        return bbsNotificationMapper.countUnread(userId);
    }

    @Override
    public int createCommentNotification(Long postId, Long commentId, String fromUserId, String isAnonymous, String postOwnerId)
    {
        // 如果评论的是自己的文章，不发送通知
        if (fromUserId.equals(postOwnerId))
        {
            return 0;
        }

        BbsPost post = bbsPostMapper.selectBbsPostById(postId);
        if (post == null)
        {
            return 0;
        }

        BbsNotification notification = new BbsNotification();
        notification.setUserId(postOwnerId);
        notification.setType("1"); // 1-评论
        notification.setTitle("有人评论了你的文章");
        notification.setContent(post.getTitle());
        notification.setTargetType("1"); // 1-帖子
        notification.setTargetId(postId);
        notification.setFromUserId(fromUserId);
        notification.setIsRead("0");
        /** 是否匿名（0实名 1匿名） */
        if(isAnonymous.equals("0")){
            notification.setFromNickName(SecurityUtils.getLoginUser().getUser().getNickName());
            notification.setFromAvatar(SecurityUtils.getLoginUser().getUser().getAvatar());
        }else{
            notification.setFromNickName("匿名用户");
            notification.setFromAvatar("https://wwcdn.weixin.qq.com/node/wwmng/wwmng/style/images/independent/DefaultAvatar$caf2a2d6.png");

        }
        return bbsNotificationMapper.insertBbsNotification(notification);
    }

    @Override
    public int createReplyNotification(Long postId, Long commentId, String fromUserId, String isAnonymous, String replyUserId)
    {
        // 如果回复的是自己，不发送通知
        if (fromUserId.equals(replyUserId))
        {
            return 0;
        }

        BbsPost post = bbsPostMapper.selectBbsPostById(postId);
        if (post == null)
        {
            return 0;
        }

        BbsNotification notification = new BbsNotification();
        notification.setUserId(replyUserId);
        notification.setType("2"); // 2-回复
        notification.setTitle("有人回复了你的评论");
        notification.setContent(post.getTitle());
        notification.setTargetType("1"); // 1-帖子
        notification.setTargetId(postId);
        notification.setFromUserId(fromUserId);
        notification.setIsRead("0");
        /** 是否匿名（0实名 1匿名） */
        if(isAnonymous.equals("0")){
            notification.setFromNickName(SecurityUtils.getLoginUser().getUser().getNickName());
            notification.setFromAvatar(SecurityUtils.getLoginUser().getUser().getAvatar());
        }else{
            notification.setFromNickName("匿名用户");
            notification.setFromAvatar("https://wwcdn.weixin.qq.com/node/wwmng/wwmng/style/images/independent/DefaultAvatar$caf2a2d6.png");

        }
        return bbsNotificationMapper.insertBbsNotification(notification);
    }

    @Override
    public int createLikeNotification(Long postId, String fromUserId, String postOwnerId)
    {
        // 如果点赞的是自己的文章，不发送通知
        if (fromUserId.equals(postOwnerId))
        {
            return 0;
        }

        BbsPost post = bbsPostMapper.selectBbsPostById(postId);
        if (post == null)
        {
            return 0;
        }

        BbsNotification notification = new BbsNotification();
        notification.setUserId(postOwnerId);
        notification.setType("3"); // 3-点赞
        notification.setTitle("有人点赞了你的文章");
        notification.setContent(post.getTitle());
        notification.setTargetType("1"); // 1-帖子
        notification.setTargetId(postId);
        notification.setFromUserId(fromUserId);
        notification.setIsRead("0");
        notification.setFromNickName(SecurityUtils.getLoginUser().getUser().getNickName());
        notification.setFromAvatar(SecurityUtils.getLoginUser().getUser().getAvatar());
        return bbsNotificationMapper.insertBbsNotification(notification);
    }

    @Override
    public int createCollectNotification(Long postId, String fromUserId, String postOwnerId)
    {
        // 如果收藏的是自己的文章，不发送通知
        if (fromUserId.equals(postOwnerId))
        {
            return 0;
        }

        BbsPost post = bbsPostMapper.selectBbsPostById(postId);
        if (post == null)
        {
            return 0;
        }

        BbsNotification notification = new BbsNotification();
        notification.setUserId(postOwnerId);
        notification.setType("4"); // 4-收藏
        notification.setTitle("有人收藏了你的文章");
        notification.setContent(post.getTitle());
        notification.setTargetType("1"); // 1-帖子
        notification.setTargetId(postId);
        notification.setFromUserId(fromUserId);
        notification.setIsRead("0");
        notification.setFromNickName(SecurityUtils.getLoginUser().getUser().getNickName());
        notification.setFromAvatar(SecurityUtils.getLoginUser().getUser().getAvatar());
        return bbsNotificationMapper.insertBbsNotification(notification);
    }
}
