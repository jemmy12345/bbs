<template>
  <div v-if="queue.length" class="notice-popup-wrap">
    <div v-for="n in queue" :key="n.noticeId" class="notice-popup">
      <div class="notice-popup-head">
        <el-tag size="mini" :type="n.noticeType === '2' ? 'success' : 'warning'">
          {{ n.noticeType === '2' ? '公告' : '通知' }}
        </el-tag>
        <span class="notice-popup-title" :title="n.noticeTitle">{{ n.noticeTitle }}</span>
      </div>
      <div class="notice-popup-body ql-editor" v-html="n.noticeContent"></div>
      <div class="notice-popup-foot">
        <el-button type="primary" size="mini" @click="markRead(n)">已读</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { listUnreadNotice, readNotice } from '@/api/system/notice'
import { getToken } from '@/utils/auth'

export default {
  name: 'NoticePopup',
  data() {
    return { queue: [], loadedFor: '' }
  },
  watch: {
    // 登录后（路由离开登录页、令牌存在）拉取一次未读公告
    '$route.path'() {
      this.load()
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    load() {
      const token = getToken()
      const path = this.$route.path
      if (!token || ['/login', '/register'].includes(path)) {
        this.queue = []
        this.loadedFor = ''
        return
      }
      if (this.loadedFor === token) return
      this.loadedFor = token
      listUnreadNotice()
        .then(res => {
          this.queue = res.data || []
        })
        .catch(() => {
          this.loadedFor = ''
        })
    },
    markRead(n) {
      readNotice(n.noticeId).finally(() => {
        this.queue = this.queue.filter(i => i.noticeId !== n.noticeId)
      })
    }
  }
}
</script>

<style scoped>
.notice-popup-wrap {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 3000;
  display: flex;
  flex-direction: column-reverse;
  gap: 12px;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
}
.notice-popup {
  width: 340px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
  padding: 12px 14px;
}
.notice-popup-head {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.notice-popup-title {
  margin-left: 8px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-popup-body {
  max-height: 160px;
  overflow-y: auto;
  font-size: 13px;
  color: #606266;
  padding: 0;
  word-break: break-all;
}
.notice-popup-foot {
  text-align: right;
  margin-top: 8px;
}
</style>
