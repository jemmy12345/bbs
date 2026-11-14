package com.ruoyi.framework.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ruoyi.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysRoleService;

/**
 * 用户权限处理
 * 
 * @author Smart BBS
 */
@Component
public class SysPermissionService
{
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 是否为系统管理员：账号ID为管理员、命中参数 sys.account.admin 配置，或拥有 admin 角色
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean isSystemAdmin(SysUser user)
    {
        if (user == null)
        {
            return false;
        }
        if (user.isAdmin() || SecurityUtils.isConfigAdmin(configService.selectConfigByKey("sys.account.admin"), user))
        {
            return true;
        }
        List<SysRole> roles = user.getRoles();
        return roles != null && roles.stream().anyMatch(role -> role != null
                && Constants.SUPER_ADMIN.equals(StringUtils.trim(role.getRoleKey()))
                && UserConstants.ROLE_NORMAL.equals(role.getStatus()));
    }

    /**
     * 获取角色标识（用于前端判断角色，如普通角色、评审员、管理员）
     * 
     * @param user 用户信息
     * @return 角色标识集合
     */
    public Set<String> getRolePermission(SysUser user)
    {
        Set<String> roles = new HashSet<String>();
        if (isSystemAdmin(user))
        {
            roles.add(Constants.SUPER_ADMIN);
        }
        else
        {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
        }
        return roles;
    }

    /**
     * 获取接口权限：角色不再配置菜单权限，管理员拥有全部权限，其他用户无后台权限。
     * 普通用户、评审员的业务权限由各业务接口按角色或评审配置单独判断。
     * 
     * @param user 用户信息
     * @return 权限集合
     */
    public Set<String> getMenuPermission(SysUser user)
    {
        Set<String> perms = new HashSet<String>();
        if (isSystemAdmin(user))
        {
            perms.add(Constants.ALL_PERMISSION);
        }
        return perms;
    }
}
