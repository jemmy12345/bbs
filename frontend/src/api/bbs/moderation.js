import request from '@/utils/request'

// 发布前统一风控预检：发帖风险进入审核，评论/回复风险直接拦截。
export function precheckContent(data) {
  return request({
    url: '/bbs/moderation/precheck',
    method: 'post',
    data
  })
}
