<template>
  <div class="bbs-mobile-layout">
    <!-- 顶部导航栏 -->
    <div class="bbs-mobile-header">
      <div class="header-top">
        <div class="logo" @click="goHome">Smart BBS</div>
        <div class="header-actions">
          <el-button type="primary" icon="el-icon-edit" size="mini" circle @click="handlePublish"
            class="publish-btn"></el-button>
          <lang-select class="lang-select-wrapper"></lang-select>
          <el-button v-if="!isLoggedIn" type="text" size="small" @click="handleLogin" class="login-btn">{{
            $t('bbs.login') }}</el-button>
          <el-dropdown v-else trigger="click" @command="handleUserCommand" class="user-dropdown">
            <div class="user-info">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                <el-avatar :src="defaultAvatar" :size="32"></el-avatar>
              </el-badge>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item disabled>
                <span>{{ $t('common.settings') }}</span>
              </el-dropdown-item>
              <el-dropdown-item command="notification">
                <span style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                  <span><i class="el-icon-bell"></i> {{ $t('bbs.notification.notificationCenter') }}</span>
                  <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="item"></el-badge>
                </span>
              </el-dropdown-item>
              <el-dropdown-item v-if="hasPermissions" command="profile">{{ $t('bbs.admin') }}</el-dropdown-item>
              <el-dropdown-item command="logout" divided>{{ $t('bbs.logout') }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="searchKeyword" :placeholder="$t('bbs.searchPlaceholder')" clearable
          @keyup.enter.native="handleSearch" @clear="handleSearchClear">
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
      </div>

      <!-- 导航标签 -->
      <div class="nav-tabs">
        <div class="nav-tab" :class="{ active: currentNav === 'home' }" @click="handleNavClick('home')">
          {{ $t('bbs.home') }}
        </div>
        <div class="nav-tab" :class="{ active: currentNav === 'topic' }" @click="handleNavClick('topic')">
          专题
        </div>
        <!-- <div 
          class="nav-tab" 
          :class="{ active: currentNav === 'hot' }"
          @click="handleNavClick('hot')"
        >
          热门
        </div>
        <div 
          class="nav-tab" 
          :class="{ active: currentNav === 'latest' }"
          @click="handleNavClick('latest')"
        >
          最新
        </div>
        <el-dropdown @command="handleCategoryNav" trigger="click" class="category-dropdown">
          <div class="nav-tab">
            分类 <i class="el-icon-arrow-down"></i>
          </div>
          <el-dropdown-menu slot="dropdown" class="category-menu">
            <el-dropdown-item 
              :command="null"
              :class="{ active: activeCategory === null }"
            >
              全部 ({{ totalPosts }})
            </el-dropdown-item>
            <el-dropdown-item 
              v-for="category in categoryList" 
              :key="category.categoryId"
              :command="category.categoryId"
              :class="{ active: activeCategory === category.categoryId }"
            >
              {{ category.categoryName }} ({{ category.postCount }})
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown> -->
      </div>
    </div>

    <!-- 主体内容区域 -->
    <div class="bbs-mobile-body">
      <main class="bbs-mobile-main">
        <router-view ref="routerView" />
      </main>
    </div>

    <!-- 底部导航栏 -->
    <div class="bbs-mobile-footer">

      <div class="footer-item" :class="{ active: currentNav === 'topic' }" @click="handleNavClick('topic')">
        <i class="el-icon-star-on"></i>
        <span>专题</span>
      </div>
      <div class="footer-item" :class="{ active: currentNav === 'home' }" @click="handleNavClick('home')">
        <i class="el-icon-menu"></i>
        <span>{{ $t('bbs.home') }}</span>
      </div>
      <div class="footer-item" @click="handlePublish">
        <i class="el-icon-edit-outline"></i>
        <span>{{ $t('bbs.publish') }}</span>
      </div>
      <div class="footer-item" :class="{ active: $route.path === '/usercenter' }" @click="handleUserCenter">
        <i class="el-icon-user"></i>
        <span>{{ $t('bbs.userCenter.userCenter') || '个人中心' }}</span>
      </div>
      <!-- <div class="footer-item" @click="showCategoryDrawer = true">
        <i class="el-icon-menu"></i>
        <span>分类</span>
      </div> -->
    </div>

    <!-- 分类抽屉 -->
    <el-drawer :title="$t('bbs.selectCategory')" :visible.sync="showCategoryDrawer" direction="btt" size="60%"
      :with-header="true">
      <div class="category-drawer-content">
        <div class="drawer-category-item" :class="{ active: activeCategory === null }"
          @click="handleCategorySelect(null)">
          <span class="category-name">{{ $t('bbs.all') }}</span>
          <span class="category-count">({{ totalPosts }})</span>
        </div>
        <div v-for="category in categoryList" :key="category.categoryId" class="drawer-category-item"
          :class="{ active: activeCategory === category.categoryId }"
          @click="handleCategorySelect(category.categoryId)">
          <span class="category-name">{{ category.categoryName }}</span>
          <span class="category-count">({{ category.postCount }})</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getHotCategories } from "@/api/bbs/category";
import { getHotPosts } from "@/api/bbs/post";
import { getUnreadCount } from "@/api/bbs/notification";
import { getToken } from "@/utils/auth";
import { mapGetters } from 'vuex'
import LangSelect from '@/components/LangSelect'

export default {
  name: 'BbsMobileLayout',
  components: {
    LangSelect
  },
  data() {
    return {
      categoryList: [],
      hotPosts: [],
      totalPosts: 0,
      searchKeyword: '',
      activeCategory: null,
      currentNav: 'home',
      showCategoryDrawer: false,
      unreadCount: 0,
      notificationTimer: null,
      defaultAvatar:
        "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
    };
  },
  computed: {
    ...mapGetters(['user', 'permissions', 'roles']),
    isLoggedIn() {
      return !!getToken();
    },
    userInfo() {
      return this.$store.state.user || {};
    },
    hasPermissions() {
      const permissions = this.permissions || [];
      const roles = this.roles || [];
      return permissions.includes('*:*:*') || roles.includes('admin');
    }
  },
  created() {
    this.getCategoryList();
    this.getHotPosts();
    this.updateFromRoute();
    // 监听帖子发布事件，更新分类数目
    this.$root.$on('bbs:post-published', this.handlePostPublished);
    // 如果已登录，获取未读消息数
    if (this.isLoggedIn) {
      this.getUnreadCount();
      // 每30秒刷新一次未读消息数
      this.notificationTimer = setInterval(() => {
        this.getUnreadCount();
      }, 30000);
    }
  },
  beforeDestroy() {
    // 移除事件监听
    this.$root.$off('bbs:post-published', this.handlePostPublished);
    // 清除定时器
    if (this.notificationTimer) {
      clearInterval(this.notificationTimer);
    }
  },
  watch: {
    '$route'(to) {
      this.updateFromRoute();
    }
  },
  methods: {
    updateFromRoute() {
      if (this.$route.path === '/topic') {
        this.currentNav = 'topic';
      }
      if (this.$route.query.categoryId) {
        this.activeCategory = parseInt(this.$route.query.categoryId);
      } else {
        this.activeCategory = null;
      }
      if (this.$route.path !== '/topic' && this.$route.query.nav) {
        this.currentNav = this.$route.query.nav;
      } else if (this.$route.path !== '/topic') {
        this.currentNav = 'home';
      }
      if (this.$route.query.keyword) {
        this.searchKeyword = this.$route.query.keyword;
      }
    },
    getCategoryList() {
      getHotCategories().then(response => {
        this.categoryList = response.data || response.rows || [];
        this.totalPosts = this.categoryList.reduce((sum, cat) => sum + (cat.postCount || 0), 0);
      });
    },
    getHotPosts() {
      getHotPosts(5).then(response => {
        this.hotPosts = response.data || [];
      });
    },
    handleCategoryChange(categoryId) {
      this.activeCategory = categoryId;
      this.showCategoryDrawer = false;
      this.$router.push({
        path: '/index',
        query: {
          ...this.$route.query,
          categoryId: categoryId || undefined,
          nav: this.currentNav === 'home' ? undefined : this.currentNav
        }
      });
    },
    handleCategorySelect(categoryId) {
      this.handleCategoryChange(categoryId);
    },
    handleCategoryNav(categoryId) {
      this.handleCategoryChange(categoryId);
    },
    handleNavClick(nav) {
      this.currentNav = nav;
      if (nav === 'topic') {
        this.$router.push({ path: '/topic' });
        return;
      }
      this.$router.push({
        path: '/index',
        query: {
          ...this.$route.query,
          nav: nav === 'home' ? undefined : nav,
          categoryId: this.activeCategory || undefined
        }
      });
    },
    handleSearch() {
      this.$router.push({
        path: '/index',
        query: {
          ...this.$route.query,
          keyword: this.searchKeyword || undefined
        }
      });
    },
    handleSearchClear() {
      this.searchKeyword = '';
      this.$router.push({
        path: '/index',
        query: {
          ...this.$route.query,
          keyword: undefined
        }
      });
    },
    handlePostClick(postId) {
      // 保存当前页数到路由参数，以便返回时恢复
      // 从当前路由获取页数信息（如果从文章列表页跳转）
      const currentQuery = this.$route.query;
      this.$router.push({
        path: "/post/" + postId,
        query: {
          page: currentQuery.page || undefined,
          categoryId: currentQuery.categoryId || undefined,
          nav: currentQuery.nav || undefined,
          keyword: currentQuery.keyword || undefined
        }
      });
    },
    handleLogin() {
      this.$router.push({ path: "/login" });
    },
    handlePublish() {
      // 统一使用路由参数方式触发发布对话框
      if (this.$route.path === '/index' || this.$route.path.startsWith('/index')) {
        // 如果已经在首页，通过路由参数触发
        this.$router.push({
          path: '/index',
          query: {
            ...this.$route.query,
            publish: 'true'
          }
        });
      } else {
        // 如果不在首页，跳转到首页并传递参数
        this.$router.push({ path: '/index', query: { publish: 'true' } });
      }
    },
    handleUserCommand(command) {
      if (command === 'profile') {
        this.$router.push({ path: "/admin/article" });
      } else if (command === 'notification') {
        this.$router.push({ path: "/notification" });
      } else if (command === 'logout') {
        this.$confirm('确定注销并退出系统吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('LogOut').then(() => {
            this.$router.push({ path: '/login' });
          });
        }).catch(() => {});
      }
    },
    goHome() {
      this.$router.push({ path: "/index" });
    },
    handlePostPublished(categoryId) {
      // 当帖子发布成功后，重新获取分类列表以更新数目
      this.getCategoryList();
    },
    handleUserCenter() {
      this.$router.push({ path: "/usercenter" });
    },
    getUnreadCount() {
      if (!this.isLoggedIn) {
        return;
      }
      getUnreadCount().then(response => {
        if (response.code === 200) {
          this.unreadCount = response.data || 0;
        }
      }).catch(() => {
        // 忽略错误，避免影响其他功能
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.bbs-mobile-layout {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  padding-bottom: 60px; // 为底部导航栏留出空间
}

/* 顶部导航栏 */
.bbs-mobile-header {
  background: #e6f7ff;
  border-bottom: 1px solid #d9d9d9;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 15px;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
  cursor: pointer;
  user-select: none;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.publish-btn {
  width: 36px;
  height: 36px;
  padding: 0;
}

.login-btn {
  padding: 5px 10px;
  font-size: 14px;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  position: relative;
  cursor: pointer;
}

.notification-badge {
  .el-badge__content {
    border: 2px solid #fff;
  }
}

.search-bar {
  padding: 0 15px 10px;
}

.search-bar .el-input {
  width: 100%;
}

/* 导航标签 */
.nav-tabs {
  display: flex;
  align-items: center;
  padding: 0 15px 10px;
  gap: 10px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    display: none;
  }
}

.nav-tab {
  flex-shrink: 0;
  padding: 6px 12px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  white-space: nowrap;

  &.active {
    background: #1890ff;
    color: white;
    font-weight: bold;
  }
}

.category-dropdown {
  .nav-tab {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.category-menu {
  .el-dropdown-menu__item {
    &.active {
      color: #1890ff;
      background: #f0f9ff;
    }
  }
}

/* 主体内容区域 */
.bbs-mobile-body {
  flex: 1;
  width: 100%;
  overflow-y: auto;
}

.bbs-mobile-main {
  width: 100%;
  min-height: calc(100vh - 200px);
  padding: 10px;
  box-sizing: border-box;
}

/* 底部导航栏 */
.bbs-mobile-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 1000;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.footer-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 5px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;

  i {
    font-size: 20px;
  }

  span {
    font-size: 12px;
  }

  &.active {
    color: #1890ff;

    i {
      color: #1890ff;
    }
  }

  &:active {
    opacity: 0.7;
  }
}

/* 分类抽屉 */
.category-drawer-content {
  padding: 10px;
}

.drawer-category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e0e0e0;

  &:active {
    opacity: 0.7;
  }

  &.active {
    background: #1890ff;
    color: white;
    border-color: #1890ff;

    .category-count {
      color: rgba(255, 255, 255, 0.8);
    }
  }

  .category-name {
    font-size: 16px;
    font-weight: 500;
  }

  .category-count {
    font-size: 14px;
    color: #999;
  }
}
</style>
