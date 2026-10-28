import request from '@/utils/request'

/** 管理端：分页查询提案 */
export function listIdeaManage(params) {
  return request({ url: '/bbs/ideaManage/list', method: 'get', params })
}

/** 管理端：提案详情 */
export function getIdeaManage(ideaId) {
  return request({ url: `/bbs/ideaManage/${ideaId}`, method: 'get' })
}

/** 管理端：删除提案（逻辑删除），ideaIds 可为逗号分隔 */
export function delIdeaManage(ideaIds) {
  return request({ url: `/bbs/ideaManage/${ideaIds}`, method: 'delete' })
}

/** 管理端：取消公示 */
export function cancelIdeaPublicize(ideaId) {
  return request({ url: `/bbs/ideaManage/${ideaId}/cancelPublicize`, method: 'put' })
}
