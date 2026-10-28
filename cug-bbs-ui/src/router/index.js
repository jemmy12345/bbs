import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '/500',
    component: () => import('@/views/error/500'),
    hidden: true
  },
  // {
  //   path: '',
  //   component: Layout,
  //   redirect: 'index',
  //   children: [
  //     {
  //       path: 'index',
  //       component: () => import('@/views/index'),
  //       name: 'Index',
  //       meta: { title: '首页', icon: 'dashboard', affix: true }
  //     }
  //   ]
  // },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/dashboard',
    name: 'BbsAdmin',
    alwaysShow: true,
    meta: { title: '论坛管理', icon: 'message', affix: false },
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/bbs/dashboard/index'),
        name: 'BbsDashboard',
        meta: { title: '运营看板', icon: 'dashboard', affix: false }
      },
      {
        path: 'sensitive',
        component: () => import('@/views/bbs/sensitive/index'),
        name: 'BbsSensitiveIndex',
        meta: { title: '敏感词管理', icon: 'message', affix: false }
      },
      {
        path: 'article',
        component: () => import('@/views/bbs/article/index'),
        name: 'BbsArticleIndex',
        meta: { title: '文章管理', icon: 'message', affix: false }
      },
      {
        path: 'comment',
        component: () => import('@/views/bbs/comment/index'),
        name: 'BbsCommentAdmin',
        meta: { title: '评论管理', icon: 'message', affix: false }
      },
      {
        path: 'admin-log',
        component: () => import('@/views/bbs/adminLog/index'),
        name: 'BbsAdminLog',
        meta: { title: '管理员操作记录', icon: 'message', affix: false }
      },
      {
        path: 'idea-review-config',
        component: () => import('@/views/bbs/ideaStageConfig/index'),
        name: 'BbsIdeaReviewConfig',
        meta: { title: '提案评审配置', icon: 'peoples', affix: false }
      },
      {
        path: 'sys-user',
        component: () => import('@/views/system/user/index'),
        name: 'AdminSysUser',
        meta: { title: '账号管理', icon: 'user', affix: false }
      },
      {
        path: 'sys-role',
        component: () => import('@/views/system/role/index'),
        name: 'AdminSysRole',
        meta: { title: '角色管理', icon: 'peoples', affix: false }
      },
      {
        path: 'sys-dept',
        component: () => import('@/views/system/dept/index'),
        name: 'AdminSysDept',
        meta: { title: '组织管理', icon: 'tree', affix: false }
      },
      {
        path: 'sys-menu',
        component: () => import('@/views/system/menu/index'),
        name: 'AdminSysMenu',
        meta: { title: '菜单管理', icon: 'tree-table', affix: false }
      },
      {
        path: 'sys-post',
        component: () => import('@/views/system/post/index'),
        name: 'AdminSysPost',
        meta: { title: '岗位管理', icon: 'post', affix: false }
      },
      {
        path: 'sys-dict',
        component: () => import('@/views/system/dict/index'),
        name: 'AdminSysDict',
        meta: { title: '字典管理', icon: 'dict', affix: false }
      },
      {
        path: 'sys-config',
        component: () => import('@/views/system/config/index'),
        name: 'AdminSysConfig',
        meta: { title: '参数配置', icon: 'edit', affix: false }
      },
      {
        path: 'sys-notice',
        component: () => import('@/views/system/notice/index'),
        name: 'AdminSysNotice',
        meta: { title: '通知公告', icon: 'message', affix: false }
      }
    ]
  },
  {
    path: '/',
    component: () => import('@/layout/BbsLayoutWrapper'),
    redirect: '/index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/bbs/index'),
        name: 'BbsIndex',
        meta: { title: '企业论坛', icon: 'message', affix: false }
      },
      {
        path: 'topic',
        component: () => import('@/views/bbs/topic'),
        name: 'BbsTopic',
        meta: { title: '论坛专题', icon: 'message', affix: false }
      },
      {
        path: 'enterprise',
        component: () => import('@/views/bbs/enterprise'),
        name: 'BbsEnterprise',
        meta: { title: '企业增长专区', icon: 'chart', affix: false }
      },
      {
        path: 'qa',
        component: () => import('@/views/bbs/qa/index'),
        name: 'BbsQa',
        meta: { title: '企业问答专区', icon: 'message', affix: false }
      },
      {
        path: 'qa/publish',
        component: () => import('@/views/bbs/qa/publish'),
        name: 'BbsQaPublish',
        hidden: true,
        meta: { title: '发起悬赏提问', activeMenu: '/qa' }
      },
      {
        path: 'qa/detail/:questionId',
        component: () => import('@/views/bbs/qa/detail'),
        name: 'BbsQaDetail',
        hidden: true,
        meta: { title: '问题详情', activeMenu: '/qa' }
      },
      {
        path: 'qa/archive',
        component: () => import('@/views/bbs/qa/archive'),
        name: 'BbsQaArchive',
        meta: { title: '问答知识库', activeMenu: '/qa' }
      },
      {
        path: 'idea',
        component: () => import('@/views/bbs/idea/index'),
        name: 'BbsIdea',
        meta: { title: '创意提案', icon: 'message', affix: false }
      },
      {
        path: 'idea/publish',
        component: () => import('@/views/bbs/idea/publish'),
        name: 'BbsIdeaPublish',
        hidden: true,
        meta: { title: '提交创意提案', activeMenu: '/idea' }
      },
      {
        path: 'idea/detail/:ideaId',
        component: () => import('@/views/bbs/idea/detail'),
        name: 'BbsIdeaDetail',
        hidden: true,
        meta: { title: '提案详情', activeMenu: '/idea' }
      },
      {
        path: 'idea/wall',
        component: () => import('@/views/bbs/idea/wall'),
        name: 'BbsIdeaWall',
        meta: { title: '成果公示墙', activeMenu: '/idea' }
      },
      {
        path: 'collect',
        component: () => import('@/views/bbs/collect/index'),
        name: 'BbsCollect',
        hidden: true,
        meta: { title: '在线收集', icon: 'message', affix: false }
      },
      {
        path: 'collect/participate/:activityId',
        component: () => import('@/views/bbs/collect/participate'),
        name: 'BbsCollectParticipate',
        hidden: true,
        meta: { title: '参与活动', activeMenu: '/collect' }
      },
      {
        path: 'collect/stats/:activityId',
        component: () => import('@/views/bbs/collect/stats'),
        name: 'BbsCollectStats',
        hidden: true,
        meta: { title: '活动数据', activeMenu: '/collect' }
      },
      {
        path: 'collect/topic/:activityId',
        component: () => import('@/views/bbs/collect/topic'),
        name: 'BbsCollectTopic',
        hidden: true,
        meta: { title: '专题活动', activeMenu: '/collect' }
      },
      {
        path: 'post/:postId',
        component: () => import('@/views/bbs/post'),
        name: 'BbsPost',
        hidden: true,
        meta: { title: '帖子详情', activeMenu: '/bbs/index' }
      },
      {
        path: 'usercenter',
        component: () => import('@/views/bbs/usercenter'),
        name: 'BbsUserCenter',
        meta: { title: '个人中心', activeMenu: '/bbs/index' }
      },
      {
        path: 'notification',
        component: () => import('@/views/bbs/notification'),
        name: 'BbsNotification',
        meta: { title: '通知中心', activeMenu: '/bbs/index' }
      }
    ]
  },
  {
    path: '/ssologin',
    component: () => import('@/views/login/ssologin'),
    hidden: true
  },
  {
    path: '/autologin',
    component: () => import('@/views/login/ssologin'),
    hidden: true
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/admin/sys-user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/admin/sys-role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/admin/sys-dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  }
]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push
let routerReplace = Router.prototype.replace
// push
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
// replace
Router.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

export default new Router({
  mode: 'history', // 去掉url中的#
  base: "bbs",
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
