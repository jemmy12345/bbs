package com.ruoyi.common.core.wx;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.utils.PropertiesUtil;
import org.springframework.web.client.RestTemplate;

/**
 * 企业微信API工具类
 * 
 * @author ruoyi
 */
public class WeChatApi {

    // 企业ID，可以从配置文件读取，默认值
    private static String corpid = null;
    
    /**
     * 获取企业ID
     */
    private static String getCorpId() {
        if (corpid == null) {
            String configCorpid = PropertiesUtil.getProperty("wechat_config.properties", "corpid");
            if (configCorpid != null && !configCorpid.isEmpty()) {
                corpid = configCorpid;
            } else {
                corpid = "wx786a96dd52ea3edb"; // 默认值
            }
        }
        return corpid;
    }

    /**
     * 获取企业微信token
     * @param appid 应用ID
     * @return access_token
     * @throws Exception
     */
    public static String getToken(String appid) {
        String secret = PropertiesUtil.getProperty("wechat_config.properties", appid);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + getCorpId() + "&corpsecret=" + secret;
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        JSONObject httpGet = JSONObject.parseObject(response);
        if (httpGet == null || !"0".equals(httpGet.get("errcode").toString())) {
            throw new ServiceException(httpGet != null ? httpGet.getString("errmsg") : "获取token失败");
        }
        String access_token = httpGet.getString("access_token");
        return access_token;
    }

    /**
     * 获取部门信息列表
     * @param access_token 访问令牌
     * @param parent_id 父部门ID
     * @return 部门列表
     */
    public static JSONArray getDeptList(String access_token, String parent_id){
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + access_token
                + "&id=" + parent_id;
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        JSONObject httpGet = JSONObject.parseObject(response);
        if (httpGet == null || !"0".equals(httpGet.get("errcode").toString())) {
            throw new ServiceException(httpGet != null ? httpGet.getString("errmsg") : "获取部门列表失败");
        }
        JSONArray department = httpGet.getJSONArray("department");
        return department;
    }

    /**
     * 获取单个部门信息
     * @param access_token 访问令牌
     * @param dept_id 部门ID
     * @return 部门信息
     */
    public static JSONObject getDeptInfo(String access_token, String dept_id){
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token=" + access_token
                + "&id=" + dept_id;
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        JSONObject httpGet = JSONObject.parseObject(response);
        if (httpGet == null || !"0".equals(httpGet.get("errcode").toString())) {
            throw new ServiceException(httpGet != null ? httpGet.getString("errmsg") : "获取部门信息失败");
        }
        JSONObject department = httpGet.getJSONObject("department");
        return department;
    }

    /**
     * 获取部门成员
     * @param access_token 访问令牌
     * @param dept_id 部门ID
     * @param fetch_child 1-递归获取，0-只获取本部门
     * @return 用户列表
     */
    public static JSONArray getUserList(String access_token, String dept_id, String fetch_child){
        String url = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=" + access_token
                + "&department_id=" + dept_id+ "&fetch_child=" + fetch_child;
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        JSONObject httpGet = JSONObject.parseObject(response);
        if (httpGet == null || !"0".equals(httpGet.get("errcode").toString())) {
            throw new ServiceException(httpGet != null ? httpGet.getString("errmsg") : "获取用户列表失败");
        }
        JSONArray userlist = httpGet.getJSONArray("userlist");
        return userlist;
    }

    /**
     * 企业微信应用消息推送
     * @param accessToken 访问令牌
     * @param params 消息参数
     * @return 推送结果
     */
    public static JSONObject sendMsg(String accessToken, JSONObject params) {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+accessToken;
        JSONObject requestBody = new JSONObject();
        
        String userid = params.getString("userid");
        requestBody.put("touser", userid);
        requestBody.put("toparty", params.get("toparty"));
        requestBody.put("totag", params.get("totag"));
        requestBody.put("msgtype", params.get("msgtype"));
        
        if("text".equals(params.get("msgtype"))) {
            java.util.Map<String, Object> content = new java.util.HashMap<String, Object>();
            content.put("content", params.get("content"));
            requestBody.put("text", content);
            requestBody.put("safe", 0);
        }else if("markdown".equals(params.get("msgtype"))) {
            java.util.Map<String, Object> content = new java.util.HashMap<String, Object>();
            content.put("content", params.get("content"));
            requestBody.put("markdown", content);
        }else if("textcard".equals(params.get("msgtype"))){
            requestBody.put("textcard", params.get("content"));
        }
        requestBody.put("agentid", params.get("agentid"));

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(url, requestBody, String.class);
        JSONObject returnObj = JSONObject.parseObject(response);

        if (returnObj == null || !"0".equals(returnObj.getString("errcode"))) {
            throw new ServiceException(returnObj != null ? returnObj.getString("errmsg") : "消息推送失败");
        }
        return returnObj;
    }

    /**
     * 企微推送群机器人消息
     * @param msgKey 群机器人Key
     * @param params 消息参数
     * @return 推送结果
     */
    public static JSONObject sendMsgWebhook(String msgKey, JSONObject params) {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key="+msgKey;
        JSONObject requestBody = new JSONObject();

        requestBody.put("msgtype", params.get("msgtype"));
        if("text".equals(params.get("msgtype"))) {
            java.util.Map<String, Object> content = new java.util.HashMap<String, Object>();
            content.put("content", params.get("content"));
            requestBody.put("text", content);
            requestBody.put("safe", 0);
        }else if("markdown".equals(params.get("msgtype"))) {
            java.util.Map<String, Object> content = new java.util.HashMap<String, Object>();
            content.put("content", params.get("content"));
            requestBody.put("markdown", content);
        }else if("markdown_v2".equals(params.get("msgtype"))) {
            java.util.Map<String, Object> content = new java.util.HashMap<String, Object>();
            content.put("content", params.get("content"));
            requestBody.put("markdown_v2", content);
        }else if("textcard".equals(params.get("msgtype"))){
            requestBody.put("textcard", params.get("content"));
        }

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(url, requestBody, String.class);
        JSONObject returnObj = JSONObject.parseObject(response);

        if (returnObj == null || !"0".equals(returnObj.getString("errcode"))) {
            throw new ServiceException(returnObj != null ? returnObj.getString("errmsg") : "消息推送失败");
        }
        return returnObj;
    }
}
