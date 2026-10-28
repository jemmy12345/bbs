<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="标题/内容" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="提交人" prop="submitNickName">
        <el-input v-model="queryParams.submitNickName" placeholder="提交人昵称" clearable style="width: 160px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="状态" style="width: 140px">
          <el-option v-for="s in STATUS" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" clearable placeholder="分类" style="width: 140px">
          <el-option v-for="c in CATS" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="编号" align="center" prop="ideaId" width="80" />
      <el-table-column label="标题" prop="title" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="分类" align="center" width="100">
        <template slot-scope="scope">{{ label(CATS, scope.row.category) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag size="mini">{{ label(STATUS, scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交人" align="center" prop="submitNickName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="平均分" align="center" prop="avgScore" width="80" />
      <el-table-column label="点赞" align="center" prop="likeCount" width="70" />
      <el-table-column label="提交时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详情</el-button>
          <el-button v-if="scope.row.isPublicized === '1'" size="mini" type="text" icon="el-icon-remove-outline" @click="handleCancelPublicize(scope.row)">取消公示</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="提案详情" :visible.sync="open" width="780px" append-to-body>
      <el-descriptions :column="2" border size="small" v-if="detail">
        <el-descriptions-item label="编号">{{ detail.ideaId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ label(STATUS, detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ label(CATS, detail.category) }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ detail.submitNickName }}</el-descriptions-item>
        <el-descriptions-item label="团队">{{ detail.teamName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="平均分">{{ detail.avgScore == null ? '—' : detail.avgScore }}</el-descriptions-item>
        <el-descriptions-item label="提案内容" :span="2"><div class="pre">{{ detail.content }}</div></el-descriptions-item>
        <el-descriptions-item label="背景" :span="2"><div class="pre">{{ detail.backgroundText || '—' }}</div></el-descriptions-item>
        <el-descriptions-item label="实施方案" :span="2"><div class="pre">{{ detail.planText || '—' }}</div></el-descriptions-item>
        <el-descriptions-item label="预期价值" :span="2"><div class="pre">{{ detail.expectedValue || '—' }}</div></el-descriptions-item>
        <el-descriptions-item label="驳回原因" :span="2" v-if="detail.rejectReason">{{ detail.rejectReason }}</el-descriptions-item>
        <el-descriptions-item label="结果摘要" :span="2" v-if="detail.resultSummary">{{ detail.resultSummary }}</el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">{{ parseTime(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <h4>评审记录</h4>
      <el-table :data="(detail && detail.reviews) || []" size="mini" border>
        <el-table-column label="环节" prop="stage" width="70" />
        <el-table-column label="评审人" prop="reviewerNickName" width="110" />
        <el-table-column label="结论" prop="action" width="80" />
        <el-table-column label="评分" prop="score" width="60" />
        <el-table-column label="意见" prop="comment" :show-overflow-tooltip="true" />
        <el-table-column label="时间" width="150"><template slot-scope="s">{{ parseTime(s.row.createTime) }}</template></el-table-column>
      </el-table>
      <h4>进展记录</h4>
      <el-table :data="(detail && detail.progressList) || []" size="mini" border>
        <el-table-column label="阶段" prop="progressStage" width="100" />
        <el-table-column label="进度%" prop="progressPercent" width="70" />
        <el-table-column label="说明" prop="progressDesc" :show-overflow-tooltip="true" />
        <el-table-column label="更新人" prop="updaterNickName" width="100" />
        <el-table-column label="时间" width="150"><template slot-scope="s">{{ parseTime(s.row.createTime) }}</template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listIdeaManage, getIdeaManage, delIdeaManage, cancelIdeaPublicize } from '@/api/bbs/ideaManage'

const STATUS = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING_L1', label: '待初审' },
  { value: 'L1_REVIEWING', label: '初审中' },
  { value: 'L1_PASSED', label: '初审通过' },
  { value: 'L2_REVIEWING', label: '复审中' },
  { value: 'L2_PASSED', label: '复审通过' },
  { value: 'L3_REVIEWING', label: '终审中' },
  { value: 'APPROVED', label: '已立项' },
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
  name: 'BbsIdeaManage',
  data() {
    return {
      STATUS,
      CATS,
      loading: true,
      showSearch: true,
      total: 0,
      list: [],
      ids: [],
      multiple: true,
      open: false,
      detail: null,
      queryParams: { pageNum: 1, pageSize: 10, keyword: undefined, submitNickName: undefined, status: undefined, category: undefined },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    label(options, v) {
      const t = options.find(i => i.value === v)
      return t ? t.label : v || '—'
    },
    getList() {
      this.loading = true
      listIdeaManage(this.queryParams)
        .then(res => {
          this.list = res.rows || []
          this.total = res.total || 0
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(i => i.ideaId)
      this.multiple = !selection.length
    },
    handleView(row) {
      getIdeaManage(row.ideaId).then(res => {
        this.detail = res.data
        this.open = true
      })
    },
    handleDelete(row) {
      const ids = row ? row.ideaId : this.ids.join(',')
      this.$modal
        .confirm('是否确认删除提案编号为"' + ids + '"的数据项？')
        .then(() => delIdeaManage(ids))
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleCancelPublicize(row) {
      this.$modal
        .confirm('是否确认取消提案"' + row.title + '"的公示？取消后将从成果墙移除，状态恢复为已落地。')
        .then(() => cancelIdeaPublicize(row.ideaId))
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('已取消公示')
        })
        .catch(() => {})
    },
    handleExport() {
      const { keyword, submitNickName, status, category } = this.queryParams
      this.download('/bbs/ideaManage/export', { keyword, submitNickName, status, category }, `idea_${new Date().getTime()}.xlsx`)
    },
  },
}
</script>

<style scoped>
.pre { white-space: pre-wrap; word-break: break-all; }
</style>
