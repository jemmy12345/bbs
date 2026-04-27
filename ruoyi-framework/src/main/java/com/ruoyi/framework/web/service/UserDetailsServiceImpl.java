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
 * @author ruoyi
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

        // 回退到企微用户查询（支持企业微信单点登录）
        String accessTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=wx786a96dd52ea3edb&corpsecret=SbJ0TwcYFTtq2PWe1zbkI_0-4e7pO8k85VZpN4uWtoQ";
        String tokenInfo = HttpUtils.sendGet(accessTokenUrl);
        JSONObject tokenObject = JSONObject.parseObject(tokenInfo);
        String accessToken = tokenObject.getString("access_token");
        String getUserInfoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=" + accessToken + "&userid=" + username;
        String userInfoStr = HttpUtils.sendGet(getUserInfoUrl);
        JSONObject userObject = JSONObject.parseObject(userInfoStr);

        if (userObject != null && userObject.containsKey("errcode") && userObject.getLong("errcode") == 0)
        {
            SysUser user = new SysUser();
            user.setUserId(userObject.getString("userid"));
            user.setUserName(userObject.getString("userid"));
            user.setNickName(userObject.getString("name"));
            user.setAvatar(userObject.getString("avatar"));
            SysDept sysDept = new SysDept();
            Long mainDepartment = userObject.getLong("main_department");

            String deptUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token=" + accessToken + "&id=" + mainDepartment;
            String deptStr = HttpUtils.sendGet(deptUrl);
            JSONObject deptObject = JSONObject.parseObject(deptStr);
            if (deptObject != null)
            {
                JSONObject department = deptObject.getJSONObject("department");
                Long parentId = department.getLong("parentid");
                deptUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token=" + accessToken + "&id=" + parentId;
                String parentDeptInfo = HttpUtils.sendGet(deptUrl);
                JSONObject parentDeptObject = JSONObject.parseObject(parentDeptInfo);
                String parentDeptName = parentDeptObject.getJSONObject("department").getString("name");
                if (parentDeptName.contains("本部") || parentDeptName.contains("运营有限公司") || parentDeptName.contains("分公司") || parentDeptName.contains("香港创新研究院"))
                {
                    sysDept.setDeptName(department.getString("name"));
                    sysDept.setDeptId(mainDepartment);
                    user.setDeptId(mainDepartment);
                }
                else
                {
                    parentId = parentDeptObject.getJSONObject("department").getLong("parentid");
                    deptUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token=" + accessToken + "&id=" + parentId;
                    String parentDeptInfo1 = HttpUtils.sendGet(deptUrl);
                    JSONObject parentDeptObject1 = JSONObject.parseObject(parentDeptInfo1);
                    String parentDeptName1 = parentDeptObject1.getJSONObject("department").getString("name");
                    if (parentDeptName1.contains("本部") || parentDeptName1.contains("运营有限公司") || parentDeptName1.contains("分公司") || parentDeptName1.contains("香港创新研究院"))
                    {
                        sysDept.setDeptName(parentDeptName);
                        sysDept.setDeptId(parentDeptObject.getJSONObject("department").getLong("id"));
                        user.setDeptId(parentDeptObject.getJSONObject("department").getLong("id"));
                    }
                    else
                    {
                        sysDept.setDeptId(parentDeptObject1.getJSONObject("department").getLong("id"));
                        sysDept.setDeptName(parentDeptName1);
                        user.setDeptId(parentDeptObject1.getJSONObject("department").getLong("id"));
                    }
                }
            }

            user.setDept(sysDept);
            user.setEmail(userObject.getString("email"));
            user.setPhonenumber(userObject.getString("mobile"));

            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getUserId());
            loginUser.setUser(user);
            loginUser.setDeptId(sysDept.getDeptId());

            Collection<? extends GrantedAuthority> authorities = loginUser.getAuthorities();
            if (authorities == null)
            {
                authorities = AuthorityUtils.NO_AUTHORITIES;
            }

            return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
        }

        log.info("登录用户：{} 不存在.", username);
        throw new ServiceException(MessageUtils.message("user.not.exists"));
    }

    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
