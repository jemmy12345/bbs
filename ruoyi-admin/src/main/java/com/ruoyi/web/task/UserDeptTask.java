package com.ruoyi.web.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.wx.WeChatApi;
import com.ruoyi.system.service.ISysDeptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用户和部门同步定时任务
 * 
 * @author ruoyi
 */
@Component
public class UserDeptTask {

    private static final Logger log = LoggerFactory.getLogger(UserDeptTask.class);

    @Autowired
    private ISysDeptService deptService;

    /**
     * 同步部门信息
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void synDept(){
        try {
            log.info("开始同步部门信息...");
            
            // 获取区域信息（如果需要使用Redis，请取消注释并注入RedisService）
            Map<String, Long> regionInfo = new HashMap<>();
            // 如果使用Redis存储区域信息，取消下面的注释
            // if (redisService != null) {
            //     regionInfo = redisService.getCacheMap("regionInfo");
            // }
            
            // 获取企微访问令牌
            String access_token = WeChatApi.getToken("1000053");
            
            // 获取部门列表（从根部门开始，ID为1）
            JSONArray deptList = WeChatApi.getDeptList(access_token, "1");
            List<Long> deptIds = new ArrayList<>();
            
            for (int i = 0; i < deptList.size(); i++) {
                JSONObject info = null;
                try {
                    // 获取部门信息对象
                    Object deptObj = deptList.get(i);
                    if (deptObj instanceof JSONObject) {
                        info = (JSONObject) deptObj;
                    } else {
                        // 如果不是JSONObject，尝试转换
                        info = JSONObject.parseObject(JSONObject.toJSONString(deptObj));
                    }
                    
                    SysDept tempDeptInfo = new SysDept();
                    Date date = new Date();
                    String now = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS).format(new Date());
                    Long id = info.getLong("id");
                    Long parentid = info.getLong("parentid");
                    deptIds.add(id.longValue());
                    tempDeptInfo.setDeptId(id.longValue());
                    tempDeptInfo.setParentId(parentid.longValue());
                    tempDeptInfo.setStatus("0");
                    tempDeptInfo.setDeptName(info.getString("name"));
                    Object orderObj = info.get("order");
                    if (orderObj != null) {
                        tempDeptInfo.setOrderNum(Integer.valueOf(String.valueOf(orderObj)));
                    }
                    tempDeptInfo.setEmail("");
                    tempDeptInfo.setLeader(info.getString("department_leader"));
                    tempDeptInfo.setPhone("");
                    tempDeptInfo.setDelFlag("0");
                    StringBuffer ancestors = new StringBuffer("1,");

                    // 查询部门是否已存在
                    SysDept dept = deptService.selectDeptById(id.longValue());
                    
                    // 设置区域ID（根据业务逻辑调整）
                    if(parentid == 850066130 || id == 850066130){ //本部
                        if (regionInfo.containsKey("本部")) {
                            tempDeptInfo.setRegionId(regionInfo.get("本部"));
                        }
                        ancestors.append(parentid).append(",").append(id);
                    }else if(parentid == 1){
                        if(id == 850066130 || id == 25 || id == 850098678){
                            if (regionInfo.containsKey("本部")) {
                                tempDeptInfo.setRegionId(regionInfo.get("本部"));
                            }
                            ancestors.append(id);
                        }else{
                            String name = info.getString("name");
                            if (regionInfo.containsKey(name)) {
                                tempDeptInfo.setRegionId(regionInfo.get(name));
                            }
                        }
                    } else if(id == 1){
                        if (regionInfo.containsKey("本部")) {
                            tempDeptInfo.setRegionId(regionInfo.get("本部"));
                        }
                        ancestors = new StringBuffer("1");
                    }else{
                        JSONObject parentInfo = WeChatApi.getDeptInfo(access_token, String.valueOf(parentid));
                        if(parentInfo != null){
                            Long parentid1 = parentInfo.getLong("parentid");
                            if(parentid1 == 850066130){
                                if (regionInfo.containsKey("本部")) {
                                    tempDeptInfo.setRegionId(regionInfo.get("本部"));
                                }
                                ancestors.append(parentid).append(",").append(id).append(",").append(parentInfo.getLong("id")).append(",");
                            }else if(parentid1 == 1){
                                String name = parentInfo.getString("name");
                                if (regionInfo.containsKey(name)) {
                                    tempDeptInfo.setRegionId(regionInfo.get(name));
                                }
                                ancestors.append(parentid);
                            }else{
                                JSONObject parentInfo1 = WeChatApi.getDeptInfo(access_token, String.valueOf(parentid1));
                                if (parentInfo1 != null) {
                                    Long parentid2 = parentInfo1.getLong("parentid");
                                    if(parentid2 == 850066130){
                                        if (regionInfo.containsKey("本部")) {
                                            tempDeptInfo.setRegionId(regionInfo.get("本部"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 如果区域ID为空，尝试从父部门获取
                    if (null == tempDeptInfo.getRegionId()) {
                        String regionId = this.getRegionId(id.longValue());
                        if (StringUtils.isNotBlank(regionId)) {
                            tempDeptInfo.setRegionId(Long.valueOf(regionId));
                        }
                    }
                    
                    tempDeptInfo.setAncestors(ancestors.toString());
                    Object orderObj2 = info.get("order");
                    if (orderObj2 != null) {
                        tempDeptInfo.setOrderNum(Integer.valueOf(String.valueOf(2000000000 - Long.valueOf(String.valueOf(orderObj2)))));
                    }
                    tempDeptInfo.setUpdateTime(date);
                    
                    if(dept != null){
                        // 更新部门
                        deptService.updateDept(tempDeptInfo);
                        log.debug("更新部门: {}", tempDeptInfo.getDeptName());
                    }else{
                        // 新增部门
                        deptService.insertDept(tempDeptInfo);
                        log.debug("新增部门: {}", tempDeptInfo.getDeptName());
                    }
                } catch (Exception e) {
                    String deptIdStr = info != null ? String.valueOf(info.get("id")) : "未知";
                    log.error("同步部门信息失败，部门ID: {}", deptIdStr, e);
                }
            }
            
            // 清理系统已不存在的部门信息
            if (deptIds.size() > 0) {
                deptService.deleteList(deptIds.toArray(new Long[deptIds.size()]));
            }
            
            log.info("部门信息同步完成，共处理 {} 个部门", deptList.size());
        } catch (Exception e) {
            log.error("同步部门信息异常", e);
        }
    }

    /**
     * 递归获取区域ID
     */
    private String getRegionId(long deptId) {
        try {
            SysDept sysDept = deptService.selectDeptById(deptId);
            if (null == sysDept) {
                return "";
            }
            if (null == sysDept.getRegionId()) {
                if (sysDept.getParentId() != null && sysDept.getParentId() != 0) {
                    return getRegionId(sysDept.getParentId());
                }
                return "";
            }
            return sysDept.getRegionId().toString();
        } catch (Exception e) {
            log.error("获取区域ID失败，部门ID: {}", deptId, e);
            return "";
        }
    }
}
