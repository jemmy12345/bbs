package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 论坛内容风控结果。
 */
public class BbsModerationCheckResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String ACTION_ALLOW = "allow";
    public static final String ACTION_REVIEW = "review";
    public static final String ACTION_BLOCK = "block";

    private boolean passed;
    private String action;
    private String message;
    private List<String> words;
    private String riskSummary;
    private String source;
    private String moderationToken;
    private String signature;

    public static BbsModerationCheckResult allow()
    {
        BbsModerationCheckResult result = new BbsModerationCheckResult();
        result.setPassed(true);
        result.setAction(ACTION_ALLOW);
        result.setMessage("内容检测通过");
        result.setSource("allow");
        return result;
    }

    public static BbsModerationCheckResult block(String message, List<String> words, String riskSummary, String source)
    {
        BbsModerationCheckResult result = new BbsModerationCheckResult();
        result.setPassed(false);
        result.setAction(ACTION_BLOCK);
        result.setMessage(message);
        result.setWords(words);
        result.setRiskSummary(riskSummary);
        result.setSource(source);
        return result;
    }

    public static BbsModerationCheckResult review(String message, String riskSummary, String source)
    {
        BbsModerationCheckResult result = new BbsModerationCheckResult();
        result.setPassed(true);
        result.setAction(ACTION_REVIEW);
        result.setMessage(message);
        result.setRiskSummary(riskSummary);
        result.setSource(source);
        return result;
    }

    public boolean isPassed()
    {
        return passed;
    }

    public void setPassed(boolean passed)
    {
        this.passed = passed;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public List<String> getWords()
    {
        return words;
    }

    public void setWords(List<String> words)
    {
        this.words = words;
    }

    public String getRiskSummary()
    {
        return riskSummary;
    }

    public void setRiskSummary(String riskSummary)
    {
        this.riskSummary = riskSummary;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getModerationToken()
    {
        return moderationToken;
    }

    public void setModerationToken(String moderationToken)
    {
        this.moderationToken = moderationToken;
    }

    public String getSignature()
    {
        return signature;
    }

    public void setSignature(String signature)
    {
        this.signature = signature;
    }
}
