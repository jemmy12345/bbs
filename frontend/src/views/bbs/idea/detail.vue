<template>
  <div class="idea-detail-page" v-loading="loading">
    <div v-if="idea" class="idea-detail-wrap">
      <div class="nav-row">
        <el-button type="text" icon="el-icon-arrow-left" @click="$router.back()">{{ $t('idea.back') }}</el-button>
      </div>

      <!-- 驳回提示 -->
      <el-alert v-if="idea.status === 'REJECTED' && isProposer" type="error" :closable="false">
        <span slot="title">
          {{ $t('idea.detail.rejectedAlert', { reason: idea.rejectReason || $t('idea.detail.noReason') }) }}
          <el-button type="primary" size="mini" style="margin-left:16px" @click="goResubmit">{{ $t('idea.publish.titleResubmit') }}</el-button>
        </span>
      </el-alert>

      <!-- 模块一：提案信息 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">{{ $t('idea.detail.info') }}</span>
          <div class="tag-group">
            <el-tag :type="statusType(idea.status)">{{ statusLabel(idea.status) }}</el-tag>
            <el-tag v-if="idea.isPublicized === '1' && idea.status !== 'PUBLICIZED'" type="success" style="margin-left:6px">{{ $t('idea.status.PUBLICIZED') }}</el-tag>
          </div>
        </div>
        <h2 class="idea-title">{{ idea.title }}</h2>
        <div class="info-meta">
          <span>{{ $t('idea.detail.proposer', { name: idea.submitNickName || idea.submitUserId }) }}</span>
          <span v-if="idea.category">{{ $t('idea.detail.categoryOf', { name: categoryLabel }) }}</span>
          <span v-if="idea.teamName">{{ $t('idea.detail.teamOf', { name: idea.teamName }) }}</span>
        </div>
        <div class="info-block">
          <div class="info-label">{{ $t('idea.publish.background') }}</div>
          <div class="info-content">{{ idea.backgroundText }}</div>
        </div>
        <div class="info-block">
          <div class="info-label">{{ $t('idea.publish.plan') }}</div>
          <div class="info-content">{{ idea.planText }}</div>
        </div>
        <div class="info-block">
          <div class="info-label">{{ $t('idea.publish.expectedValue') }}</div>
          <div class="info-content">{{ idea.expectedValue }}</div>
        </div>
        <div v-if="idea.bizValue" class="info-block">
          <div class="info-label">{{ $t('idea.publish.bizValue') }}</div>
          <div class="info-content">{{ idea.bizValue }}</div>
        </div>
        <div v-if="attachmentList(idea.attachmentUrls).length" class="info-block">
          <div class="info-label">{{ $t('idea.detail.attachmentsLabel') }}</div>
          <div class="attachment-list">
            <a
              v-for="(file, index) in attachmentList(idea.attachmentUrls)"
              :key="`${file}-${index}`"
              :href="resolveFileUrl(file)"
              target="_blank"
              rel="noopener noreferrer"
              class="attachment-link"
            >
              {{ fileName(file) }}
            </a>
          </div>
        </div>
        <div v-if="idea.relatedCase" class="info-block">
          <div class="info-label">{{ $t('idea.publish.relatedCase') }}</div>
          <div class="info-content">{{ idea.relatedCase }}</div>
        </div>

        <!-- 落地成果（已公示时展示） -->
        <div v-if="idea.status === 'PUBLICIZED'" class="result-box" style="margin-top:16px">
          <div class="info-label">{{ $t('idea.detail.result') }}</div>
          <div class="info-content">{{ idea.resultSummary }}</div>
          <div class="info-meta" style="margin-top:6px">
            {{ $t('idea.detail.rewardInfo', { points: idea.incentivePoints || 0, badge: idea.honorBadge || '-' }) }}
          </div>
          <div style="margin-top:10px">
            <el-button size="small" :type="idea.liked ? 'warning' : 'default'" :icon="idea.liked ? 'el-icon-star-on' : 'el-icon-star-off'" @click="handleLike">
              {{ idea.liked ? $t('idea.detail.liked') : $t('idea.detail.like') }} {{ idea.likeCount || 0 }}
            </el-button>
            <el-button size="small" icon="el-icon-share" @click="handleShare(idea)">
              {{ $t('idea.detail.share') }} {{ idea.shareCount || 0 }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 模块二：进度时间线 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">{{ $t('idea.detail.timeline') }}</span>
          <span class="section-sub">{{ $t('idea.detail.recordCount', { count: (idea.progressList || []).length }) }}</span>
        </div>
        <div v-if="(idea.progressList || []).length === 0" class="empty-tip">{{ $t('idea.detail.noProgress') }}</div>
        <el-timeline v-else style="padding-top:8px">
          <el-timeline-item
            v-for="p in (idea.progressList || [])"
            :key="p.progressId"
            :timestamp="parseTime(p.createTime)"
            placement="top"
          >
            <div class="timeline-stage">{{ p.progressStage }}<span v-if="p.statusEnum" class="timeline-status">（{{ statusLabel(p.statusEnum) }}）</span></div>
            <div v-if="p.progressDesc" class="timeline-desc">{{ p.progressDesc }}</div>
            <div v-if="p.progressPercent" class="timeline-percent">
              <el-progress :percentage="p.progressPercent" :stroke-width="6" style="width:200px;display:inline-block" />
            </div>
            <div v-if="attachmentList(p.attachmentUrls).length" class="attachment-list timeline-attachments">
              <a
                v-for="(file, index) in attachmentList(p.attachmentUrls)"
                :key="`${p.progressId}-${index}`"
                :href="resolveFileUrl(file)"
                target="_blank"
                rel="noopener noreferrer"
                class="attachment-link"
              >
                {{ fileName(file) }}
              </a>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 模块三：评审意见 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">{{ $t('idea.detail.reviews') }}</span>
          <span class="section-sub">{{ $t('idea.detail.reviewCount', { count: (idea.reviews || []).length }) }}</span>
        </div>
        <div v-if="(idea.reviews || []).length === 0" class="empty-tip">{{ $t('idea.detail.noReviews') }}</div>
        <div v-for="r in (idea.reviews || [])" :key="r.reviewId" class="review-item">
          <div class="review-header">
            <el-tag size="mini" :type="r.action === 'pass' ? 'success' : 'danger'">{{ r.action === 'pass' ? $t('idea.detail.pass') : $t('idea.detail.reject') }}</el-tag>
            <span class="review-stage">{{ ['L1', 'L2', 'L3'].includes(r.stage) ? $t('idea.stage.' + r.stage) : r.stage }}</span>
            <span class="review-score">{{ $t('idea.detail.reviewScore', { score: r.score }) }}</span>
            <span class="review-user">{{ r.reviewerNickName || r.reviewerUserId }}</span>
            <span class="review-time">{{ parseTime(r.reviewTime || r.createTime) }}</span>
          </div>
          <div v-if="r.comment" class="review-comment">{{ $t('idea.detail.commentOf', { comment: r.comment }) }}</div>
          <div v-if="r.rejectReason" class="review-reject">{{ $t('idea.detail.rejectReasonOf', { reason: r.rejectReason }) }}</div>
        </div>
      </div>

      <!-- 模块四：操作 -->
      <div v-if="canReviewCurrentStage || canUpdateProgress || canPublishResult" class="section-card">
        <div class="section-header">
          <span class="section-title">{{ $t('idea.detail.operations') }}</span>
        </div>

        <!-- 评审操作 -->
        <template v-if="canReviewCurrentStage">
          <div class="ops-subtitle">{{ $t('idea.detail.reviewOps') }}</div>
          <el-form ref="reviewFormRef" :model="reviewForm" :rules="effectiveReviewRules" size="small" label-width="70px">
            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item :label="$t('idea.detail.reviewStage')">
                  <el-tag type="warning">{{ currentReviewStageLabel }}</el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item :label="$t('idea.detail.action')" prop="action">
                  <el-select v-model="reviewForm.action" style="width:100%">
                    <el-option :label="$t('idea.detail.pass')" value="pass" />
                    <el-option :label="$t('idea.detail.reject')" value="reject" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item :label="$t('idea.detail.score')" prop="score">
                  <el-input-number
                    v-model="reviewForm.score"
                    :min="1"
                    :max="10"
                    :step="1"
                    controls-position="right"
                    class="review-score-input"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="24">
                <el-form-item :label="$t('idea.detail.comment')" prop="comment" class="review-comment-item">
                  <el-input v-model="reviewForm.comment" type="textarea" :rows="2" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row v-if="reviewForm.action === 'reject'" :gutter="12">
              <el-col :span="24">
                <el-form-item :label="$t('idea.detail.rejectReason')" prop="rejectReason">
                  <el-input v-model="reviewForm.rejectReason" :placeholder="$t('idea.detail.rejectPlaceholder')" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="doReview">{{ $t('idea.detail.submitReview') }}</el-button>
            </el-form-item>
          </el-form>
        </template>

        <!-- 进度更新 -->
        <template v-if="canUpdateProgress">
          <div class="ops-subtitle">{{ $t('idea.detail.progressUpdate') }} <small>{{ $t('idea.detail.proposerOnly') }}</small></div>
          <el-form size="small" label-width="70px">
            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item :label="$t('idea.detail.progressStatus')">
                  <el-select v-model="progressForm.status" style="width:100%">
                    <el-option :label="$t('idea.detail.inProgress')" value="IMPLEMENTING" />
                    <el-option :label="$t('idea.status.LANDED')" value="LANDED" />
                    <el-option :label="$t('idea.status.PAUSED')" value="PAUSED" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item :label="$t('idea.detail.currentPhase')">
                  <el-input v-model="progressForm.stage" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="$t('idea.detail.percent')">
                  <el-input v-model.number="progressForm.percent" type="number" :min="0" :max="100" style="width:120px">
                    <template slot="append">%</template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="20">
                <el-form-item :label="$t('idea.detail.progressDesc')">
                  <el-input v-model="progressForm.desc" type="textarea" :rows="2" :placeholder="$t('idea.detail.progressDescPlaceholder')" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="20">
                <el-form-item :label="$t('idea.detail.progressAttachments')">
                  <FileUpload v-model="progressForm.attachmentUrls" :limit="5" :file-size="10" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="doProgress">{{ $t('idea.detail.submitProgress') }}</el-button>
            </el-form-item>
          </el-form>
        </template>

        <!-- 成果公示 -->
        <template v-if="canPublishResult">
          <div class="ops-subtitle">{{ $t('idea.detail.publishSection') }} <small>{{ $t('idea.detail.publishHint') }}</small></div>
          <el-button type="success" icon="el-icon-trophy" @click="publishDialogVisible = true">{{ $t('idea.detail.submitPublish') }}</el-button>
        </template>
      </div>
    </div>

    <!-- 成果公示弹框 -->
    <el-dialog :title="$t('idea.detail.publishDialog')" :visible.sync="publishDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="publishForm" label-width="90px" size="small">
        <el-form-item :label="$t('idea.detail.resultSummary')">
          <el-input v-model="publishForm.resultSummary" type="textarea" :rows="4" :placeholder="$t('idea.detail.resultPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('idea.detail.incentive')">
          <strong>{{ incentivePoints }}</strong> {{ $t('idea.detail.pointsUnit') }}
          <span style="margin-left:8px;color:#909399;font-size:12px">{{ $t('idea.detail.fixedHint') }}</span>
        </el-form-item>
        <el-form-item :label="$t('idea.detail.honorBadge')">
          <el-select v-model="publishForm.honorBadge" :placeholder="$t('idea.detail.selectBadge')" style="width:200px">
            <el-option v-for="d in honorBadgeOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="publishDialogVisible = false">{{ $t('idea.detail.cancel') }}</el-button>
        <el-button type="success" :loading="publishing" @click="doPublish">{{ $t('idea.detail.confirmPublish') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getIdeaDetail, reviewIdea, addIdeaProgress, publishIdeaResult, getCurrentIdeaReviewPermission, toggleIdeaLike, shareIdea } from '@/api/bbs/idea'
import { getDicts } from '@/api/system/dict/data'
import { mapGetters } from 'vuex'

const STATUS_KEYS = [
  'DRAFT', 'PENDING_L1', 'L1_REVIEWING', 'L1_PASSED', 'L2_REVIEWING', 'L2_PASSED', 'L3_REVIEWING',
  'APPROVED', 'IMPLEMENTING', 'LANDED', 'PUBLICIZED', 'REJECTED', 'PAUSED',
]
const STATUS_TYPE = {
  REJECTED: 'danger', PUBLICIZED: 'success', LANDED: 'success',
  APPROVED: 'success', IMPLEMENTING: '', DRAFT: 'info', PAUSED: 'warning',
}

export default {
  name: 'BbsIdeaDetail',
  data() {
    return {
      loading: false,
      idea: null,
      publishDialogVisible: false,
      publishing: false,
      permission: { reviewStages: [], canReviewAny: false, canManageFlow: false },
      reviewForm: { stage: 'L1', action: 'pass', score: 8, comment: '', rejectReason: '' },
      progressForm: { status: 'IMPLEMENTING', stage: '推进中', desc: '', percent: 60 },
      publishForm: { resultSummary: '', honorBadge: '创新先锋' },
      incentivePoints: 100,
      honorBadgeOptions: [],
    }
  },
  computed: {
    ...mapGetters(['id']),
    reviewRules() {
      return {
        action: [{ required: true, message: this.$t('idea.detail.ruleAction'), trigger: 'change' }],
        score: [{ required: true, message: this.$t('idea.detail.ruleScore'), trigger: 'change' }],
        rejectReason: [{ required: true, message: this.$t('idea.detail.ruleReject'), trigger: 'blur' }],
      }
    },
    categoryLabel() {
      if (!this.idea) return ''
      const c = this.idea.category
      return ['product', 'tech', 'manage', 'cost', 'other'].includes(c) ? this.$t('idea.cat.' + c) : c
    },
    isProposer() {
      return this.idea && String(this.idea.submitUserId) === String(this.id)
    },
    canUpdateProgress() {
      const postApprovalStatuses = ['APPROVED', 'IMPLEMENTING', 'PAUSED', 'LANDED']
      return this.isProposer && this.idea &&
        postApprovalStatuses.includes(this.idea.status) &&
        this.maxProgressPercent < 100
    },
    maxProgressPercent() {
      const list = (this.idea && this.idea.progressList) || []
      return list.reduce((max, p) => Math.max(max, p.progressPercent || 0), 0)
    },
    canPublishResult() {
      return this.isProposer && this.idea &&
        this.idea.status === 'LANDED' &&
        this.maxProgressPercent >= 100
    },
    canReviewCurrentStage() {
      const reviewEndedStatuses = ['APPROVED', 'IMPLEMENTING', 'LANDED', 'PUBLICIZED', 'REJECTED']
      return this.idea != null &&
        !!this.idea.reviewStage &&
        this.idea.reviewStage !== 'NONE' &&
        !reviewEndedStatuses.includes(this.idea.status) &&
        (this.permission.reviewStages || []).includes(this.idea.reviewStage)
    },
    canManageFlow() {
      return !!this.permission.canManageFlow
    },
    currentReviewStageLabel() {
      const stage = this.idea && this.idea.reviewStage
      if (['L1', 'L2', 'L3'].includes(stage)) return this.$t('idea.stage.' + stage)
      return stage || '-'
    },
    effectiveReviewRules() {
      const rules = { ...this.reviewRules }
      if (this.reviewForm.action !== 'reject') {
        const { rejectReason, ...rest } = rules
        return rest
      }
      return rules
    },
  },
  created() {
    getDicts('bbs_idea_honor_badge').then(res => {
      this.honorBadgeOptions = (res.data || []).map(d => ({ label: d.dictLabel, value: d.dictValue }))
      if (this.honorBadgeOptions.length && !this.honorBadgeOptions.some(o => o.value === this.publishForm.honorBadge)) {
        this.publishForm.honorBadge = this.honorBadgeOptions[0].value
      }
    })
    this.load()
  },
  methods: {
    copyText(text) {
      if (navigator.clipboard && window.isSecureContext) {
        return navigator.clipboard.writeText(text)
      }
      return new Promise((resolve, reject) => {
        const ta = document.createElement('textarea')
        ta.value = text
        ta.style.position = 'fixed'
        ta.style.opacity = '0'
        document.body.appendChild(ta)
        ta.select()
        const ok = document.execCommand('copy')
        document.body.removeChild(ta)
        ok ? resolve() : reject(new Error('copy failed'))
      })
    },
    handleShare(idea) {
      const url = window.location.origin + this.$router.resolve(`/idea/detail/${idea.ideaId}`).href
      this.copyText(url).then(() => {
        this.$message.success(this.$t('idea.wall.shareCopied'))
      }).catch(() => {
        this.$message.warning(this.$t('idea.wall.copyFailed'))
      })
      shareIdea(idea.ideaId).then(res => {
        this.$set(idea, 'shareCount', res.data.shareCount)
      })
    },
    handleLike() {
      toggleIdeaLike(this.idea.ideaId).then(res => {
        this.$set(this.idea, 'liked', res.data.liked)
        this.$set(this.idea, 'likeCount', res.data.likeCount)
      })
    },
    load() {
      this.loading = true
      Promise.all([getIdeaDetail(this.$route.params.ideaId), getCurrentIdeaReviewPermission()]).then(([detailRes, permissionRes]) => {
        this.idea = detailRes.data
        this.permission = permissionRes.data || { reviewStages: [], canReviewAny: false, canManageFlow: false }
        if (this.idea && this.idea.reviewStage && (this.permission.reviewStages || []).includes(this.idea.reviewStage)) {
          this.reviewForm.stage = this.idea.reviewStage
        }
      }).finally(() => { this.loading = false })
    },
    doReview() {
      this.$refs.reviewFormRef.validate(valid => {
        if (!valid) return
        this.reviewForm.stage = this.idea.reviewStage
        reviewIdea(this.idea.ideaId, this.reviewForm).then(() => {
          this.$message.success(this.$t('idea.detail.reviewOk'))
          this.reviewForm.action = 'pass'
          this.reviewForm.score = 8
          this.reviewForm.comment = ''
          this.reviewForm.rejectReason = ''
          this.load()
        })
      })
    },
    doProgress() {
      if (!this.progressForm.desc) {
        this.$message.warning(this.$t('idea.detail.needDesc'))
        return
      }
      const percent = Number(this.progressForm.percent)
      if (Number.isNaN(percent) || percent < 0 || percent > 100) {
        this.$message.warning(this.$t('idea.detail.percentRange'))
        return
      }
      if (percent < this.maxProgressPercent) {
        this.$message.warning(this.$t('idea.detail.percentLow', { percent: this.maxProgressPercent }))
        return
      }
      if (this.progressForm.status === 'LANDED' && percent < 100) {
        this.$message.warning(this.$t('idea.detail.landedNeed100'))
        return
      }
      addIdeaProgress(this.idea.ideaId, this.progressForm).then(() => {
        this.$message.success(this.$t('idea.detail.progressOk'))
        this.progressForm.desc = ''
        this.progressForm.attachmentUrls = ''
        this.load()
      })
    },
    doPublish() {
      if (!this.publishForm.resultSummary) {
        this.$message.warning(this.$t('idea.detail.needSummary'))
        return
      }
      this.publishing = true
      publishIdeaResult(this.idea.ideaId, this.publishForm).then(() => {
        this.$message.success(this.$t('idea.detail.publishOk'))
        this.publishDialogVisible = false
        this.load()
      }).finally(() => { this.publishing = false })
    },
    statusLabel(v) {
      return STATUS_KEYS.includes(v) ? this.$t('idea.status.' + v) : v
    },
    statusType(v) {
      return STATUS_TYPE[v] || 'warning'
    },
    attachmentList(value) {
      if (!value) {
        return []
      }
      return String(value).split(',').map(item => item.trim()).filter(Boolean)
    },
    resolveFileUrl(file) {
      if (!file) {
        return ''
      }
      if (/^https?:\/\//.test(file)) {
        return file
      }
      const base = process.env.VUE_APP_BASE_API || ''
      return `${base}${file}`
    },
    fileName(file) {
      if (!file) {
        return ''
      }
      return file.substring(file.lastIndexOf('/') + 1)
    },
    goResubmit() {
      this.$router.push(`/idea/publish?ideaId=${this.idea.ideaId}&resubmit=1`)
    },
  },
}
</script>

<style scoped>
.idea-detail-page { max-width: 860px; margin: 0 auto; padding: 16px 16px 40px; }
.idea-detail-wrap { display: flex; flex-direction: column; gap: 16px; }
.nav-row { margin-bottom: -4px; }

.section-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 5px rgba(0,0,0,.07);
}
.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.section-sub { font-size: 12px; color: #909399; }

.idea-title { margin: 0 0 8px; font-size: 20px; color: #1a1a1a; }
.tag-group { margin-left: auto; }
.info-meta { font-size: 12px; color: #909399; margin-bottom: 14px; }
.info-meta span + span { margin-left: 8px; }

.info-block { margin-bottom: 14px; }
.info-label { font-size: 13px; font-weight: 600; color: #606266; margin-bottom: 4px; }
.info-content { font-size: 14px; color: #303133; line-height: 1.7; white-space: pre-wrap; }

.result-box {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  padding: 12px 16px;
}

.empty-tip { color: #c0c4cc; font-size: 13px; padding: 8px 0; }

.timeline-stage { font-size: 14px; font-weight: 500; color: #303133; }
.timeline-status { color: #909399; font-weight: normal; }
.timeline-desc { font-size: 13px; color: #606266; margin-top: 4px; }
.timeline-percent { margin-top: 6px; }
.timeline-attachments { margin-top: 8px; }
.review-score-input { width: 150px; }
.review-comment-item :deep(.el-form-item__label) { white-space: nowrap; }

.review-item {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 10px;
  background: #fafafa;
}
.review-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.review-stage { font-size: 13px; color: #303133; font-weight: 500; }
.attachment-list { display: flex; flex-wrap: wrap; gap: 8px; }
.attachment-link { color: #409EFF; font-size: 13px; text-decoration: none; }
.attachment-link:hover { text-decoration: underline; }
.review-score { font-size: 12px; color: #909399; }
.review-user { font-size: 12px; color: #909399; margin-left: auto; }
.review-time { font-size: 12px; color: #909399; }
.review-comment { font-size: 13px; color: #606266; }
.review-reject { font-size: 13px; color: #f56c6c; margin-top: 4px; }

.ops-subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin: 0 0 12px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
.ops-subtitle + .ops-subtitle,
.el-form + .ops-subtitle,
.el-button + .ops-subtitle { margin-top: 20px; }
.ops-subtitle small { font-weight: normal; color: #909399; margin-left: 6px; }
</style>
