package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsIdeaStageReviewerConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IBbsIdeaStageReviewerConfigService
{
    BbsIdeaStageReviewerConfig selectBbsIdeaStageReviewerConfigById(Long configId);

    List<BbsIdeaStageReviewerConfig> selectBbsIdeaStageReviewerConfigList(BbsIdeaStageReviewerConfig config);

    int insertBbsIdeaStageReviewerConfig(BbsIdeaStageReviewerConfig config);

    int updateBbsIdeaStageReviewerConfig(BbsIdeaStageReviewerConfig config);

    int deleteBbsIdeaStageReviewerConfigByIds(Long[] configIds);

    boolean canReviewStage(String stageCode, Collection<String> currentRoleKeys, boolean isAdmin);

    boolean canReviewStageByUserId(String stageCode, String userId, Collection<String> currentRoleKeys, boolean isAdmin);

    boolean canReviewAnyStage(Collection<String> currentRoleKeys, boolean isAdmin);

    Map<String, Object> buildCurrentPermission(Collection<String> currentRoleKeys, boolean isAdmin);

    /** 获取某环节配置的所有处理人用户ID列表（不含基于角色的） */
    List<String> getReviewerUserIdsForStage(String stageCode);
}