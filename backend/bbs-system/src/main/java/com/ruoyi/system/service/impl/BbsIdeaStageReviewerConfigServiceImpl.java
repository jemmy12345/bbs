package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsIdeaStageReviewerConfig;
import com.ruoyi.system.mapper.BbsIdeaStageReviewerConfigMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsIdeaStageReviewerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BbsIdeaStageReviewerConfigServiceImpl implements IBbsIdeaStageReviewerConfigService
{
    @Autowired
    private BbsIdeaStageReviewerConfigMapper configMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public BbsIdeaStageReviewerConfig selectBbsIdeaStageReviewerConfigById(Long configId)
    {
        return configMapper.selectBbsIdeaStageReviewerConfigById(configId);
    }

    @Override
    public List<BbsIdeaStageReviewerConfig> selectBbsIdeaStageReviewerConfigList(BbsIdeaStageReviewerConfig config)
    {
        return configMapper.selectBbsIdeaStageReviewerConfigList(config);
    }

    @Override
    public int insertBbsIdeaStageReviewerConfig(BbsIdeaStageReviewerConfig config)
    {
        validateConfig(config, true);
        return configMapper.insertBbsIdeaStageReviewerConfig(config);
    }

    @Override
    public int updateBbsIdeaStageReviewerConfig(BbsIdeaStageReviewerConfig config)
    {
        validateConfig(config, false);
        return configMapper.updateBbsIdeaStageReviewerConfig(config);
    }

    @Override
    public int deleteBbsIdeaStageReviewerConfigByIds(Long[] configIds)
    {
        return configMapper.deleteBbsIdeaStageReviewerConfigByIds(configIds);
    }

    @Override
    public boolean canReviewStage(String stageCode, Collection<String> currentRoleKeys, boolean isAdmin)
    {
        if (isAdmin)
        {
            return true;
        }
        if (StringUtils.isBlank(stageCode))
        {
            return false;
        }
        BbsIdeaStageReviewerConfig config = configMapper.selectByStageCode(stageCode);
        return matchConfig(config, currentRoleKeys);
    }

    @Override
    public boolean canReviewStageByUserId(String stageCode, String userId, Collection<String> currentRoleKeys, boolean isAdmin)
    {
        if (isAdmin)
        {
            return true;
        }
        if (StringUtils.isBlank(stageCode) || StringUtils.isBlank(userId))
        {
            return false;
        }
        BbsIdeaStageReviewerConfig config = configMapper.selectByStageCode(stageCode);
        if (config == null || !"0".equals(config.getStatus()))
        {
            return false;
        }
        // The config page assigns concrete reviewers. Roles only define candidate scope.
        if (StringUtils.isNotBlank(config.getReviewerUserIds()))
        {
            for (String uid : config.getReviewerUserIds().split(","))
            {
                if (userId.equals(uid.trim()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getReviewerUserIdsForStage(String stageCode)
    {
        BbsIdeaStageReviewerConfig config = configMapper.selectByStageCode(stageCode);
        if (config == null || !"0".equals(config.getStatus()))
        {
            return new ArrayList<>();
        }
        java.util.LinkedHashSet<String> idSet = new java.util.LinkedHashSet<>();
        if (StringUtils.isNotBlank(config.getReviewerUserIds()))
        {
            for (String uid : config.getReviewerUserIds().split(","))
            {
                String trimmed = uid.trim();
                if (StringUtils.isNotBlank(trimmed))
                {
                    idSet.add(trimmed);
                }
            }
        }
        return new ArrayList<>(idSet);
    }

    @Override
    public List<SysUser> getReviewerCandidatesForStage(String stageCode)
    {
        return sysUserMapper.selectUsersByRoleKey(roleKeyForStage(stageCode));
    }

    @Override
    public boolean canReviewAnyStage(String userId, Collection<String> currentRoleKeys, boolean isAdmin)
    {
        if (isAdmin)
        {
            return true;
        }
        List<BbsIdeaStageReviewerConfig> configs = configMapper.selectEnabledConfigs();
        for (BbsIdeaStageReviewerConfig config : configs)
        {
            if (matchExplicitUser(config, userId))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> buildCurrentPermission(String userId, Collection<String> currentRoleKeys, boolean isAdmin)
    {
        Map<String, Object> data = new HashMap<>();
        List<String> reviewStages = new ArrayList<>();
        List<BbsIdeaStageReviewerConfig> configs = configMapper.selectEnabledConfigs();
        for (BbsIdeaStageReviewerConfig config : configs)
        {
            if (matchExplicitUser(config, userId) || isAdmin)
            {
                reviewStages.add(config.getStageCode());
            }
        }
        data.put("reviewStages", reviewStages);
        data.put("canReviewAny", isAdmin || !reviewStages.isEmpty());
        data.put("canManageFlow", isAdmin || reviewStages.contains("L3"));
        return data;
    }

    private boolean matchConfig(BbsIdeaStageReviewerConfig config, Collection<String> currentRoleKeys)
    {
        if (config == null || !"0".equals(config.getStatus()) || currentRoleKeys == null || currentRoleKeys.isEmpty()
                || StringUtils.isBlank(config.getRoleKeys()))
        {
            return false;
        }
        String[] roleKeys = config.getRoleKeys().split(",");
        for (String roleKey : roleKeys)
        {
            String trimmed = roleKey == null ? null : roleKey.trim();
            if (StringUtils.isNotBlank(trimmed) && currentRoleKeys.contains(trimmed))
            {
                return true;
            }
        }
        return false;
    }

    private boolean matchExplicitUser(BbsIdeaStageReviewerConfig config, String userId)
    {
        if (config == null || !"0".equals(config.getStatus()) || StringUtils.isBlank(userId)
                || StringUtils.isBlank(config.getReviewerUserIds()))
        {
            return false;
        }
        for (String uid : config.getReviewerUserIds().split(","))
        {
            if (userId.equals(uid.trim()))
            {
                return true;
            }
        }
        return false;
    }

    private void validateConfig(BbsIdeaStageReviewerConfig config, boolean insert)
    {
        if (config == null)
        {
            throw new ServiceException("评审配置不能为空");
        }
        if (StringUtils.isBlank(config.getStageCode()))
        {
            throw new ServiceException("评审环节编码不能为空");
        }
        if (!"L1".equals(config.getStageCode()) && !"L2".equals(config.getStageCode()) && !"L3".equals(config.getStageCode()))
        {
            throw new ServiceException("仅支持 L1/L2/L3 三个评审环节");
        }
        String roleKey = roleKeyForStage(config.getStageCode());
        config.setRoleKeys(roleKey);
        if (StringUtils.isBlank(config.getStageName()))
        {
            config.setStageName(stageNameForStage(config.getStageCode()));
        }
        String reviewerUserIds = normalizeCsv(config.getReviewerUserIds());
        config.setReviewerUserIds(reviewerUserIds);
        if (StringUtils.isBlank(reviewerUserIds))
        {
            throw new ServiceException("请至少选择一名" + stageNameForStage(config.getStageCode()) + "处理人");
        }

        Set<String> allowedUserIds = new HashSet<>(sysUserMapper.selectUserIdsByRoleKey(roleKey));
        for (String uid : reviewerUserIds.split(","))
        {
            if (!allowedUserIds.contains(uid.trim()))
            {
                throw new ServiceException("指定处理人必须属于" + stageNameForStage(config.getStageCode()) + "角色");
            }
        }
        BbsIdeaStageReviewerConfig existed = configMapper.selectByStageCode(config.getStageCode());
        if (insert && existed != null)
        {
            throw new ServiceException("该评审环节已存在配置，请直接修改");
        }
        if (!insert && existed != null && !existed.getConfigId().equals(config.getConfigId()))
        {
            throw new ServiceException("该评审环节已存在其他配置");
        }
        if (StringUtils.isBlank(config.getStatus()))
        {
            config.setStatus("0");
        }
    }

    private String roleKeyForStage(String stageCode)
    {
        if ("L1".equals(stageCode))
        {
            return "idea_l1_reviewer";
        }
        if ("L2".equals(stageCode))
        {
            return "idea_l2_reviewer";
        }
        if ("L3".equals(stageCode))
        {
            return "idea_l3_reviewer";
        }
        throw new ServiceException("仅支持 L1/L2/L3 三个评审环节");
    }

    private String stageNameForStage(String stageCode)
    {
        if ("L1".equals(stageCode))
        {
            return "初审";
        }
        if ("L2".equals(stageCode))
        {
            return "复审";
        }
        if ("L3".equals(stageCode))
        {
            return "终审";
        }
        return "评审";
    }

    private String normalizeCsv(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return "";
        }
        java.util.LinkedHashSet<String> items = new java.util.LinkedHashSet<>();
        for (String item : value.split(","))
        {
            String trimmed = item == null ? "" : item.trim();
            if (StringUtils.isNotBlank(trimmed))
            {
                items.add(trimmed);
            }
        }
        return String.join(",", items);
    }
}
