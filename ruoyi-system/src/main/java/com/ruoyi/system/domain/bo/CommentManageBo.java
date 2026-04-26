package com.ruoyi.system.domain.bo;

public class CommentManageBo {
  /** 评论id */
  private Long commentId;

  /** 评论删除标志 */
  private String commentDelFlag;

  public Long getCommentId() {
    return commentId;
  }

  public void setCommentId(Long commentId) {
    this.commentId = commentId;
  }

  public String getCommentDelFlag() {
    return commentDelFlag;
  }

  public void setCommentDelFlag(String commentDelFlag) {
    this.commentDelFlag = commentDelFlag;
  }
}
