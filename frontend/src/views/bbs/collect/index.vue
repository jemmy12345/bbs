<template>
  <div class="collect-page">
    <div class="hero">
      <h2>{{ $t('collect.title') }}</h2>
      <p>{{ $t('collect.subtitle') }}</p>
      <div class="actions">
        <el-button type="primary" @click="showCreate('vote')">{{ $t('collect.newVote') }}</el-button>
        <el-button type="success" @click="showCreate('survey')">{{ $t('collect.newSurvey') }}</el-button>
        <el-button type="warning" @click="showCreate('demand')">{{ $t('collect.newForm') }}</el-button>
        <el-button type="danger" @click="showCreate('topic')">{{ $t('collect.newTopic') }}</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-select v-model="query.activityType" clearable :placeholder="$t('collect.activityType')" size="small" @change="loadList">
        <el-option :label="$t('collect.typeVote')" value="vote" />
        <el-option :label="$t('collect.typeSurvey')" value="survey" />
        <el-option :label="$t('collect.typeDemand')" value="demand" />
        <el-option :label="$t('collect.typeTopic')" value="topic" />
      </el-select>
      <el-select v-model="query.status" clearable :placeholder="$t('collect.activityStatus')" size="small" @change="loadList">
        <el-option :label="$t('collect.statusOngoing')" value="PUBLISHED" />
        <el-option :label="$t('collect.statusEnded')" value="ENDED" />
      </el-select>
      <el-input v-model="query.keyword" :placeholder="$t('collect.searchTitle')" size="small" style="width: 220px" @keyup.enter.native="doSearch">
        <el-button slot="append" icon="el-icon-search" @click="doSearch" />
      </el-input>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" :label="$t('collect.colTitle')" min-width="220" />
      <el-table-column prop="activityType" :label="$t('collect.colType')" width="110" />
      <el-table-column prop="status" :label="$t('collect.colStatus')" width="110" />
      <el-table-column prop="participantCount" :label="$t('collect.colParticipants')" width="110" />
      <el-table-column prop="startTime" :label="$t('collect.colStart')" width="170" />
      <el-table-column prop="endTime" :label="$t('collect.colEnd')" width="170" />
      <el-table-column :label="$t('collect.colActions')" min-width="280">
        <template slot-scope="scope">
          <el-button size="mini" @click="goParticipate(scope.row)">{{ $t('collect.participate') }}</el-button>
          <el-button
            size="mini"
            type="primary"
            v-if="scope.row.activityType !== 'vote' || isVoteCreator(scope.row)"
            @click="goStats(scope.row)"
          >{{ $t('collect.data') }}</el-button>
          <el-button size="mini" type="success" v-if="scope.row.activityType === 'topic'" @click="goTopic(scope.row)">{{ $t('collect.topicPage') }}</el-button>
          <el-button
            size="mini"
            type="warning"
            v-if="scope.row.status === 'PUBLISHED' && (scope.row.activityType !== 'vote' || isVoteCreator(scope.row))"
            @click="endOne(scope.row)"
          >{{ $t('collect.end') }}</el-button>
          <el-button
            size="mini"
            type="danger"
            v-if="scope.row.activityType !== 'vote' || isVoteCreator(scope.row)"
            @click="removeOne(scope.row)"
          >{{ $t('collect.delete') }}</el-button>
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
        <el-form-item :label="$t('collect.activityTitle')">
          <el-input v-model="form.title" maxlength="200" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="$t('collect.scope')">
              <el-select v-model="form.scopeType" style="width:100%" @change="handleScopeTypeChange">
                <el-option :label="$t('collect.scopeAll')" value="all" />
                <el-option :label="$t('collect.scopeDept')" value="dept" />
                <el-option :label="$t('collect.scopeUsers')" value="users" />
                <el-option :label="$t('collect.scopeOrg')" value="org" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('collect.scopeValue')" v-if="form.scopeType !== 'org'">
              <el-input v-model="form.scopeValue" :placeholder="$t('collect.scopeValuePlaceholder')" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="form.scopeType === 'org'" :label="$t('collect.orgScope')">
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
          <div class="org-tree-tip">{{ $t('collect.orgTip') }}</div>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item :label="$t('collect.startTime')"><el-date-picker v-model="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="$t('collect.endTime')"><el-date-picker v-model="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item :label="$t('collect.anonymous')"><el-switch v-model="form.anonymousFlagBool" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item :label="$t('collect.realtime')"><el-switch v-model="form.realtimeFlagBool" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item :label="$t('collect.limitOnce')"><el-switch v-model="form.limitOnceFlagBool" /></el-form-item></el-col>
        </el-row>

        <template v-if="createType === 'vote'">
          <el-form-item :label="$t('collect.voteType')">
            <el-select v-model="form.voteType">
              <el-option :label="$t('collect.single')" value="single" />
              <el-option :label="$t('collect.multiple')" value="multiple" />
              <el-option :label="$t('collect.score')" value="score" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('collect.description')"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
          <el-form-item :label="$t('collect.voteOptions')">
            <div v-for="(item, idx) in form.options" :key="idx" style="display:flex; gap:8px; margin-bottom:6px;">
              <el-input v-model="form.options[idx]" :placeholder="$t('collect.optionContent')" />
              <el-button icon="el-icon-delete" @click="form.options.splice(idx,1)" />
            </div>
            <el-button size="mini" @click="form.options.push('')">{{ $t('collect.addOption') }}</el-button>
          </el-form-item>
        </template>

        <template v-if="createType === 'survey'">
          <el-form-item :label="$t('collect.surveyQuestions')">
            <el-input v-model="form.questionsJson" type="textarea" :rows="8" :placeholder="surveyPlaceholder" />
          </el-form-item>
        </template>

        <template v-if="createType === 'demand'">
          <el-form-item :label="$t('collect.description')"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
          <el-form-item :label="$t('collect.formSchema')">
            <el-input v-model="form.formSchema" type="textarea" :rows="6" :placeholder="schemaPlaceholder" />
          </el-form-item>
        </template>

        <template v-if="createType === 'topic'">
          <el-form-item :label="$t('collect.ruleText')"><el-input v-model="form.ruleText" type="textarea" :rows="2" /></el-form-item>
          <el-form-item :label="$t('collect.topicContent')"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        </template>
      </el-form>
      <div slot="footer">
        <el-button @click="createVisible = false">{{ $t('collect.cancel') }}</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">{{ $t('collect.create') }}</el-button>
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
    surveyPlaceholder() {
      return JSON.stringify([{
        questionType: 'single',
        questionTitle: this.$t('collect.sampleSatisfaction'),
        optionJson: JSON.stringify([this.$t('collect.optHigh'), this.$t('collect.optMedium'), this.$t('collect.optLow')]),
        jumpLogicJson: '{}',
        requiredFlag: '1',
      }])
    },
    schemaPlaceholder() {
      return JSON.stringify([{ field: this.$t('collect.sampleDemandName'), required: true }])
    },
    createDialogTitle() {
      const m = { vote: this.$t('collect.dlgVote'), survey: this.$t('collect.dlgSurvey'), demand: this.$t('collect.dlgDemand'), topic: this.$t('collect.dlgTopic') }
      return m[this.createType] || this.$t('collect.dlgDefault')
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
        this.$message.error(this.$t('collect.errTitle'))
        return
      }
      const common = this.buildCommonPayload()
      if (this.createType === 'vote' && common.scopeType === 'org' && !common.scopeValue) {
        this.$message.error(this.$t('collect.errOrg'))
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
          this.$message.error(this.$t('collect.errSurveyJson'))
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
        this.$message.success(this.$t('collect.created'))
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
        this.$message.success(this.$t('collect.ended'))
        this.loadList()
      })
    },
    removeOne(row) {
      this.$confirm(this.$t('collect.confirmDelete'), this.$t('collect.tip'), { type: 'warning' }).then(() => {
        deleteCollectActivity(row.activityId).then(() => {
          this.$message.success(this.$t('collect.deleted'))
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
