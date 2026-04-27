package com.ruoyi.system.service;

import com.ruoyi.system.domain.ai.BbsAiModerationResult;

/**
 * BBS AI能力服务
 */
public interface IBbsAiService
{
    /**
     * 帖子内容风险审核
     *
     * @param title 标题
     * @param content 内容
     * @return 审核结果
     */
    BbsAiModerationResult moderatePost(String title, String content);

    /**
     * AI助写帖子内容
     *
     * @param keywords 关键词
     * @param postType 帖子类型
     * @return 生成内容
     */
    String generatePostContent(String keywords, String postType);

}
