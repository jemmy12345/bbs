package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.wx.WeChatApi;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDeptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 部门信息
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(SysDeptController.class);
    
    @Autowired
    private ISysDeptService deptService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public AjaxResult list(SysDept dept)
    {
        log.info("开始从企业微信获取部门列表...");
        try {
            // 从企业微信获取部门列表
            log.info("正在获取企业微信访问令牌...");
            String access_token = WeChatApi.getToken("1000053");
            log.info("企业微信访问令牌获取成功");
            
            // 获取所有部门（从根部门1开始）
            log.info("正在从企业微信获取部门列表，parent_id=1");
            JSONArray deptList = WeChatApi.getDeptList(access_token, "1");
            log.info("企业微信返回部门数量: {}", deptList != null ? deptList.size() : 0);
            
            // 转换为SysDept列表
            List<SysDept> allDepts = convertToSysDeptList(deptList);
            
            // 额外获取本部的子部门（确保获取到所有parentId=850066130的部门）
            try {
                log.info("正在从企业微信获取本部子部门列表，parent_id=850066130");
                JSONArray benbuDeptList = WeChatApi.getDeptList(access_token, "850066130");
                if (benbuDeptList != null && benbuDeptList.size() > 0) {
                    List<SysDept> benbuChildren = convertToSysDeptList(benbuDeptList);
                    // 合并到总列表中（去重）
                    for (SysDept child : benbuChildren) {
                        boolean exists = false;
                        for (SysDept existing : allDepts) {
                            if (existing.getDeptId() != null && existing.getDeptId().equals(child.getDeptId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            allDepts.add(child);
                        }
                    }
                    log.info("本部子部门数量: {}", benbuChildren.size());
                }
            } catch (Exception e) {
                log.warn("获取本部子部门失败，继续处理: {}", e.getMessage());
            }
            
            // 构建树形结构
            List<SysDept> result = buildDeptTree(allDepts);
            
            log.info("从企业微信获取部门列表成功，原始部门数量: {}，构建树形结构后顶层部门数量: {}", allDepts.size(), result.size());
            return success(result);
        } catch (Exception e) {
            log.error("从企业微信获取部门列表失败", e);
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }
    
    /**
     * 构建部门树形结构
     * 规则：
     * 1. 如果部门ID是850066130（本部），把所有parentId是850066130的部门都作为本部的子部门
     * 2. 其他parentId=1的部门跟本部同个层级
     */
    private List<SysDept> buildDeptTree(List<SysDept> allDepts) {
        List<SysDept> result = new ArrayList<>();
        
        // 找到本部（deptId=850066130）
        SysDept benbu = null;
        // 存储所有parentId=850066130的子部门
        List<SysDept> benbuChildren = new ArrayList<>();
        // 存储所有parentId=1的其他部门（不包括本部）
        List<SysDept> otherTopLevelDepts = new ArrayList<>();
        
        // 遍历所有部门，分类处理
        for (SysDept dept : allDepts) {
            Long deptId = dept.getDeptId();
            Long parentId = dept.getParentId();
            
            // 找到本部
            if (deptId != null && deptId.equals(850066130L)) {
                benbu = dept;
                // 初始化本部的子部门列表
                if (benbu.getChildren() == null) {
                    benbu.setChildren(new ArrayList<>());
                }
            }
            // 找到所有parentId=850066130的子部门（这些要作为本部的子部门）
            else if (parentId != null && parentId.equals(850066130L)) {
                benbuChildren.add(dept);
            }
            // 找到所有parentId=1的其他部门（与本部同层级，但不包括本部本身）
            else if (parentId != null && parentId.equals(1L) 
                    && (deptId == null || !deptId.equals(850066130L))) {
                otherTopLevelDepts.add(dept);
            }
        }
        
        // 如果找到了本部，设置其子部门
        if (benbu != null) {
            benbu.setChildren(benbuChildren);
            result.add(benbu);
        }
        
        // 添加其他parentId=1的部门（与本部同层级）
        result.addAll(otherTopLevelDepts);
        
        return result;
    }
    
    /**
     * 将企业微信部门列表转换为SysDept列表
     */
    private List<SysDept> convertToSysDeptList(JSONArray deptList) {
        List<SysDept> depts = new ArrayList<>();
        
        for (int i = 0; i < deptList.size(); i++) {
            try {
                Object deptObj = deptList.get(i);
                JSONObject info = null;
                if (deptObj instanceof JSONObject) {
                    info = (JSONObject) deptObj;
                } else {
                    info = JSONObject.parseObject(JSONObject.toJSONString(deptObj));
                }
                
                SysDept sysDept = new SysDept();
                Long id = info.getLong("id");
                Long parentid = info.getLong("parentid");
                
                sysDept.setDeptId(id);
                sysDept.setParentId(parentid);
                sysDept.setDeptName(info.getString("name"));
                sysDept.setStatus("0");
                sysDept.setDelFlag("0");
                
                // 设置排序
                Object orderObj = info.get("order");
                if (orderObj != null) {
                    sysDept.setOrderNum(Integer.valueOf(String.valueOf(orderObj)));
                }
                
                // 设置负责人
                sysDept.setLeader(info.getString("department_leader"));
                
                // 设置其他字段的默认值
                sysDept.setEmail("");
                sysDept.setPhone("");
                
                depts.add(sysDept);
            } catch (Exception e) {
                // 忽略转换失败的部门，继续处理下一个
                continue;
            }
        }
        
        return depts;
    }

    /**
     * 获取部门列表（用于发帖时选择回应部门，不需要权限，从企业微信获取）
     */
    @GetMapping("/listForPost")
    public AjaxResult listForPost(SysDept dept)
    {
        log.info("开始从企业微信获取部门列表（用于发帖选择回应部门）...");
        try {
            // 从企业微信获取部门列表
            log.info("正在获取企业微信访问令牌...");
            String access_token = WeChatApi.getToken("1000053");
            log.info("企业微信访问令牌获取成功");
            
            // 获取所有部门（从根部门1开始）
            log.info("正在从企业微信获取部门列表，parent_id=1");
            JSONArray deptList = WeChatApi.getDeptList(access_token, "1");
            log.info("企业微信返回部门数量: {}", deptList != null ? deptList.size() : 0);
            
            // 转换为SysDept列表
            List<SysDept> allDepts = convertToSysDeptList(deptList);
            
            // 额外获取本部的子部门（确保获取到所有parentId=850066130的部门）
            try {
                log.info("正在从企业微信获取本部子部门列表，parent_id=850066130");
                JSONArray benbuDeptList = WeChatApi.getDeptList(access_token, "850066130");
                if (benbuDeptList != null && benbuDeptList.size() > 0) {
                    List<SysDept> benbuChildren = convertToSysDeptList(benbuDeptList);
                    // 合并到总列表中（去重）
                    for (SysDept child : benbuChildren) {
                        boolean exists = false;
                        for (SysDept existing : allDepts) {
                            if (existing.getDeptId() != null && existing.getDeptId().equals(child.getDeptId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            allDepts.add(child);
                        }
                    }
                    log.info("本部子部门数量: {}", benbuChildren.size());
                }
            } catch (Exception e) {
                log.warn("获取本部子部门失败，继续处理: {}", e.getMessage());
            }
            
            // 构建树形结构
            List<SysDept> result = buildDeptTree(allDepts);
            
            log.info("从企业微信获取部门列表成功（用于发帖），原始部门数量: {}，构建树形结构后顶层部门数量: {}", allDepts.size(), result.size());
            return success(result);
        } catch (Exception e) {
            log.error("从企业微信获取部门列表失败（用于发帖）", e);
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }

    /**
     * 获取部门列表（无数据权限过滤）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/allList")
    public AjaxResult allList(SysDept dept)
    {
        List<SysDept> depts = deptService.selectDeptListNoDataScope(dept);
        return success(depts);
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId)
    {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId)
    {
        deptService.checkDeptDataScope(deptId);
        return success(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysDept dept)
    {
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(getUsername());
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysDept dept)
    {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        else if (dept.getParentId().equals(deptId))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            return error("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(getUsername());
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId)
    {
        if (deptService.hasChildByDeptId(deptId))
        {
            return warn("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId))
        {
            return warn("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return toAjax(deptService.deleteDeptById(deptId));
    }

    /**
     * 企微同步，清理不存在的部门
     */
    @PostMapping("/deleteDeptList")
    public AjaxResult deleteDeptList(@RequestParam("deptIds") Long[] deptIds)
    {
        return toAjax(deptService.deleteList(deptIds));
    }
}
