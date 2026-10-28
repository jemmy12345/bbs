package com.ruoyi.web.controller.bbs;

import java.util.Map;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsModerationCheckResult;
import com.ruoyi.system.service.IBbsModerationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 论坛内容风控预检，统一前台发帖、评论和回复的提示体验。
 */
@RestController
@RequestMapping("/bbs/moderation")
@Api(tags = "论坛风控预检")
public class BbsModerationController extends BaseController
{
    @Autowired
    private IBbsModerationService bbsModerationService;

    @ApiOperation("内容发布前风控预检")
    @PostMapping("/precheck")
    public AjaxResult precheck(@RequestBody Map<String, String> body)
    {
        String scene = StringUtils.defaultString(body.get("scene"), "post");
        String title = StringUtils.defaultString(body.get("title"), "");
        BbsModerationCheckResult result = bbsModerationService.precheck(scene, title, body.get("content"));
        return success(result);
    }
}
