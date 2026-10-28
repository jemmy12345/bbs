<template>
  <div class="idea-page">
    <div class="idea-banner">
      <div>
        <h2>创意提案专区</h2>
        <p>投稿 - 评审 - 跟进 - 落地 - 公示，全流程线上化</p>
      </div>
      <div>
        <el-button type="primary" icon="el-icon-plus" @click="goPublish">提交创意</el-button>
        <el-button type="success" plain @click="goWall">成果公示墙</el-button>
      </div>
    </div>

    <div class="idea-toolbar">
      <el-radio-group v-model="query.mine" size="small" @change="queryList" style="margin-right:4px">
        <el-radio-button :label="false">全部</el-radio-button>
        <el-radio-button :label="true">我的提案</el-radio-button>
      </el-radio-group>
      <el-select v-model="query.status" clearable placeholder="状态" size="small" @change="queryList">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.category" clearable placeholder="分类" size="small" @change="queryList">
        <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
      <el-input v-model="query.keyword" clearable size="small" placeholder="搜索提案" style="width: 220px" @keyup.enter.native="queryList">
        <el-button slot="append" icon="el-icon-search" @click="queryList" />
      </el-input>
    </div>

    <div class="idea-list" v-loading="loading">
      <div class="idea-card" v-for="item in list" :key="item.ideaId" @click="goDetail(item.ideaId)">
        <div class="idea-top">
          <el-tag size="mini" :type="catType(item.category)">{{ catLabel(item.category) }}</el-tag>
          <el-tag size="mini" :type="statusType(item.status)">{{ statusLabel(item.status) }}</el-tag>
          <el-tag v-if="item.isPublicized === '1'" size="mini" type="success">已公示</el-tag>
          <el-tag v-if="item.isDraft === '1'" size="mini" type="info">草稿</el-tag>
        </div>
        <div class="idea-title">{{ item.title }}</div>
        <div class="idea-meta">
          <span>{{ item.submitNickName || '-' }}</span>
          <span>评分 {{ item.avgScore || 0 }}</span>
          <span>{{ parseTime(item.createTime, '{y}-{m}-{d}') }}</span>
          <el-button v-if="item.isDraft === '1'" size="mini" type="text" icon="el-icon-edit"
            @click.stop="goEditDraft(item.ideaId)">编辑草稿</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无提案" />
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
import { listIdeas } from '@/api/bbs/idea'

const STATUS = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING_L1', label: '待初审' },
  { value: 'L2_REVIEWING', label: '复审中' },
  { value: 'L3_REVIEWING', label: '终审中' },
  { value: 'IMPLEMENTING', label: '落地中' },
  { value: 'LANDED', label: '已落地' },
  { value: 'PUBLICIZED', label: '已公示' },
  { value: 'REJECTED', label: '已驳回' },
]

const CATS = [
  { value: 'product', label: '产品创新' },
  { value: 'tech', label: '技术优化' },
  { value: 'manage', label: '管理提升' },
  { value: 'cost', label: '降本增效' },
  { value: 'other', label: '其他' },
]

export default {
  name: 'BbsIdeaIndex',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      statusOptions: STATUS,
      categoryOptions: CATS,
      query: {
        status: '',
        category: '',
        keyword: '',
        mine: false,
        pageNum: 1,
        pageSize: 10,
      },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listIdeas(this.query).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    queryList() {
      this.query.pageNum = 1
      this.getList()
    },
    goEditDraft(id) {
      this.$router.push(`/idea/publish?ideaId=${id}`)
    },
    goPublish() {
      this.$router.push('/idea/publish')
    },
    goDetail(id) {
      this.$router.push(`/idea/detail/${id}`)
    },
    goWall() {
      this.$router.push('/idea/wall')
    },
    statusLabel(v) {
      const t = STATUS.find(i => i.value === v)
      return t ? t.label : v
    },
    statusType(v) {
      const map = { DRAFT: 'info', PENDING_L1: 'warning', L2_REVIEWING: 'warning', L3_REVIEWING: 'warning', IMPLEMENTING: '', LANDED: 'success', PUBLICIZED: 'success', REJECTED: 'danger' }
      return map[v] || 'info'
    },
    catLabel(v) {
      const t = CATS.find(i => i.value === v)
      return t ? t.label : v
    },
    catType(v) {
      const map = { product: 'success', tech: 'primary', manage: 'warning', cost: 'danger', other: 'info' }
      return map[v] || 'info'
    },
  },
}
</script>

<style scoped>
.idea-page { max-width: 1100px; margin: 0 auto; padding: 16px; }
.idea-banner { display:flex; justify-content:space-between; align-items:center; background:linear-gradient(120deg,#2b8aef,#53c5f8); color:#fff; border-radius:10px; padding:20px; margin-bottom:16px; }
.idea-banner h2 { margin:0 0 6px; }
.idea-banner p { margin:0; opacity:.9; }
.idea-toolbar { display:flex; gap:8px; margin-bottom:12px; }
.idea-list { display:flex; flex-direction:column; gap:10px; }
.idea-card { background:#fff; border-radius:8px; padding:14px; box-shadow:0 1px 4px rgba(0,0,0,.06); cursor:pointer; }
.idea-card:hover { box-shadow:0 4px 10px rgba(43,138,239,.16); }
.idea-top { display:flex; gap:6px; margin-bottom:8px; }
.idea-title { font-size:15px; font-weight:600; color:#303133; margin-bottom:8px; }
.idea-meta { color:#909399; font-size:12px; display:flex; gap:12px; }
</style>
