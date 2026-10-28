package com.ruoyi.system.domain;

public class BbsCollectSurveyQuestion
{
    private Long questionId;
    private Long activityId;
    private Integer questionNo;
    private String questionType;
    private String questionTitle;
    private String optionJson;
    private String jumpLogicJson;
    private String requiredFlag;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Integer getQuestionNo() { return questionNo; }
    public void setQuestionNo(Integer questionNo) { this.questionNo = questionNo; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getQuestionTitle() { return questionTitle; }
    public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }

    public String getOptionJson() { return optionJson; }
    public void setOptionJson(String optionJson) { this.optionJson = optionJson; }

    public String getJumpLogicJson() { return jumpLogicJson; }
    public void setJumpLogicJson(String jumpLogicJson) { this.jumpLogicJson = jumpLogicJson; }

    public String getRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(String requiredFlag) { this.requiredFlag = requiredFlag; }
}
