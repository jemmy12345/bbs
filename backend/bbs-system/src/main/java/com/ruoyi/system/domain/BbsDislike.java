package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 点踩对象 bbs_dislike
 *
 * @author simonyang
 * @date 2026-04-08
 */
public class BbsDislike extends BaseEntity {
  private static final long serialVersionUID = 1L;

  /** 点踩ID */
  private Long dislikeId;

  /** 目标类型（1帖子 2评论） */
  private String targetType;

  /** 目标ID */
  private Long targetId;

  /** 用户ID */
  private String userId;

  public void setDislikeId(Long dislikeId) {
    this.dislikeId = dislikeId;
  }

  public Long getDislikeId() {
    return dislikeId;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  public String getTargetType() {
    return targetType;
  }

  public void setTargetId(Long targetId) {
    this.targetId = targetId;
  }

  public Long getTargetId() {
    return targetId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("dislikeId", getDislikeId())
        .append("targetType", getTargetType())
        .append("targetId", getTargetId())
        .append("userId", getUserId())
        .append("createTime", getCreateTime())
        .toString();
  }
}
