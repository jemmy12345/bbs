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

    List<Map<String, Object>> getTopPointUsers(Integer limit);
}
