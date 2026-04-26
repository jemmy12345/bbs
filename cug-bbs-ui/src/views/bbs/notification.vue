<template>
  <div class="notification-container">
    <div class="notification-header">
      <h2 class="notification-title">{{ $t('bbs.notification.title') }}</h2>
      <div class="header-actions">
        <el-button 
          type="success" 
          size="small" 
          @click="handleMarkAllRead"
          :disabled="unreadCount === 0"
        >
          {{ $t('bbs.notification.markAllAsRead') }}
        </el-button>
      </div>
    </div>

    <div class="notification-list" v-loading="loading">
      <div 
        v-for="notification in notificationList" 
        :key="notification.notificationId"
        class="notification-item"
        :class="{ 'unread': notification.isRead === '0' }"
        @click="handleNotificationClick(notification)"
      >
        <div class="notification-content">
          <div class="notification-text">
            {{ getFullNotificationText(notification) }}
          </div>
          <div class="notification-time">
            ({{ parseTime(notification.createTime, '{y}-{m}-{d} {h}:{i}') }})
          </div>
        </div>
        <div v-if="notification.isRead === '0'" class="unread-dot"></div>
      </div>
      
      <div v-if="notificationList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-bell"></i>
        <p>{{ $t('bbs.notification.noNotifications') }}</p>
      </div>
    </div>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listNotification, markAsRead, markAllAsRead, getUnreadCount } from "@/api/bbs/notification";
import { parseTime } from "@/utils/ruoyi";
import Pagination from "@/components/Pagination";

export default {
  name: 'BbsNotification',
  components: {
    Pagination
  },
  data() {
    return {
      notificationList: [],
      loading: false,
      unreadCount: 0,
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize:10
      }
    };
  },
  created() {
    this.getList();
    this.getUnreadCount();
  },
  activated() {
    // 如果是从其他页面返回，刷新未读数量
    this.getUnreadCount();
  },
  methods: {
    getList() {
      this.loading = true;
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        isRead: this.$route.query.isRead || undefined
      };
      listNotification(params).then(response => {
        if (response.code === 200) {
          this.notificationList = response.rows || response.data || [];
          this.total = response.total || 0;
        }
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    getUnreadCount() {
      getUnreadCount().then(response => {
        if (response.code === 200) {
          this.unreadCount = response.data || 0;
        }
      });
    },
    getFromUserDisplay(notification) {
      // 如果是评论或回复通知，且是匿名评论，显示匿名用户
      if ((notification.type === '1' || notification.type === '2') && notification.isAnonymous === '1') {
        return this.$t('bbs.anonymousUser') || '匿名用户';
      }
      // 优先显示昵称，如果没有则显示用户名，最后显示用户ID
      if (notification.fromNickName) {
        return notification.fromNickName;
      }
      // 如果fromUserId是邮箱格式，显示邮箱（可以部分隐藏）
      if (notification.fromUserId && notification.fromUserId.includes('@')) {
        const email = notification.fromUserId;
        const parts = email.split('@');
        if (parts[0].length > 3) {
          return parts[0].substring(0, 1) + '***@' + parts[1];
        }
        return email;
      }
      return notification.fromUserId || this.$t('bbs.notification.unknownUser');
    },
    getActionText(notification) {
      const type = notification.type;
      if (type === '1') {
        return this.$t('bbs.notification.commentedInArticle');
      } else if (type === '2') {
        return this.$t('bbs.notification.mentionedInReply');
      } else if (type === '3') {
        return this.$t('bbs.notification.likedYourArticle');
      } else if (type === '4') {
        return this.$t('bbs.notification.collectedYourArticle');
      }
      return '';
    },
    getFullNotificationText(notification) {
      const fromUser = this.getFromUserDisplay(notification);
      const type = notification.type;
      const postTitle = notification.content;
      
      if (type === '1') {
        return this.$t('bbs.notification.commentedInArticleFull', { user: fromUser, title: postTitle });
      } else if (type === '2') {
        return this.$t('bbs.notification.mentionedInReplyFull', { user: fromUser, title: postTitle });
      } else if (type === '3') {
        return this.$t('bbs.notification.likedYourArticleFull', { user: fromUser, title: postTitle });
      } else if (type === '4') {
        return this.$t('bbs.notification.collectedYourArticleFull', { user: fromUser, title: postTitle });
      }
      return notification.title || '';
    },
    handleNotificationClick(notification) {
      // 如果未读，标记为已读
      if (notification.isRead === '0') {
        markAsRead(notification.notificationId).then(() => {
          notification.isRead = '1';
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        });
      }
      
      // 跳转到对应的文章详情页
      const postId = notification.targetId;
      if (postId) {
        // 如果通知类型是评论或回复，跳转到评论区
        if (notification.type === '1' || notification.type === '2') {
          this.$router.push({ 
            path: "/post/" + postId,
            query: { scrollToComment: 'true' }
          });
        } else {
          // 点赞和收藏，直接跳转到文章详情页
          this.$router.push({ 
            path: "/post/" + postId
          });
        }
      }
    },
    handleMarkAllRead() {
      this.$modal.confirm(this.$t('bbs.notification.confirmMarkAllRead')).then(() => {
        markAllAsRead().then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess(this.$t('bbs.notification.markAllReadSuccess'));
            // 更新列表中的已读状态
            this.notificationList.forEach(item => {
              item.isRead = '1';
            });
            this.unreadCount = 0;
          }
        });
      });
    },
    handleSettings() {
      this.$modal.msgInfo(this.$t('bbs.notification.settingsInDevelopment'));
    },
    parseTime
  }
};
</script>

<style lang="scss" scoped>
.notification-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  min-height: calc(100vh - 200px);
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 20px;
}

.notification-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.settings-btn {
  color: #666;
  padding: 0;
}

.notification-list {
  min-height: 400px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
  margin-bottom: 10px;
  &:hover {
    background-color: #f5f5f5;
  }

  &.unread {
    background-color: #f0f9ff;
  }
}

.notification-content {
  flex: 1;
}

.notification-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 6px;
  cursor: pointer;

  &:hover {
    color: #1890ff;
  }
}

.notification-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
  margin-left: 10px;
  margin-top: 6px;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;

  i {
    font-size: 48px;
    margin-bottom: 16px;
    display: block;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

.load-more {
  text-align: center;
  padding: 20px;
  color: #1890ff;
  cursor: pointer;
  font-size: 14px;

  &:hover {
    color: #40a9ff;
  }
}

@media (max-width: 768px) {
  .notification-container {
    padding: 15px;
  }

  .notification-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
