package com.ruoyi.system.service;

import com.ruoyi.system.domain.bo.CommentManageBo;
import com.ruoyi.system.domain.bo.CommentManageQueryBo;
import com.ruoyi.system.domain.vo.CommentManageVo;

import java.util.List;

public interface IBbsCommentManageService {
  /**
   * 查询评论管理列表
   *
   * @param commentManageQueryBo
   * @return
   */
  public List<CommentManageVo> list(CommentManageQueryBo commentManageQueryBo);

  /**
   * 状态变更
   *
   * @param commentManageBo
   */
  public int setDelFlag(CommentManageBo commentManageBo);
}
