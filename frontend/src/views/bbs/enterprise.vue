<template>
  <div class="enterprise-page">
    <div class="page-header">
      <h2>企业增长专区</h2>
      <p>问答闭环、创新提案、投票调研与专题活动一体化平台</p>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="企业问答专区" name="qa">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="showAskDialog = true">悬赏提问</el-button>
        </div>
        <div class="card-list">
          <div class="card" v-for="q in qaQuestions" :key="q.questionId" @click="openQaDetail(q.questionId)">
            <div class="title">{{ q.title }}</div>
            <div class="meta">悬赏 {{ q.rewardPoints || 0 }} 积分 · 状态 {{ q.status }}</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="创新提案板块" name="idea">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="showIdeaDialog = true">提交创意</el-button>
        </div>
        <div class="card-list">
          <div class="card" v-for="idea in ideas" :key="idea.ideaId" @click="openIdeaDetail(idea.ideaId)">
            <div class="title">{{ idea.title }}</div>
            <div class="meta">状态 {{ idea.status }} · 均分 {{ idea.avgScore || 0 }}</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="投票 / 调研 / 活动" name="engage">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="panel">
              <div class="panel-title">在线投票 / 调研</div>
              <el-button type="text" @click="showVoteDialog = true">创建投票</el-button>
              <div class="mini-list">
                <div v-for="v in votes" :key="v.voteId" class="mini-item" @click="openVoteDetail(v.voteId)">
                  {{ v.title }}
                </div>
              </div>
            </div>
          </el-col>

          <el-col :span="8">
            <div class="panel">
              <div class="panel-title">需求征集表单</div>
              <el-button type="text" @click="showDemandDialog = true">创建表单</el-button>
              <div class="mini-list">
                <div v-for="f in demandForms" :key="f.formId" class="mini-item">
                  <div>{{ f.title }}</div>
                  <el-input type="textarea" :rows="2" v-model="demandDraft[f.formId]" placeholder="填写你的需求"></el-input>
                  <el-button size="mini" @click="submitDemandNow(f.formId)">提交</el-button>
                </div>
              </div>
            </div>
          </el-col>

          <el-col :span="8">
            <div class="panel">
              <div class="panel-title">专题话题活动</div>
              <el-button type="text" @click="showTopicDialog = true">发起活动</el-button>
              <div class="mini-list">
                <div v-for="a in topicActivities" :key="a.activityId" class="mini-item">
                  <div>{{ a.title }}（{{ a.joinCount || 0 }}人）</div>
                  <el-button size="mini" @click="joinTopicNow(a.activityId)">报名参与</el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="悬赏提问" :visible.sync="showAskDialog" width="520px">
      <el-form :model="askForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="askForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input type="textarea" :rows="4" v-model="askForm.content" /></el-form-item>
        <el-form-item label="悬赏积分"><el-input-number v-model="askForm.rewardPoints" :min="1" :max="500" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAskDialog = false">取消</el-button>
        <el-button type="primary" @click="createQuestionNow">提交</el-button>
      </div>
    </el-dialog>

    <el-dialog title="问题详情" :visible.sync="showQaDetail" width="760px">
      <div v-if="qaDetail">
        <h3>{{ qaDetail.title }}</h3>
        <p>{{ qaDetail.content }}</p>
        <el-tag size="mini">状态：{{ qaDetail.status }}</el-tag>
        <el-divider></el-divider>
        <div v-for="ans in qaDetail.answers || []" :key="ans.answerId" class="answer-item">
          <div>{{ ans.content }}</div>
          <div class="meta">{{ ans.answerNickName }}
            <el-tag v-if="ans.isBest === '1'" size="mini" type="success">最佳答案</el-tag>
            <el-button v-if="qaDetail.status === 'open'" size="mini" type="text" @click="acceptAnswerNow(ans.answerId)">采纳</el-button>
          </div>
        </div>
        <el-input type="textarea" :rows="3" v-model="qaAnswerContent" placeholder="输入你的回答"></el-input>
        <el-button size="mini" type="primary" @click="submitAnswerNow">提交回答</el-button>
        <el-button size="mini" @click="archiveQuestionNow">归档问题</el-button>
      </div>
    </el-dialog>

    <el-dialog title="提交创新提案" :visible.sync="showIdeaDialog" width="560px">
      <el-form :model="ideaForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="ideaForm.title" /></el-form-item>
        <el-form-item label="创意内容"><el-input type="textarea" :rows="4" v-model="ideaForm.content" /></el-form-item>
        <el-form-item label="业务价值"><el-input type="textarea" :rows="3" v-model="ideaForm.bizValue" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showIdeaDialog = false">取消</el-button>
        <el-button type="primary" @click="submitIdeaNow">提交</el-button>
      </div>
    </el-dialog>

    <el-dialog title="提案详情" :visible.sync="showIdeaDetail" width="760px">
      <div v-if="ideaDetail">
        <h3>{{ ideaDetail.title }}</h3>
        <p>{{ ideaDetail.content }}</p>
        <p>业务价值：{{ ideaDetail.bizValue }}</p>
        <el-tag size="mini">状态：{{ ideaDetail.status }}</el-tag>
        <el-divider>线上评审</el-divider>
        <div>
          <el-input-number v-model="reviewForm.score" :min="1" :max="10"></el-input-number>
          <el-input v-model="reviewForm.comment" style="width:60%; margin-left:8px;" placeholder="评审意见"></el-input>
          <el-button size="mini" type="primary" @click="submitReviewNow">提交评审</el-button>
        </div>
        <div v-for="rv in ideaDetail.reviews || []" :key="rv.reviewId" class="mini-item">
          {{ rv.reviewerNickName }}：{{ rv.score }} 分，{{ rv.comment }}
        </div>

        <el-divider>进度跟进</el-divider>
        <div>
          <el-input v-model="progressForm.stage" style="width:25%" placeholder="阶段"></el-input>
          <el-input v-model="progressForm.desc" style="width:40%; margin-left:8px;" placeholder="说明"></el-input>
          <el-input-number v-model="progressForm.percent" :min="0" :max="100" style="margin-left:8px"></el-input-number>
          <el-button size="mini" type="primary" @click="addProgressNow">更新</el-button>
        </div>
        <div v-for="pg in ideaDetail.progressList || []" :key="pg.progressId" class="mini-item">
          {{ pg.progressStage }} - {{ pg.progressPercent }}% - {{ pg.progressDesc }}
        </div>

        <el-divider>成果公示</el-divider>
        <el-input type="textarea" :rows="3" v-model="publishSummary" placeholder="成果总结"></el-input>
        <el-button size="mini" type="success" @click="publishResultNow">公示成果</el-button>
      </div>
    </el-dialog>

    <el-dialog title="创建投票" :visible.sync="showVoteDialog" width="560px">
      <el-form :model="voteForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="voteForm.title" /></el-form-item>
        <el-form-item label="说明"><el-input type="textarea" :rows="3" v-model="voteForm.description" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="voteForm.voteType"><el-option value="single" label="单选"></el-option><el-option value="multiple" label="多选"></el-option></el-select>
        </el-form-item>
        <el-form-item label="选项(每行一个)"><el-input type="textarea" :rows="4" v-model="voteOptionsText" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="showVoteDialog = false">取消</el-button><el-button type="primary" @click="createVoteNow">创建</el-button></div>
    </el-dialog>

    <el-dialog title="投票详情" :visible.sync="showVoteDetail" width="620px">
      <div v-if="voteDetail">
        <h3>{{ voteDetail.title }}</h3>
        <p>{{ voteDetail.description }}</p>
        <el-checkbox-group v-model="selectedVoteOptionIds">
          <el-checkbox v-for="o in voteDetail.options || []" :key="o.optionId" :label="o.optionId">{{ o.optionText }}（{{ o.voteCount }}）</el-checkbox>
        </el-checkbox-group>
        <div style="margin-top:10px"><el-button type="primary" size="mini" @click="submitVoteNow">提交投票</el-button></div>
      </div>
    </el-dialog>

    <el-dialog title="创建需求表单" :visible.sync="showDemandDialog" width="520px">
      <el-form :model="demandForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="demandForm.title" /></el-form-item>
        <el-form-item label="说明"><el-input type="textarea" :rows="3" v-model="demandForm.description" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="showDemandDialog = false">取消</el-button><el-button type="primary" @click="createDemandNow">创建</el-button></div>
    </el-dialog>

    <el-dialog title="发起专题活动" :visible.sync="showTopicDialog" width="520px">
      <el-form :model="topicForm" label-width="90px">
        <el-form-item label="活动标题"><el-input v-model="topicForm.title" /></el-form-item>
        <el-form-item label="活动内容"><el-input type="textarea" :rows="4" v-model="topicForm.content" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="showTopicDialog = false">取消</el-button><el-button type="primary" @click="createTopicNow">发起</el-button></div>
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
      voteOptionsText: '选项A\n选项B',
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
        this.$message.success('提问成功')
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
        this.$message.success('已采纳最佳答案')
        this.openQaDetail(this.qaDetail.questionId)
        this.loadQa()
      })
    },
    archiveQuestionNow() {
      archiveQaQuestion(this.qaDetail.questionId).then(() => {
        this.$message.success('问题已归档')
        this.openQaDetail(this.qaDetail.questionId)
        this.loadQa()
      })
    },

    submitIdeaNow() {
      submitIdea(this.ideaForm).then(() => {
        this.$message.success('创意已提交')
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
        this.$message.success('评审提交成功')
        this.reviewForm.comment = ''
        this.openIdeaDetail(this.ideaDetail.ideaId)
        this.loadIdeas()
      })
    },
    addProgressNow() {
      addIdeaProgress(this.ideaDetail.ideaId, this.progressForm).then(() => {
        this.$message.success('进度已更新')
        this.progressForm = { stage: '', desc: '', percent: 0 }
        this.openIdeaDetail(this.ideaDetail.ideaId)
      })
    },
    publishResultNow() {
      publishIdeaResult(this.ideaDetail.ideaId, this.publishSummary).then(() => {
        this.$message.success('成果已公示')
        this.publishSummary = ''
        this.openIdeaDetail(this.ideaDetail.ideaId)
        this.loadIdeas()
      })
    },

    createVoteNow() {
      const options = this.voteOptionsText.split('\n').map(x => x.trim()).filter(Boolean)
      createVote({ ...this.voteForm, options }).then(() => {
        this.$message.success('投票创建成功')
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
        this.$message.success('投票成功')
        this.openVoteDetail(this.voteDetail.voteId)
        this.loadVotes()
      })
    },

    createDemandNow() {
      createDemandForm(this.demandForm).then(() => {
        this.$message.success('需求表单已创建')
        this.showDemandDialog = false
        this.demandForm = { title: '', description: '' }
        this.loadDemandForms()
      })
    },
    submitDemandNow(formId) {
      const content = this.demandDraft[formId] || ''
      submitDemand(formId, JSON.stringify({ text: content })).then(() => {
        this.$message.success('需求已提交')
        this.$set(this.demandDraft, formId, '')
      })
    },

    createTopicNow() {
      createTopicActivity(this.topicForm).then(() => {
        this.$message.success('活动已发起')
        this.showTopicDialog = false
        this.topicForm = { title: '', content: '' }
        this.loadTopicActivities()
      })
    },
    joinTopicNow(activityId) {
      joinTopicActivity(activityId, '报名参与').then(() => {
        this.$message.success('报名成功')
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
