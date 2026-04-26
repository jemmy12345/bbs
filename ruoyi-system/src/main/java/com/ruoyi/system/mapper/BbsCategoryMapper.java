package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsCategory;

/**
 * 论坛分类表 数据层
 * 
 * @author ruoyi
 */
public interface BbsCategoryMapper
{
    /**
     * 查询分类信息
     * 
     * @param categoryId 分类ID
     * @return 分类信息
     */
    public BbsCategory selectBbsCategoryById(Long categoryId);

    /**
     * 查询分类列表
     * 
     * @param bbsCategory 分类信息
     * @return 分类集合
     */
    public List<BbsCategory> selectBbsCategoryList(BbsCategory bbsCategory);

    /**
     * 新增分类
     * 
     * @param bbsCategory 分类信息
     * @return 结果
     */
    public int insertBbsCategory(BbsCategory bbsCategory);

    /**
     * 修改分类
     * 
     * @param bbsCategory 分类信息
     * @return 结果
     */
    public int updateBbsCategory(BbsCategory bbsCategory);

    /**
     * 删除分类
     * 
     * @param categoryId 分类ID
     * @return 结果
     */
    public int deleteBbsCategoryById(Long categoryId);

    /**
     * 批量删除分类
     * 
     * @param categoryIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBbsCategoryByIds(Long[] categoryIds);

    /**
     * 增加分类帖子数
     * 
     * @param categoryId 分类ID
     * @return 结果
     */
    public int incrementPostCount(Long categoryId);

    /**
     * 减少分类帖子数
     * 
     * @param categoryId 分类ID
     * @return 结果
     */
    public int decrementPostCount(Long categoryId);

    /**
     * 查询热门分类列表（按帖子数排序）
     * 
     * @return 分类集合
     */
    public List<BbsCategory> selectHotCategoryList();
}
