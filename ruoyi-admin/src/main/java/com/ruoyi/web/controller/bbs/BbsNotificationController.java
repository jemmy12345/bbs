package com.ruoyi.web.controller.bbs;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.BbsNotification;
import com.ruoyi.system.service.IBbsNotificationService;

/**
 * 通知 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/bbs/notification")
@Api(tags = "通知")
public class BbsNotificationController extends BaseController
{
    @Autowired
    private IBbsNotificationService bbsNotificationService;

    /**
     * 查询通知列表
     */
    @ApiOperation("查询通知列表")
    @GetMapping("/list")
    public TableDataInfo list(BbsNotification bbsNotification)
    {
        // 只查询当前用户的通知
        String userId = SecurityUtils.getUserId();
        String anonymousUser = bbsNotification.getUserId();
        if(anonymousUser != null){
            userId = anonymousUser;
        }
        bbsNotification.setUserId(userId);
        startPage();
        List<BbsNotification> list = bbsNotificationService.selectBbsNotificationList(bbsNotification);
        return getDataTable(list);
    }

    /**
     * 获取未读通知数
     */
    @ApiOperation("获取未读通知数")
    @GetMapping("/unread/count")
    public AjaxResult getUnreadCount(String code)
    {
        String userId = SecurityUtils.getUserId();
        if(code != null){
            userId = code;
        }
        int count = bbsNotificationService.countUnread(userId);
        return success(count);
    }

    /**
     * 标记为已读
     */
    @ApiOperation("标记为已读")
    @PostMapping("/read/{notificationId}")
    public AjaxResult markAsRead(@PathVariable Long notificationId)
    {
        bbsNotificationService.markAsRead(notificationId);
        return toAjax(true);
    }

    /**
     * 全部标记为已读， 传code则已读所有匿名
     */
    @ApiOperation("全部标记为已读")
    @PostMapping("/read/all")
    public AjaxResult markAllAsRead(String code)
    {
        String userId = SecurityUtils.getUserId();
        if(code != null){
            userId = code;
        }
        bbsNotificationService.markAllAsRead(userId);
        return toAjax(true);
    }

    /**
     * 获取通知详情
     */
    @ApiOperation("获取通知详情")
    @GetMapping("/{notificationId}")
    public AjaxResult getInfo(@PathVariable Long notificationId)
    {
        BbsNotification notification = bbsNotificationService.selectBbsNotificationById(notificationId);
        return success(notification);
    }
}
