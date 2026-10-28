package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.List;

public class BbsQaAnswer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long answerId;
    private Long questionId;
    private Long parentAnswerId;
    private String content;
    private String isBest;
    private Integer likeCount;
    private String answerUserId;
    private String answerNickName;
    private String answerAvatar;
    private String answerDeptName;
    private String delFlag;

    /** 子回答（楼中楼追问）*/
    private List<BbsQaAnswer> children;
    /** 当前用户是否已点赞此回答 */
    private Boolean isLiked;

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getParentAnswerId() { return parentAnswerId; }
    public void setParentAnswerId(Long parentAnswerId) { this.parentAnswerId = parentAnswerId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getIsBest() { return isBest; }
    public void setIsBest(String isBest) { this.isBest = isBest; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public String getAnswerUserId() { return answerUserId; }
    public void setAnswerUserId(String answerUserId) { this.answerUserId = answerUserId; }

    public String getAnswerNickName() { return answerNickName; }
    public void setAnswerNickName(String answerNickName) { this.answerNickName = answerNickName; }

    public String getAnswerAvatar() { return answerAvatar; }
    public void setAnswerAvatar(String answerAvatar) { this.answerAvatar = answerAvatar; }

    public String getAnswerDeptName() { return answerDeptName; }
    public void setAnswerDeptName(String answerDeptName) { this.answerDeptName = answerDeptName; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public List<BbsQaAnswer> getChildren() { return children; }
    public void setChildren(List<BbsQaAnswer> children) { this.children = children; }

    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
}
