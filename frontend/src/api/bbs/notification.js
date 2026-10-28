import request from '@/utils/request'

// 查询通知列表
export function listNotification(query) {
  return request({
    url: '/bbs/notification/list',
    method: 'get',
    params: query
  })
}

// 获取未读通知数
export function getUnreadCount() {
  return request({
    url: '/bbs/notification/unread/count',
    method: 'get'
  })
}

// 标记为已读
export function markAsRead(notificationId) {
  return request({
    url: '/bbs/notification/read/' + notificationId,
    method: 'post'
  })
}

// 全部标记为已读
export function markAllAsRead() {
  return request({
    url: '/bbs/notification/read/all',
    method: 'post'
  })
}

// 获取通知详情
export function getNotification(notificationId) {
  return request({
    url: '/bbs/notification/' + notificationId,
    method: 'get'
  })
}
