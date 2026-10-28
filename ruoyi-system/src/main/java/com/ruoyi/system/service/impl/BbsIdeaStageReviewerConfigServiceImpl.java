package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
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
import java.util.List;
import java.util.Map;

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
        // check by explicit user IDs first
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
        // fallback: check by role
        return matchConfig(config, currentRoleKeys);
    }

    @Override
    public List<String> getReviewerUserIdsForStage(String stageCode)
    {
        BbsIdeaStageReviewerConfig config = configMapper.selectByStageCode(stageCode);
        if (config == null || !"0".equals(config.getStatus()))
        {
            return new ArrayList<>();
        }
        // Use LinkedHashSet to deduplicate while preserving order
        java.util.LinkedHashSet<String> idSet = new java.util.LinkedHashSet<>();
        // 1. explicit user IDs
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
        // 2. users belonging to configured role keys
        if (StringUtils.isNotBlank(config.getRoleKeys()))
        {
            for (String rk : config.getRoleKeys().split(","))
            {
                String trimmedRk = rk.trim();
                if (StringUtils.isNotBlank(trimmedRk))
                {
                    List<String> roleUserIds = sysUserMapper.selectUserIdsByRoleKey(trimmedRk);
                    idSet.addAll(roleUserIds);
                }
            }
        }
        return new ArrayList<>(idSet);
    }

    @Override
    public boolean canReviewAnyStage(Collection<String> currentRoleKeys, boolean isAdmin)
    {
        if (isAdmin)
        {
            return true;
        }
        List<BbsIdeaStageReviewerConfig> configs = configMapper.selectEnabledConfigs();
        for (BbsIdeaStageReviewerConfig config : configs)
        {
            if (matchConfig(config, currentRoleKeys))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> buildCurrentPermission(Collection<String> currentRoleKeys, boolean isAdmin)
    {
        Map<String, Object> data = new HashMap<>();
        List<String> reviewStages = new ArrayList<>();
        List<BbsIdeaStageReviewerConfig> configs = configMapper.selectEnabledConfigs();
        for (BbsIdeaStageReviewerConfig config : configs)
        {
            if (matchConfig(config, currentRoleKeys) || isAdmin)
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
        if (config == null || !"0".equals(config.getStatus()) || currentRoleKeys == null || currentRoleKeys.isEmpty())
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

    private void validateConfig(BbsIdeaStageReviewerConfig config, boolean insert)
    {
        if (config == null)
        {
            throw new ServiceException("评审配置不能为空");
        }
        if (StringUtils.isBlank(config.getStageCode()) || StringUtils.isBlank(config.getStageName()))
        {
            throw new ServiceException("评审环节编码和名称不能为空");
        }
        if (!"L1".equals(config.getStageCode()) && !"L2".equals(config.getStageCode()) && !"L3".equals(config.getStageCode()))
        {
            throw new ServiceException("仅支持 L1/L2/L3 三个评审环节");
        }
        if (StringUtils.isBlank(config.getRoleKeys()))
        {
            throw new ServiceException("请至少配置一个角色标识");
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
}