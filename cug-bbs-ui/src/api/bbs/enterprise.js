import request from '@/utils/request'

// QA
export function createQaQuestion(data) {
  return request({ url: '/bbs/qa/question', method: 'post', data })
}

export function listQaQuestions(status) {
  return request({ url: '/bbs/qa/question/list', method: 'get', params: { status } })
}

export function getQaQuestionDetail(questionId) {
  return request({ url: `/bbs/qa/question/${questionId}`, method: 'get' })
}

export function answerQaQuestion(questionId, content) {
  return request({ url: `/bbs/qa/question/${questionId}/answer`, method: 'post', data: { content } })
}

export function acceptQaAnswer(questionId, answerId) {
  return request({ url: `/bbs/qa/question/${questionId}/accept/${answerId}`, method: 'post' })
}

export function archiveQaQuestion(questionId) {
  return request({ url: `/bbs/qa/question/${questionId}/archive`, method: 'post' })
}

// Idea
export function submitIdea(data) {
  return request({ url: '/bbs/idea', method: 'post', data })
}

export function listIdeas(status) {
  return request({ url: '/bbs/idea/list', method: 'get', params: { status } })
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

export function updateIdeaStatus(ideaId, status) {
  return request({ url: `/bbs/idea/${ideaId}/status`, method: 'post', data: { status } })
}

export function publishIdeaResult(ideaId, resultSummary) {
  return request({ url: `/bbs/idea/${ideaId}/publish`, method: 'post', data: { resultSummary } })
}

// Engagement: vote
export function createVote(data) {
  return request({ url: '/bbs/engagement/vote', method: 'post', data })
}

export function listVotes(status) {
  return request({ url: '/bbs/engagement/vote/list', method: 'get', params: { status } })
}

export function getVoteDetail(voteId) {
  return request({ url: `/bbs/engagement/vote/${voteId}`, method: 'get' })
}

export function submitVote(voteId, optionIds) {
  return request({ url: `/bbs/engagement/vote/${voteId}/submit`, method: 'post', data: { optionIds } })
}

// Engagement: demand
export function createDemandForm(data) {
  return request({ url: '/bbs/engagement/demand/form', method: 'post', data })
}

export function listDemandForms(status) {
  return request({ url: '/bbs/engagement/demand/form/list', method: 'get', params: { status } })
}

export function submitDemand(formId, submissionContent) {
  return request({ url: `/bbs/engagement/demand/form/${formId}/submit`, method: 'post', data: { submissionContent } })
}

// Engagement: topic activity
export function createTopicActivity(data) {
  return request({ url: '/bbs/engagement/topic', method: 'post', data })
}

export function listTopicActivities(status) {
  return request({ url: '/bbs/engagement/topic/list', method: 'get', params: { status } })
}

export function joinTopicActivity(activityId, remark) {
  return request({ url: `/bbs/engagement/topic/${activityId}/join`, method: 'post', data: { remark } })
}
