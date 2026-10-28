import request from '@/utils/request'
import { mockBbsAPI } from '@/mock/bbs'

// 是否使用Mock数据（开发环境）
const USE_MOCK = false

// 查询分类列表
export function listCategory(query) {
  if (USE_MOCK) {
    return mockBbsAPI.getCategoryList(query)
  }
  return request({
    url: '/bbs/category/list',
    method: 'get',
    params: query
  })
}

// 查询分类详细
export function getCategory(categoryId) {
  return request({
    url: '/bbs/category/' + categoryId,
    method: 'get'
  })
}

// 新增分类
export function addCategory(data) {
  return request({
    url: '/bbs/category',
    method: 'post',
    data: data
  })
}

// 修改分类
export function updateCategory(data) {
  return request({
    url: '/bbs/category',
    method: 'put',
    data: data
  })
}

// 删除分类
export function delCategory(categoryId) {
  return request({
    url: '/bbs/category/' + categoryId,
    method: 'delete'
  })
}

// 获取热门分类
export function getHotCategories() {
  if (USE_MOCK) {
    return mockBbsAPI.getCategoryList()
  }
  return request({
    url: '/bbs/category/hot',
    method: 'get'
  })
}
