package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsDeptContact;

/**
 * 部门接口人表 数据层
 * 
 * @author ruoyi
 */
public interface BbsDeptContactMapper
{
    /**
     * 查询部门接口人信息
     * 
     * @param contactId 接口人ID
     * @return 部门接口人信息
     */
    public BbsDeptContact selectBbsDeptContactById(Long contactId);

    /**
     * 根据部门ID查询部门接口人信息
     * 
     * @param deptId 部门ID
     * @return 部门接口人信息
     */
    public BbsDeptContact selectBbsDeptContactByDeptId(Long deptId);

    /**
     * 查询部门接口人列表
     * 
     * @param bbsDeptContact 部门接口人信息
     * @return 部门接口人集合
     */
    public List<BbsDeptContact> selectBbsDeptContactList(BbsDeptContact bbsDeptContact);

    /**
     * 新增部门接口人
     * 
     * @param bbsDeptContact 部门接口人信息
     * @return 结果
     */
    public int insertBbsDeptContact(BbsDeptContact bbsDeptContact);

    /**
     * 修改部门接口人
     * 
     * @param bbsDeptContact 部门接口人信息
     * @return 结果
     */
    public int updateBbsDeptContact(BbsDeptContact bbsDeptContact);

    /**
     * 删除部门接口人
     * 
     * @param contactId 接口人ID
     * @return 结果
     */
    public int deleteBbsDeptContactById(Long contactId);

    /**
     * 批量删除部门接口人
     * 
     * @param contactIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBbsDeptContactByIds(Long[] contactIds);
}
