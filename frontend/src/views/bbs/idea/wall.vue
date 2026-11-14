<template>
  <div class="idea-wall-page">
    <div class="hero">
      <h2>{{ $t('idea.wall.title') }}</h2>
      <p>{{ $t('idea.wall.subtitle') }}</p>
    </div>

    <div class="toolbar">
      <el-select v-model="query.sortBy" size="small" @change="doSearch">
        <el-option :label="$t('idea.wall.latest')" value="latest" />
        <el-option :label="$t('idea.wall.like')" value="like" />
      </el-select>
      <el-input v-model="query.keyword" clearable size="small" style="width:220px" :placeholder="$t('idea.wall.search')" @keyup.enter.native="doSearch">
        <el-button slot="append" icon="el-icon-search" @click="doSearch" />
      </el-input>
    </div>

    <div class="wall-grid" v-loading="loading">
      <div class="wall-card" v-for="item in list" :key="item.ideaId">
        <div class="head">
          <el-tag size="mini" type="success">{{ $t('idea.wall.published') }}</el-tag>
          <el-tag size="mini" type="warning">{{ item.honorBadge || $t('idea.wall.defaultBadge') }}</el-tag>
        </div>
        <div class="title" @click="goDetail(item.ideaId)">{{ item.title }}</div>
        <div class="summary">{{ item.resultSummary || $t('idea.wall.defaultSummary') }}</div>
        <div class="meta">
          <span>{{ item.submitNickName }}</span>
          <span>{{ $t('idea.wall.incentive', { points: item.incentivePoints || 0 }) }}</span>
          <span class="like-btn" :class="{ liked: item.liked }" @click="toggleLike(item)">
            <i :class="item.liked ? 'el-icon-star-on' : 'el-icon-star-off'" /> {{ item.likeCount || 0 }}
          </span>
          <span class="like-btn" @click="handleShare(item)">
            <i class="el-icon-share" /> {{ item.shareCount || 0 }}
          </span>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" :description="$t('idea.wall.empty')" />
    </div>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="query.pageNum"
      :limit.sync="query.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listIdeaWall, toggleIdeaLike, shareIdea } from '@/api/bbs/idea'

export default {
  name: 'BbsIdeaWall',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: { sortBy: 'latest', keyword: '', pageNum: 1, pageSize: 12 },
    }
  },
  created() { this.getList() },
  methods: {
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
    copyText(text) {
      if (navigator.clipboard && window.isSecureContext) {
        return navigator.clipboard.writeText(text)
      }
      return new Promise((resolve, reject) => {
        const ta = document.createElement('textarea')
        ta.value = text
        ta.style.position = 'fixed'
        ta.style.opacity = '0'
        document.body.appendChild(ta)
        ta.select()
        const ok = document.execCommand('copy')
        document.body.removeChild(ta)
        ok ? resolve() : reject(new Error('copy failed'))
      })
    },
    handleShare(item) {
      const url = window.location.origin + this.$router.resolve(`/idea/detail/${item.ideaId}`).href
      this.copyText(url).then(() => {
        this.$message.success(this.$t('idea.wall.shareCopied'))
      }).catch(() => {
        this.$message.warning(this.$t('idea.wall.copyFailed'))
      })
      shareIdea(item.ideaId).then(res => {
        this.$set(item, 'shareCount', res.data.shareCount)
      })
    },
    toggleLike(item) {
      toggleIdeaLike(item.ideaId).then(res => {
        this.$set(item, 'liked', res.data.liked)
        this.$set(item, 'likeCount', res.data.likeCount)
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
.like-btn { cursor:pointer; user-select:none; }
.like-btn.liked { color:#f5a623; }
.meta { color:#909399; font-size:12px; display:flex; justify-content:space-between; }
</style>
