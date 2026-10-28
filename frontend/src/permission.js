import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken, removeToken, setToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/500']

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
    if (isWhiteList(to.path)) {
      next()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            router.addRoutes(accessRoutes)
            next({ ...to, replace: true })
          })
        }).catch(err => {
          store.dispatch('LogOut').then(() => {
            Message.error(err)
            next({ path: '/' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      console.log('重定向到登录页')
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
