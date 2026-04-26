import axios from 'axios'
import { Notification, MessageBox, Message, Loading } from 'element-ui'
import errorCode from '@/utils/errorCode'
import { tansParams } from "@/utils/ruoyi";
const WXURL = process.env.NODE_ENV === "production" ? "https://oa.cugsw.solutions/cug-app" : 'https://oaapptest.cugsw.solutions:8088/cug-app';

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const WXservice = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: WXURL,
  // 超时
  timeout: 10000
})

// request拦截器
WXservice.interceptors.request.use(config => {
  // 是否需要设置 token
  // 是否需要防止数据重复提交
 
  // get请求映射params参数
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params);
    url = url.slice(0, -1);
    config.params = {};
    config.url = url;
  }
  
  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

// 响应拦截器
WXservice.interceptors.response.use(res => {
    // 未设置状态码则默认成功状态
    const code = res.data.code || 200;
    // 获取错误信息
    const msg = errorCode[code] || res.data.msg || errorCode['default']

    if (code === 500) {
      Message({ message: msg, type: 'error' })
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      Notification.error({ title: msg })
      return Promise.reject('error')
    } else {
      return res.data
    }
  },
  error => {
    console.log('err' + error)
    let { message } = error;
    if (message == "Network Error") {
      message = "后端接口连接异常";
    } else if (message.includes("timeout")) {
      message = "系统接口请求超时";
    } else if (message.includes("Request failed with status code")) {
      message = "系统接口" + message.substr(message.length - 3) + "异常";
    }
    Message({ message: message, type: 'error', duration: 5 * 1000 })
    return Promise.reject(error)
  }
)


export default WXservice
