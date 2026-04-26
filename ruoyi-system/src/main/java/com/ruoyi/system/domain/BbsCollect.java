package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 收藏表 bbs_collect
 * 
 * @author ruoyi
 */
public class BbsCollect
{
    private static final long serialVersionUID = 1L;

    /** 收藏ID */
    private Long collectId;

    /** 帖子ID */
    private Long postId;

    /** 用户ID */
    private String userId;

    /** 创建时间 */
    private java.util.Date createTime;

    public Long getCollectId()
    {
        return collectId;
    }

    public void setCollectId(Long collectId)
    {
        this.collectId = collectId;
    }

    public Long getPostId()
    {
        return postId;
    }

    public void setPostId(Long postId)
    {
        this.postId = postId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public java.util.Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime)
    {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("collectId", getCollectId())
            .append("postId", getPostId())
            .append("userId", getUserId())
            .append("createTime", getCreateTime())
            .toString();
    }
}
