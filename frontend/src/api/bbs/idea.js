import request from '@/utils/request'

export function saveIdeaDraft(data) {
  return request({ url: '/bbs/idea/draft', method: 'post', data })
}

export function updateIdeaDraft(ideaId, data) {
  return request({ url: `/bbs/idea/${ideaId}/draft`, method: 'put', data })
}

export function submitIdea(data) {
  return request({ url: '/bbs/idea', method: 'post', data })
}

export function listIdeas(params) {
  return request({ url: '/bbs/idea/list', method: 'get', params })
}

export function getIdeaDetail(ideaId) {
  return request({ url: `/bbs/idea/${ideaId}`, method: 'get' })
}

export function reviewIdea(ideaId, data) {
  return request({ url: `/bbs/idea/${ideaId}/review`, method: 'post', data })
}

export function addIdeaProgress(ideaId, data) {
  return request({ url: `/bbs/idea/${ideaId}/progress`, method: 'post', data })
}

export function publishIdeaResult(ideaId, data) {
  return request({ url: `/bbs/idea/${ideaId}/publish`, method: 'post', data })
}

export function listIdeaWall(params) {
  return request({ url: '/bbs/idea/wall', method: 'get', params })
}

export function likeIdea(ideaId) {
  return request({ url: `/bbs/idea/${ideaId}/like`, method: 'post' })
}

export function shareIdea(ideaId) {
  return request({ url: `/bbs/idea/${ideaId}/share`, method: 'post' })
}

export function getCurrentIdeaReviewPermission() {
  return request({ url: '/bbs/ideaStageConfig/current/permission', method: 'get' })
}

export function resubmitIdea(ideaId, data) {
  return request({ url: `/bbs/idea/${ideaId}/resubmit`, method: 'post', data })
}
