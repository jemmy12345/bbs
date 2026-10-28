package com.ruoyi.system.domain.bo;

import com.ruoyi.common.core.domain.BaseEntity;

public class CommentManageQueryBo extends BaseEntity {
  /** 贴子标题 */
  private String title;

  /** 评论内容 */
  private String commentContent;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCommentContent() {
    return commentContent;
  }

  public void setCommentContent(String commentContent) {
    this.commentContent = commentContent;
  }
}
