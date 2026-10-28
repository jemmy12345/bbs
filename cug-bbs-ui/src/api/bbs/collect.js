import request from '@/utils/request'

export function createCollectVote(data) {
  return request({ url: '/bbs/collect/vote/create', method: 'post', data })
}

export function getCollectVoteTargetTree() {
  return request({ url: '/bbs/collect/vote/target/tree', method: 'get' })
}

export function createCollectSurvey(data) {
  return request({ url: '/bbs/collect/survey/create', method: 'post', data })
}

export function createCollectDemand(data) {
  return request({ url: '/bbs/collect/demand/create', method: 'post', data })
}

export function createCollectTopic(data) {
  return request({ url: '/bbs/collect/topic/create', method: 'post', data })
}

export function listCollectActivities(params) {
  return request({ url: '/bbs/collect/activity/list', method: 'get', params })
}

export function getCollectActivity(activityId) {
  return request({ url: `/bbs/collect/activity/${activityId}`, method: 'get' })
}

export function endCollectActivity(activityId) {
  return request({ url: `/bbs/collect/activity/${activityId}/end`, method: 'post' })
}

export function deleteCollectActivity(activityId) {
  return request({ url: `/bbs/collect/activity/${activityId}`, method: 'delete' })
}

export function submitCollectVote(activityId, optionIds) {
  return request({ url: `/bbs/collect/vote/${activityId}/submit`, method: 'post', data: { optionIds } })
}

export function saveCollectSurveyDraft(activityId, answersJson) {
  return request({ url: `/bbs/collect/survey/${activityId}/draft`, method: 'post', data: { answersJson } })
}

export function submitCollectSurvey(activityId, answersJson) {
  return request({ url: `/bbs/collect/survey/${activityId}/submit`, method: 'post', data: { answersJson } })
}

export function submitCollectDemand(activityId, data) {
  return request({ url: `/bbs/collect/demand/${activityId}/submit`, method: 'post', data })
}

export function listCollectDemandTickets(activityId, params) {
  return request({ url: `/bbs/collect/demand/${activityId}/ticket/list`, method: 'get', params })
}

export function assignCollectDemand(ticketId, data) {
  return request({ url: `/bbs/collect/demand/ticket/${ticketId}/assign`, method: 'post', data })
}

export function updateCollectDemandStatus(ticketId, data) {
  return request({ url: `/bbs/collect/demand/ticket/${ticketId}/status`, method: 'post', data })
}

export function joinCollectTopic(activityId, remark) {
  return request({ url: `/bbs/collect/topic/${activityId}/join`, method: 'post', data: { remark } })
}

export function addCollectTopicContent(activityId, data) {
  return request({ url: `/bbs/collect/topic/${activityId}/content`, method: 'post', data })
}

export function getCollectTopicDetail(activityId) {
  return request({ url: `/bbs/collect/topic/${activityId}`, method: 'get' })
}

export function getCollectActivityStats(activityId) {
  return request({ url: `/bbs/collect/activity/${activityId}/stats`, method: 'get' })
}

export function getCollectVoteMeta(activityId) {
  return request({ url: `/bbs/collect/vote/${activityId}/meta`, method: 'get' })
}

export function archiveCollectTopic(activityId, summaryText) {
  return request({ url: `/bbs/collect/topic/${activityId}/archive`, method: 'post', data: { summaryText } })
}
