package com.ruoyi.web.controller.bbs;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.BbsPost;
import com.ruoyi.system.service.IBbsUserCenterService;

/**
 * 个人中心 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/bbs/usercenter")
@Api(tags = "个人中心")
public class BbsUserCenterController extends BaseController
{
    @Autowired
    private IBbsUserCenterService bbsUserCenterService;

    /**
     * 获取个人中心统计信息
     */
    @ApiOperation("获取个人中心统计信息")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        String userId = SecurityUtils.getUserId();
        Map<String, Object> stats = bbsUserCenterService.getUserCenterStats(userId);
        return success(stats);
    }

    /**
     * 查询我的帖子列表
     */
    @ApiOperation("查询我的帖子列表")
    @GetMapping("/posts")
    public TableDataInfo getMyPosts()
    {
        String userId = SecurityUtils.getUserId();
        startPage();
        List<BbsPost> list = bbsUserCenterService.selectUserPostList(userId);
        return getDataTable(list);
    }

    /**
     * 查询我的收藏列表
     */
    @ApiOperation("查询我的收藏列表")
    @GetMapping("/collects")
    public TableDataInfo getMyCollects()
    {
        String userId = SecurityUtils.getUserId();
        startPage();
        List<BbsPost> list = bbsUserCenterService.selectUserCollectList(userId);
        return getDataTable(list);
    }

    /**
     * 查询我的点赞列表
     */
    @ApiOperation("查询我的点赞列表")
    @GetMapping("/likes")
    public TableDataInfo getMyLikes()
    {
        String userId = SecurityUtils.getUserId();
        startPage();
        List<BbsPost> list = bbsUserCenterService.selectUserLikeList(userId);
        return getDataTable(list);
    }
}
