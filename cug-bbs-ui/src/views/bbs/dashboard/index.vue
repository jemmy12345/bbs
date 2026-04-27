<template>
  <div class="bbs-dashboard">
    <!-- 顶部操作栏 -->
    <div class="dashboard-header">
      <span class="dashboard-title">论坛运营看板</span>
      <el-button size="small" icon="el-icon-refresh" @click="loadStats" :loading="loading">刷新</el-button>
    </div>

    <!-- KPI卡片行 -->
    <el-row :gutter="16" class="kpi-row" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-total">
          <div class="kpi-icon"><i class="el-icon-document"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.total_posts || 0 }}</div>
            <div class="kpi-label">帖子总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-pending">
          <div class="kpi-icon"><i class="el-icon-time"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.pending_count || 0 }}</div>
            <div class="kpi-label">待审核</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-published">
          <div class="kpi-icon"><i class="el-icon-check"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.published_count || 0 }}</div>
            <div class="kpi-label">已发布</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-comments">
          <div class="kpi-icon"><i class="el-icon-chat-dot-round"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.total_comments || 0 }}</div>
            <div class="kpi-label">评论总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-views">
          <div class="kpi-icon"><i class="el-icon-view"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ formatCount(summary.total_views) }}</div>
            <div class="kpi-label">总浏览量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="kpi-card kpi-anon">
          <div class="kpi-icon"><i class="el-icon-user"></i></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ summary.anonymous_count || 0 }}</div>
            <div class="kpi-label">匿名帖子</div>
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
            <span>近7天发帖趋势</span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <!-- 帖子类型分布 -->
      <el-col :xs="24" :md="10">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>帖子类型分布</span>
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
            <span>帖子状态分布</span>
          </div>
          <div ref="statusChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <!-- 实名 vs 匿名 -->
      <el-col :xs="24" :md="14">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>实名 / 匿名比例</span>
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
            <span>活跃用户 Top 10</span>
          </div>
          <el-table :data="topUsers" size="small" stripe>
            <el-table-column type="index" label="排名" width="55" align="center" />
            <el-table-column label="用户" prop="nick_name" />
            <el-table-column label="发帖数" prop="post_cnt" align="center" width="80" />
            <el-table-column label="总浏览" prop="total_views" align="center" width="90">
              <template slot-scope="scope">{{ formatCount(scope.row.total_views) }}</template>
            </el-table-column>
            <el-table-column label="总获赞" prop="total_likes" align="center" width="90" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>待审核帖子</span>
            <el-button type="primary" size="mini" plain @click="$router.push('/admin/article')">前往管理</el-button>
          </div>
          <el-table :data="pendingPosts" size="small" stripe v-loading="pendingLoading">
            <el-table-column label="标题" prop="title" :show-overflow-tooltip="true" />
            <el-table-column label="类型" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="getTypeTag(scope.row.postType)" size="mini">{{ getTypeName(scope.row.postType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="作者" width="90" align="center">
              <template slot-scope="scope">{{ scope.row.isAnonymous === '1' ? '匿名' : scope.row.nickName }}</template>
            </el-table-column>
            <el-table-column label="时间" width="100" align="center">
              <template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="pendingPosts.length === 0 && !pendingLoading" class="no-pending">
            <i class="el-icon-check"></i> 暂无待审核帖子
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
          name: '发帖数',
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
      const nameMap = { share: '经验分享', suggestion: '意见建议', opinion: '意见投票', announcement: '公告', discussion: '讨论' }
      const colorMap = { share: '#67C23A', suggestion: '#E6A23C', opinion: '#F56C6C', announcement: '#909399', discussion: '#409EFF' }
      const data = this.typeStats.map(item => ({
        name: nameMap[item.post_type] || item.post_type || '未知',
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
      const nameMap = { '0': '已发布', '1': '已关闭', '2': '待审核', '3': '审核不通过' }
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
            { name: '实名发帖', value: real, itemStyle: { color: '#409EFF' } },
            { name: '匿名发帖', value: anon, itemStyle: { color: '#C0C4CC' } }
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
      return { share: '分享', suggestion: '建议', opinion: '投票', announcement: '公告', discussion: '讨论' }[type] || type || '-'
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
