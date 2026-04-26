package com.ruoyi.system.mapper;

/**
 * 关注表 数据层
 * 
 * @author ruoyi
 */
public interface BbsFollowMapper
{
    /**
     * 查询关注信息
     * 
     * @param userId 用户ID
     * @param followUserId 被关注用户ID
     * @return 关注信息
     */
    public int selectBbsFollow(String userId, Long followUserId);

    /**
     * 新增关注
     * 
     * @param userId 用户ID
     * @param followUserId 被关注用户ID
     * @return 结果
     */
    public int insertBbsFollow(String userId, Long followUserId);

    /**
     * 删除关注
     * 
     * @param userId 用户ID
     * @param followUserId 被关注用户ID
     * @return 结果
     */
    public int deleteBbsFollow(String userId, Long followUserId);

    /**
     * 统计关注数
     * 
     * @param userId 用户ID
     * @return 关注数
     */
    public int countFollow(String userId);

    /**
     * 统计粉丝数
     * 
     * @param userId 用户ID
     * @return 粉丝数
     */
    public int countFans(String userId);
}
