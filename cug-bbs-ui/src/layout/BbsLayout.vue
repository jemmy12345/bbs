<template>
  <div class="bbs-layout">
    <!-- 顶部导航栏 -->
    <div class="bbs-header">
      <div class="header-content">
        <div class="nav-left">
          <div class="logo" @click="goHome">CUGer BBS</div>
          <div class="nav-menu">
            <span class="nav-item" :class="{ active: currentNav === 'home' }" @click="handleNavClick('home')">
              {{ $t('bbs.home') }}
            </span>
            <!-- <el-dropdown @command="handleCategoryNav" trigger="hover">
              <span class="nav-item">
                分类 <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item 
                  v-for="category in categoryList" 
                  :key="category.categoryId"
                  :command="category.categoryId"
                >
                  {{ category.categoryName }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown> -->
            <!-- <span 
              class="nav-item" 
              :class="{ active: currentNav === 'hot' }"
              @click="handleNavClick('hot')"
            >
              热门
            </span>
            <span 
              class="nav-item" 
              :class="{ active: currentNav === 'latest' }"
              @click="handleNavClick('latest')"
            >
              最新
            </span> -->
          </div>
        </div>
        <div class="nav-right">
          <el-input v-model="searchKeyword" :placeholder="$t('bbs.searchPlaceholderShort')" class="search-input"
            clearable @keyup.enter.native="handleSearch" @clear="handleSearchClear">
            <el-button slot="append" type="primary" icon="el-icon-search" @click="handleSearch">{{ $t('common.search')
            }}</el-button>
          </el-input>
          <el-button type="primary" icon="el-icon-edit" @click="handlePublish">{{ $t('bbs.publish') }}</el-button>
          <lang-select class="lang-select-wrapper"></lang-select>
          <el-button type="primary" @click="handleLogin" v-if="!isLoggedIn">{{ $t('bbs.login') }}</el-button>
          <el-dropdown v-else trigger="hover" @command="handleUserCommand">
            <div class="user-info">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                <el-avatar :src="defaultAvatar" :size="32"></el-avatar>
              </el-badge>
              <span class="user-name">{{ $t('common.settings') }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="usercenter">{{ $t('bbs.userCenter.userCenter') }}</el-dropdown-item>
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
    </div>

    <!-- 主体内容区域 -->
    <div class="bbs-body">
      <!-- <aside class="bbs-sidebar" v-if="!isMobile">
        <div class="sidebar-section">
          <div class="section-title">热门分类</div>
          <div class="category-list">
            <div 
              class="category-item" 
              :class="{ active: activeCategory === null }"
              @click="handleCategoryChange(null)"
            >
              <span class="category-name">全部</span>
              <span class="category-count">({{ totalPosts }})</span>
            </div>
            <div 
              v-for="category in categoryList" 
              :key="category.categoryId"
              class="category-item"
              :class="{ active: activeCategory === category.categoryId }"
              @click="handleCategoryChange(category.categoryId)"
            >
              <span class="category-name">{{ category.categoryName }}</span>
              <span class="category-count">({{ category.postCount }})</span>
            </div>
          </div>
        </div>

        <div class="sidebar-section hot-posts-section">
          <div class="section-title">热门文章</div>
          <div class="hot-posts-list">
            <div 
              v-for="(post, index) in hotPosts" 
              :key="post.postId"
              class="hot-post-item"
              @click="handlePostClick(post.postId)"
            >
              <span class="hot-post-index">{{ index + 1 }}</span>
              <span class="hot-post-title">{{ post.title }}</span>
            </div>
          </div>
        </div>
      </aside> -->

      <!-- 主内容区域 -->
      <main class="bbs-main">
        <router-view ref="routerView" />
      </main>
    </div>
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
  name: 'BbsLayout',
  components: {
    LangSelect
  },
  data() {
    return {
      categoryList: [],
      hotPosts: [],
      totalPosts: 0,
      searchKeyword: '',
      isMobile: false,
      activeCategory: null,
      currentNav: 'home',
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
    this.checkMobile();
    window.addEventListener('resize', this.checkMobile);
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
    window.removeEventListener('resize', this.checkMobile);
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
    checkMobile() {
      this.isMobile = window.innerWidth < 768;
    },
    updateFromRoute() {
      if (this.$route.query.categoryId) {
        this.activeCategory = parseInt(this.$route.query.categoryId);
      } else {
        this.activeCategory = null;
      }
      if (this.$route.query.nav) {
        this.currentNav = this.$route.query.nav;
      } else {
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
      this.$router.push({
        path: '/index',
        query: {
          ...this.$route.query,
          categoryId: categoryId || undefined,
          nav: this.currentNav === 'home' ? undefined : this.currentNav
        }
      });
    },
    handleCategoryNav(categoryId) {
      this.handleCategoryChange(categoryId);
    },
    handleNavClick(nav) {
      this.currentNav = nav;
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
      if (command === 'usercenter') {
        this.$router.push({ path: "/usercenter" });
      } else if (command === 'notification') {
        this.$router.push({ path: "/notification" });
      } else if (command === 'profile') {
        this.$router.push({ path: "/admin/article" });
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
    },
    handlePostPublished(categoryId) {
      // 当帖子发布成功后，重新获取分类列表以更新数目
      this.getCategoryList();
    }
  }
};
</script>

<style lang="scss" scoped>
.bbs-layout {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.bbs-header {
  background: #e6f7ff;
  border-bottom: 1px solid #d9d9d9;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #1890ff;
  cursor: pointer;
  user-select: none;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  cursor: pointer;
  color: #333;
  font-size: 14px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
  user-select: none;
}

.nav-item:hover {
  color: #1890ff;
  background: #f0f9ff;
}

.nav-item.active {
  color: #1890ff;
  font-weight: bold;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.lang-select-wrapper {
  margin-right: 5px;
}

.search-input {
  width: 300px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f0f0f0;
}

.notification-badge {
  .el-badge__content {
    border: 2px solid #fff;
  }
}

.user-name {
  font-size: 14px;
  color: #333;
}

/* 主体内容区域 */
.bbs-body {
  flex: 1;
  max-width: 1200px;
  margin: 20px auto 0;
  padding: 0 20px 20px;
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 60px);
  width: 100%;
  box-sizing: border-box;
}

/* 左侧边栏 */
.bbs-sidebar {
  width: 250px;
  flex-shrink: 0;
}

.sidebar-section {
  background: white;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #ff4757;
}

.category-list {
  display: flex;
  flex-direction: column;
}

.category-item {
  padding: 12px 10px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 5px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.category-item:hover {
  background: #f0f9ff;
}

.category-item.active {
  background: #1890ff;
  color: white;
}

.category-name {
  font-size: 14px;
}

.category-count {
  font-size: 12px;
  color: #999;
}

.category-item.active .category-count {
  color: rgba(255, 255, 255, 0.8);
}

/* 热门文章 */
.hot-posts-list {
  display: flex;
  flex-direction: column;
}

.hot-post-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.hot-post-item:last-child {
  border-bottom: none;
}

.hot-post-item:hover {
  color: #1890ff;
}

.hot-post-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: #999;
  margin-right: 10px;
  flex-shrink: 0;
}

.hot-post-item:nth-child(1) .hot-post-index,
.hot-post-item:nth-child(2) .hot-post-index,
.hot-post-item:nth-child(3) .hot-post-index {
  background: #ff4757;
  color: white;
}

.hot-post-title {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主内容区域 */
.bbs-main {
  flex: 1;
  min-width: 600px;
  background: transparent;
  position: relative;
  transition: width 0.3s ease;
}

.bbs-main>* {
  width: 100%;
  min-width: 0;
}

/* 中等尺寸适配 (768px - 1024px) */
@media screen and (max-width: 1024px) and (min-width: 769px) {
  .header-content {
    padding: 0 15px;
    height: 60px;
  }

  .nav-left {
    gap: 15px;
    flex-shrink: 0;
  }

  .logo {
    font-size: 18px;
    white-space: nowrap;
  }

  .nav-menu {
    gap: 10px;
    flex-wrap: nowrap;
  }

  .nav-item {
    font-size: 13px;
    padding: 5px 8px;
    white-space: nowrap;
  }

  .search-input {
    width: 180px;
    flex-shrink: 1;
  }

  .nav-right {
    gap: 8px;
    flex-shrink: 0;
  }

  .nav-right .el-button {
    padding: 8px 12px;
    font-size: 13px;
    white-space: nowrap;
  }

  .nav-right .el-button .el-icon-edit {
    display: none;
  }

  .user-info {
    padding: 5px 8px;
  }

  .user-name {
    font-size: 13px;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .bbs-body {
    padding: 0 15px 20px;
  }

  .bbs-main {
    min-width: 400px;
  }
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .header-content {
    padding: 10px;
    height: auto;
    flex-direction: column;
    gap: 10px;
  }

  .nav-left {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    order: 1;
  }

  .logo {
    font-size: 18px;
    flex-shrink: 0;
  }

  .nav-menu {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }

  .nav-item {
    font-size: 13px;
    padding: 4px 8px;
  }

  .nav-menu .el-dropdown {
    font-size: 13px;
  }

  .nav-right {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 10px;
    order: 3;
  }

  .search-input {
    width: 100%;
    order: 2;
  }

  .nav-right .el-button {
    width: 100%;
    margin: 0 !important;
    height: 40px;
  }

  .nav-right .el-button+.el-button {
    margin-left: 0 !important;
    margin-top: 0;
  }

  .nav-right .el-dropdown {
    width: 100%;
  }

  .nav-right .user-info {
    width: 100%;
    justify-content: center;
    padding: 8px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    background: white;
  }

  .user-info {
    width: 100%;
    justify-content: center;
    padding: 8px;
  }

  .bbs-body {
    padding: 0 10px;
    flex-direction: column;
  }

  .bbs-sidebar {
    width: 100%;
  }

  .bbs-main {
    min-width: 100%;
    width: 100%;
  }
}
</style>
