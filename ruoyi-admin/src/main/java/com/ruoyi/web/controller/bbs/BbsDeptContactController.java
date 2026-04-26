package com.ruoyi.web.controller.bbs;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsDeptContact;
import com.ruoyi.system.service.IBbsDeptContactService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 部门接口人Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/bbs/deptContact")
@Api(tags = "部门接口人")
public class BbsDeptContactController extends BaseController
{
    @Autowired
    private IBbsDeptContactService bbsDeptContactService;

    /**
     * 查询部门接口人列表
     */
    @ApiOperation("查询部门接口人列表")
    @PreAuthorize("@ss.hasPermi('bbs:deptContact:list')")
    @GetMapping("/list")
    public TableDataInfo list(BbsDeptContact bbsDeptContact)
    {
        startPage();
        List<BbsDeptContact> list = bbsDeptContactService.selectBbsDeptContactList(bbsDeptContact);
        return getDataTable(list);
    }

    /**
     * 导出部门接口人列表
     */
    @ApiOperation("导出部门接口人列表")
    @PreAuthorize("@ss.hasPermi('bbs:deptContact:export')")
    @Log(title = "部门接口人", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BbsDeptContact bbsDeptContact)
    {
        List<BbsDeptContact> list = bbsDeptContactService.selectBbsDeptContactList(bbsDeptContact);
        ExcelUtil<BbsDeptContact> util = new ExcelUtil<BbsDeptContact>(BbsDeptContact.class);
        util.exportExcel(response, list, "部门接口人数据");
    }

    /**
     * 获取部门接口人详细信息
     */
    @ApiOperation("获取部门接口人详细信息")
    @PreAuthorize("@ss.hasPermi('bbs:deptContact:query')")
    @GetMapping(value = "/{contactId}")
    public AjaxResult getInfo(@PathVariable("contactId") Long contactId)
    {
        return success(bbsDeptContactService.selectBbsDeptContactById(contactId));
    }

    /**
     * 根据部门ID获取部门接口人信息
     */
    @ApiOperation("根据部门ID获取部门接口人信息")
    @GetMapping(value = "/dept/{deptId}")
    public AjaxResult getInfoByDeptId(@PathVariable("deptId") Long deptId)
    {
        return success(bbsDeptContactService.selectBbsDeptContactByDeptId(deptId));
    }

    /**
     * 判断当前用户是否是某个部门的接口人
     */
    @ApiOperation("判断当前用户是否是某个部门的接口人")
    @GetMapping(value = "/check/{deptId}")
    public AjaxResult checkIsDeptContact(@PathVariable("deptId") Long deptId)
    {
        try
        {
            String currentUserId = SecurityUtils.getUserId();
            BbsDeptContact deptContact = bbsDeptContactService.selectBbsDeptContactByDeptId(deptId);
            
            boolean isContact = false;
            if (deptContact != null && "0".equals(deptContact.getStatus()))
            {
                String contactUserId = deptContact.getContactUserId();
                if (StringUtils.isNotEmpty(contactUserId) && contactUserId.equals(currentUserId))
                {
                    isContact = true;
                }
            }
            
            return success(isContact);
        }
        catch (Exception e)
        {
            // 如果用户未登录或其他异常，返回false
            return success(false);
        }
    }

    /**
     * 新增部门接口人
     */
    @ApiOperation("新增部门接口人")
    @PreAuthorize("@ss.hasPermi('bbs:deptContact:add')")
    @Log(title = "部门接口人", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BbsDeptContact bbsDeptContact)
    {
        bbsDeptContact.setCreateBy(getUsername());
        return toAjax(bbsDeptContactService.insertBbsDeptContact(bbsDeptContact));
    }

    /**
     * 修改部门接口人
     */
    @ApiOperation("修改部门接口人")
    @PreAuthorize("@ss.hasPermi('bbs:deptContact:edit')")
    @Log(title = "部门接口人", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BbsDeptContact bbsDeptContact)
    {
        bbsDeptContact.setUpdateBy(getUsername());
        return toAjax(bbsDeptContactService.updateBbsDeptContact(bbsDeptContact));
    }

    /**
     * 删除部门接口人
     */
    @ApiOperation("删除部门接口人")
    @PreAuthorize("@ss.hasPermi('bbs:deptContact:remove')")
    @Log(title = "部门接口人", businessType = BusinessType.DELETE)
	@DeleteMapping("/{contactIds}")
    public AjaxResult remove(@PathVariable Long[] contactIds)
    {
        return toAjax(bbsDeptContactService.deleteBbsDeptContactByIds(contactIds));
    }
}
