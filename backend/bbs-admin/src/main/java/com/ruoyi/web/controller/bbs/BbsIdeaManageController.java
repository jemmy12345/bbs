package com.ruoyi.web.controller.bbs;

import com.ruoyi.common.annotation.AdminLog;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.AdminOperationType;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.BbsIdeaProposal;
import com.ruoyi.system.service.IBbsIdeaService;
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 提案管理（管理端）：分页查询、详情、导出、逻辑删除
 */
@RestController
@RequestMapping("/bbs/ideaManage")
@Api(tags = "提案管理")
public class BbsIdeaManageController extends BaseController
{
    @Autowired
    private IBbsIdeaService bbsIdeaService;

    @Autowired
    private ISysConfigService sysConfigService;

    /** 提案管理仅限系统配置的管理员使用 */
    private boolean isAdmin()
    {
        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        return SecurityUtils.isConfigAdmin(adminConfig, SecurityUtils.getLoginUser().getUser());
    }

    @ApiOperation("分页查询提案")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String submitNickName)
    {
        if (!isAdmin())
        {
            throw new com.ruoyi.common.exception.ServiceException("无权限操作");
        }
        startPage();
        return getDataTable(bbsIdeaService.listIdeasForManage(status, category, keyword, submitNickName));
    }

    @ApiOperation("提案详情")
    @GetMapping("/{ideaId}")
    public AjaxResult detail(@PathVariable Long ideaId)
    {
        if (!isAdmin())
        {
            return error("无权限操作");
        }
        return success(bbsIdeaService.getIdeaForManage(ideaId));
    }

    @ApiOperation("导出提案")
    @Log(title = "提案管理", businessType = BusinessType.EXPORT)
    @AdminLog(module = "提案管理", operationType = AdminOperationType.EXPORT, description = "导出提案列表")
    @PostMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String submitNickName)
    {
        if (!isAdmin())
        {
            throw new com.ruoyi.common.exception.ServiceException("无权限操作");
        }
        List<BbsIdeaProposal> list = bbsIdeaService.listIdeasForManage(status, category, keyword, submitNickName);
        ExcelUtil<BbsIdeaProposal> util = new ExcelUtil<BbsIdeaProposal>(BbsIdeaProposal.class);
        util.exportExcel(response, list, "提案数据", "提案数据");
    }

    @ApiOperation("删除提案（逻辑删除）")
    @Log(title = "提案管理", businessType = BusinessType.DELETE)
    @AdminLog(module = "提案管理", operationType = AdminOperationType.DELETE, description = "管理员删除提案")
    @DeleteMapping("/{ideaIds}")
    public AjaxResult remove(@PathVariable Long[] ideaIds)
    {
        if (!isAdmin())
        {
            return error("无权限操作");
        }
        return toAjax(bbsIdeaService.deleteIdeas(ideaIds));
    }

    @ApiOperation("取消公示")
    @Log(title = "提案管理", businessType = BusinessType.UPDATE)
    @AdminLog(module = "提案管理", operationType = AdminOperationType.OFFLINE, description = "管理员取消提案公示")
    @PutMapping("/{ideaId}/cancelPublicize")
    public AjaxResult cancelPublicize(@PathVariable Long ideaId)
    {
        if (!isAdmin())
        {
            return error("无权限操作");
        }
        bbsIdeaService.cancelPublicize(ideaId);
        return success();
    }
}
