<template>
  <div class="enterprise-page">
    <div class="page-header">
      <h2>{{ $t('enterprise.pageTitle') }}</h2>
      <p>{{ $t('enterprise.pageDesc') }}</p>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane :label="$t('enterprise.tabQa')" name="qa">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="showAskDialog = true">{{ $t('enterprise.askReward') }}</el-button>
        </div>
        <div class="card-list">
          <div class="card" v-for="q in qaQuestions" :key="q.questionId" @click="openQaDetail(q.questionId)">
            <div class="title">{{ q.title }}</div>
            <div class="meta">{{ $t('enterprise.rewardMeta', { points: q.rewardPoints || 0, status: q.status }) }}</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="$t('enterprise.tabIdea')" name="idea">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="showIdeaDialog = true">{{ $t('enterprise.submitIdea') }}</el-button>
        </div>
        <div class="card-list">
          <div class="card" v-for="idea in ideas" :key="idea.ideaId" @click="openIdeaDetail(idea.ideaId)">
            <div class="title">{{ idea.title }}</div>
            <div class="meta">{{ $t('enterprise.ideaMeta', { status: idea.status, score: idea.avgScore || 0 }) }}</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="$t('enterprise.tabEngage')" name="engage">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="panel">
              <div class="panel-title">{{ $t('enterprise.onlineVote') }}</div>
              <el-button type="text" @click="showVoteDialog = true">{{ $t('enterprise.createVote') }}</el-button>
              <div class="mini-list">
                <div v-for="v in votes" :key="v.voteId" class="mini-item" @click="openVoteDetail(v.voteId)">
                  {{ v.title }}
                </div>
              </div>
            </div>
          </el-col>

          <el-col :span="8">
            <div class="panel">
              <div class="panel-title">{{ $t('enterprise.demandForms') }}</div>
              <el-button type="text" @click="showDemandDialog = true">{{ $t('enterprise.createForm') }}</el-button>
              <div class="mini-list">
                <div v-for="f in demandForms" :key="f.formId" class="mini-item">
                  <div>{{ f.title }}</div>
                  <el-input type="textarea" :rows="2" v-model="demandDraft[f.formId]" :placeholder="$t('enterprise.fillDemand')"></el-input>
                  <el-button size="mini" @click="submitDemandNow(f.formId)">{{ $t('enterprise.submit') }}</el-button>
                </div>
              </div>
            </div>
          </el-col>

          <el-col :span="8">
            <div class="panel">
              <div class="panel-title">{{ $t('enterprise.topicActivities') }}</div>
              <el-button type="text" @click="showTopicDialog = true">{{ $t('enterprise.startActivity') }}</el-button>
              <div class="mini-list">
                <div v-for="a in topicActivities" :key="a.activityId" class="mini-item">
                  <div>{{ $t('enterprise.peopleCount', { title: a.title, count: a.joinCount || 0 }) }}</div>
                  <el-button size="mini" @click="joinTopicNow(a.activityId)">{{ $t('enterprise.join') }}</el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="$t('enterprise.askReward')" :visible.sync="showAskDialog" width="520px">
      <el-form :model="askForm" label-width="90px">
        <el-form-item :label="$t('enterprise.title')"><el-input v-model="askForm.title" /></el-form-item>
        <el-form-item :label="$t('enterprise.content')"><el-input type="textarea" :rows="4" v-model="askForm.content" /></el-form-item>
        <el-form-item :label="$t('enterprise.rewardPoints')"><el-input-number v-model="askForm.rewardPoints" :min="1" :max="500" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAskDialog = false">{{ $t('enterprise.cancel') }}</el-button>
        <el-button type="primary" @click="createQuestionNow">{{ $t('enterprise.submit') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('enterprise.questionDetail')" :visible.sync="showQaDetail" width="760px">
      <div v-if="qaDetail">
        <h3>{{ qaDetail.title }}</h3>
        <p>{{ qaDetail.content }}</p>
        <el-tag size="mini">{{ $t('enterprise.statusLabel', { status: qaDetail.status }) }}</el-tag>
        <el-divider></el-divider>
        <div v-for="ans in qaDetail.answers || []" :key="ans.answerId" class="answer-item">
          <div>{{ ans.content }}</div>
          <div class="meta">{{ ans.answerNickName }}
            <el-tag v-if="ans.isBest === '1'" size="mini" type="success">{{ $t('enterprise.bestAnswer') }}</el-tag>
            <el-button v-if="qaDetail.status === 'open'" size="mini" type="text" @click="acceptAnswerNow(ans.answerId)">{{ $t('enterprise.accept') }}</el-button>
          </div>
        </div>
        <el-input type="textarea" :rows="3" v-model="qaAnswerContent" :placeholder="$t('enterprise.answerPlaceholder')"></el-input>
        <el-button size="mini" type="primary" @click="submitAnswerNow">{{ $t('enterprise.submitAnswer') }}</el-button>
        <el-button size="mini" @click="archiveQuestionNow">{{ $t('enterprise.archiveQuestion') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('enterprise.submitIdeaTitle')" :visible.sync="showIdeaDialog" width="560px">
      <el-form :model="ideaForm" label-width="90px">
        <el-form-item :label="$t('enterprise.title')"><el-input v-model="ideaForm.title" /></el-form-item>
        <el-form-item :label="$t('enterprise.ideaContent')"><el-input type="textarea" :rows="4" v-model="ideaForm.content" /></el-form-item>
        <el-form-item :label="$t('enterprise.bizValue')"><el-input type="textarea" :rows="3" v-model="ideaForm.bizValue" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showIdeaDialog = false">{{ $t('enterprise.cancel') }}</el-button>
        <el-button type="primary" @click="submitIdeaNow">{{ $t('enterprise.submit') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('enterprise.ideaDetail')" :visible.sync="showIdeaDetail" width="760px">
      <div v-if="ideaDetail">
        <h3>{{ ideaDetail.title }}</h3>
        <p>{{ ideaDetail.content }}</p>
        <p>{{ $t('enterprise.bizValueLine', { value: ideaDetail.bizValue }) }}</p>
        <el-tag size="mini">{{ $t('enterprise.statusLabel', { status: ideaDetail.status }) }}</el-tag>
        <el-divider>{{ $t('enterprise.onlineReview') }}</el-divider>
        <div>
          <el-input-number v-model="reviewForm.score" :min="1" :max="10"></el-input-number>
          <el-input v-model="reviewForm.comment" style="width:60%; margin-left:8px;" :placeholder="$t('enterprise.reviewComment')"></el-input>
          <el-button size="mini" type="primary" @click="submitReviewNow">{{ $t('enterprise.submitReview') }}</el-button>
        </div>
        <div v-for="rv in ideaDetail.reviews || []" :key="rv.reviewId" class="mini-item">
          {{ $t('enterprise.reviewLine', { name: rv.reviewerNickName, score: rv.score, comment: rv.comment }) }}
        </div>

        <el-divider>{{ $t('enterprise.progress') }}</el-divider>
        <div>
          <el-input v-model="progressForm.stage" style="width:25%" :placeholder="$t('enterprise.stage')"></el-input>
          <el-input v-model="progressForm.desc" style="width:40%; margin-left:8px;" :placeholder="$t('enterprise.desc')"></el-input>
          <el-input-number v-model="progressForm.percent" :min="0" :max="100" style="margin-left:8px"></el-input-number>
          <el-button size="mini" type="primary" @click="addProgressNow">{{ $t('enterprise.update') }}</el-button>
        </div>
        <div v-for="pg in ideaDetail.progressList || []" :key="pg.progressId" class="mini-item">
          {{ pg.progressStage }} - {{ pg.progressPercent }}% - {{ pg.progressDesc }}
        </div>

        <el-divider>{{ $t('enterprise.resultPublish') }}</el-divider>
        <el-input type="textarea" :rows="3" v-model="publishSummary" :placeholder="$t('enterprise.resultSummary')"></el-input>
        <el-button size="mini" type="success" @click="publishResultNow">{{ $t('enterprise.publishResult') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('enterprise.createVote')" :visible.sync="showVoteDialog" width="560px">
      <el-form :model="voteForm" label-width="90px">
        <el-form-item :label="$t('enterprise.title')"><el-input v-model="voteForm.title" /></el-form-item>
        <el-form-item :label="$t('enterprise.description')"><el-input type="textarea" :rows="3" v-model="voteForm.description" /></el-form-item>
        <el-form-item :label="$t('enterprise.type')">
          <el-select v-model="voteForm.voteType"><el-option value="single" :label="$t('enterprise.single')"></el-option><el-option value="multiple" :label="$t('enterprise.multiple')"></el-option></el-select>
        </el-form-item>
        <el-form-item :label="$t('enterprise.optionsLabel')"><el-input type="textarea" :rows="4" v-model="voteOptionsText" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="showVoteDialog = false">{{ $t('enterprise.cancel') }}</el-button><el-button type="primary" @click="createVoteNow">{{ $t('enterprise.create') }}</el-button></div>
    </el-dialog>

    <el-dialog :title="$t('enterprise.voteDetail')" :visible.sync="showVoteDetail" width="620px">
      <div v-if="voteDetail">
        <h3>{{ voteDetail.title }}</h3>
        <p>{{ voteDetail.description }}</p>
        <el-checkbox-group v-model="selectedVoteOptionIds">
          <el-checkbox v-for="o in voteDetail.options || []" :key="o.optionId" :label="o.optionId">{{ $t('enterprise.optionCount', { text: o.optionText, count: o.voteCount }) }}</el-checkbox>
        </el-checkbox-group>
        <div style="margin-top:10px"><el-button type="primary" size="mini" @click="submitVoteNow">{{ $t('enterprise.submitVote') }}</el-button></div>
      </div>
    </el-dialog>

    <el-dialog :title="$t('enterprise.createDemandForm')" :visible.sync="showDemandDialog" width="520px">
      <el-form :model="demandForm" label-width="90px">
        <el-form-item :label="$t('enterprise.title')"><el-input v-model="demandForm.title" /></el-form-item>
        <el-form-item :label="$t('enterprise.description')"><el-input type="textarea" :rows="3" v-model="demandForm.description" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="showDemandDialog = false">{{ $t('enterprise.cancel') }}</el-button><el-button type="primary" @click="createDemandNow">{{ $t('enterprise.create') }}</el-button></div>
    </el-dialog>

    <el-dialog :title="$t('enterprise.startTopicActivity')" :visible.sync="showTopicDialog" width="520px">
      <el-form :model="topicForm" label-width="90px">
        <el-form-item :label="$t('enterprise.activityTitle')"><el-input v-model="topicForm.title" /></el-form-item>
        <el-form-item :label="$t('enterprise.activityContent')"><el-input type="textarea" :rows="4" v-model="topicForm.content" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="showTopicDialog = false">{{ $t('enterprise.cancel') }}</el-button><el-button type="primary" @click="createTopicNow">{{ $t('enterprise.start') }}</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createQaQuestion,
  listQaQuestions,
  getQaQuestionDetail,
  answerQaQuestion,
  acceptQaAnswer,
  archiveQaQuestion,
  submitIdea,
  listIdeas,
  getIdeaDetail,
  reviewIdea,
  addIdeaProgress,
  publishIdeaResult,
  createVote,
  listVotes,
  getVoteDetail,
  submitVote,
  createDemandForm,
  listDemandForms,
  submitDemand,
  createTopicActivity,
  listTopicActivities,
  joinTopicActivity
} from '@/api/bbs/enterprise'

export default {
  name: 'BbsEnterprise',
  data() {
    return {
      activeTab: 'qa',
      qaQuestions: [],
      ideas: [],
      votes: [],
      demandForms: [],
      topicActivities: [],
      demandDraft: {},

      showAskDialog: false,
      askForm: { title: '', content: '', rewardPoints: 20 },

      showQaDetail: false,
      qaDetail: null,
      qaAnswerContent: '',

      showIdeaDialog: false,
      ideaForm: { title: '', content: '', bizValue: '' },
      showIdeaDetail: false,
      ideaDetail: null,
      reviewForm: { score: 8, comment: '' },
      progressForm: { stage: '', desc: '', percent: 0 },
      publishSummary: '',

      showVoteDialog: false,
      voteForm: { title: '', description: '', voteType: 'single' },
      voteOptionsText: this.$t('enterprise.defaultOptions'),
      showVoteDetail: false,
      voteDetail: null,
      selectedVoteOptionIds: [],

      showDemandDialog: false,
      demandForm: { title: '', description: '' },

      showTopicDialog: false,
      topicForm: { title: '', content: '' }
    }
  },
  created() {
    this.loadAll()
  },
  methods: {
    loadAll() {
      this.loadQa()
      this.loadIdeas()
      this.loadVotes()
      this.loadDemandForms()
      this.loadTopicActivities()
    },
    loadQa() {
      listQaQuestions().then(res => { this.qaQuestions = res.data || [] })
    },
    loadIdeas() {
      listIdeas().then(res => { this.ideas = res.data || [] })
    },
    loadVotes() {
      listVotes().then(res => { this.votes = res.data || [] })
    },
    loadDemandForms() {
      listDemandForms().then(res => { this.demandForms = res.data || [] })
    },
    loadTopicActivities() {
      listTopicActivities().then(res => { this.topicActivities = res.data || [] })
    },

    createQuestionNow() {
      createQaQuestion(this.askForm).then(() => {
        this.$message.success(this.$t('enterprise.msgAsked'))
        this.showAskDialog = false
        this.askForm = { title: '', content: '', rewardPoints: 20 }
        this.loadQa()
      })
    },
    openQaDetail(id) {
      getQaQuestionDetail(id).then(res => {
        this.qaDetail = res.data
        this.showQaDetail = true
      })
    },
    submitAnswerNow() {
      if (!this.qaDetail) return
      answerQaQuestion(this.qaDetail.questionId, { content: this.qaAnswerContent }).then(() => {
        this.qaAnswerContent = ''
        this.openQaDetail(this.qaDetail.questionId)
      })
    },
    acceptAnswerNow(answerId) {
      acceptQaAnswer(this.qaDetail.questionId, answerId).then(() => {
        this.$message.success(this.$t('enterprise.msgBestAccepted'))
        this.openQaDetail(this.qaDetail.questionId)
        this.loadQa()
      })
    },
    archiveQuestionNow() {
      archiveQaQuestion(this.qaDetail.questionId).then(() => {
        this.$message.success(this.$t('enterprise.msgArchived'))
        this.openQaDetail(this.qaDetail.questionId)
        this.loadQa()
      })
    },

    submitIdeaNow() {
      submitIdea(this.ideaForm).then(() => {
        this.$message.success(this.$t('enterprise.msgIdeaSubmitted'))
        this.showIdeaDialog = false
        this.ideaForm = { title: '', content: '', bizValue: '' }
        this.loadIdeas()
      })
    },
    openIdeaDetail(id) {
      getIdeaDetail(id).then(res => {
        this.ideaDetail = res.data
        this.showIdeaDetail = true
      })
    },
    submitReviewNow() {
      reviewIdea(this.ideaDetail.ideaId, this.reviewForm).then(() => {
        this.$message.success(this.$t('enterprise.msgReviewSubmitted'))
        this.reviewForm.comment = ''
        this.openIdeaDetail(this.ideaDetail.ideaId)
        this.loadIdeas()
      })
    },
    addProgressNow() {
      addIdeaProgress(this.ideaDetail.ideaId, this.progressForm).then(() => {
        this.$message.success(this.$t('enterprise.msgProgressUpdated'))
        this.progressForm = { stage: '', desc: '', percent: 0 }
        this.openIdeaDetail(this.ideaDetail.ideaId)
      })
    },
    publishResultNow() {
      publishIdeaResult(this.ideaDetail.ideaId, this.publishSummary).then(() => {
        this.$message.success(this.$t('enterprise.msgResultPublished'))
        this.publishSummary = ''
        this.openIdeaDetail(this.ideaDetail.ideaId)
        this.loadIdeas()
      })
    },

    createVoteNow() {
      const options = this.voteOptionsText.split('\n').map(x => x.trim()).filter(Boolean)
      createVote({ ...this.voteForm, options }).then(() => {
        this.$message.success(this.$t('enterprise.msgVoteCreated'))
        this.showVoteDialog = false
        this.loadVotes()
      })
    },
    openVoteDetail(voteId) {
      getVoteDetail(voteId).then(res => {
        this.voteDetail = res.data
        this.showVoteDetail = true
        this.selectedVoteOptionIds = []
      })
    },
    submitVoteNow() {
      submitVote(this.voteDetail.voteId, this.selectedVoteOptionIds).then(() => {
        this.$message.success(this.$t('enterprise.msgVoted'))
        this.openVoteDetail(this.voteDetail.voteId)
        this.loadVotes()
      })
    },

    createDemandNow() {
      createDemandForm(this.demandForm).then(() => {
        this.$message.success(this.$t('enterprise.msgDemandFormCreated'))
        this.showDemandDialog = false
        this.demandForm = { title: '', description: '' }
        this.loadDemandForms()
      })
    },
    submitDemandNow(formId) {
      const content = this.demandDraft[formId] || ''
      submitDemand(formId, JSON.stringify({ text: content })).then(() => {
        this.$message.success(this.$t('enterprise.msgDemandSubmitted'))
        this.$set(this.demandDraft, formId, '')
      })
    },

    createTopicNow() {
      createTopicActivity(this.topicForm).then(() => {
        this.$message.success(this.$t('enterprise.msgActivityStarted'))
        this.showTopicDialog = false
        this.topicForm = { title: '', content: '' }
        this.loadTopicActivities()
      })
    },
    joinTopicNow(activityId) {
      joinTopicActivity(activityId, '报名参与').then(() => {
        this.$message.success(this.$t('enterprise.msgJoined'))
        this.loadTopicActivities()
      })
    }
  }
}
</script>

<style scoped>
.enterprise-page {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}
.page-header h2 {
  margin: 0;
}
.page-header p {
  margin: 8px 0 16px;
  color: #909399;
}
.toolbar {
  margin-bottom: 10px;
}
.card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
}
.card:hover {
  background: #f8fbff;
}
.title {
  font-weight: 600;
  color: #303133;
}
.meta {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
.panel {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px;
}
.panel-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.mini-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.mini-item {
  border: 1px solid #f2f6fc;
  padding: 8px;
  border-radius: 6px;
}
.answer-item {
  margin-bottom: 8px;
  padding: 8px;
  border: 1px solid #f2f6fc;
  border-radius: 6px;
}
</style>
