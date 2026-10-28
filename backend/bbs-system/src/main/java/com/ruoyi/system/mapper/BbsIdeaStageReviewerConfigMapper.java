package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BbsIdeaStageReviewerConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BbsIdeaStageReviewerConfigMapper
{
    BbsIdeaStageReviewerConfig selectBbsIdeaStageReviewerConfigById(@Param("configId") Long configId);

    List<BbsIdeaStageReviewerConfig> selectBbsIdeaStageReviewerConfigList(BbsIdeaStageReviewerConfig config);

    List<BbsIdeaStageReviewerConfig> selectEnabledConfigs();

    BbsIdeaStageReviewerConfig selectByStageCode(@Param("stageCode") String stageCode);

    int insertBbsIdeaStageReviewerConfig(BbsIdeaStageReviewerConfig config);

    int updateBbsIdeaStageReviewerConfig(BbsIdeaStageReviewerConfig config);

    int deleteBbsIdeaStageReviewerConfigByIds(@Param("configIds") Long[] configIds);
}