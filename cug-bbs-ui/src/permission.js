import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken, removeToken, setToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/ssologin', '/500']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}
router.beforeEach((to, from, next) => {
if(to.query.busiToken ){
  removeToken();
  setTimeout(() => {
    setToken(to.query.busiToken);
    next({ path: '/' })
  }, 1000);
}
console.log('to.',to,from)
if(to.query.code && !from.query.redirect){
  console.log('有code，删除token')
  removeToken();
}
  NProgress.start()
  if (getToken()) {
    console.log('有token，直接进入')
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    /* has token*/
    if (to.path === '/ssologin') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      next()
      // if (store.getters.roles.length === 0) {
      //   isRelogin.show = true
      //   // 判断当前用户是否已拉取完user_info信息
      //   store.dispatch('GetInfo').then(() => {
      //     alert(8)
      //     isRelogin.show = false
      //     store.dispatch('GenerateRoutes').then(accessRoutes => {
      //       // 根据roles权限生成可访问的路由表
      //       router.addRoutes(accessRoutes) // 动态添加可访问路由表
      //       // 如果是从白名单页面跳转过来的，保留历史记录；否则使用replace确保addRoutes已完成
      //       const shouldReplace = !from.path || from.path === '/' || from.path === to.path
      //       next({ ...to, replace: shouldReplace }) // hack方法 确保addRoutes已完成
      //     })
      //   }).catch(err => {
      //       store.dispatch('LogOut').then(() => {
      //         Message.error(err)
      //         next({ path: '/' })
      //       })
      //     })
      // } else {
      //   next()
      // }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      console.log('重定向到登录页')
      // https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx786a96dd52ea3edb&redirect_uri=https://oaapptest.cugsw.solutions:8088/bbs/ssologin&response_type=code&scope=snsapi_base&agentid=1000165&state=STATE#wechat_redirect
      next(`/ssologin?redirect=${encodeURIComponent(to.fullPath)}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
