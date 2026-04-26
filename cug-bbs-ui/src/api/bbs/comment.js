import request from '@/utils/request'
import { mockBbsAPI } from '@/mock/bbs'


// 查询评论列表
export function listComment(query) {
  return request({
    url: '/bbs/comment/list',
    method: 'get',
    params: query
  })
}

/** 管理端：分页查询全站评论（含已删除），查询参数 title、commentContent 等 */
export function listCommentAdmin(query) {
  return request({
    url: '/bbs/commentManage/list',
    method: 'get',
    params: query
  })
}

/** 管理端：删除/恢复评论 commentDelFlag：0-恢复，2-删除 */
export function setCommentDelFlag(data) {

  return request({
    url: '/bbs/commentManage/setDelFlag',
    method: 'post',
    data: data
  })
}

/** 管理端：恢复已删除评论 */

// 新增评论
/**
 * 添加评论的函数
 * @param {Object} data - 评论数据对象
 * @returns {Promise} - 返回一个Promise对象，包含请求的结果
 */
/**
  return request({  // 使用request函数发送HTTP请求
    url: '/bbs/comment',  // 请求的URL路径
    method: 'post',  // 请求方法为POST
 */
export function addComment(data) {
  return request({
    url: '/bbs/comment',
    method: 'post',
    data: data
  })
}

// 修改评论
export function updateComment(data) {
  return request({
    url: '/bbs/comment',
    method: 'put',
    data: data
  })
}

// 删除评论
export function delComment(commentId) {
  return request({
    url: '/bbs/comment/' + commentId,
    method: 'delete'
  })
}

// 点赞/取消点赞
export function toggleLike(commentId) {
  return request({
    url: '/bbs/comment/like/' + commentId,
    method: 'post'
  })
}

// 点踩/取消点踩（评论区 targetType 固定为 2）
export function toggleDislike(commentId) {
  return request({
    url: '/bbs/dislike/dislike',
    method: 'post',
    data: {
      targetId: commentId,
      targetType: '2'
    }
  })
}

// 个人删除评论
export function delByPersonal(commentId) {
  return request({
    url: '/bbs/comment/delByPersonal/' + commentId,
    method: 'delete'
  })
}

