import request from '@/utils/request'

export function listIdeaStageConfig(query) {
  return request({
    url: '/bbs/ideaStageConfig/list',
    method: 'get',
    params: query
  })
}

export function getIdeaStageConfig(configId) {
  return request({
    url: '/bbs/ideaStageConfig/' + configId,
    method: 'get'
  })
}

export function addIdeaStageConfig(data) {
  return request({
    url: '/bbs/ideaStageConfig',
    method: 'post',
    data
  })
}

export function updateIdeaStageConfig(data) {
  return request({
    url: '/bbs/ideaStageConfig',
    method: 'put',
    data
  })
}

export function delIdeaStageConfig(configId) {
  return request({
    url: '/bbs/ideaStageConfig/' + configId,
    method: 'delete'
  })
}

export function listReviewerOptions(stageCode) {
  return request({
    url: '/bbs/ideaStageConfig/stage/' + stageCode + '/reviewerOptions',
    method: 'get'
  })
}

export function getCurrentIdeaReviewPermission() {
  return request({
    url: '/bbs/ideaStageConfig/current/permission',
    method: 'get'
  })
}
