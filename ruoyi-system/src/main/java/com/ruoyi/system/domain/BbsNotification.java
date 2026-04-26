package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 通知表 bbs_notification
 * 
 * @author ruoyi
 */
public class BbsNotification
{
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    private Long notificationId;

    /** 用户ID（接收者） */
    private String userId;

    /** 通知类型（1评论 2回复 3点赞 4关注 5系统） */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 目标类型（1帖子 2评论） */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 来源用户ID */
    private String fromUserId;

    /** 是否已读（0未读 1已读） */
    private String isRead;

    /** 创建时间 */
    private java.util.Date createTime;

    /** 来源用户昵称 */
    private String fromNickName;

    /** 来源用户头像 */
    private String fromAvatar;

    /** 是否匿名（0实名 1匿名）- 用于评论和回复通知 */
    private String isAnonymous;

    public Long getNotificationId()
    {
        return notificationId;
    }

    public void setNotificationId(Long notificationId)
    {
        this.notificationId = notificationId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getTargetType()
    {
        return targetType;
    }

    public void setTargetType(String targetType)
    {
        this.targetType = targetType;
    }

    public Long getTargetId()
    {
        return targetId;
    }

    public void setTargetId(Long targetId)
    {
        this.targetId = targetId;
    }

    public String getFromUserId()
    {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId)
    {
        this.fromUserId = fromUserId;
    }

    public String getIsRead()
    {
        return isRead;
    }

    public void setIsRead(String isRead)
    {
        this.isRead = isRead;
    }

    public java.util.Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime)
    {
        this.createTime = createTime;
    }

    public String getFromNickName()
    {
        return fromNickName;
    }

    public void setFromNickName(String fromNickName)
    {
        this.fromNickName = fromNickName;
    }

    public String getFromAvatar()
    {
        return fromAvatar;
    }

    public void setFromAvatar(String fromAvatar)
    {
        this.fromAvatar = fromAvatar;
    }

    public String getIsAnonymous()
    {
        return isAnonymous;
    }

    public void setIsAnonymous(String isAnonymous)
    {
        this.isAnonymous = isAnonymous;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("notificationId", getNotificationId())
            .append("userId", getUserId())
            .append("type", getType())
            .append("title", getTitle())
            .append("content", getContent())
            .append("targetType", getTargetType())
            .append("targetId", getTargetId())
            .append("fromUserId", getFromUserId())
            .append("isRead", getIsRead())
            .append("createTime", getCreateTime())
            .toString();
    }
}
