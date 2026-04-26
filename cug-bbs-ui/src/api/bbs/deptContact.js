import request from '@/utils/request'

// 查询部门接口人列表
export function listDeptContact(query) {
  return request({
    url: '/bbs/deptContact/list',
    method: 'get',
    params: query
  })
}

// 查询部门接口人详细
export function getDeptContact(contactId) {
  return request({
    url: '/bbs/deptContact/' + contactId,
    method: 'get'
  })
}

// 根据部门ID查询部门接口人
export function getDeptContactByDeptId(deptId) {
  return request({
    url: '/bbs/deptContact/dept/' + deptId,
    method: 'get'
  })
}

// 新增部门接口人
export function addDeptContact(data) {
  return request({
    url: '/bbs/deptContact',
    method: 'post',
    data: data
  })
}

// 修改部门接口人
export function updateDeptContact(data) {
  return request({
    url: '/bbs/deptContact',
    method: 'put',
    data: data
  })
}

// 删除部门接口人
export function delDeptContact(contactId) {
  return request({
    url: '/bbs/deptContact/' + contactId,
    method: 'delete'
  })
}

// 判断当前用户是否是某个部门的接口人
export function checkIsDeptContact(deptId) {
  return request({
    url: '/bbs/deptContact/check/' + deptId,
    method: 'get'
  })
}