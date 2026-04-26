package com.ruoyi.framework.web.service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.system.service.ISysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
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
import com.ruoyi.system.service.ISysUserService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private SysPasswordService passwordService;

    @Autowired
    private SysPermissionService permissionService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
//        passwordService.validate(user);
        String accessTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=wx786a96dd52ea3edb&corpsecret=SbJ0TwcYFTtq2PWe1zbkI_0-4e7pO8k85VZpN4uWtoQ";
        String tokenInfo = HttpUtils.sendGet(accessTokenUrl);
        JSONObject tokenObject = JSONObject.parseObject(tokenInfo);
        String access_token = tokenObject.getString("access_token");
        String getUserInfoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+access_token+"&userid="+username;
        String userInfoStr = HttpUtils.sendGet(getUserInfoUrl);
        JSONObject userObject = JSONObject.parseObject(userInfoStr);
        if(userObject != null && userObject.containsKey("errcode") && userObject.getLong("errcode") == 0){
            SysUser user = new SysUser();
            user.setUserId(userObject.getString("userid"));
            user.setUserName(userObject.getString("userid"));
            user.setNickName(userObject.getString("name"));
            user.setAvatar(userObject.getString("avatar"));
            SysDept sysDept = new SysDept();
            Long mainDepartment = userObject.getLong("main_department");

            String deptUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token="+access_token+"&id="+mainDepartment;
            String deptStr = HttpUtils.sendGet(deptUrl);
            JSONObject deptObject = JSONObject.parseObject(deptStr);
            if(deptObject != null){
                JSONObject department = deptObject.getJSONObject("department");
                Long parentId = department.getLong("parentid");
                deptUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token="+access_token+"&id="+parentId;
                String parentDeptInfo = HttpUtils.sendGet(deptUrl);
                JSONObject parentDeptObject = JSONObject.parseObject(parentDeptInfo);
                String parerentDeptName = parentDeptObject.getJSONObject("department").getString("name");
                if(parerentDeptName.contains("本部") || parerentDeptName.contains("运营有限公司") || parerentDeptName.contains("分公司") || parerentDeptName.contains("香港创新研究院")){
                    sysDept.setDeptName(department.getString("name"));
                    sysDept.setDeptId(mainDepartment);
                    user.setDeptId(mainDepartment);
                }else{
                    parentId = parentDeptObject.getJSONObject("department").getLong("parentid");
                    deptUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token="+access_token+"&id="+parentId;
                    String parentDeptInfo1 = HttpUtils.sendGet(deptUrl);
                    JSONObject parentDeptObject1 = JSONObject.parseObject(parentDeptInfo1);
                    String parerentDeptName1 = parentDeptObject1.getJSONObject("department").getString("name");
                    if(parerentDeptName1.contains("本部") || parerentDeptName1.contains("运营有限公司") || parerentDeptName1.contains("分公司") || parerentDeptName1.contains("香港创新研究院")){
                        sysDept.setDeptName(parerentDeptName);
                        sysDept.setDeptId(parentDeptObject.getJSONObject("department").getLong("id"));
                        user.setDeptId(parentDeptObject.getJSONObject("department").getLong("id"));
                    }else{
                        sysDept.setDeptId(parentDeptObject1.getJSONObject("department").getLong("id"));
                        sysDept.setDeptName(parerentDeptName1);
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
            // 3. 核心修复：确保权限集合非null
            Collection<? extends GrantedAuthority> authorities = loginUser.getAuthorities();
            if (authorities == null) {
                // 使用框架提供的空权限集合
                authorities = AuthorityUtils.NO_AUTHORITIES;
            }


            return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
//            return createLoginUser(user);
        }else{
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
    }

    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
