package com.ruoyi.web.controller.bbs;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.BbsCategory;
import com.ruoyi.system.service.IBbsCategoryService;

/**
 * 论坛分类 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/bbs/category")
@Api(tags = "论坛分类")
public class BbsCategoryController extends BaseController
{
    @Autowired
    private IBbsCategoryService bbsCategoryService;

    /**
     * 查询分类列表
     */
    @ApiOperation("查询分类列表")
    @GetMapping("/list")
    public AjaxResult list(BbsCategory bbsCategory)
    {
        List<BbsCategory> list = bbsCategoryService.selectBbsCategoryList(bbsCategory);
        return success(list);
    }

    /**
     * 根据分类编号获取详细信息
     */
    @ApiOperation("根据分类编号获取详细信息")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(bbsCategoryService.selectBbsCategoryById(categoryId));
    }

    /**
     * 新增分类
     */
    @ApiOperation("新增分类")
    @PreAuthorize("@ss.hasPermi('bbs:category:add')")
    @Log(title = "论坛分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BbsCategory bbsCategory)
    {
        return toAjax(bbsCategoryService.insertBbsCategory(bbsCategory));
    }

    /**
     * 修改分类
     */
    @ApiOperation("修改分类")
    @PreAuthorize("@ss.hasPermi('bbs:category:edit')")
    @Log(title = "论坛分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BbsCategory bbsCategory)
    {
        return toAjax(bbsCategoryService.updateBbsCategory(bbsCategory));
    }

    /**
     * 删除分类
     */
    @PreAuthorize("@ss.hasPermi('bbs:category:remove')")
    @Log(title = "论坛分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(bbsCategoryService.deleteBbsCategoryByIds(categoryIds));
    }

    /**
     * 获取热门分类列表
     */
    @ApiOperation("获取热门分类列表")
    @GetMapping("/hot")
    public AjaxResult getHotCategories()
    {
        List<BbsCategory> list = bbsCategoryService.selectHotCategoryList();
        return success(list);
    }
}
