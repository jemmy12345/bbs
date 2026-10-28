import request from '@/utils/request'

// 查询公告列表
export function listNotice(query) {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params: query
  })
}

// 查询公告详细
export function getNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'get'
  })
}

// 新增公告
export function addNotice(data) {
  return request({
    url: '/system/notice',
    method: 'post',
    data: data
  })
}

// 修改公告
export function updateNotice(data) {
  return request({
    url: '/system/notice',
    method: 'put',
    data: data
  })
}

// 删除公告
export function delNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'delete'
  })
}
// 用户端：查询当前用户未读的有效公告（右下角弹窗）
export function listUnreadNotice() {
  return request({ url: '/system/notice/unread', method: 'get' })
}

// 用户端：分页查询有效公告（通知中心），noticeType：1通知 2公告
export function listMyNotice(params) {
  return request({ url: '/system/notice/mine', method: 'get', params })
}

// 用户端：标记公告已读
export function readNotice(noticeId) {
  return request({ url: '/system/notice/read/' + noticeId, method: 'put' })
}
