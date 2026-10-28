package com.ruoyi.web.controller.bbs;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.BbsIdeaStageReviewerConfig;
import com.ruoyi.system.service.IBbsIdeaStageReviewerConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bbs/ideaStageConfig")
@Api(tags = "提案评审配置")
public class BbsIdeaStageReviewerConfigController extends BaseController
{
    @Autowired
    private IBbsIdeaStageReviewerConfigService configService;

    @ApiOperation("查询提案评审配置列表")
    @PreAuthorize("@ss.hasPermi('bbs:ideaStageConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(BbsIdeaStageReviewerConfig config)
    {
        startPage();
        return getDataTable(configService.selectBbsIdeaStageReviewerConfigList(config));
    }

    @ApiOperation("查询提案评审配置详情")
    @PreAuthorize("@ss.hasPermi('bbs:ideaStageConfig:query')")
    @GetMapping("/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId)
    {
        return success(configService.selectBbsIdeaStageReviewerConfigById(configId));
    }

    @ApiOperation("查询某环节候选处理人")
    @PreAuthorize("@ss.hasPermi('bbs:ideaStageConfig:query')")
    @GetMapping("/stage/{stageCode}/reviewerOptions")
    public AjaxResult reviewerOptions(@PathVariable String stageCode)
    {
        List<SysUser> users = configService.getReviewerCandidatesForStage(stageCode);
        return success(users);
    }

    @ApiOperation("新增提案评审配置")
    @PreAuthorize("@ss.hasPermi('bbs:ideaStageConfig:add')")
    @Log(title = "提案评审配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BbsIdeaStageReviewerConfig config)
    {
        config.setCreateBy(getUsername());
        return toAjax(configService.insertBbsIdeaStageReviewerConfig(config));
    }

    @ApiOperation("修改提案评审配置")
    @PreAuthorize("@ss.hasPermi('bbs:ideaStageConfig:edit')")
    @Log(title = "提案评审配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BbsIdeaStageReviewerConfig config)
    {
        config.setUpdateBy(getUsername());
        return toAjax(configService.updateBbsIdeaStageReviewerConfig(config));
    }

    @ApiOperation("删除提案评审配置")
    @PreAuthorize("@ss.hasPermi('bbs:ideaStageConfig:remove')")
    @Log(title = "提案评审配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(configService.deleteBbsIdeaStageReviewerConfigByIds(configIds));
    }

    @ApiOperation("查询当前用户提案评审权限")
    @GetMapping("/current/permission")
    public AjaxResult currentPermission()
    {
        boolean isAdmin = false;
        try
        {
            isAdmin = SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin");
        }
        catch (Exception ignored)
        {
        }
        Collection<String> currentRoleKeys = SecurityUtils.getLoginUser().getUser().getRoles().stream()
                .map(role -> role.getRoleKey())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        return success(configService.buildCurrentPermission(SecurityUtils.getUserId(), currentRoleKeys, isAdmin));
    }
}
