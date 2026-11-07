<template>
  <div class="bbs-dashboard">
    <!-- 顶部操作栏 -->
    <div class="dashboard-header">
      <span class="dashboard-title">{{ $t('bbsExtra.dashboard.title') }}</span>
      <el-button size="small" icon="el-icon-refresh" @click="loadStats" :loading="loading">{{ $t('bbsExtra.dashboard.refresh') }}</el-button>
    </div>

    <!-- KPI卡片行 -->
    <el-row :gutter="16" class="kpi-row" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-total">
          <div class="kpi-icon"><i class="el-icon-document"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.total_posts || 0 }}</div>
            <div class="kpi-label">{{ $t('bbsExtra.dashboard.kpiTotal') }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-pending">
          <div class="kpi-icon"><i class="el-icon-time"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.pending_count || 0 }}</div>
            <div class="kpi-label">{{ $t('bbsExtra.dashboard.kpiPending') }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-published">
          <div class="kpi-icon"><i class="el-icon-check"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.published_count || 0 }}</div>
            <div class="kpi-label">{{ $t('bbsExtra.dashboard.kpiPublished') }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-comments">
          <div class="kpi-icon"><i class="el-icon-chat-dot-round"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.total_comments || 0 }}</div>
            <div class="kpi-label">{{ $t('bbsExtra.dashboard.kpiComments') }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-views">
          <div class="kpi-icon"><i class="el-icon-view"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ formatCount(summary.total_views) }}</div>
            <div class="kpi-label">{{ $t('bbsExtra.dashboard.kpiViews') }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-anon">
          <div class="kpi-icon"><i class="el-icon-user"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.anonymous_count || 0 }}</div>
            <div class="kpi-label">{{ $t('bbsExtra.dashboard.kpiAnon') }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <!-- 7天趋势 -->
      <el-col :xs="24" :md="14">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>{{ $t('bbsExtra.dashboard.cardTrend') }}</span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <!-- 帖子类型分布 -->
      <el-col :xs="24" :md="10">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>{{ $t('bbsExtra.dashboard.cardType') }}</span>
          </div>
          <div ref="typeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <!-- 帖子状态分布 -->
      <el-col :xs="24" :md="10">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>{{ $t('bbsExtra.dashboard.cardStatus') }}</span>
          </div>
          <div ref="statusChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <!-- 实名 vs 匿名 -->
      <el-col :xs="24" :md="14">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>{{ $t('bbsExtra.dashboard.cardAnon') }}</span>
          </div>
          <div ref="anonChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活跃用户 + 待审核快速处理 -->
    <el-row :gutter="16" class="bottom-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>{{ $t('bbsExtra.dashboard.cardTop') }}</span>
          </div>
          <el-table :data="topUsers" size="small" stripe>
            <el-table-column type="index" :label="$t('bbsExtra.dashboard.colRank')" width="55" align="center" />
            <el-table-column :label="$t('bbsExtra.dashboard.colUser')" prop="nick_name" />
            <el-table-column :label="$t('bbsExtra.dashboard.colPosts')" prop="post_cnt" align="center" width="80" />
            <el-table-column :label="$t('bbsExtra.dashboard.colViews')" prop="total_views" align="center" width="90">
              <template slot-scope="scope">{{ formatCount(scope.row.total_views) }}</template>
            </el-table-column>
            <el-table-column :label="$t('bbsExtra.dashboard.colLikes')" prop="total_likes" align="center" width="90" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>{{ $t('bbsExtra.dashboard.cardPending') }}</span>
            <el-button type="primary" size="mini" plain @click="$router.push('/admin/article')">{{ $t('bbsExtra.dashboard.goManage') }}</el-button>
          </div>
          <el-table :data="pendingPosts" size="small" stripe v-loading="pendingLoading">
            <el-table-column :label="$t('bbsExtra.dashboard.colTitle')" prop="title" :show-overflow-tooltip="true" />
            <el-table-column :label="$t('bbsExtra.dashboard.colType')" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="getTypeTag(scope.row.postType)" size="mini">{{ getTypeName(scope.row.postType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('bbsExtra.dashboard.colAuthor')" width="90" align="center">
              <template slot-scope="scope">{{ scope.row.isAnonymous === '1' ? $t('bbs.anonymous') : scope.row.nickName }}</template>
            </el-table-column>
            <el-table-column :label="$t('bbsExtra.dashboard.colTime')" width="100" align="center">
              <template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="pendingPosts.length === 0 && !pendingLoading" class="no-pending">
            <i class="el-icon-check"></i> {{ $t('bbsExtra.dashboard.noPending') }}
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getPostStats } from '@/api/bbs/post'
import { listPost } from '@/api/bbs/post'
import * as echarts from 'echarts'

export default {
  name: 'BbsDashboard',
  data() {
    return {
      loading: false,
      pendingLoading: false,
      summary: {},
      statusStats: [],
      typeStats: [],
      dailyTrend: [],
      topUsers: [],
      pendingPosts: [],
      charts: {}
    }
  },
  watch: {
    '$i18n.locale'() {
      this.$nextTick(() => {
        this.renderTrendChart()
        this.renderTypeChart()
        this.renderStatusChart()
        this.renderAnonChart()
      })
    }
  },
  mounted() {
    this.loadStats()
    this.loadPending()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    Object.values(this.charts).forEach(c => c && c.dispose())
  },
  methods: {
    loadStats() {
      this.loading = true
      getPostStats().then(res => {
        const data = res.data || {}
        this.summary = data.summary || {}
        this.statusStats = data.statusStats || []
        this.typeStats = data.typeStats || []
        this.dailyTrend = data.dailyTrend || []
        this.topUsers = data.topUsers || []
        this.$nextTick(() => {
          this.renderTrendChart()
          this.renderTypeChart()
          this.renderStatusChart()
          this.renderAnonChart()
        })
      }).finally(() => { this.loading = false })
    },
    loadPending() {
      this.pendingLoading = true
      listPost({ status: '2', pageNum: 1, pageSize: 10 }).then(res => {
        this.pendingPosts = res.rows || []
      }).finally(() => { this.pendingLoading = false })
    },
    renderTrendChart() {
      const el = this.$refs.trendChart
      if (!el) return
      if (!this.charts.trend) {
        this.charts.trend = echarts.init(el)
      }
      // Fill missing days in last 7 days
      const days = []
      for (let i = 6; i >= 0; i--) {
        const d = new Date()
        d.setDate(d.getDate() - i)
        days.push(d.toISOString().slice(0, 10))
      }
      const dataMap = {}
      this.dailyTrend.forEach(item => { dataMap[item.date_str] = Number(item.cnt) })
      const values = days.map(d => dataMap[d] || 0)
      this.charts.trend.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 20, top: 20, bottom: 30 },
        xAxis: { type: 'category', data: days.map(d => d.slice(5)), axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{
          name: this.$t('bbsExtra.dashboard.colPosts'),
          type: 'bar',
          data: values,
          barMaxWidth: 40,
          itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] },
          label: { show: true, position: 'top', fontSize: 11 }
        }]
      })
    },
    renderTypeChart() {
      const el = this.$refs.typeChart
      if (!el) return
      if (!this.charts.type) {
        this.charts.type = echarts.init(el)
      }
      const nameMap = { share: this.$t('bbsExtra.dashboard.typeShare'), suggestion: this.$t('bbsExtra.dashboard.typeSuggestion'), opinion: this.$t('bbsExtra.dashboard.typeOpinion'), announcement: this.$t('bbsExtra.dashboard.typeAnnouncement'), discussion: this.$t('bbsExtra.dashboard.typeDiscussion') }
      const colorMap = { share: '#67C23A', suggestion: '#E6A23C', opinion: '#F56C6C', announcement: '#909399', discussion: '#409EFF' }
      const data = this.typeStats.map(item => ({
        name: nameMap[item.post_type] || item.post_type || this.$t('bbsExtra.dashboard.unknown'),
        value: Number(item.cnt),
        itemStyle: { color: colorMap[item.post_type] || '#909399' }
      }))
      this.charts.type.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 11 } },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          data,
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } }
        }]
      })
    },
    renderStatusChart() {
      const el = this.$refs.statusChart
      if (!el) return
      if (!this.charts.status) {
        this.charts.status = echarts.init(el)
      }
      const nameMap = { '0': this.$t('bbsExtra.dashboard.stPublished'), '1': this.$t('bbsExtra.dashboard.stClosed'), '2': this.$t('bbsExtra.dashboard.stPending'), '3': this.$t('bbsExtra.dashboard.stRejected') }
      const colorMap = { '0': '#67C23A', '1': '#909399', '2': '#E6A23C', '3': '#F56C6C' }
      const data = this.statusStats.map(item => ({
        name: nameMap[item.status] || item.status,
        value: Number(item.cnt),
        itemStyle: { color: colorMap[item.status] || '#909399' }
      }))
      this.charts.status.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 11 } },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          data,
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } }
        }]
      })
    },
    renderAnonChart() {
      const el = this.$refs.anonChart
      if (!el) return
      if (!this.charts.anon) {
        this.charts.anon = echarts.init(el)
      }
      const total = Number(this.summary.total_posts) || 0
      const anon = Number(this.summary.anonymous_count) || 0
      const real = total - anon
      const days = []
      for (let i = 6; i >= 0; i--) {
        const d = new Date()
        d.setDate(d.getDate() - i)
        days.push(d.toISOString().slice(0, 10))
      }
      this.charts.anon.setOption({
        tooltip: { trigger: 'item' },
        legend: { right: 20, top: '30%', orient: 'vertical', itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 12 } },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['40%', '50%'],
          data: [
            { name: this.$t('bbsExtra.dashboard.realPosts'), value: real, itemStyle: { color: '#409EFF' } },
            { name: this.$t('bbsExtra.dashboard.anonPosts'), value: anon, itemStyle: { color: '#C0C4CC' } }
          ],
          label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
          emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } }
        }]
      })
    },
    resizeCharts() {
      Object.values(this.charts).forEach(c => c && c.resize())
    },
    formatCount(val) {
      const n = Number(val) || 0
      if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
      return n
    },
    formatDate(ts) {
      if (!ts) return '-'
      const d = new Date(ts)
      return `${d.getMonth() + 1}/${d.getDate()}`
    },
    getTypeTag(type) {
      return { share: 'success', suggestion: 'warning', opinion: 'danger' }[type] || 'info'
    },
    getTypeName(type) {
      return { share: this.$t('bbsExtra.dashboard.shortShare'), suggestion: this.$t('bbsExtra.dashboard.shortSuggestion'), opinion: this.$t('bbsExtra.dashboard.shortOpinion'), announcement: this.$t('bbsExtra.dashboard.shortAnnouncement'), discussion: this.$t('bbsExtra.dashboard.shortDiscussion') }[type] || type || '-'
    }
  }
}
</script>

<style scoped>
.bbs-dashboard {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.dashboard-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* KPI 卡片 */
.kpi-row {
  margin-bottom: 16px;
}

.kpi-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  color: #fff;
  margin-bottom: 8px;
  min-height: 80px;
}

.kpi-icon {
  font-size: 28px;
  margin-right: 12px;
  opacity: 0.85;
}

.kpi-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
}

.kpi-label {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.9;
}

.kpi-total     { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.kpi-pending   { background: linear-gradient(135deg, #E6A23C, #f0c070); }
.kpi-published { background: linear-gradient(135deg, #67C23A, #95d475); }
.kpi-comments  { background: linear-gradient(135deg, #909399, #b1b3b8); }
.kpi-views     { background: linear-gradient(135deg, #5470c6, #7a90d9); }
.kpi-anon      { background: linear-gradient(135deg, #ee6666, #f28b8b); }

/* 图表区域 */
.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 8px;
}

.chart-container {
  height: 240px;
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

/* 底部行 */
.bottom-row .el-card {
  margin-bottom: 8px;
}

.no-pending {
  text-align: center;
  color: #67C23A;
  padding: 20px 0;
  font-size: 14px;
}

.no-pending i {
  margin-right: 6px;
}
</style>
