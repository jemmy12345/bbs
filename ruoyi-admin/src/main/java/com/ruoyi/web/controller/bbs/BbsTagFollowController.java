package com.ruoyi.web.controller.bbs;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.IBbsTagFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签订阅
 */
@RestController
@RequestMapping("/bbs/tag/follow")
@Api(tags = "标签订阅")
public class BbsTagFollowController extends BaseController
{
    @Autowired
    private IBbsTagFollowService bbsTagFollowService;

    /**
     * 切换订阅/取消订阅
     */
    @ApiOperation("切换标签订阅状态")
    @PostMapping("/{tagId}")
    public AjaxResult toggle(@PathVariable Long tagId)
    {
        String userId = SecurityUtils.getUserId();
        boolean following = bbsTagFollowService.toggleFollow(userId, tagId);
        Map<String, Object> result = new HashMap<>();
        result.put("following", following);
        return success(result);
    }

    /**
     * 查询某标签是否已订阅
     */
    @ApiOperation("查询标签订阅状态")
    @GetMapping("/status/{tagId}")
    public AjaxResult status(@PathVariable Long tagId)
    {
        String userId = SecurityUtils.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("following", bbsTagFollowService.isFollowing(userId, tagId));
        return success(result);
    }

    /**
     * 获取当前用户所有订阅的标签ID列表
     */
    @ApiOperation("我的标签订阅列表")
    @GetMapping("/my")
    public AjaxResult myFollowed()
    {
        String userId = SecurityUtils.getUserId();
        List<Long> tagIds = bbsTagFollowService.getFollowedTagIds(userId);
        return success(tagIds);
    }
}
