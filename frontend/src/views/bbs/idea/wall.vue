<template>
  <div class="idea-wall-page">
    <div class="hero">
      <h2>创新成果公示墙</h2>
      <p>沉淀企业创新成果，展示创新荣誉</p>
    </div>

    <div class="toolbar">
      <el-select v-model="query.sortBy" size="small" @change="getList">
        <el-option label="最新公示" value="latest" />
        <el-option label="点赞最多" value="like" />
        <el-option label="效果优先" value="effect" />
      </el-select>
      <el-input v-model="query.keyword" clearable size="small" style="width:220px" placeholder="搜索公示内容" @keyup.enter.native="doSearch">
        <el-button slot="append" icon="el-icon-search" @click="doSearch" />
      </el-input>
    </div>

    <div class="wall-grid" v-loading="loading">
      <div class="wall-card" v-for="item in list" :key="item.ideaId">
        <div class="head">
          <el-tag size="mini" type="success">已公示</el-tag>
          <el-tag size="mini" type="warning">{{ item.honorBadge || '创新贡献' }}</el-tag>
        </div>
        <div class="title" @click="goDetail(item.ideaId)">{{ item.title }}</div>
        <div class="summary">{{ item.resultSummary || '已完成成果公示' }}</div>
        <div class="meta">
          <span>{{ item.submitNickName }}</span>
          <span>激励 {{ item.incentivePoints || 0 }}</span>
        </div>
        <div class="ops">
          <el-button type="text" icon="el-icon-star-on" @click="doLike(item)">
            {{ hasLiked(item.ideaId) ? '已点赞' : '点赞' }} {{ item.likeCount || 0 }}
          </el-button>
          <el-button type="text" icon="el-icon-share" @click="doShare(item)">
            {{ hasShared(item.ideaId) ? '已分享' : '分享' }} {{ item.shareCount || 0 }}
          </el-button>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无公示成果" />
    </div>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="query.pageNum"
      :limit.sync="query.pageSize"
      @pagination="getList"
    />

    <!-- 分享弹窗 -->
    <el-dialog title="分享成果" :visible.sync="shareDialogVisible" width="420px" append-to-body>
      <p style="margin:0 0 10px;color:#606266;font-size:13px;">复制下方链接，将此成果分享给同事：</p>
      <el-input ref="shareInput" :value="shareUrl" readonly>
        <el-button slot="append" icon="el-icon-document-copy" @click="copyShareUrl">复制</el-button>
      </el-input>
      <div style="margin-top:14px;display:flex;gap:10px;justify-content:flex-end">
        <el-button size="small" @click="shareDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIdeaWall, likeIdea, shareIdea } from '@/api/bbs/idea'

export default {
  name: 'BbsIdeaWall',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: { sortBy: 'latest', keyword: '', pageNum: 1, pageSize: 12 },
      shareDialogVisible: false,
      shareUrl: '',
      currentShareIdeaId: null,
      actedState: this.loadActedState(),
    }
  },
  created() { this.getList() },
  methods: {
    loadActedState() {
      try {
        const raw = window.localStorage.getItem('bbs-idea-wall-actions')
        const parsed = raw ? JSON.parse(raw) : {}
        return {
          liked: parsed.liked || {},
          shared: parsed.shared || {},
        }
      } catch (e) {
        return { liked: {}, shared: {} }
      }
    },
    persistActedState() {
      window.localStorage.setItem('bbs-idea-wall-actions', JSON.stringify(this.actedState))
    },
    hasLiked(ideaId) {
      const item = this.list.find(candidate => candidate.ideaId === ideaId)
      if (item) {
        return item.likedByCurrentUser === '1'
      }
      return false
    },
    hasShared(ideaId) {
      return !!this.actedState.shared[String(ideaId)]
    },
    getList() {
      this.loading = true
      listIdeaWall(this.query).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    doSearch() {
      this.query.pageNum = 1
      this.getList()
    },
    doLike(item) {
      likeIdea(item.ideaId).then((res) => {
        if (res && res.data === true) {
          item.likeCount = (item.likeCount || 0) + 1
          item.likedByCurrentUser = '1'
          this.$message.success('点赞成功')
        } else {
          item.likeCount = Math.max((item.likeCount || 0) - 1, 0)
          item.likedByCurrentUser = '0'
          this.$message.success('已取消点赞')
        }
      })
    },
    doShare(item) {
      const resolved = this.$router.resolve({ path: `/idea/detail/${item.ideaId}` })
      this.shareUrl = `${window.location.origin}${resolved.href}`
      this.currentShareIdeaId = item.ideaId

      const preferNativeShare =
        typeof navigator.share === 'function' &&
        typeof window.matchMedia === 'function' &&
        window.matchMedia('(pointer: coarse)').matches

      if (preferNativeShare) {
        navigator.share({
          title: item.title,
          url: this.shareUrl,
        }).then(() => {
          this.recordShare(item)
        }).catch((error) => {
          if (error && error.name === 'AbortError') {
            return
          }
          this.shareDialogVisible = true
        })
        return
      }

      this.shareDialogVisible = true
    },
    copyShareUrl() {
      const onSuccess = () => {
        this.$message.success('链接已复制到剪贴板')
        const item = this.list.find(candidate => candidate.ideaId === this.currentShareIdeaId)
        if (item) {
          this.recordShare(item)
        }
      }
      if (navigator.clipboard) {
        navigator.clipboard.writeText(this.shareUrl).then(onSuccess).catch(() => {
          const input = this.$refs.shareInput.$el.querySelector('input')
          input.select()
          document.execCommand('copy')
          onSuccess()
        })
        return
      }
      const input = this.$refs.shareInput.$el.querySelector('input')
      input.select()
      document.execCommand('copy')
      onSuccess()
    },
    recordShare(item) {
      if (this.hasShared(item.ideaId)) {
        return
      }
      shareIdea(item.ideaId).then((res) => {
        this.actedState.shared[String(item.ideaId)] = true
        this.persistActedState()
        if (res && res.data) {
          item.shareCount = (item.shareCount || 0) + 1
        } else {
          this.$message.info('该成果链接您已分享过')
        }
      })
    },
    goDetail(id) {
      this.$router.push(`/idea/detail/${id}`)
    },
  },
}
</script>

<style scoped>
.idea-wall-page { max-width: 1100px; margin: 0 auto; padding: 16px; }
.hero { background:linear-gradient(120deg,#faad14,#fadb14); border-radius:10px; padding:20px; margin-bottom:12px; }
.hero h2 { margin:0 0 6px; }
.hero p { margin:0; }
.toolbar { display:flex; gap:8px; margin-bottom:12px; }
.wall-grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(250px,1fr)); gap:12px; }
.wall-card { background:#fff; border-radius:8px; padding:12px; box-shadow:0 1px 5px rgba(0,0,0,.08); }
.head { display:flex; gap:6px; margin-bottom:8px; }
.title { font-weight:600; color:#303133; margin-bottom:8px; cursor:pointer; }
.summary { color:#606266; font-size:13px; min-height:38px; margin-bottom:8px; }
.meta { color:#909399; font-size:12px; display:flex; justify-content:space-between; }
.ops { display:flex; justify-content:space-between; margin-top:8px; }
</style>
