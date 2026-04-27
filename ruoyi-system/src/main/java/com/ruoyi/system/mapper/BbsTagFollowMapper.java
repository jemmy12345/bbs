package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户标签订阅 Mapper
 */
public interface BbsTagFollowMapper
{
    /**
     * 查询用户是否订阅了某标签（返回 1 or 0）
     */
    int countByUserAndTag(@Param("userId") String userId, @Param("tagId") Long tagId);

    /**
     * 新增订阅
     */
    int insert(@Param("userId") String userId, @Param("tagId") Long tagId);

    /**
     * 取消订阅
     */
    int delete(@Param("userId") String userId, @Param("tagId") Long tagId);

    /**
     * 查询用户所有订阅的标签ID列表
     */
    List<Long> selectTagIdsByUser(@Param("userId") String userId);

    /**
     * 查询订阅了某标签的所有用户ID列表
     */
    List<String> selectUserIdsByTag(@Param("tagId") Long tagId);
}
