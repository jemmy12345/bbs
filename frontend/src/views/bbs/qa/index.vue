<template>
  <div class="qa-page">
    <!-- 顶部 Banner -->
    <div class="qa-banner">
      <div class="qa-banner-content">
        <div class="qa-banner-text">
          <h2>{{ $t('qa.index.title') }}</h2>
          <p>{{ $t('qa.index.subtitle') }}</p>
        </div>
        <div class="qa-banner-actions">
          <el-button type="primary" icon="el-icon-plus" @click="handlePublish" class="qa-ask-btn">
            {{ $t('qa.index.ask') }}
          </el-button>
          <el-button icon="el-icon-collection" @click="goArchive" class="qa-kb-btn">
            {{ $t('qa.index.kb') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="qa-toolbar">
      <div class="qa-filter-tabs">
        <span
          v-for="tab in statusTabs"
          :key="tab.value"
          class="qa-filter-tab"
          :class="{ active: queryParams.status === tab.value }"
          @click="handleStatusChange(tab.value)"
        >
          {{ tab.label }}
        </span>
      </div>
      <div class="qa-filter-right">
        <el-select
          v-model="queryParams.category"
          :placeholder="$t('qa.index.allCategories')"
          clearable
          size="small"
          @change="handleFilter"
        >
          <el-option
            v-for="cat in categories"
            :key="cat.value"
            :label="cat.label"
            :value="cat.value"
          />
        </el-select>
        <el-select
          v-model="queryParams.orderBy"
          :placeholder="$t('qa.index.sort')"
          size="small"
          @change="handleFilter"
        >
          <el-option :label="$t('qa.sort.latest')" value="latest" />
          <el-option :label="$t('qa.sort.reward')" value="reward" />
          <el-option :label="$t('qa.sort.view')" value="view" />
          <el-option :label="$t('qa.sort.answer')" value="answer" />
        </el-select>
        <el-input
          v-model="queryParams.keyword"
          :placeholder="$t('qa.index.searchPlaceholder')"
          size="small"
          clearable
          style="width: 200px;"
          @keyup.enter.native="handleFilter"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleFilter" />
        </el-input>
      </div>
    </div>

    <!-- 问题列表 -->
    <div class="qa-list" v-loading="loading">
      <div
        v-for="q in questionList"
        :key="q.questionId"
        class="qa-card"
        @click="handleDetail(q.questionId)"
      >
        <!-- 左侧状态图标 -->
        <div class="qa-card-left">
          <div class="qa-status-icon" :class="'status-' + q.status">
            <i :class="statusIcon(q.status)"></i>
          </div>
          <div class="qa-reward">
            <span class="reward-num">{{ q.rewardPoints || 0 }}</span>
            <span class="reward-label">{{ $t('qa.common.points') }}</span>
          </div>
        </div>

        <!-- 中间内容 -->
        <div class="qa-card-body">
          <div class="qa-card-title">
            <el-tag size="mini" :type="categoryType(q.category)" class="qa-cat-tag">
              {{ categoryLabel(q.category) }}
            </el-tag>
            <span class="qa-title-text">{{ q.title }}</span>
            <el-tag v-if="q.status === 'solved'" size="mini" type="success" effect="dark" class="qa-solved-tag">{{ $t('qa.status.solved') }}</el-tag>
            <el-tag v-if="q.status === 'archived'" size="mini" type="info" effect="dark" class="qa-solved-tag">{{ $t('qa.status.archived') }}</el-tag>
          </div>
          <div class="qa-card-tags" v-if="q.tags">
            <el-tag
              v-for="tag in q.tags.split(',')"
              :key="tag"
              size="mini"
              effect="plain"
              type="info"
              class="qa-tag-item"
            ># {{ tag.trim() }}</el-tag>
          </div>
          <div class="qa-card-meta">
            <el-avatar :src="q.askAvatar" :size="20" class="qa-meta-avatar" />
            <span class="qa-meta-user">{{ q.askNickName || $t('qa.common.anonymous') }}</span>
            <span class="qa-meta-dept" v-if="q.askDeptName">{{ q.askDeptName }}</span>
            <span class="qa-meta-time">{{ formatTime(q.createTime) }}</span>
          </div>
        </div>

        <!-- 右侧统计 -->
        <div class="qa-card-stats">
          <div class="qa-stat-item">
            <span class="qa-stat-num">{{ q.answerCount || 0 }}</span>
            <span class="qa-stat-label">{{ $t('qa.index.answers') }}</span>
          </div>
          <div class="qa-stat-item">
            <span class="qa-stat-num">{{ q.viewCount || 0 }}</span>
            <span class="qa-stat-label">{{ $t('qa.index.views') }}</span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && questionList.length === 0" :description="$t('qa.index.empty')" />
    </div>

    <!-- 分页 -->
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
import { listQaQuestions } from '@/api/bbs/qa'

const CATEGORY_KEYS = ['tech', 'product', 'hr', 'admin', 'other']

export default {
  name: 'BbsQaIndex',
  data() {
    return {
      loading: false,
      questionList: [],
      total: 0,
      queryParams: {
        status: '',
        category: '',
        keyword: '',
        orderBy: 'latest',
        pageNum: 1,
        pageSize: 15,
      },
    }
  },
  computed: {
    categories() {
      return CATEGORY_KEYS.map(v => ({ value: v, label: this.$t('qa.cat.' + v) }))
    },
    statusTabs() {
      return [
        { value: '', label: this.$t('qa.status.all') },
        { value: 'open', label: this.$t('qa.status.open') },
        { value: 'solved', label: this.$t('qa.status.solved') },
        { value: 'archived', label: this.$t('qa.status.archived') },
      ]
    },
  },
  created() {
    const q = this.$route.query
    if (q.status) this.queryParams.status = q.status
    if (q.category) this.queryParams.category = q.category
    if (q.keyword) this.queryParams.keyword = q.keyword
    if (q.orderBy) this.queryParams.orderBy = q.orderBy
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQaQuestions({
        status: this.queryParams.status || undefined,
        category: this.queryParams.category || undefined,
        keyword: this.queryParams.keyword || undefined,
        orderBy: this.queryParams.orderBy,
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
      })
        .then(res => {
          this.questionList = res.rows || []
          this.total = res.total || 0
        })
        .finally(() => { this.loading = false })
    },
    handleFilter() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleStatusChange(val) {
      this.queryParams.status = val
      this.queryParams.pageNum = 1
      this.getList()
    },
    handlePublish() {
      if (!this.$store.getters.token) {
        this.$message.warning(this.$t('qa.common.loginFirst'))
        return
      }
      this.$router.push({ path: '/qa/publish' })
    },
    handleDetail(questionId) {
      this.$router.push({ path: `/qa/detail/${questionId}` })
    },
    goArchive() {
      this.$router.push({ path: '/qa/archive' })
    },
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      const now = new Date()
      const diff = (now - d) / 1000
      if (diff < 60) return this.$t('qa.time.justNow')
      if (diff < 3600) return this.$t('qa.time.minutesAgo', { n: Math.floor(diff / 60) })
      if (diff < 86400) return this.$t('qa.time.hoursAgo', { n: Math.floor(diff / 3600) })
      if (diff < 86400 * 30) return this.$t('qa.time.daysAgo', { n: Math.floor(diff / 86400) })
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    statusIcon(status) {
      const map = { open: 'el-icon-question', solved: 'el-icon-circle-check', archived: 'el-icon-collection', cancelled: 'el-icon-circle-close' }
      return map[status] || 'el-icon-question'
    },
    categoryLabel(val) {
      return CATEGORY_KEYS.includes(val) ? this.$t('qa.cat.' + val) : val
    },
    categoryType(val) {
      const map = { tech: 'primary', product: 'success', hr: 'warning', admin: '', other: 'info' }
      return map[val] || 'info'
    },
  },
}
</script>

<style scoped>
.qa-page { max-width: 1100px; margin: 0 auto; padding: 16px; }

.qa-banner {
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 20px;
  color: #fff;
}
.qa-banner-content { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; }
.qa-banner-text h2 { margin: 0 0 6px; font-size: 22px; }
.qa-banner-text p { margin: 0; font-size: 14px; opacity: .9; }
.qa-banner-actions { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.qa-ask-btn,
.qa-kb-btn { height: 46px; padding: 0 28px; font-size: 16px; font-weight: 600; border: none; border-radius: 23px; }
.qa-ask-btn { background: #fff; color: #409EFF; }
.qa-ask-btn:hover, .qa-ask-btn:focus { background: #f0f7ff; color: #409EFF; }
.qa-kb-btn { background: linear-gradient(135deg, #ffb347 0%, #ff7a45 100%); color: #fff; box-shadow: 0 4px 12px rgba(255, 122, 69, .45); }
.qa-kb-btn:hover, .qa-kb-btn:focus { background: linear-gradient(135deg, #ffc266 0%, #ff8c5e 100%); color: #fff; }

.qa-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: 10px;
  background: #fff; border-radius: 8px; padding: 12px 16px;
  margin-bottom: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.08);
}
.qa-filter-tabs { display: flex; gap: 4px; }
.qa-filter-tab {
  cursor: pointer; padding: 6px 14px; border-radius: 20px;
  font-size: 14px; color: #606266;
  transition: all .2s;
}
.qa-filter-tab:hover { background: #f0f7ff; color: #409EFF; }
.qa-filter-tab.active { background: #409EFF; color: #fff; }
.qa-filter-right { display: flex; gap: 8px; align-items: center; }

.qa-list { display: flex; flex-direction: column; gap: 10px; }

.qa-card {
  display: flex; align-items: flex-start; gap: 16px;
  background: #fff; border-radius: 8px; padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  cursor: pointer; transition: box-shadow .2s, transform .1s;
}
.qa-card:hover { box-shadow: 0 4px 12px rgba(64,158,255,.15); transform: translateY(-1px); }

.qa-card-left { display: flex; flex-direction: column; align-items: center; gap: 8px; min-width: 56px; }
.qa-status-icon {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.status-open { background: #e6f7ff; color: #409EFF; }
.status-solved { background: #f6ffed; color: #52c41a; }
.status-archived { background: #f0f0f0; color: #8c8c8c; }
.status-cancelled { background: #fff1f0; color: #f5222d; }
.qa-reward { text-align: center; }
.reward-num { font-size: 18px; font-weight: 700; color: #e6a23c; display: block; }
.reward-label { font-size: 11px; color: #999; }

.qa-card-body { flex: 1; min-width: 0; }
.qa-card-title { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.qa-cat-tag { flex-shrink: 0; }
.qa-title-text { font-size: 15px; font-weight: 600; color: #303133; flex: 1; min-width: 0; }
.qa-solved-tag { flex-shrink: 0; }
.qa-card-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 8px; }
.qa-tag-item { font-size: 12px; }
.qa-card-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #909399; }
.qa-meta-avatar { flex-shrink: 0; }
.qa-meta-dept { color: #c0c4cc; }

.qa-card-stats { display: flex; flex-direction: column; gap: 8px; min-width: 52px; text-align: center; }
.qa-stat-item { }
.qa-stat-num { font-size: 16px; font-weight: 700; color: #606266; display: block; }
.qa-stat-label { font-size: 11px; color: #c0c4cc; }
</style>
