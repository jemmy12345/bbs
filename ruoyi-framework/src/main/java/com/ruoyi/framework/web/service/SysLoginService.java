package com.ruoyi.framework.web.service;

import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.utils.http.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.BlackListException;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.exception.user.UserNotExistsException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

import java.util.Collection;
import java.util.Set;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Value("${globaloa.url}")
    private String globaloaUrl;
    @Value("${globaloa.appId}")
    private String globaloaAppId;

    @Autowired
    private SysPermissionService permissionService;
    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @param loginType 登录类型（password/sso）
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid, String loginType)
    {
        boolean ssoMode = "sso".equalsIgnoreCase(loginType);

        // SSO 模式可不传密码；账号密码模式必须校验密码
        if (!ssoMode && StringUtils.isEmpty(password))
        {
            throw new ServiceException("密码不能为空");
        }

        // 验证码仅在账号密码模式启用
        if (!ssoMode)
        {
            validateCaptcha(username, code, uuid);
        }

        // 登录前置校验
        loginPreCheck(username, password);

        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, ssoMode ? "" : password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 调用UserDetailsServiceImpl.loadUserByUsername加载用户信息
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new ServiceException(MessageUtils.message("user.password.not.match"));
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        String successMsg = ssoMode ? "企业微信单点登录成功" : MessageUtils.message("user.login.success");
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, successMsg));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        //放匿名追踪
//        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
    }


    public String globalOaCheckAuthentication(String token) {
        LoginUser userInfo = null;
        try{
            String url = globaloaUrl + "/uac-sso/check_authentication?appid=" + globaloaAppId + "&token=" + token;
            String sendGet = HttpUtils.sendGet(url);
            JSONObject jsonObject = JSONObject.parseObject(sendGet);
            System.out.println("httpGet jsonObject:"+jsonObject);
            if(null != jsonObject){
                String code = jsonObject.getString("code");
                if(!code.equals("urn:oasis:names:tc:SAML:2.0:status:Success")){
                    throw new ServiceException(jsonObject.getString("message"));
                }
                String userId = jsonObject.getJSONObject("userEntity").getString("emailAddress");
                userInfo = getUserInfo(userId);
//                userInfo = userService.getUserInfo(userId);
//                if (!UserStatus.OK.getCode().equals(userInfo.getUser().getDelFlag()) ) {
//                    throw new ServiceException("用户名或密码错误");
//                }
                SysUser sysUser = new SysUser();
                sysUser.setUserId(userId);
                Set<String> menuPermission = permissionService.getMenuPermission(sysUser);
                userInfo.setPermissions(menuPermission);
                userInfo.setOaToken(token);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(userInfo.getUsername(), Constants.LOGIN_SUCCESS, "国际OA单点成功"));
        // 生成token
        return tokenService.createToken(userInfo);
    }

    private LoginUser getUserInfo(String username){
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
            sysDept.setDeptId(mainDepartment);

            String deptUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token="+access_token+"&id="+mainDepartment;
            String deptStr = HttpUtils.sendGet(deptUrl);
            JSONObject deptObject = JSONObject.parseObject(deptStr);
            if(deptObject != null){
                String dept_name = deptObject.getJSONObject("department").getString("name");
                sysDept.setDeptName(dept_name);
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
            return loginUser;
        }else{
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
//        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
//        {
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
//            throw new UserNotExistsException();
//        }
//        // 密码如果不在指定范围内 错误
//        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
//                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
//        {
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
//            throw new UserPasswordNotMatchException();
//        }
        if (StringUtils.isEmpty(username))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP白名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        String ipAddr = IpUtils.getRealIp();
        System.out.println("<UNK>IP<UNK>:" + ipAddr);
        if (!IpUtils.isMatchedIp(blackStr, IpUtils.getRealIp()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(String userId)
    {
        userService.updateLoginInfo(userId, IpUtils.getIpAddr(), DateUtils.getNowDate());
    }
}
