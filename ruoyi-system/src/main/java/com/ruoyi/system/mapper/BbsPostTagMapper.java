package com.ruoyi.system.mapper;

import java.util.List;

/**
 * 帖子标签关联表 数据层
 * 
 * @author ruoyi
 */
public interface BbsPostTagMapper
{
    /**
     * 新增帖子标签关联
     * 
     * @param postId 帖子ID
     * @param tagId 标签ID
     * @return 结果
     */
    public int insertBbsPostTag(Long postId, Long tagId);

    /**
     * 删除帖子标签关联
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    public int deleteBbsPostTagByPostId(Long postId);

    /**
     * 删除帖子标签关联
     * 
     * @param tagId 标签ID
     * @return 结果
     */
    public int deleteBbsPostTagByTagId(Long tagId);

    /**
     * 根据帖子ID查询标签ID列表
     * 
     * @param postId 帖子ID
     * @return 标签ID集合
     */
    public List<Long> selectTagIdsByPostId(Long postId);
}
