package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.BbsCategory;
import com.ruoyi.system.mapper.BbsCategoryMapper;
import com.ruoyi.system.service.IBbsCategoryService;

@Service
public class BbsCategoryServiceImpl implements IBbsCategoryService
{
    @Autowired
    private BbsCategoryMapper bbsCategoryMapper;

    @Override
    public List<BbsCategory> selectBbsCategoryList(BbsCategory bbsCategory)
    {
        return bbsCategoryMapper.selectBbsCategoryList(bbsCategory);
    }

    @Override
    public BbsCategory selectBbsCategoryById(Long categoryId)
    {
        return bbsCategoryMapper.selectBbsCategoryById(categoryId);
    }

    @Override
    public int insertBbsCategory(BbsCategory bbsCategory)
    {
        return bbsCategoryMapper.insertBbsCategory(bbsCategory);
    }

    @Override
    public int updateBbsCategory(BbsCategory bbsCategory)
    {
        return bbsCategoryMapper.updateBbsCategory(bbsCategory);
    }

    @Override
    public int deleteBbsCategoryByIds(Long[] categoryIds)
    {
        return bbsCategoryMapper.deleteBbsCategoryByIds(categoryIds);
    }

    @Override
    public List<BbsCategory> selectHotCategoryList()
    {
        return bbsCategoryMapper.selectHotCategoryList();
    }
}
