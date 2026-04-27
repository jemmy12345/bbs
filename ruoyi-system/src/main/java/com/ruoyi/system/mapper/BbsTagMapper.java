package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsTag;

/**
 * 标签表 数据层
 * 
 * @author ruoyi
 */
public interface BbsTagMapper
{
    /**
     * 查询标签信息
     * 
     * @param tagId 标签ID
     * @return 标签信息
     */
    public BbsTag selectBbsTagById(Long tagId);

    /**
     * 查询标签信息
     * 
     * @param tagName 标签名称
     * @return 标签信息
     */
    public BbsTag selectBbsTagByName(String tagName);

    /**
     * 查询标签列表
     * 
     * @param bbsTag 标签信息
     * @return 标签集合
     */
    public List<BbsTag> selectBbsTagList(BbsTag bbsTag);

    /**
     * 新增标签
     * 
     * @param bbsTag 标签信息
     * @return 结果
     */
    public int insertBbsTag(BbsTag bbsTag);

    /**
     * 修改标签
     * 
     * @param bbsTag 标签信息
     * @return 结果
     */
    public int updateBbsTag(BbsTag bbsTag);

    /**
     * 删除标签
     * 
     * @param tagId 标签ID
     * @return 结果
     */
    public int deleteBbsTagById(Long tagId);

    /**
     * 批量删除标签
     * 
     * @param tagIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBbsTagByIds(Long[] tagIds);

    /**
     * 增加使用次数
     * 
     * @param tagId 标签ID
     * @return 结果
     */
    public int incrementUseCount(Long tagId);

    /**
     * 减少使用次数
     * 
     * @param tagId 标签ID
     * @return 结果
     */
    public int decrementUseCount(Long tagId);

    /**
     * 根据帖子ID查询标签列表
     * 
     * @param postId 帖子ID
     * @return 标签集合
     */
    public List<BbsTag> selectTagsByPostId(Long postId);

    /**
     * 查询热门标签
     *
     * @param limit 限制数量
     * @return 标签集合
     */
    public List<BbsTag> selectHotTags(Integer limit);
}
