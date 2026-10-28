import request from '@/utils/request'

/** 管理端：分页查询管理员操作记录 */
export function listBbsAdminLog(query) {
  return request({
    url: '/bbs/BbsAdminLog/list',
    method: 'get',
    params: query
  })
}
