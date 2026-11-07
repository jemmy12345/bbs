<template>
  <div class="collect-stats" v-loading="loading">
    <div class="head">
      <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/collect')">{{ $t('collect.backToList') }}</el-button>
      <h2>{{ activity.title || $t('collect.statsTitle') }}</h2>
      <el-button
        v-if="canExport"
        size="mini"
        type="primary"
        icon="el-icon-download"
        @click="exportCsv"
      >{{ $t('collect.exportCsv') }}</el-button>
    </div>

    <div v-if="activity.activityType === 'vote'" class="panel">
      <h4>{{ $t('collect.voteResult') }}</h4>
      <el-table :data="voteOptions" border>
        <el-table-column prop="optionText" :label="$t('collect.option')" min-width="220" />
        <el-table-column prop="voteCount" :label="$t('collect.votes')" width="120" />
      </el-table>
      <div class="chart-like">
        <div v-for="(o, i) in voteOptions" :key="i" class="bar-row">
          <div class="name">{{ o.optionText }}</div>
          <div class="bar"><span :style="barStyle(o.voteCount)" /></div>
          <div class="num">{{ o.voteCount }}</div>
        </div>
      </div>
    </div>

    <div v-if="activity.activityType === 'survey'" class="panel">
      <h4>{{ $t('collect.surveyStats') }}</h4>
      <div>{{ $t('collect.submittedCount', { count: surveySubmittedCount }) }}</div>
      <el-table :data="surveyResponses" border style="margin-top: 8px;">
        <el-table-column prop="userNickName" :label="$t('collect.submitter')" width="160" />
        <el-table-column prop="submitTime" :label="$t('collect.submitTime')" width="180" />
        <el-table-column prop="answersJson" :label="$t('collect.answersJson')" min-width="420" />
      </el-table>
    </div>

    <div v-if="activity.activityType === 'demand'" class="panel">
      <h4>{{ $t('collect.ticketStatus') }}</h4>
      <el-table :data="tickets" border>
        <el-table-column prop="demandName" :label="$t('collect.demandName')" min-width="180" />
        <el-table-column prop="demandType" :label="$t('collect.colType')" width="120" />
        <el-table-column prop="priority" :label="$t('collect.priority')" width="100" />
        <el-table-column prop="status" :label="$t('collect.colStatus')" width="120" />
        <el-table-column prop="assigneeNickName" :label="$t('collect.assignee')" width="120" />
        <el-table-column prop="createTime" :label="$t('collect.submitTime')" width="180" />
      </el-table>
    </div>

    <div v-if="activity.activityType === 'topic'" class="panel">
      <h4>{{ $t('collect.topicRankTitle') }}</h4>
      <el-table :data="topicRank" border>
        <el-table-column type="index" width="70" :label="$t('collect.rank')" />
        <el-table-column prop="nickName" :label="$t('collect.user')" width="180" />
        <el-table-column prop="submitCount" :label="$t('collect.submitCount')" width="120" />
        <el-table-column prop="totalScore" :label="$t('collect.totalScore')" width="120" />
        <el-table-column prop="likeCount" :label="$t('collect.likesReceived')" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script>
import { getCollectActivityStats } from '@/api/bbs/collect'

export default {
  name: 'BbsCollectStats',
  data() {
    return {
      loading: false,
      activity: {},
      voteOptions: [],
      surveySubmittedCount: 0,
      surveyResponses: [],
      tickets: [],
      topicRank: [],
    }
  },
  computed: {
    canExport() {
      return ['vote', 'survey', 'demand'].includes(this.activity.activityType)
    },
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      getCollectActivityStats(this.$route.params.activityId).then(res => {
        const data = res.data || {}
        this.activity = data.activity || {}
        this.voteOptions = data.options || []
        this.surveySubmittedCount = data.submittedCount || 0
        this.surveyResponses = data.responses || []
        this.tickets = data.tickets || []
        this.topicRank = data.rankList || []
      }).finally(() => {
        this.loading = false
      })
    },
    barStyle(v) {
      const max = Math.max(...(this.voteOptions.map(i => i.voteCount || 0)), 1)
      const w = Math.round(((v || 0) / max) * 100)
      return { width: `${w}%` }
    },
    exportCsv() {
      const type = this.activity.activityType
      if (type === 'vote') {
        const rows = this.voteOptions.map(i => ({
          optionText: i.optionText || '',
          voteCount: i.voteCount || 0,
        }))
        this.downloadCsv([this.$t('collect.option'), this.$t('collect.votes')], rows.map(i => [i.optionText, i.voteCount]), `vote_result_${this.activity.activityId || ''}.csv`)
        return
      }
      if (type === 'survey') {
        const rows = (this.surveyResponses || []).map(i => [
          i.userNickName || '',
          i.submitTime || '',
          i.answersJson || '',
        ])
        this.downloadCsv([this.$t('collect.submitter'), this.$t('collect.submitTime'), this.$t('collect.answersJson')], rows, `survey_response_${this.activity.activityId || ''}.csv`)
        return
      }
      if (type === 'demand') {
        const rows = (this.tickets || []).map(i => [
          i.demandName || '',
          i.demandType || '',
          i.priority || '',
          i.status || '',
          i.assigneeNickName || '',
          i.createTime || '',
          i.demandDetail || '',
        ])
        this.downloadCsv([this.$t('collect.demandName'), this.$t('collect.colType'), this.$t('collect.priority'), this.$t('collect.colStatus'), this.$t('collect.assignee'), this.$t('collect.submitTime'), this.$t('collect.demandDetail')], rows, `demand_ticket_${this.activity.activityId || ''}.csv`)
      }
    },
    downloadCsv(headers, rows, filename) {
      const csvRows = [headers, ...rows].map(row => row.map(this.escapeCsvCell).join(','))
      const csvText = `\uFEFF${csvRows.join('\n')}`
      const blob = new Blob([csvText], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      this.$message.success(this.$t('collect.csvExported'))
    },
    escapeCsvCell(value) {
      const text = value == null ? '' : String(value)
      const escaped = text.replace(/"/g, '""')
      return `"${escaped}"`
    },
  },
}
</script>

<style scoped>
.collect-stats { max-width: 1100px; margin: 0 auto; padding: 16px; }
.head { display: flex; align-items: center; gap: 8px; }
.head h2 { margin-right: auto; }
.panel { margin-top: 10px; background: #fff; border-radius: 8px; padding: 12px; box-shadow: 0 1px 5px rgba(0,0,0,.08); }
.chart-like { margin-top: 10px; }
.bar-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.name { width: 180px; color: #606266; }
.bar { flex: 1; height: 10px; background: #f0f3f8; border-radius: 5px; overflow: hidden; }
.bar span { display: block; height: 100%; background: linear-gradient(90deg, #36d1dc, #5b86e5); }
.num { width: 50px; text-align: right; color: #909399; }
</style>
