package com.ruoyi.framework.security.handle;


import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;

import javax.annotation.Resource;

/**
 * 无密码认证提供者，跳过密码校验
 */
@Component
public class NoPasswordAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials() == null ? null : authentication.getCredentials().toString();
        // 加载若依原生 LoginUser 对象
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 兼容双模式登录：有密码时执行密码校验；无密码时按 SSO 模式放行
        if (StringUtils.isNotEmpty(password)) {
            String encodedPassword = userDetails.getPassword();
            if (StringUtils.isEmpty(encodedPassword) || !bCryptPasswordEncoder.matches(password, encodedPassword)) {
                throw new BadCredentialsException("用户名或密码错误");
            }
        }

        // 构建认证令牌，主体为 LoginUser，无密码校验
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(authentication.getDetails());
        return authToken;
    }

    /**
     * 支持用户名密码认证类型
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}