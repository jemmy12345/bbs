import request from '@/utils/request'

// ======= 问题相关 =======

export function listQaQuestions(params) {
  return request({
    url: '/bbs/qa/question/list',
    method: 'get',
    params
  })
}

export function getQaQuestionDetail(questionId) {
  return request({
    url: `/bbs/qa/question/${questionId}`,
    method: 'get'
  })
}

export function createQaQuestion(data) {
  return request({
    url: '/bbs/qa/question',
    method: 'post',
    data
  })
}

export function answerQaQuestion(questionId, data) {
  const payload = typeof data === 'string' ? { content: data } : data
  return request({
    url: `/bbs/qa/question/${questionId}/answer`,
    method: 'post',
    data: payload
  })
}

export function acceptBestAnswer(questionId, answerId) {
  return request({
    url: `/bbs/qa/question/${questionId}/accept/${answerId}`,
    method: 'post'
  })
}

export function cancelQaQuestion(questionId) {
  return request({
    url: `/bbs/qa/question/${questionId}`,
    method: 'delete'
  })
}

export function archiveQaQuestion(questionId) {
  return request({
    url: `/bbs/qa/question/${questionId}/archive`,
    method: 'post'
  })
}

// ======= 回答相关 =======

export function likeQaAnswer(answerId) {
  return request({
    url: `/bbs/qa/answer/${answerId}/like`,
    method: 'post'
  })
}

// ======= 知识库（已归档）=======

export function listQaArchive(params) {
  return request({
    url: '/bbs/qa/archive',
    method: 'get',
    params
  })
}

// ======= 管理员 =======

export function adminAcceptBestAnswer(questionId, answerId) {
  return request({
    url: `/bbs/qa/admin/question/${questionId}/accept/${answerId}`,
    method: 'post'
  })
}

export function auditQaQuestion(questionId, data) {
  return request({
    url: `/bbs/qa/admin/question/${questionId}/audit`,
    method: 'post',
    data
  })
}
