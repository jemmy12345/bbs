package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsDislike;

/**
 * 点踩Service接口
 *
 * @author simonyang
 * @date 2026-04-08
 */
public interface IBbsDislikeService {
  /**
   * 点踩 or 取消点踩
   *
   * @param bbsDislike
   * @return
   */
  public int dislike(BbsDislike bbsDislike);
}
