package com.ruoyi.system.service;

import java.util.List;

/**
 * 标签订阅 Service 接口
 */
public interface IBbsTagFollowService
{
    /**
     * 切换订阅状态（已订阅则取消，未订阅则订阅）
     * @return true=当前已订阅, false=当前已取消
     */
    boolean toggleFollow(String userId, Long tagId);

    /**
     * 查询用户是否已订阅某标签
     */
    boolean isFollowing(String userId, Long tagId);

    /**
     * 获取用户所有订阅的标签ID列表
     */
    List<Long> getFollowedTagIds(String userId);

    /**
     * 获取订阅了某标签的所有用户ID列表（用于发通知）
     */
    List<String> getFollowerUserIds(Long tagId);
}
