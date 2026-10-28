package com.ruoyi.framework.web.service;

import java.util.Collection;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysDept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.system.service.ISysUserService;

/**
 * 用户验证处理
 *
 * @author Smart BBS
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        // 优先使用本地账号体系（支持独立账号密码登录）
        SysUser localUser = userService.selectUserByUserName(username);
        if (StringUtils.isNotNull(localUser))
        {
            if (UserStatus.DELETED.getCode().equals(localUser.getDelFlag()))
            {
                log.info("登录用户：{} 已被删除.", username);
                throw new ServiceException(MessageUtils.message("user.password.delete"));
            }
            if (UserStatus.DISABLE.getCode().equals(localUser.getStatus()))
            {
                log.info("登录用户：{} 已被停用.", username);
                throw new ServiceException(MessageUtils.message("user.blocked"));
            }
            return createLoginUser(localUser);
        }


        log.info("登录用户：{} 不存在.", username);
        throw new ServiceException(MessageUtils.message("user.not.exists"));
    }

    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
