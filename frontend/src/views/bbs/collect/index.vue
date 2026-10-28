<template>
  <div class="collect-page">
    <div class="hero">
      <h2>在线收集中心</h2>
      <p>在线投票、调研问卷、需求征集、专题话题活动统一管理</p>
      <div class="actions">
        <el-button type="primary" @click="showCreate('vote')">新建投票</el-button>
        <el-button type="success" @click="showCreate('survey')">新建调研</el-button>
        <el-button type="warning" @click="showCreate('demand')">新建表单</el-button>
        <el-button type="danger" @click="showCreate('topic')">新建专题</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-select v-model="query.activityType" clearable placeholder="活动类型" size="small" @change="loadList">
        <el-option label="投票" value="vote" />
        <el-option label="调研" value="survey" />
        <el-option label="需求" value="demand" />
        <el-option label="专题" value="topic" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="活动状态" size="small" @change="loadList">
        <el-option label="进行中" value="PUBLISHED" />
        <el-option label="已结束" value="ENDED" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索标题" size="small" style="width: 220px" @keyup.enter.native="doSearch">
        <el-button slot="append" icon="el-icon-search" @click="doSearch" />
      </el-input>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="activityType" label="类型" width="110" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column prop="participantCount" label="参与人数" width="110" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column label="操作" min-width="280">
        <template slot-scope="scope">
          <el-button size="mini" @click="goParticipate(scope.row)">参与</el-button>
          <el-button
            size="mini"
            type="primary"
            v-if="scope.row.activityType !== 'vote' || isVoteCreator(scope.row)"
            @click="goStats(scope.row)"
          >数据</el-button>
          <el-button size="mini" type="success" v-if="scope.row.activityType === 'topic'" @click="goTopic(scope.row)">专题页</el-button>
          <el-button
            size="mini"
            type="warning"
            v-if="scope.row.status === 'PUBLISHED' && (scope.row.activityType !== 'vote' || isVoteCreator(scope.row))"
            @click="endOne(scope.row)"
          >结束</el-button>
          <el-button
            size="mini"
            type="danger"
            v-if="scope.row.activityType !== 'vote' || isVoteCreator(scope.row)"
            @click="removeOne(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="query.pageNum"
      :limit.sync="query.pageSize"
      @pagination="loadList"
    />

    <el-dialog :title="createDialogTitle" :visible.sync="createVisible" width="760px" destroy-on-close>
      <el-form :model="form" label-width="110px">
        <el-form-item label="活动标题">
          <el-input v-model="form.title" maxlength="200" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="参与范围">
              <el-select v-model="form.scopeType" style="width:100%" @change="handleScopeTypeChange">
                <el-option label="全员" value="all" />
                <el-option label="部门" value="dept" />
                <el-option label="指定人" value="users" />
                <el-option label="组织树(部门+人员)" value="org" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="范围值" v-if="form.scopeType !== 'org'">
              <el-input v-model="form.scopeValue" placeholder="部门ID或用户ID，逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="form.scopeType === 'org'" label="组织树范围">
          <div class="org-tree-box" v-loading="targetTreeLoading">
            <el-tree
              ref="targetTree"
              :data="targetTree"
              node-key="id"
              show-checkbox
              check-strictly
              default-expand-all
              :props="{ label: 'label', children: 'children' }"
            />
          </div>
          <div class="org-tree-tip">可同时勾选部门和人员；勾选部门后该部门下所有人员均会收到投票通知。</div>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="匿名"><el-switch v-model="form.anonymousFlagBool" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实时结果"><el-switch v-model="form.realtimeFlagBool" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="限投一次"><el-switch v-model="form.limitOnceFlagBool" /></el-form-item></el-col>
        </el-row>

        <template v-if="createType === 'vote'">
          <el-form-item label="投票类型">
            <el-select v-model="form.voteType">
              <el-option label="单选" value="single" />
              <el-option label="多选" value="multiple" />
              <el-option label="评分(1-5星)" value="score" />
            </el-select>
          </el-form-item>
          <el-form-item label="活动描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="投票选项">
            <div v-for="(item, idx) in form.options" :key="idx" style="display:flex; gap:8px; margin-bottom:6px;">
              <el-input v-model="form.options[idx]" placeholder="选项内容" />
              <el-button icon="el-icon-delete" @click="form.options.splice(idx,1)" />
            </div>
            <el-button size="mini" @click="form.options.push('')">新增选项</el-button>
          </el-form-item>
        </template>

        <template v-if="createType === 'survey'">
          <el-form-item label="问卷题目(JSON)">
            <el-input v-model="form.questionsJson" type="textarea" :rows="8" placeholder='[{"questionType":"single","questionTitle":"你的满意度","optionJson":"["高","中","低"]","jumpLogicJson":"{}","requiredFlag":"1"}]' />
          </el-form-item>
        </template>

        <template v-if="createType === 'demand'">
          <el-form-item label="活动描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="表单Schema(JSON)">
            <el-input v-model="form.formSchema" type="textarea" :rows="6" placeholder='[{"field":"需求名称","required":true}]' />
          </el-form-item>
        </template>

        <template v-if="createType === 'topic'">
          <el-form-item label="活动规则"><el-input v-model="form.ruleText" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="专题内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        </template>
      </el-form>
      <div slot="footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createCollectVote,
  createCollectSurvey,
  createCollectDemand,
  createCollectTopic,
  getCollectVoteTargetTree,
  listCollectActivities,
  endCollectActivity,
  deleteCollectActivity,
} from '@/api/bbs/collect'

export default {
  name: 'BbsCollectIndex',
  data() {
    return {
      loading: false,
      creating: false,
      list: [],
      total: 0,
      currentUserId: '',
      targetTreeLoading: false,
      targetTree: [],
      createVisible: false,
      createType: 'vote',
      query: { activityType: '', status: '', keyword: '', pageNum: 1, pageSize: 10 },
      form: this.getInitForm(),
    }
  },
  computed: {
    createDialogTitle() {
      const m = { vote: '新建在线投票', survey: '新建调研问卷', demand: '新建需求征集表单', topic: '新建专题话题活动' }
      return m[this.createType] || '新建活动'
    },
  },
  created() {
    this.currentUserId = this.$store.getters.userId || ''
    this.loadList()
    this.loadVoteTargetTree()
  },
  methods: {
    getInitForm() {
      return {
        title: '', scopeType: 'all', scopeValue: '', startTime: '', endTime: '',
        anonymousFlagBool: false, realtimeFlagBool: true, limitOnceFlagBool: true,
        voteType: 'single', description: '', options: ['', ''],
        questionsJson: '[{"questionType":"single","questionTitle":"你的满意度","optionJson":"[\"高\",\"中\",\"低\"]","jumpLogicJson":"{}","requiredFlag":"1"}]',
        formSchema: '[{"field":"需求名称","required":true},{"field":"需求类型","required":true},{"field":"需求详情","required":true},{"field":"优先级","required":true},{"field":"附件","required":false},{"field":"联系人","required":true}]',
        content: '', ruleText: '',
      }
    },
    isVoteCreator(row) {
      return String(row.creatorUserId || '') === String(this.currentUserId || '')
    },
    handleScopeTypeChange(type) {
      if (type !== 'org' && this.$refs.targetTree) {
        this.$refs.targetTree.setCheckedKeys([])
      }
    },
    loadVoteTargetTree() {
      this.targetTreeLoading = true
      getCollectVoteTargetTree().then(res => {
        this.targetTree = res.data || []
      }).finally(() => {
        this.targetTreeLoading = false
      })
    },
    buildCommonPayload() {
      const payload = {
        title: this.form.title,
        scopeType: this.form.scopeType,
        scopeValue: this.form.scopeValue,
        startTime: this.form.startTime,
        endTime: this.form.endTime,
        anonymousFlag: this.form.anonymousFlagBool ? '1' : '0',
        realtimeFlag: this.form.realtimeFlagBool ? '1' : '0',
        limitOnceFlag: this.form.limitOnceFlagBool ? '1' : '0',
        ruleText: this.form.ruleText,
      }
      if (this.form.scopeType === 'org') {
        const keys = this.$refs.targetTree ? this.$refs.targetTree.getCheckedKeys() : []
        payload.scopeValue = (keys || []).join(',')
      }
      return payload
    },
    showCreate(type) {
      this.createType = type
      this.form = this.getInitForm()
      this.$nextTick(() => {
        if (this.$refs.targetTree) {
          this.$refs.targetTree.setCheckedKeys([])
        }
      })
      this.createVisible = true
    },
    doSearch() {
      this.query.pageNum = 1
      this.loadList()
    },
    loadList() {
      this.loading = true
      listCollectActivities(this.query).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    submitCreate() {
      if (!this.form.title) {
        this.$message.error('请输入活动标题')
        return
      }
      const common = this.buildCommonPayload()
      if (this.createType === 'vote' && common.scopeType === 'org' && !common.scopeValue) {
        this.$message.error('请选择至少一个部门或人员')
        return
      }
      this.creating = true
      let p = null
      if (this.createType === 'vote') {
        p = createCollectVote({ ...common, voteType: this.form.voteType, description: this.form.description, options: this.form.options })
      } else if (this.createType === 'survey') {
        let questions = []
        try {
          questions = JSON.parse(this.form.questionsJson || '[]')
        } catch (e) {
          this.$message.error('问卷题目JSON格式错误')
          this.creating = false
          return
        }
        p = createCollectSurvey({ ...common, questions })
      } else if (this.createType === 'demand') {
        p = createCollectDemand({ ...common, description: this.form.description, formSchema: this.form.formSchema })
      } else {
        p = createCollectTopic({ ...common, content: this.form.content })
      }
      p.then(() => {
        this.$message.success('创建成功')
        this.createVisible = false
        this.loadList()
      }).finally(() => {
        this.creating = false
      })
    },
    goParticipate(row) {
      this.$router.push(`/collect/participate/${row.activityId}`)
    },
    goStats(row) {
      this.$router.push(`/collect/stats/${row.activityId}`)
    },
    goTopic(row) {
      this.$router.push(`/collect/topic/${row.activityId}`)
    },
    endOne(row) {
      endCollectActivity(row.activityId).then(() => {
        this.$message.success('活动已结束')
        this.loadList()
      })
    },
    removeOne(row) {
      this.$confirm('确定删除该活动吗？', '提示', { type: 'warning' }).then(() => {
        deleteCollectActivity(row.activityId).then(() => {
          this.$message.success('已删除')
          this.loadList()
        })
      }).catch(() => {})
    },
  },
}
</script>

<style scoped>
.collect-page { max-width: 1200px; margin: 0 auto; padding: 16px; }
.hero { background: linear-gradient(135deg, #31c27c, #b8e986); border-radius: 12px; padding: 20px; margin-bottom: 14px; }
.hero h2 { margin: 0 0 8px; }
.hero p { margin: 0 0 12px; color: #1f2d3d; }
.actions { display: flex; gap: 10px; flex-wrap: wrap; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.org-tree-box { border: 1px solid #dcdfe6; border-radius: 6px; padding: 8px; max-height: 260px; overflow: auto; }
.org-tree-tip { margin-top: 6px; color: #909399; font-size: 12px; }
</style>
