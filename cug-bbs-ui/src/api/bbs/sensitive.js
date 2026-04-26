import request from '@/utils/request'
import { mockBbsAPI } from '@/mock/bbs'

// 是否使用Mock数据（开发环境）
const USE_MOCK = false

// 查询敏感词列表
export function listSensitiveWord(query) {
  return request({
    url: '/bbs/sensitive/list',
    method: 'get',
    params: query
  })
}

// 查询敏感词详细
export function getSensitiveWord(wordId) {
  return request({
    url: '/bbs/sensitive/' + wordId,
    method: 'get'
  })
}

// 新增敏感词
export function addSensitiveWord(data) {
  return request({
    url: '/bbs/sensitive',
    method: 'post',
    data: data
  })
}

// 修改敏感词
export function updateSensitiveWord(data) {
  return request({
    url: '/bbs/sensitive',
    method: 'put',
    data: data
  })
}

// 删除敏感词
export function delSensitiveWord(wordId) {
  return request({
    url: '/bbs/sensitive/' + wordId,
    method: 'delete'
  })
}

// 检测文本中的敏感词
export function checkSensitiveWords(data) {
  return request({
    url: '/bbs/sensitive/check',
    method: 'post',
    data: data
  })
}
