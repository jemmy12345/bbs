import requestWX from '@/utils/requestWX'

/** 获取企业微信token */
export function getWXtoken(query){
  return requestWX({
    url: '/token/getWxToken',
    method: 'get',
    params: query
  })
}

/** 获取企业微信userid */
export function getWXuserid(query) {
  return requestWX({
    url: '/wxWork/getWeChatUserInfo',
    method: 'get',
    params: query
  })
}
