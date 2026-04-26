package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BbsDeptContactMapper;
import com.ruoyi.system.domain.BbsDeptContact;
import com.ruoyi.system.service.IBbsDeptContactService;

/**
 * 部门接口人表 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class BbsDeptContactServiceImpl implements IBbsDeptContactService
{
    @Autowired
    private BbsDeptContactMapper bbsDeptContactMapper;

    /**
     * 查询部门接口人信息
     * 
     * @param contactId 接口人ID
     * @return 部门接口人信息
     */
    @Override
    public BbsDeptContact selectBbsDeptContactById(Long contactId)
    {
        return bbsDeptContactMapper.selectBbsDeptContactById(contactId);
    }

    /**
     * 根据部门ID查询部门接口人信息
     * 
     * @param deptId 部门ID
     * @return 部门接口人信息
     */
    @Override
    public BbsDeptContact selectBbsDeptContactByDeptId(Long deptId)
    {
        return bbsDeptContactMapper.selectBbsDeptContactByDeptId(deptId);
    }

    /**
     * 查询部门接口人列表
     * 
     * @param bbsDeptContact 部门接口人信息
     * @return 部门接口人集合
     */
    @Override
    public List<BbsDeptContact> selectBbsDeptContactList(BbsDeptContact bbsDeptContact)
    {
        return bbsDeptContactMapper.selectBbsDeptContactList(bbsDeptContact);
    }

    /**
     * 新增部门接口人
     * 
     * @param bbsDeptContact 部门接口人信息
     * @return 结果
     */
    @Override
    public int insertBbsDeptContact(BbsDeptContact bbsDeptContact)
    {
        return bbsDeptContactMapper.insertBbsDeptContact(bbsDeptContact);
    }

    /**
     * 修改部门接口人
     * 
     * @param bbsDeptContact 部门接口人信息
     * @return 结果
     */
    @Override
    public int updateBbsDeptContact(BbsDeptContact bbsDeptContact)
    {
        return bbsDeptContactMapper.updateBbsDeptContact(bbsDeptContact);
    }

    /**
     * 批量删除部门接口人
     * 
     * @param contactIds 需要删除的部门接口人ID
     * @return 结果
     */
    @Override
    public int deleteBbsDeptContactByIds(Long[] contactIds)
    {
        return bbsDeptContactMapper.deleteBbsDeptContactByIds(contactIds);
    }

    /**
     * 删除部门接口人信息
     * 
     * @param contactId 部门接口人ID
     * @return 结果
     */
    @Override
    public int deleteBbsDeptContactById(Long contactId)
    {
        return bbsDeptContactMapper.deleteBbsDeptContactById(contactId);
    }
}
