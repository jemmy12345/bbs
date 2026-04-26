package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BbsCategory;

/**
 * 论坛分类 服务层
 * 
 * @author ruoyi
 */
public interface IBbsCategoryService
{
    public List<BbsCategory> selectBbsCategoryList(BbsCategory bbsCategory);
    public BbsCategory selectBbsCategoryById(Long categoryId);
    public int insertBbsCategory(BbsCategory bbsCategory);
    public int updateBbsCategory(BbsCategory bbsCategory);
    public int deleteBbsCategoryByIds(Long[] categoryIds);
    public List<BbsCategory> selectHotCategoryList();
}
