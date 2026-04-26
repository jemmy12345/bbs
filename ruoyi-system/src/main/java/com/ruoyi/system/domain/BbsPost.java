package com.ruoyi.system.domain;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;

/**
 * 帖子表 bbs_post
 * 
 * @author ruoyi
 */
public class BbsPost extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    private Long postId;

    /** 分类ID */
    private Long categoryId;

    /** 用户ID */
    private String userId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 摘要 */
    private String summary;

    /** 浏览数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 是否置顶（0否 1是） */
    private String isTop;

    /** 是否精华（0否 1是） */
    private String isEssence;

    /** 是否匿名（0实名 1匿名） */
    private String isAnonymous;

    /** 是否管理员发帖（0否 1是） */
    private String isAdminPush;

    /** 帖子类型（share分享、suggestion建议、opinion意见） */
    private String postType;

    /** 回应部门ID */
    private Long responseDeptId;

    /** 回应部门名称 */
    private String responseDeptName;

    /** 状态（0正常 1关闭 2待审核 3审核不通过 4草稿） */
    private String status;

    /** 审核不通过原因 */
    private String auditReason;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 最后回复时间 */
    private java.util.Date lastReplyTime;

    /** 最后回复用户ID */
    private Long lastReplyUser;

    /** 分类名称 */
    private String categoryName;

    /** 用户昵称 */
    private String nickName;

    /** 用户头像 */
    private String avatar;

    /** 是否已点赞 */
    private Boolean isLiked;

    /** 是否已收藏 */
    private Boolean isCollected;

    /** 标签列表 */
    private List<BbsTag> tags;
    private String sortType;

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public Long getPostId()
    {
        return postId;
    }

    public void setPostId(Long postId)
    {
        this.postId = postId;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    @Xss(message = "帖子标题不能包含脚本字符")
    @NotBlank(message = "帖子标题不能为空")
    @Size(min = 0, max = 200, message = "帖子标题不能超过200个字符")
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

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Integer getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(Integer viewCount)
    {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount()
    {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount)
    {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount()
    {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount)
    {
        this.commentCount = commentCount;
    }

    public Integer getCollectCount()
    {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount)
    {
        this.collectCount = collectCount;
    }

    public String getIsTop()
    {
        return isTop;
    }

    public void setIsTop(String isTop)
    {
        this.isTop = isTop;
    }

    public String getIsEssence()
    {
        return isEssence;
    }

    public void setIsEssence(String isEssence)
    {
        this.isEssence = isEssence;
    }

    public String getIsAnonymous()
    {
        return isAnonymous;
    }

    public void setIsAnonymous(String isAnonymous)
    {
        this.isAnonymous = isAnonymous;
    }

    public String getPostType()
    {
        return postType;
    }

    public void setPostType(String postType)
    {
        this.postType = postType;
    }

    public Long getResponseDeptId()
    {
        return responseDeptId;
    }

    public void setResponseDeptId(Long responseDeptId)
    {
        this.responseDeptId = responseDeptId;
    }

    public String getResponseDeptName()
    {
        return responseDeptName;
    }

    public void setResponseDeptName(String responseDeptName)
    {
        this.responseDeptName = responseDeptName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public java.util.Date getLastReplyTime()
    {
        return lastReplyTime;
    }

    public void setLastReplyTime(java.util.Date lastReplyTime)
    {
        this.lastReplyTime = lastReplyTime;
    }

    public Long getLastReplyUser()
    {
        return lastReplyUser;
    }

    public void setLastReplyUser(Long lastReplyUser)
    {
        this.lastReplyUser = lastReplyUser;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public Boolean getIsLiked()
    {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked)
    {
        this.isLiked = isLiked;
    }

    public Boolean getIsCollected()
    {
        return isCollected;
    }

    public void setIsCollected(Boolean isCollected)
    {
        this.isCollected = isCollected;
    }

    public List<BbsTag> getTags()
    {
        return tags;
    }

    public void setTags(List<BbsTag> tags)
    {
        this.tags = tags;
    }

    public String getAuditReason()
    {
        return auditReason;
    }

    public void setAuditReason(String auditReason)
    {
        this.auditReason = auditReason;
    }

    public String getIsAdminPush() {
        return isAdminPush;
    }

    public void setIsAdminPush(String isAdminPush) {
        this.isAdminPush = isAdminPush;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("postId", getPostId())
            .append("categoryId", getCategoryId())
            .append("userId", getUserId())
            .append("title", getTitle())
            .append("content", getContent())
            .append("summary", getSummary())
            .append("viewCount", getViewCount())
            .append("likeCount", getLikeCount())
            .append("commentCount", getCommentCount())
            .append("collectCount", getCollectCount())
            .append("isTop", getIsTop())
            .append("isEssence", getIsEssence())
            .append("isAnonymous", getIsAnonymous())
            .append("postType", getPostType())
            .append("responseDeptId", getResponseDeptId())
            .append("status", getStatus())
            .append("auditReason", getAuditReason())
            .append("delFlag", getDelFlag())
            .append("lastReplyTime", getLastReplyTime())
            .append("lastReplyUser", getLastReplyUser())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
