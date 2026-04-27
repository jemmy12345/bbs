package com.ruoyi.web.controller.bbs;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.IBbsCheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 每日签到
 */
@RestController
@RequestMapping("/bbs/checkin")
@Api(tags = "每日签到")
public class BbsCheckinController extends BaseController
{
    @Autowired
    private IBbsCheckinService bbsCheckinService;

    /**
     * 查询今日签到状态（是否已签到 + 当前连续天数）
     */
    @ApiOperation("今日签到状态")
    @GetMapping("/today")
    public AjaxResult todayStatus()
    {
        String userId = SecurityUtils.getUserId();
        return success(bbsCheckinService.getTodayStatus(userId));
    }

    /**
     * 执行签到
     */
    @ApiOperation("执行签到")
    @PostMapping
    public AjaxResult doCheckin()
    {
        String userId = SecurityUtils.getUserId();
        return success(bbsCheckinService.doCheckin(userId));
    }
}
