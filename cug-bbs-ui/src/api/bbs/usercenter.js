import request from '@/utils/request'

// 获取个人中心统计信息
export function getUserCenterStats() {
  return request({
    url: '/bbs/usercenter/stats',
    method: 'get'
  })
}

// 查询我的帖子列表
export function getMyPosts(query) {
  return request({
    url: '/bbs/usercenter/posts',
    method: 'get',
    params: query
  })
}

// 查询我的收藏列表
export function getMyCollects(query) {
  return request({
    url: '/bbs/usercenter/collects',
    method: 'get',
    params: query
  })
}

// 查询我的点赞列表
export function getMyLikes(query) {
  return request({
    url: '/bbs/usercenter/likes',
    method: 'get',
    params: query
  })
}

// 根据秘钥 hash 查询我的匿名帖子（路径参数 hashCode）
export function listAnonymousPostsByKey(hashCode, query) {
  return request({
    url: '/bbs/post/listAnonymousByKey/' + encodeURIComponent(hashCode),
    method: 'get',
    params: query
  })
}

// 根据秘钥 hash 查询我的匿名回复（路径参数 hashCode）
export function listAnonymousRepliesByKey(hashCode, query) {
  return request({
    url: '/bbs/comment/listAnonymousByKey/' + encodeURIComponent(hashCode),
    method: 'get',
    params: query
  })
}

// 根据秘钥 hash 查询我的匿名评论（接口路径待定，确定后在此补充并接入页面）
// export function listAnonymousCommentsByKey(hashCode, query) {
//   return request({
//     url: '/bbs/comment/listAnonymousCommentByKey/' + encodeURIComponent(hashCode),
//     method: 'get',
//     params: query
//   })
// }
