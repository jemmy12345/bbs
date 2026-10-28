package com.ruoyi.web.controller.bbs;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.BbsDislike;
import com.ruoyi.system.service.IBbsDislikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 点踩Controller
 *
 * @author simonyang
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/bbs/dislike")
@Api(tags = "点踩")
public class BbsDislikeController extends BaseController {
  @Autowired private IBbsDislikeService bbsDislikeService;

  /** 获取点踩详细信息 */
  @ApiOperation(value = "点踩/取消点踩")
  @PostMapping(value = "/dislike")
  public AjaxResult dislike(@RequestBody BbsDislike bbsDislike) {
    bbsDislikeService.dislike(bbsDislike);
    return AjaxResult.success();
  }
}
