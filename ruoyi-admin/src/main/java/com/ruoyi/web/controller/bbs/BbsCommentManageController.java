package com.ruoyi.web.controller.bbs;

import com.ruoyi.common.annotation.AdminLog;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.AdminOperationType;
import com.ruoyi.common.utils.AdminLogContext;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsComment;
import com.ruoyi.system.domain.bo.CommentManageBo;
import com.ruoyi.system.domain.bo.CommentManageQueryBo;
import com.ruoyi.system.domain.vo.CommentManageVo;
import com.ruoyi.system.mapper.BbsCommentMapper;
import com.ruoyi.system.service.IBbsCommentManageService;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/bbs/commentManage")
@Api(tags = "评论管理")
public class BbsCommentManageController extends BaseController {
  @Autowired private IBbsCommentManageService bbsCommentManageService;
  @Autowired private BbsCommentMapper bbsCommentMapper;

  /** 查询所有评论列表 */
  @ApiOperation("查询所有评论列表")
  @GetMapping("/list")
  public AjaxResult list(CommentManageQueryBo commentManageQueryBo) {
    startPage();
    List<CommentManageVo> list = bbsCommentManageService.list(commentManageQueryBo);
    return success(list);
  }

  /** 状态变更 */
  @ApiOperation("状态变更")
  @PostMapping("/setDelFlag")
  @AdminLog(module = "评论管理", operationType = AdminOperationType.UPDATE, description = "管理员变更评论状态")
  public AjaxResult setDelFlag(@RequestBody CommentManageBo commentManageBo) {
    // 查询评论信息，用于记录操作详情
    BbsComment comment = bbsCommentMapper.selectBbsCommentForAdminById(commentManageBo.getCommentId());
    if (comment != null)
    {
        String content = comment.getContent();
        // 去除HTML标签并截取摘要
        if (StringUtils.isNotEmpty(content))
        {
            content = content.replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ").trim();
            content = content.length() > 50 ? content.substring(0, 50) + "…" : content;
        }
        String action = "2".equals(commentManageBo.getCommentDelFlag()) ? "删除" : "恢复";
        AdminLogContext.setDescription(action + "评论：「" + content + "」");
    }
    bbsCommentManageService.setDelFlag(commentManageBo);
    return AjaxResult.success();
  }
}
