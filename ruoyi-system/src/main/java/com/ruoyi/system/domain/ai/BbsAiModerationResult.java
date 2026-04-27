package com.ruoyi.system.domain.ai;

/**
 * AI内容审核结果
 */
public class BbsAiModerationResult
{
    /** 是否存在风险 */
    private boolean risk;

    /** 风险摘要 */
    private String riskSummary;

    /** 大模型原始输出 */
    private String rawResponse;

    public boolean isRisk()
    {
        return risk;
    }

    public void setRisk(boolean risk)
    {
        this.risk = risk;
    }

    public String getRiskSummary()
    {
        return riskSummary;
    }

    public void setRiskSummary(String riskSummary)
    {
        this.riskSummary = riskSummary;
    }

    public String getRawResponse()
    {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse)
    {
        this.rawResponse = rawResponse;
    }
}
