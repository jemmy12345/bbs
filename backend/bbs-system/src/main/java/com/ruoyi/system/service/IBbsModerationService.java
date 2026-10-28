package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsModerationCheckResult;

/**
 * 论坛发帖、评论、回复统一风控服务。
 */
public interface IBbsModerationService
{
    BbsModerationCheckResult precheck(String scene, String title, String content);

    BbsModerationCheckResult checkForSubmit(String scene, String title, String content, String moderationToken);

    String extractPlainText(String content);
}
