<template>
  <div class="collect-stats" v-loading="loading">
    <div class="head">
      <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/collect')">返回列表</el-button>
      <h2>{{ activity.title || '活动数据统计' }}</h2>
      <el-button
        v-if="canExport"
        size="mini"
        type="primary"
        icon="el-icon-download"
        @click="exportCsv"
      >导出 CSV</el-button>
    </div>

    <div v-if="activity.activityType === 'vote'" class="panel">
      <h4>投票结果</h4>
      <el-table :data="voteOptions" border>
        <el-table-column prop="optionText" label="选项" min-width="220" />
        <el-table-column prop="voteCount" label="票数" width="120" />
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
      <h4>问卷统计</h4>
      <div>已提交人数：{{ surveySubmittedCount }}</div>
      <el-table :data="surveyResponses" border style="margin-top: 8px;">
        <el-table-column prop="userNickName" label="提交人" width="160" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="answersJson" label="答案JSON" min-width="420" />
      </el-table>
    </div>

    <div v-if="activity.activityType === 'demand'" class="panel">
      <h4>需求工单状态</h4>
      <el-table :data="tickets" border>
        <el-table-column prop="demandName" label="需求名称" min-width="180" />
        <el-table-column prop="demandType" label="类型" width="120" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="assigneeNickName" label="负责人" width="120" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
      </el-table>
    </div>

    <div v-if="activity.activityType === 'topic'" class="panel">
      <h4>专题参与榜单</h4>
      <el-table :data="topicRank" border>
        <el-table-column type="index" width="70" label="排名" />
        <el-table-column prop="nickName" label="用户" width="180" />
        <el-table-column prop="submitCount" label="提交数" width="120" />
        <el-table-column prop="totalScore" label="总分" width="120" />
        <el-table-column prop="likeCount" label="获赞" width="120" />
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
        this.downloadCsv(['选项', '票数'], rows.map(i => [i.optionText, i.voteCount]), `vote_result_${this.activity.activityId || ''}.csv`)
        return
      }
      if (type === 'survey') {
        const rows = (this.surveyResponses || []).map(i => [
          i.userNickName || '',
          i.submitTime || '',
          i.answersJson || '',
        ])
        this.downloadCsv(['提交人', '提交时间', '答案JSON'], rows, `survey_response_${this.activity.activityId || ''}.csv`)
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
        this.downloadCsv(['需求名称', '类型', '优先级', '状态', '负责人', '提交时间', '需求详情'], rows, `demand_ticket_${this.activity.activityId || ''}.csv`)
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
      this.$message.success('CSV 导出成功')
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
