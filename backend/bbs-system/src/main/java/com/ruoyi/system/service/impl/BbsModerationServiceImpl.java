package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.system.domain.BbsModerationCheckResult;
import com.ruoyi.system.domain.ai.BbsAiModerationResult;
import com.ruoyi.system.service.IBbsAiService;
import com.ruoyi.system.service.IBbsModerationService;
import com.ruoyi.system.service.IBbsSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 本地规则、企业敏感词和AI审核合并成同一套风控结果。
 */
@Service
public class BbsModerationServiceImpl implements IBbsModerationService
{
    private static final String ACTION_BLOCK = BbsModerationCheckResult.ACTION_BLOCK;
    private static final String ACTION_REVIEW = BbsModerationCheckResult.ACTION_REVIEW;
    private static final String TOKEN_PREFIX = "bbs:moderation:token:";
    private static final int TOKEN_TTL_SECONDS = 180;

    private static final String[] LOCAL_RISK_PHRASES = {
        "我要打人", "想打人", "打人", "打他", "打她", "打死", "揍他", "揍她", "弄死",
        "杀了", "砍人", "威胁", "恐吓", "霸凌", "人身攻击", "harass", "threaten",
        "cyberbullying", "bullying"
    };

    @Autowired
    private IBbsSensitiveWordService bbsSensitiveWordService;

    @Autowired
    private IBbsAiService bbsAiService;

    @Autowired
    private RedisCache redisCache;

    @Override
    public BbsModerationCheckResult precheck(String scene, String title, String content)
    {
        BbsModerationCheckResult result = check(scene, title, content);
        attachToken(scene, title, content, result);
        return result;
    }

    @Override
    public BbsModerationCheckResult checkForSubmit(String scene, String title, String content, String moderationToken)
    {
        String signature = signature(scene, title, content);
        if (StringUtils.isNotEmpty(moderationToken))
        {
            BbsModerationCheckResult cached = redisCache.getCacheObject(TOKEN_PREFIX + moderationToken);
            if (cached != null && signature.equals(cached.getSignature()))
            {
                return cached;
            }
        }
        return check(scene, title, content);
    }

    private BbsModerationCheckResult check(String scene, String title, String content)
    {
        String safeScene = StringUtils.defaultIfEmpty(scene, "post");
        String plainContent = extractPlainText(content);
        String checkText = (StringUtils.defaultString(title) + " " + plainContent).trim();
        if (StringUtils.isEmpty(checkText))
        {
            return BbsModerationCheckResult.allow();
        }

        List<String> localHits = findLocalRiskPhrases(checkText);
        if (!localHits.isEmpty())
        {
            String reason = "内容包含暴力、威胁或人身攻击的不当表达";
            String submitMessage = targetName(safeScene) + "包含暴力、威胁或人身攻击的不当表达，存在风险，请修改后重新提交";
            if (isPost(safeScene))
            {
                return BbsModerationCheckResult.review(
                    reason + "，发布后将进入管理员审核。",
                    reason,
                    "local-rule"
                );
            }
            return BbsModerationCheckResult.block(
                submitMessage,
                localHits,
                reason,
                "local-rule"
            );
        }

        List<String> sensitiveWords = bbsSensitiveWordService.checkSensitiveWords(checkText);
        if (sensitiveWords != null && !sensitiveWords.isEmpty())
        {
            return BbsModerationCheckResult.block(
                targetName(safeScene) + "包含敏感词：" + String.join("、", sensitiveWords) + "，请修改后重新提交",
                sensitiveWords,
                null,
                "sensitive-word"
            );
        }

        try
        {
            BbsAiModerationResult moderation = isPost(safeScene)
                ? bbsAiService.moderatePost(title, plainContent)
                : bbsAiService.moderateComment(title, plainContent);
            if (moderation != null && moderation.isRisk())
            {
                String reason = StringUtils.isEmpty(moderation.getRiskSummary())
                    ? "AI识别内容存在潜在风险"
                    : moderation.getRiskSummary();
                if (isPost(safeScene))
                {
                    return BbsModerationCheckResult.review("AI检测到内容存在潜在风险，发布后将进入管理员审核。", reason, "ai");
                }
                return BbsModerationCheckResult.block(reason + "，请修改后重新提交", null, reason, "ai");
            }
        }
        catch (Exception e)
        {
            if (isPost(safeScene))
            {
                return BbsModerationCheckResult.review(
                    "AI审核服务异常，发布后将进入管理员人工复核。",
                    "AI审核服务异常，请管理员人工复核",
                    "ai-exception"
                );
            }
            return BbsModerationCheckResult.block("AI审核服务异常，请稍后重试或联系管理员", null, null, "ai-exception");
        }

        return BbsModerationCheckResult.allow();
    }

    private void attachToken(String scene, String title, String content, BbsModerationCheckResult result)
    {
        String token = UUID.randomUUID().toString().replace("-", "");
        result.setModerationToken(token);
        result.setSignature(signature(scene, title, content));
        redisCache.setCacheObject(TOKEN_PREFIX + token, result, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String signature(String scene, String title, String content)
    {
        String userId;
        try
        {
            userId = SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            userId = "";
        }
        return Md5Utils.hash(StringUtils.defaultString(userId) + "|"
            + StringUtils.defaultIfEmpty(scene, "post") + "|"
            + StringUtils.defaultString(title) + "|"
            + extractPlainText(content));
    }

    private List<String> findLocalRiskPhrases(String text)
    {
        String normalized = text.toLowerCase(Locale.ROOT)
            .replaceAll("[\\s\\p{Punct}，。！？、；：“”‘’（）【】《》…]+", "");
        List<String> hits = new ArrayList<>();
        for (String phrase : LOCAL_RISK_PHRASES)
        {
            String normalizedPhrase = phrase.toLowerCase(Locale.ROOT);
            if (normalized.contains(normalizedPhrase))
            {
                hits.add(phrase);
            }
        }
        return hits;
    }

    private boolean isPost(String scene)
    {
        return "post".equalsIgnoreCase(scene);
    }

    private String targetName(String scene)
    {
        if (isPost(scene))
        {
            return "帖子内容";
        }
        return "reply".equalsIgnoreCase(scene) ? "回复内容" : "评论内容";
    }

    @Override
    public String extractPlainText(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return "";
        }
        return content.replaceAll("<[^>]+>", " ")
            .replace("&nbsp;", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}
