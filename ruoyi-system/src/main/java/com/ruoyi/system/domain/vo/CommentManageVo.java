package com.ruoyi.system.domain.vo;

import com.ruoyi.common.core.domain.BaseEntity;

public class CommentManageVo extends BaseEntity {
  // {
  // "postId":"123",
  // "title":"456",
  // "commentId":123,
  // "commentUserId":"456",
  // "commentNickName":"456",
  // "commentContent":"456",
  // "commentStatus":"456",
  // "commentDelFlag":"456"
  // }
  /** 贴子id */
  private Long postId;

  /** 贴子标题 */
  private String title;

  /** 评论id */
  private Long commentId;

  /** 评论用户id */
  private String commentUserId;

  /** 评论用户昵称 */
  private String commentNickName;

  /** 评论内容 */
  private String commentContent;

  /** 评论状态 */
  private String commentStatus;

  /** 评论删除标志 */
  private String commentDelFlag;

  public Long getPostId() {
    return postId;
  }

  public void setPostId(Long postId) {
    this.postId = postId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getCommentId() {
    return commentId;
  }

  public void setCommentId(Long commentId) {
    this.commentId = commentId;
  }

  public String getCommentUserId() {
    return commentUserId;
  }

  public void setCommentUserId(String commentUserId) {
    this.commentUserId = commentUserId;
  }

  public String getCommentNickName() {
    return commentNickName;
  }

  public void setCommentNickName(String commentNickName) {
    this.commentNickName = commentNickName;
  }

  public String getCommentContent() {
    return commentContent;
  }

  public void setCommentContent(String commentContent) {
    this.commentContent = commentContent;
  }

  public String getCommentStatus() {
    return commentStatus;
  }

  public void setCommentStatus(String commentStatus) {
    this.commentStatus = commentStatus;
  }

  public String getCommentDelFlag() {
    return commentDelFlag;
  }

  public void setCommentDelFlag(String commentDelFlag) {
    this.commentDelFlag = commentDelFlag;
  }
}
