<template>
  <div class="qa-archive-page">
    <!-- 头部 -->
    <div class="qa-archive-banner">
      <div class="qa-archive-banner-content">
        <div>
          <h2>问答知识库</h2>
          <p>汇聚已解决的高质量问答，沉淀团队知识</p>
        </div>
        <el-button type="text" @click="$router.push('/qa')" style="color: #fff;">
          <i class="el-icon-arrow-left"></i> 返回问答列表
        </el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="qa-archive-filter">
      <!-- 分类标签 -->
      <div class="qa-cat-tabs">
        <span
          class="qa-cat-tab"
          :class="{ active: queryParams.category === '' }"
          @click="handleCatChange('')"
        >全部</span>
        <span
          v-for="cat in categories"
          :key="cat.value"
          class="qa-cat-tab"
          :class="{ active: queryParams.category === cat.value }"
          @click="handleCatChange(cat.value)"
        >{{ cat.label }}</span>
      </div>

      <div class="qa-archive-search">
        <el-select v-model="queryParams.orderBy" size="small" @change="handleFilter">
          <el-option label="最新归档" value="latest" />
          <el-option label="浏览最多" value="view" />
        </el-select>
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索关键词"
          size="small"
          clearable
          style="width: 200px;"
          @keyup.enter.native="handleFilter"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleFilter" />
        </el-input>
      </div>
    </div>

    <!-- 列表 -->
    <div class="qa-archive-list" v-loading="loading">
      <div
        v-for="q in list"
        :key="q.questionId"
        class="qa-archive-card"
        @click="handleDetail(q.questionId)"
      >
        <div class="qa-archive-card-body">
          <div class="qa-archive-card-header">
            <el-tag size="mini" :type="categoryType(q.category)">{{ categoryLabel(q.category) }}</el-tag>
            <span class="qa-archive-title">{{ q.title }}</span>
          </div>
          <div class="qa-archive-card-tags" v-if="q.tags">
            <el-tag
              v-for="tag in q.tags.split(',')"
              :key="tag"
              size="mini"
              effect="plain"
              type="info"
            ># {{ tag.trim() }}</el-tag>
          </div>
          <div class="qa-archive-meta">
            <el-avatar :src="q.askAvatar" :size="18" />
            <span>{{ q.askNickName }}</span>
            <span class="qa-archive-time">{{ formatDate(q.archiveTime || q.closeTime) }}</span>
            <span><i class="el-icon-view"></i> {{ q.viewCount || 0 }}</span>
            <span><i class="el-icon-chat-dot-round"></i> {{ q.answerCount || 0 }} 个回答</span>
          </div>
        </div>
        <div class="qa-archive-reward">
          <span class="reward-num">{{ q.rewardPoints || 0 }}</span>
          <span class="reward-label">积分</span>
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无归档内容" />
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
import { listQaArchive } from '@/api/bbs/qa'

const CATEGORIES = [
  { value: 'tech', label: '技术研发' },
  { value: 'product', label: '产品设计' },
  { value: 'hr', label: '人事行政' },
  { value: 'admin', label: '运营管理' },
  { value: 'other', label: '其他' },
]

export default {
  name: 'BbsQaArchive',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      categories: CATEGORIES,
      queryParams: {
        category: '',
        keyword: '',
        orderBy: 'latest',
        pageNum: 1,
        pageSize: 12,
      },
    }
  },
  created() {
    if (this.$route.query.category) this.queryParams.category = this.$route.query.category
    if (this.$route.query.keyword) this.queryParams.keyword = this.$route.query.keyword
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQaArchive({
        category: this.queryParams.category || undefined,
        keyword: this.queryParams.keyword || undefined,
        orderBy: this.queryParams.orderBy,
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
      })
        .then(res => {
          this.list = res.rows || []
          this.total = res.total || 0
        })
        .finally(() => { this.loading = false })
    },
    handleFilter() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleCatChange(val) {
      this.queryParams.category = val
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleDetail(questionId) {
      this.$router.push({ path: `/qa/detail/${questionId}` })
    },
    formatDate(t) {
      if (!t) return ''
      const d = new Date(t)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    categoryLabel(val) {
      const c = CATEGORIES.find(c => c.value === val)
      return c ? c.label : val
    },
    categoryType(val) {
      const map = { tech: 'primary', product: 'success', hr: 'warning', admin: '', other: 'info' }
      return map[val] || 'info'
    },
  },
}
</script>

<style scoped>
.qa-archive-page { max-width: 1100px; margin: 0 auto; padding: 16px; }

.qa-archive-banner {
  background: linear-gradient(135deg, #722ed1 0%, #1890ff 100%);
  border-radius: 12px; padding: 20px 28px; margin-bottom: 20px; color: #fff;
}
.qa-archive-banner-content { display: flex; align-items: center; justify-content: space-between; }
.qa-archive-banner h2 { margin: 0 0 4px; font-size: 20px; }
.qa-archive-banner p { margin: 0; font-size: 13px; opacity: .9; }

.qa-archive-filter {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: 10px;
  background: #fff; border-radius: 8px; padding: 12px 16px;
  margin-bottom: 16px; box-shadow: 0 1px 4px rgba(0,0,0,.08);
}
.qa-cat-tabs { display: flex; gap: 4px; flex-wrap: wrap; }
.qa-cat-tab {
  cursor: pointer; padding: 5px 14px; border-radius: 20px;
  font-size: 14px; color: #606266; transition: all .2s;
}
.qa-cat-tab:hover { background: #f0f7ff; color: #409EFF; }
.qa-cat-tab.active { background: #722ed1; color: #fff; }
.qa-archive-search { display: flex; gap: 8px; align-items: center; }

.qa-archive-list { display: flex; flex-direction: column; gap: 10px; }

.qa-archive-card {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-radius: 8px; padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  cursor: pointer; transition: box-shadow .2s, transform .1s;
}
.qa-archive-card:hover { box-shadow: 0 4px 12px rgba(114,46,209,.15); transform: translateY(-1px); }
.qa-archive-card-body { flex: 1; min-width: 0; }
.qa-archive-card-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.qa-archive-title { font-size: 15px; font-weight: 600; color: #303133; }
.qa-archive-card-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 8px; }
.qa-archive-meta { display: flex; align-items: center; gap: 10px; font-size: 12px; color: #909399; }
.qa-archive-time { color: #c0c4cc; }

.qa-archive-reward { text-align: center; min-width: 48px; }
.reward-num { font-size: 18px; font-weight: 700; color: #e6a23c; display: block; }
.reward-label { font-size: 11px; color: #999; }
</style>
