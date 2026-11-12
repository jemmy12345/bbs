<template>
  <div class="qa-detail-page" v-loading="pageLoading">
    <div v-if="question" class="qa-detail-wrap">
      <!-- 返回导航 -->
      <div class="qa-detail-nav">
        <el-button icon="el-icon-arrow-left" type="text" @click="$router.back()">{{ $t('qa.detail.back') }}</el-button>
      </div>

      <!-- 问题卡片 -->
      <div class="qa-question-card">
        <div class="qa-q-header">
          <div class="qa-q-meta-top">
            <el-tag size="small" :type="categoryType(question.category)" class="qa-cat-tag">
              {{ categoryLabel(question.category) }}
            </el-tag>
            <el-tag v-if="question.status === 'solved'" size="small" type="success" effect="dark">{{ $t('qa.status.solved') }}</el-tag>
            <el-tag v-if="question.status === 'archived'" size="small" type="info" effect="dark">{{ $t('qa.status.archived') }}</el-tag>
            <el-tag v-if="question.status === 'cancelled'" size="small" type="danger" effect="dark">{{ $t('qa.status.cancelled') }}</el-tag>
            <el-tag v-if="question.status === 'open'" size="small" type="primary" effect="dark">{{ $t('qa.status.open') }}</el-tag>
          </div>
          <h1 class="qa-q-title">{{ question.title }}</h1>

          <div class="qa-q-meta">
            <el-avatar :src="question.askAvatar" :size="32" />
            <span class="qa-q-author">{{ question.askNickName || $t('qa.common.anonymous') }}</span>
            <span v-if="question.askDeptName" class="qa-q-dept">{{ question.askDeptName }}</span>
            <span class="qa-q-time">{{ formatTime(question.createTime) }}</span>
            <span class="qa-q-views"><i class="el-icon-view"></i> {{ $t('qa.detail.viewsCount', { n: question.viewCount || 0 }) }}</span>
            <span class="qa-q-reward">
              <i class="el-icon-coin"></i>
              <i18n path="qa.detail.reward" tag="span">
                <strong place="n">{{ question.rewardPoints || 0 }}</strong>
              </i18n>
            </span>
          </div>
        </div>

        <div class="qa-q-content">{{ question.content }}</div>

        <div class="qa-q-tags" v-if="question.tags">
          <el-tag
            v-for="tag in question.tags.split(',')"
            :key="tag"
            size="mini"
            effect="plain"
            type="info"
          ># {{ tag.trim() }}</el-tag>
        </div>

        <!-- 提问者操作区 -->
        <div class="qa-q-actions" v-if="isOwner && question.status === 'open'">
          <el-popconfirm
            :title="$t('qa.detail.cancelConfirm')"
            @confirm="handleCancel"
          >
            <el-button slot="reference" size="mini" type="danger" plain :loading="cancelling">
              {{ $t('qa.detail.cancel') }}
            </el-button>
          </el-popconfirm>
          <el-button
            v-if="question.status === 'solved'"
            size="mini"
            type="info"
            plain
            @click="handleArchive"
          >{{ $t('qa.detail.archive') }}</el-button>
        </div>
        <div class="qa-q-actions" v-if="isOwner && question.status === 'solved'">
          <el-button size="mini" type="info" plain @click="handleArchive">
            {{ $t('qa.detail.archive') }}
          </el-button>
        </div>
      </div>

      <!-- 回答区域 -->
      <div class="qa-answers-section">
        <h3 class="qa-answers-title">
          <i class="el-icon-chat-dot-round"></i>
          {{ $t('qa.detail.answersCount', { n: question.answerCount || (question.answers && question.answers.length) || 0 }) }}
        </h3>

        <!-- 最佳答案置顶 -->
        <div
          v-if="bestAnswer"
          class="qa-answer-card qa-best-answer"
        >
          <div class="qa-best-badge">
            <i class="el-icon-trophy"></i> {{ $t('qa.detail.best') }}
          </div>
          <div class="answer-inner">
            <div class="answer-meta">
              <el-avatar :src="bestAnswer.answerAvatar" :size="32" />
              <span class="answer-author">{{ bestAnswer.answerNickName || $t('qa.common.anonymous') }}</span>
              <span v-if="bestAnswer.answerDeptName" class="answer-dept">{{ bestAnswer.answerDeptName }}</span>
              <span class="answer-time">{{ formatAnswerTime(bestAnswer.createTime) }}</span>
            </div>
            <div class="answer-content">{{ displayAnswerContent(bestAnswer) }}</div>
            <div class="answer-actions">
              <el-button
                v-if="isOwner && question.status === 'open' && bestAnswer.isBest !== '1'"
                size="mini"
                type="text"
                icon="el-icon-trophy"
                style="color: #e6a23c;"
                @click="handleAccept(bestAnswer.answerId)"
              >{{ $t('qa.detail.accept') }}</el-button>
            </div>
          </div>
        </div>

        <!-- 其他回答 -->
        <div v-for="ans in otherAnswers" :key="ans.answerId" class="qa-answer-card">
          <div class="answer-inner">
            <div class="answer-meta">
              <el-avatar :src="ans.answerAvatar" :size="32" />
              <span class="answer-author">{{ ans.answerNickName || $t('qa.common.anonymous') }}</span>
              <span v-if="ans.answerDeptName" class="answer-dept">{{ ans.answerDeptName }}</span>
              <span class="answer-time">{{ formatAnswerTime(ans.createTime) }}</span>
            </div>
            <div class="answer-content">{{ displayAnswerContent(ans) }}</div>
            <div class="answer-actions">
              <el-button
                v-if="isOwner && question.status === 'open' && ans.isBest !== '1'"
                size="mini"
                type="text"
                icon="el-icon-trophy"
                style="color: #e6a23c;"
                @click="handleAccept(ans.answerId)"
              >{{ $t('qa.detail.accept') }}</el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="!question.answers || question.answers.length === 0" :description="$t('qa.detail.noAnswers')" />
      </div>

      <!-- 我的回答区 -->
      <div class="qa-reply-section" v-if="question.status === 'open' && !isOwner">
        <h3 class="qa-reply-title"><i class="el-icon-edit-outline"></i> {{ $t('qa.detail.myAnswer') }}</h3>
        <el-input
          v-model="answerContent"
          type="textarea"
          :rows="6"
          :placeholder="$t('qa.detail.answerPlaceholder')"
          maxlength="3000"
          show-word-limit
        />
        <div class="qa-reply-actions">
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="!answerContent.trim()"
            @click="handleAnswer"
          >{{ $t('qa.detail.submit') }}</el-button>
        </div>
      </div>

      <!-- 已关闭时提示 -->
      <div class="qa-closed-tip" v-if="question.status !== 'open'">
        <el-alert
          :title="closedTip"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </div>

    <el-empty v-if="!pageLoading && !question" :description="$t('qa.detail.notFound')" />
  </div>
</template>

<script>
import { getQaQuestionDetail, answerQaQuestion, acceptBestAnswer, cancelQaQuestion, archiveQaQuestion } from '@/api/bbs/qa'
import { getToken } from '@/utils/auth'

const CATEGORY_KEYS = ['tech', 'product', 'hr', 'admin', 'other']

export default {
  name: 'BbsQaDetail',
  data() {
    return {
      pageLoading: false,
      question: null,
      answerContent: '',
      submitting: false,
      cancelling: false,
    }
  },
  computed: {
    categories() {
      return CATEGORY_KEYS.map(v => ({ value: v, label: this.$t('qa.cat.' + v) }))
    },
    isLoggedIn() { return !!getToken() },
    currentUserId() {
      const stateUser = this.$store.state.user || {}
      return this.$store.getters.userId || stateUser.userId || stateUser.id || null
    },
    isOwner() {
      if (!this.question) return false
      if (this.question.isCurrentUserAsker === true) return true
      return this.currentUserId && String(this.question.askUserId) === String(this.currentUserId)
    },
    bestAnswer() {
      if (!this.question || !this.question.answers) return null
      return this.question.answers.find(a => a.isBest === '1') || null
    },
    otherAnswers() {
      if (!this.question || !this.question.answers) return []
      return this.question.answers.filter(a => a.isBest !== '1')
    },
    closedTip() {
      const s = this.question && this.question.status
      if (s === 'solved') return this.$t('qa.detail.closed.solved')
      if (s === 'archived') return this.$t('qa.detail.closed.archived')
      if (s === 'cancelled') return this.$t('qa.detail.closed.cancelled')
      return this.$t('qa.detail.closed.default')
    },
  },
  created() {
    this.loadDetail()
  },
  methods: {
    formatAnswerTime(t) {
      if (!t) return ''
      const d = new Date(t)
      const now = new Date()
      const diff = (now - d) / 1000
      if (diff < 60) return this.$t('qa.time.justNow')
      if (diff < 3600) return this.$t('qa.time.minutesAgo', { n: Math.floor(diff / 60) })
      if (diff < 86400) return this.$t('qa.time.hoursAgo', { n: Math.floor(diff / 3600) })
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    displayAnswerContent(answer) {
      const raw = answer && (answer.content || answer.answerContent || answer.answerText)
      const text = raw == null ? '' : String(raw).trim()
      return text || this.$t('qa.detail.emptyAnswer')
    },
    loadDetail() {
      const id = this.$route.params.questionId
      if (!id) return
      this.pageLoading = true
      getQaQuestionDetail(id)
        .then(res => { this.question = res.data })
        .finally(() => { this.pageLoading = false })
    },
    handleAnswer() {
      if (!this.isLoggedIn) { this.$message.warning(this.$t('qa.common.loginFirst')); return }
      if (!this.answerContent.trim()) return
      this.submitting = true
      answerQaQuestion(this.question.questionId, { content: this.answerContent.trim() })
        .then(() => {
          this.$message.success(this.$t('qa.detail.answerSuccess'))
          this.answerContent = ''
          this.loadDetail()
        })
        .finally(() => { this.submitting = false })
    },
    handleAccept(answerId) {
      this.$confirm(this.$t('qa.detail.acceptConfirm'), this.$t('qa.detail.acceptTitle'), {
        confirmButtonText: this.$t('qa.detail.acceptOk'),
        cancelButtonText: this.$t('qa.detail.acceptCancel'),
        type: 'warning',
      }).then(() => {
        acceptBestAnswer(this.question.questionId, answerId)
          .then(() => {
            this.$message.success(this.$t('qa.detail.acceptSuccess'))
            this.loadDetail()
          })
      }).catch(() => {})
    },
    handleCancel() {
      this.cancelling = true
      cancelQaQuestion(this.question.questionId)
        .then(() => {
          this.$message.success(this.$t('qa.detail.cancelSuccess'))
          this.$router.push('/qa')
        })
        .finally(() => { this.cancelling = false })
    },
    handleArchive() {
      archiveQaQuestion(this.question.questionId)
        .then(() => {
          this.$message.success(this.$t('qa.detail.archiveSuccess'))
          this.loadDetail()
        })
    },
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    categoryLabel(val) {
      return CATEGORY_KEYS.includes(val) ? this.$t('qa.cat.' + val) : val
    },
    categoryType(val) {
      const map = { tech: 'primary', product: 'success', hr: 'warning', admin: '', other: 'info' }
      return map[val] || 'info'
    },
  },
}
</script>

<style scoped>
.qa-detail-page { max-width: 900px; margin: 0 auto; padding: 16px; }
.qa-detail-wrap { display: flex; flex-direction: column; gap: 16px; }

.qa-question-card {
  background: #fff; border-radius: 10px; padding: 28px 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,.07);
}
.qa-q-header { margin-bottom: 16px; }
.qa-q-meta-top { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.qa-q-title { font-size: 22px; font-weight: 700; color: #303133; margin: 0 0 16px; line-height: 1.4; }
.qa-q-meta { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #909399; flex-wrap: wrap; }
.qa-q-author { font-weight: 600; color: #606266; }
.qa-q-dept { color: #c0c4cc; }
.qa-q-reward strong { color: #e6a23c; font-size: 16px; }

.qa-q-content {
  font-size: 15px; line-height: 1.8; color: #303133;
  margin: 20px 0 16px;
  white-space: pre-wrap; word-break: break-word;
  background: #fafafa; border-radius: 8px; padding: 16px;
}
.qa-q-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 16px; }
.qa-q-actions { margin-top: 12px; display: flex; gap: 10px; }

.qa-answers-section {
  background: #fff; border-radius: 10px; padding: 24px 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,.07);
}
.qa-answers-title { font-size: 16px; font-weight: 600; color: #303133; margin: 0 0 16px; }

.qa-answer-card {
  border-radius: 8px; padding: 16px 20px;
  border: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.qa-best-answer {
  border-color: #e6a23c;
  background: linear-gradient(135deg, #fffbf0 0%, #fff 100%);
}
.qa-best-badge {
  font-size: 13px; font-weight: 700; color: #e6a23c;
  margin-bottom: 10px;
}

.answer-meta { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #909399; margin-bottom: 12px; }
.answer-author { font-weight: 600; color: #606266; }
.answer-dept { color: #c0c4cc; }
.answer-content {
  font-size: 14px; line-height: 1.8; color: #303133;
  white-space: pre-wrap; word-break: break-word;
  margin-bottom: 10px;
}
.answer-actions { display: flex; gap: 12px; }

.qa-reply-section {
  background: #fff; border-radius: 10px; padding: 24px 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,.07);
}
.qa-reply-title { font-size: 16px; font-weight: 600; margin: 0 0 16px; }
.qa-reply-actions { margin-top: 12px; display: flex; justify-content: flex-end; }

</style>
