package com.ruoyi.web.controller.bbs;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.AdminLog;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.AdminOperationType;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.BbsAdminLog;
import com.ruoyi.system.service.IBbsAdminLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 管理员操作日志Controller
 * 
 * @author simonyang
 * @date 2026-04-10
 */
@RestController
@RequestMapping("/bbs/BbsAdminLog")
@Api(tags = "管理员操作日志")
public class BbsAdminLogController extends BaseController
{
    @Autowired
    private IBbsAdminLogService bbsAdminLogService;

    /**
     * 查询管理员操作日志列表
     */
    @ApiOperation(value = "查询管理员操作日志列表")
    @GetMapping("/list")
    public TableDataInfo list(BbsAdminLog bbsAdminLog)
    {
        startPage();
        List<BbsAdminLog> list = bbsAdminLogService.selectBbsAdminLogList(bbsAdminLog);
        return getDataTable(list);
    }

    /**
     * 导出管理员操作日志列表
     */
    @ApiOperation(value = "导出管理员操作日志列表")
    @Log(title = "管理员操作日志", businessType = BusinessType.EXPORT)
    @AdminLog(module = "管理员日志管理", operationType = AdminOperationType.EXPORT, description = "导出管理员操作日志")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BbsAdminLog bbsAdminLog)
    {
        List<BbsAdminLog> list = bbsAdminLogService.selectBbsAdminLogList(bbsAdminLog);
        ExcelUtil<BbsAdminLog> util = new ExcelUtil<BbsAdminLog>(BbsAdminLog.class);
        util.exportExcel(response, list, "管理员操作日志数据", "管理员操作日志数据");
    }

}
