package com.ruoyi.system.mapper;

import java.util.List;

import com.ruoyi.system.domain.BbsDislike;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 点踩Mapper接口
 *
 * @author simonyang
 * @date 2026-04-08
 */
@Repository
public interface BbsDislikeMapper {
  /**
   * 查询点踩
   *
   * @param dislikeId 点踩主键
   * @return 点踩
   */
  public BbsDislike selectBbsDislikeByDislikeId(Long dislikeId);

  /**
   * 查询点踩列表
   *
   * @param bbsDislike 点踩
   * @return 点踩集合
   */
  public List<BbsDislike> selectBbsDislikeList(BbsDislike bbsDislike);

  /**
   * 新增点踩
   *
   * @param bbsDislike 点踩
   * @return 结果
   */
  public int insertBbsDislike(BbsDislike bbsDislike);

  /**
   * 批量新增点踩
   *
   * @param bbsDislikeList 点踩
   * @return 结果
   */
  public int batchInsert(List<BbsDislike> bbsDislikeList);

  /**
   * 修改点踩
   *
   * @param bbsDislike 点踩
   * @return 结果
   */
  public int updateBbsDislike(BbsDislike bbsDislike);

  /**
   * 删除点踩
   *
   * @param dislikeId 点踩主键
   * @return 结果
   */
  public int deleteBbsDislikeByDislikeId(Long dislikeId);

  /**
   * 批量删除点踩
   *
   * @param dislikeIds 需要删除的数据主键集合
   * @return 结果
   */
  public int deleteBbsDislikeByDislikeIds(Long[] dislikeIds);

  /**
   * 根据目标ID集合查询点踩列表
   *
   * @param targetIds 目标ID集合
   * @param targetType 目标类型
   * @return 点踩集合
   */
  public List<BbsDislike> selectBbsDislikeListByTargetIds(@Param("targetIds") List<Long> targetIds, @Param("targetType") String targetType);
}
