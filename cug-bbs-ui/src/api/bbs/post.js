import request from '@/utils/request'
import { mockBbsAPI } from '@/mock/bbs'

// 是否使用Mock数据（开发环境）
const USE_MOCK = false

// 查询帖子列表
export function listPost(query) {
  if (USE_MOCK) {
    return mockBbsAPI.getPostList(query)
  }
  return request({
    url: '/bbs/post/list',
    method: 'get',
    params: query
  })
}

// 查询帖子详细
export function getPost(postId) {
  if (USE_MOCK) {
    return mockBbsAPI.getPostDetail(postId)
  }
  return request({
    url: '/bbs/post/' + postId,
    method: 'get'
  })
}

// 新增帖子
export function addPost(data) {
  if (USE_MOCK) {
    return mockBbsAPI.addPost(data)
  }
  return request({
    url: '/bbs/post',
    method: 'post',
    data: data
  })
}

// 修改帖子
export function updatePost(data) {
  return request({
    url: '/bbs/post',
    method: 'put',
    data: data
  })
}

// 删除帖子
export function delPost(postId) {
  return request({
    url: '/bbs/post/' + postId,
    method: 'delete'
  })
}

// 点赞/取消点赞
export function toggleLike(postId) {
  if (USE_MOCK) {
    return mockBbsAPI.toggleLike(postId)
  }
  return request({
    url: '/bbs/post/like/' + postId,
    method: 'post'
  })
}

// 收藏/取消收藏
export function toggleCollect(postId) {
  if (USE_MOCK) {
    return mockBbsAPI.toggleCollect(postId)
  }
  return request({
    url: '/bbs/post/collect/' + postId,
    method: 'post'
  })
}

// 获取热门文章
export function getHotPosts(limit = 5) {
  if (USE_MOCK) {
    return mockBbsAPI.getHotPosts(limit)
  }
  return request({
    url: '/bbs/post/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取运营看板统计数据
export function getPostStats() {
  return request({
    url: '/bbs/post/stats',
    method: 'get'
  })
}

// 获取审核开关状态
export function getAuditEnabled() {
  return request({
    url: '/bbs/post/audit/enabled',
    method: 'get'
  })
}

// 设置审核开关
export function setAuditEnabled(enabled) {
  return request({
    url: '/bbs/post/audit/enabled',
    method: 'post',
    data: { enabled }
  })
}

// 批量审核通过文章
export function approvePosts(postIds) {
  return request({
    url: '/bbs/post/approve',
    method: 'post',
    data: postIds
  })
}

// 批量审核不通过文章
export function rejectPosts(postIds) {
  return request({
    url: '/bbs/post/reject',
    method: 'post',
    data: { postIds }
  })
}

// 置顶/取消置顶
export function toggleTop(postId) {
  return request({
    url: '/bbs/post/top/' + postId,
    method: 'post'
  })
}

// 查询草稿列表
export function listDrafts(query) {
  return request({
    url: '/bbs/post/drafts',
    method: 'get',
    params: query
  })
}

// 个人删除帖子
export function delByPersonal(postId) {
  return request({
    url: '/bbs/post/delByPersonal/' + postId,
    method: 'delete'
  })
}

// AI助写帖子内容
export function generatePostByAi(data) {
  return request({
    url: '/bbs/post/ai/generate',
    method: 'post',
    data: data
  })
}

// 查询热门标签
export function getHotTags(limit = 20) {
  return request({
    url: '/bbs/post/tags/hot',
    method: 'get',
    params: { limit }
  })
}

// 更新建议/意见帖闭环状态
export function updatePostFollowup(data) {
  return request({
    url: '/bbs/post/followup',
    method: 'post',
    data: data
  })
}

// 获取最新闭环记录
export function getPostFollowupLatest(postId) {
  return request({
    url: '/bbs/post/followup/latest/' + postId,
    method: 'get'
  })
}

// 获取闭环记录历史
export function getPostFollowupHistory(postId) {
  return request({
    url: '/bbs/post/followup/history/' + postId,
    method: 'get'
  })
}
