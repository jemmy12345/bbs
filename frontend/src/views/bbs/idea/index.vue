<template>
  <div class="idea-page">
    <div class="idea-banner">
      <div>
        <h2>{{ $t('idea.index.title') }}</h2>
        <p>{{ $t('idea.index.subtitle') }}</p>
      </div>
      <div>
        <el-button type="primary" icon="el-icon-plus" @click="goPublish">{{ $t('idea.index.submit') }}</el-button>
        <el-button type="success" plain @click="goWall">{{ $t('idea.index.wall') }}</el-button>
      </div>
    </div>

    <div class="idea-toolbar">
      <el-radio-group v-model="query.mine" size="small" @change="queryList" style="margin-right:4px">
        <el-radio-button :label="false">{{ $t('idea.index.all') }}</el-radio-button>
        <el-radio-button :label="true">{{ $t('idea.index.mine') }}</el-radio-button>
      </el-radio-group>
      <el-select v-model="query.status" clearable :placeholder="$t('idea.index.status')" size="small" @change="queryList">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.category" clearable :placeholder="$t('idea.index.category')" size="small" @change="queryList">
        <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
      <el-input v-model="query.keyword" clearable size="small" :placeholder="$t('idea.index.search')" style="width: 220px" @keyup.enter.native="queryList">
        <el-button slot="append" icon="el-icon-search" @click="queryList" />
      </el-input>
    </div>

    <div class="idea-list" v-loading="loading">
      <div class="idea-card" v-for="item in list" :key="item.ideaId" @click="goDetail(item.ideaId)">
        <div class="idea-top">
          <el-tag size="mini" :type="catType(item.category)">{{ catLabel(item.category) }}</el-tag>
          <el-tag size="mini" :type="statusType(item.status)">{{ statusLabel(item.status) }}</el-tag>
          <el-tag v-if="item.isDraft === '1'" size="mini" type="info">{{ $t('idea.index.draft') }}</el-tag>
        </div>
        <div class="idea-title">{{ item.title }}</div>
        <div v-if="item.status === 'REJECTED' && item.rejectReason" class="idea-note reject-note">
          {{ $t('idea.index.rejectReason', { reason: item.rejectReason }) }}
        </div>
        <div v-else-if="item.reviewStage && item.reviewStage !== 'NONE'" class="idea-note">
          {{ $t('idea.index.currentStage', { stage: stageLabel(item.reviewStage) }) }}
          <span v-if="item.reviewDeadline">{{ $t('idea.index.deadline', { date: parseTime(item.reviewDeadline, '{y}-{m}-{d}') }) }}</span>
        </div>
        <div class="idea-meta">
          <span>{{ item.submitNickName || '-' }}</span>
          <span>{{ $t('idea.index.score', { score: item.avgScore || 0 }) }}</span>
          <span>{{ parseTime(item.createTime, '{y}-{m}-{d}') }}</span>
          <el-button v-if="canEditDraft(item)" size="mini" type="text" icon="el-icon-edit"
            @click.stop="goEditDraft(item.ideaId)">{{ $t('idea.index.editDraft') }}</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" :description="$t('idea.index.empty')" />
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
import { mapGetters } from 'vuex'
import { listIdeas } from '@/api/bbs/idea'

const STATUS_KEYS = ['DRAFT', 'PENDING_L1', 'L1_REVIEWING', 'L1_PASSED', 'L2_REVIEWING', 'L2_PASSED', 'L3_REVIEWING', 'APPROVED', 'IMPLEMENTING', 'LANDED', 'PUBLICIZED', 'REJECTED', 'PAUSED']
const CAT_KEYS = ['product', 'tech', 'manage', 'cost', 'other']

export default {
  name: 'BbsIdeaIndex',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
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
  computed: {
    ...mapGetters(['id']),
    statusOptions() {
      return STATUS_KEYS.map(k => ({ value: k, label: this.$t('idea.status.' + k) }))
    },
    categoryOptions() {
      return CAT_KEYS.map(k => ({ value: k, label: this.$t('idea.cat.' + k) }))
    },
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
    canEditDraft(item) {
      return item && item.isDraft === '1' && String(item.submitUserId) === String(this.id)
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
      return STATUS_KEYS.includes(v) ? this.$t('idea.status.' + v) : v
    },
    statusType(v) {
      const map = { DRAFT: 'info', PENDING_L1: 'warning', L2_REVIEWING: 'warning', L3_REVIEWING: 'warning', IMPLEMENTING: '', LANDED: 'success', PUBLICIZED: 'success', REJECTED: 'danger' }
      return map[v] || 'info'
    },
    catLabel(v) {
      return CAT_KEYS.includes(v) ? this.$t('idea.cat.' + v) : v
    },
    catType(v) {
      const map = { product: 'success', tech: 'primary', manage: 'warning', cost: 'danger', other: 'info' }
      return map[v] || 'info'
    },
    stageLabel(v) {
      return ['L1', 'L2', 'L3'].includes(v) ? this.$t('idea.stage.' + v) : v
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
.idea-note { color:#606266; font-size:12px; margin-bottom:8px; }
.reject-note { color:#f56c6c; }
.idea-meta { color:#909399; font-size:12px; display:flex; gap:12px; }
</style>
