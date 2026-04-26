package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsCollect;
import com.ruoyi.system.domain.BbsPost;

/**
 * 收藏表 数据层
 * 
 * @author ruoyi
 */
public interface BbsCollectMapper
{
    /**
     * 查询收藏信息
     * 
     * @param bbsCollect 收藏信息
     * @return 收藏信息
     */
    public BbsCollect selectBbsCollect(BbsCollect bbsCollect);

    /**
     * 新增收藏
     * 
     * @param bbsCollect 收藏信息
     * @return 结果
     */
    public int insertBbsCollect(BbsCollect bbsCollect);

    /**
     * 删除收藏
     * 
     * @param bbsCollect 收藏信息
     * @return 结果
     */
    public int deleteBbsCollect(BbsCollect bbsCollect);

    /**
     * 查询用户收藏的帖子列表
     * 
     * @param userId 用户ID
     * @return 帖子集合
     */
    public List<BbsPost> selectCollectPostList(String userId);

    /**
     * 根据帖子ID集合查询收藏列表
     * 
     * @param postIds 帖子ID集合
     * @return 收藏集合
     */
    public List<BbsCollect> selectBbsCollectListByPostIds(List<Long> postIds);
}
