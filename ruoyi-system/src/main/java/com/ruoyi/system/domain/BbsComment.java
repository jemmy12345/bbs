package com.ruoyi.system.domain;

import java.util.List;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 评论表 bbs_comment
 * 
 * @author ruoyi
 */
public class BbsComment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评论ID */
    private Long commentId;

    /** 帖子ID */
    private Long postId;

    /** 用户ID- 匿名用户为秘钥的hash值 */
    private String userId;

    /** 父评论ID（0表示一级评论） */
    private Long parentId;

    /** 回复的用户ID */
    private String replyUserId;

    /** 评论内容 */
    private String content;

    /** 评论图片（多个用逗号分隔） */
    private String images;

    /** 是否匿名（0实名 1匿名） */
    private String isAnonymous;

    /** 点赞数 */
    private Integer likeCount;

    /** 点踩数 */
    private Integer dislikeCount;

    /** 状态（0正常 1关闭） */
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 用户昵称 */
    private String nickName;

    /** 用户头像 */
    private String avatar;

    /** 回复用户昵称 */
    private String replyNickName;

    /** 用户部门名称 */
    private String deptName;

    /** 是否已点赞 */
    private Boolean isLiked;

    /** 是否已点踩 */
    private Boolean isDisliked;

    /** 子评论列表 */
    private List<BbsComment> children;

    /** 是否官方回复（即该回复人员是否为当前帖子的部门接口人） */
    private Boolean isOfficialReply;

    public Long getCommentId()
    {
        return commentId;
    }

    public void setCommentId(Long commentId)
    {
        this.commentId = commentId;
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

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getReplyUserId()
    {
        return replyUserId;
    }

    public void setReplyUserId(String replyUserId)
    {
        this.replyUserId = replyUserId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getImages()
    {
        return images;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public String getIsAnonymous()
    {
        return isAnonymous;
    }

    public void setIsAnonymous(String isAnonymous)
    {
        this.isAnonymous = isAnonymous;
    }

    public Integer getLikeCount()
    {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount)
    {
        this.likeCount = likeCount;
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

    public String getReplyNickName()
    {
        return replyNickName;
    }

    public void setReplyNickName(String replyNickName)
    {
        this.replyNickName = replyNickName;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public Boolean getIsLiked()
    {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked)
    {
        this.isLiked = isLiked;
    }

    public List<BbsComment> getChildren()
    {
        return children;
    }

    public void setChildren(List<BbsComment> children)
    {
        this.children = children;
    }

    public Boolean getIsOfficialReply()
    {
        return isOfficialReply;
    }

    public void setIsOfficialReply(Boolean isOfficialReply)
    {
        this.isOfficialReply = isOfficialReply;
    }

    public Integer getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(Integer dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public Boolean getLiked() {
        return isLiked;
    }

    public void setLiked(Boolean liked) {
        isLiked = liked;
    }

    public Boolean getIsDisliked() {
        return isDisliked;
    }

    public void setIsDisliked(Boolean disliked) {
        isDisliked = disliked;
    }

    public Boolean getOfficialReply() {
        return isOfficialReply;
    }

    public void setOfficialReply(Boolean officialReply) {
        isOfficialReply = officialReply;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("commentId", getCommentId())
            .append("postId", getPostId())
            .append("userId", getUserId())
            .append("parentId", getParentId())
            .append("replyUserId", getReplyUserId())
            .append("content", getContent())
            .append("images", getImages())
            .append("isAnonymous", getIsAnonymous())
            .append("likeCount", getLikeCount())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
