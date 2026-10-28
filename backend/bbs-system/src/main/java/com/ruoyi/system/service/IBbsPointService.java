package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;

/**
 * Forum points service.
 */
public interface IBbsPointService
{
    void awardPostPublishPoints(String userId, Long postId);

    void awardCommentPublishPoints(String userId, Long commentId);

    /** 匿名帖子奖励积分：流水不记录帖子编号，避免通过积分流水反查匿名作者 */
    void awardAnonymousPostPublishPoints(String userId);

    /** 匿名评论奖励积分：流水不记录评论编号，避免通过积分流水反查匿名作者 */
    void awardAnonymousCommentPublishPoints(String userId);

    List<Map<String, Object>> getTopPointUsers(Integer limit);
}
