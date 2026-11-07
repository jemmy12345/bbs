<template>
  <div class="collect-participate" v-loading="loading">
    <div v-if="activity" class="card">
      <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/collect')">{{ $t('collect.backToList') }}</el-button>
      <h2>{{ activity.title }}</h2>
      <div class="meta">
        <el-tag type="success" size="mini">{{ activity.activityType }}</el-tag>
        <el-tag :type="activity.status === 'PUBLISHED' ? 'success' : 'info'" size="mini">{{ activity.status }}</el-tag>
        <span>{{ $t('collect.validity', { start: activity.startTime || '-', end: activity.endTime || '-' }) }}</span>
      </div>

      <div v-if="activity.hasParticipated === '1'" class="joined">{{ $t('collect.joined') }}</div>

      <div v-if="activity.activityType === 'vote'" class="panel">
        <el-checkbox-group v-if="vote.voteType !== 'single'" v-model="voteOptionIds">
          <el-checkbox v-for="o in vote.options" :key="o.optionId" :label="o.optionId">{{ o.optionText }}</el-checkbox>
        </el-checkbox-group>
        <el-radio-group v-else v-model="voteSingleId">
          <el-radio v-for="o in vote.options" :key="o.optionId" :label="o.optionId">{{ o.optionText }}</el-radio>
        </el-radio-group>
        <div style="margin-top: 10px">
          <el-button type="primary" :disabled="activity.hasParticipated === '1'" @click="submitVote">{{ $t('collect.submitVote') }}</el-button>
        </div>
      </div>

      <div v-if="activity.activityType === 'survey'" class="panel">
        <el-input type="textarea" :rows="8" v-model="surveyAnswersJson" :placeholder="$t('collect.surveyAnswerPlaceholder', { example: '{&quot;Q1&quot;:&quot;A&quot;,&quot;Q2&quot;:[&quot;B&quot;,&quot;C&quot;]}' })" />
        <div style="margin-top: 10px">
          <el-button @click="saveSurveyDraft">{{ $t('collect.saveDraft') }}</el-button>
          <el-button type="primary" :disabled="activity.hasParticipated === '1'" @click="submitSurvey">{{ $t('collect.submitSurvey') }}</el-button>
        </div>
      </div>

      <div v-if="activity.activityType === 'demand'" class="panel">
        <el-form :model="demandForm" label-width="100px">
          <el-form-item :label="$t('collect.demandName')"><el-input v-model="demandForm.demandName" /></el-form-item>
          <el-form-item :label="$t('collect.demandType')"><el-input v-model="demandForm.demandType" /></el-form-item>
          <el-form-item :label="$t('collect.demandDetail')"><el-input type="textarea" :rows="4" v-model="demandForm.demandDetail" /></el-form-item>
          <el-form-item :label="$t('collect.priority')">
            <el-select v-model="demandForm.priority">
              <el-option label="P0" value="P0" />
              <el-option label="P1" value="P1" />
              <el-option label="P2" value="P2" />
              <el-option label="P3" value="P3" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('collect.attachmentUrl')"><el-input v-model="demandForm.attachmentUrls" /></el-form-item>
          <el-form-item :label="$t('collect.contactPerson')"><el-input v-model="demandForm.contactPerson" /></el-form-item>
          <el-form-item :label="$t('collect.customField')"><el-input type="textarea" :rows="3" v-model="demandForm.customFieldJson" :placeholder="customFieldPlaceholder" /></el-form-item>
        </el-form>
        <el-button type="primary" @click="submitDemand">{{ $t('collect.submitDemand') }}</el-button>
      </div>

      <div v-if="activity.activityType === 'topic'" class="panel">
        <el-button type="primary" @click="joinTopic">{{ $t('collect.checkIn') }}</el-button>
        <el-divider />
        <el-form :model="topicContent" :inline="true" size="small">
          <el-form-item :label="$t('collect.contentType')">
            <el-select v-model="topicContent.contentType" style="width:120px">
              <el-option :label="$t('collect.ctQa')" value="qa" />
              <el-option :label="$t('collect.ctIdea')" value="idea" />
              <el-option :label="$t('collect.ctPost')" value="post" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('collect.contentId')"><el-input-number v-model="topicContent.contentId" :min="1" /></el-form-item>
          <el-form-item :label="$t('collect.colTitle')"><el-input v-model="topicContent.contentTitle" style="width:220px" /></el-form-item>
          <el-form-item><el-button type="success" @click="addTopicContentNow">{{ $t('collect.linkContent') }}</el-button></el-form-item>
        </el-form>
        <div class="tip">{{ $t('collect.topicTip') }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  getCollectActivity,
  getCollectVoteMeta,
  submitCollectVote,
  saveCollectSurveyDraft,
  submitCollectSurvey,
  submitCollectDemand,
  joinCollectTopic,
  addCollectTopicContent,
} from '@/api/bbs/collect'

export default {
  name: 'BbsCollectParticipate',
  data() {
    return {
      loading: false,
      activity: null,
      vote: { voteType: 'single', options: [] },
      voteOptionIds: [],
      voteSingleId: null,
      surveyAnswersJson: '{}',
      demandForm: {
        demandName: '', demandType: '', demandDetail: '', priority: 'P2',
        attachmentUrls: '', contactPerson: '', customFieldJson: '{}',
      },
      topicContent: { contentType: 'post', contentId: 1, contentTitle: '' },
    }
  },
  computed: {
    customFieldPlaceholder() {
      return JSON.stringify({ [this.$t('collect.sampleScenario')]: 'xxx' })
    },
  },
  created() { this.load() },
  methods: {
    load() {
      const activityId = this.$route.params.activityId
      this.loading = true
      getCollectActivity(activityId)
        .then((detailRes) => {
          this.activity = detailRes.data
          if (this.activity.activityType === 'vote') {
            return getCollectVoteMeta(activityId).then(metaRes => {
              const meta = metaRes.data || {}
              this.vote = { ...(meta.vote || {}), options: meta.options || [] }
            })
          }
          return null
        })
        .finally(() => { this.loading = false })
    },
    submitVote() {
      const ids = this.vote.voteType === 'single' ? (this.voteSingleId ? [this.voteSingleId] : []) : this.voteOptionIds
      if (!ids.length) return this.$message.error(this.$t('collect.errSelectOption'))
      submitCollectVote(this.activity.activityId, ids).then(() => {
        this.$message.success(this.$t('collect.voted'))
        this.load()
      })
    },
    saveSurveyDraft() {
      saveCollectSurveyDraft(this.activity.activityId, this.surveyAnswersJson).then(() => {
        this.$message.success(this.$t('collect.draftSaved'))
      })
    },
    submitSurvey() {
      submitCollectSurvey(this.activity.activityId, this.surveyAnswersJson).then(() => {
        this.$message.success(this.$t('collect.surveySubmitted'))
        this.load()
      })
    },
    submitDemand() {
      submitCollectDemand(this.activity.activityId, this.demandForm).then(() => {
        this.$message.success(this.$t('collect.demandSubmitted'))
        this.$router.push(`/collect/stats/${this.activity.activityId}`)
      })
    },
    joinTopic() {
      joinCollectTopic(this.activity.activityId, '参与专题打卡').then(() => {
        this.$message.success(this.$t('collect.checkedIn'))
      })
    },
    addTopicContentNow() {
      addCollectTopicContent(this.activity.activityId, this.topicContent).then(() => {
        this.$message.success(this.$t('collect.linked'))
      })
    },
  },
}
</script>

<style scoped>
.collect-participate { max-width: 980px; margin: 0 auto; padding: 16px; }
.card { background: #fff; border-radius: 10px; padding: 18px; box-shadow: 0 1px 6px rgba(0,0,0,.08); }
.meta { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; color: #909399; flex-wrap: wrap; }
.joined { color: #e6a23c; margin: 10px 0; }
.panel { border: 1px solid #f1f1f1; border-radius: 8px; padding: 12px; margin-top: 10px; }
.tip { color: #909399; margin-top: 8px; }
</style>
