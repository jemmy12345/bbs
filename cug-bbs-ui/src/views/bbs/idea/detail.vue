<template>
  <div class="idea-detail-page" v-loading="loading">
    <div v-if="idea" class="idea-detail-wrap">
      <div class="nav-row">
        <el-button type="text" icon="el-icon-arrow-left" @click="$router.back()">返回</el-button>
      </div>

      <!-- 驳回提示 -->
      <el-alert v-if="idea.status === 'REJECTED' && isProposer" type="error" :closable="false">
        <span slot="title">
          提案已被驳回：{{ idea.rejectReason || '无说明' }}
          <el-button type="primary" size="mini" style="margin-left:16px" @click="goResubmit">修改并重新提交</el-button>
        </span>
      </el-alert>

      <!-- 模块一：提案信息 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">提案信息</span>
          <div class="tag-group">
            <el-tag :type="statusType(idea.status)">{{ statusLabel(idea.status) }}</el-tag>
            <el-tag v-if="idea.isPublicized === '1'" type="success" style="margin-left:6px">已公示</el-tag>
          </div>
        </div>
        <h2 class="idea-title">{{ idea.title }}</h2>
        <div class="info-meta">
          <span>提案人：{{ idea.submitNickName || idea.submitUserId }}</span>
          <span v-if="idea.category">· 分类：{{ categoryLabel }}</span>
          <span v-if="idea.teamName">· 团队：{{ idea.teamName }}</span>
        </div>
        <div class="info-block">
          <div class="info-label">提案背景</div>
          <div class="info-content">{{ idea.backgroundText }}</div>
        </div>
        <div class="info-block">
          <div class="info-label">实施方案</div>
          <div class="info-content">{{ idea.planText }}</div>
        </div>
        <div class="info-block">
          <div class="info-label">预期价值</div>
          <div class="info-content">{{ idea.expectedValue }}</div>
        </div>
        <div v-if="idea.bizValue" class="info-block">
          <div class="info-label">业务价值</div>
          <div class="info-content">{{ idea.bizValue }}</div>
        </div>
        <div v-if="idea.relatedCase" class="info-block">
          <div class="info-label">相关案例</div>
          <div class="info-content">{{ idea.relatedCase }}</div>
        </div>

        <!-- 落地成果（已公示时展示） -->
        <div v-if="idea.status === 'PUBLICIZED'" class="result-box" style="margin-top:16px">
          <div class="info-label">落地成果</div>
          <div class="info-content">{{ idea.resultSummary }}</div>
          <div class="info-meta" style="margin-top:6px">
            激励积分：{{ idea.incentivePoints || 0 }}；荣誉勋章：{{ idea.honorBadge || '-' }}
          </div>
        </div>
      </div>

      <!-- 模块二：进度时间线 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">进度时间线</span>
          <span class="section-sub">共 {{ (idea.progressList || []).length }} 条记录</span>
        </div>
        <div v-if="(idea.progressList || []).length === 0" class="empty-tip">暂无进度记录</div>
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
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 模块三：评审意见 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-title">评审意见</span>
          <span class="section-sub">共 {{ (idea.reviews || []).length }} 条</span>
        </div>
        <div v-if="(idea.reviews || []).length === 0" class="empty-tip">暂无评审意见</div>
        <div v-for="r in (idea.reviews || [])" :key="r.reviewId" class="review-item">
          <div class="review-header">
            <el-tag size="mini" :type="r.action === 'pass' ? 'success' : 'danger'">{{ r.action === 'pass' ? '通过' : '驳回' }}</el-tag>
            <span class="review-stage">{{ { L1:'初审', L2:'复审', L3:'终审' }[r.stage] || r.stage }}</span>
            <span class="review-score">评分 {{ r.score }}</span>
            <span class="review-user">{{ r.reviewerNickName || r.reviewerUserId }}</span>
          </div>
          <div v-if="r.comment" class="review-comment">意见：{{ r.comment }}</div>
          <div v-if="r.rejectReason" class="review-reject">驳回原因：{{ r.rejectReason }}</div>
        </div>
      </div>

      <!-- 模块四：操作 -->
      <div v-if="canReviewAny || canUpdateProgress || canPublishResult" class="section-card">
        <div class="section-header">
          <span class="section-title">操作</span>
        </div>

        <!-- 评审操作 -->
        <template v-if="canReviewAny">
          <div class="ops-subtitle">评审操作</div>
          <el-form ref="reviewFormRef" :model="reviewForm" :rules="effectiveReviewRules" size="small" label-width="70px">
            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item label="评审环节" prop="stage">
                  <el-select v-model="reviewForm.stage" style="width:100%">
                    <el-option v-for="item in reviewStageOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="操作" prop="action">
                  <el-select v-model="reviewForm.action" style="width:100%">
                    <el-option label="通过" value="pass" />
                    <el-option label="驳回" value="reject" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label="评分" prop="score">
                  <el-input-number v-model="reviewForm.score" :min="1" :max="10" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="9">
                <el-form-item label="评审意见" prop="comment">
                  <el-input v-model="reviewForm.comment" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row v-if="reviewForm.action === 'reject'" :gutter="12">
              <el-col :span="24">
                <el-form-item label="驳回原因" prop="rejectReason">
                  <el-input v-model="reviewForm.rejectReason" placeholder="请填写驳回原因（必填）" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="doReview">提交评审</el-button>
            </el-form-item>
          </el-form>
        </template>

        <!-- 进度更新 -->
        <template v-if="canUpdateProgress">
          <div class="ops-subtitle">进度更新 <small>（仅提案人可操作）</small></div>
          <el-form size="small" label-width="70px">
            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item label="进度状态">
                  <el-select v-model="progressForm.status" style="width:100%">
                    <el-option label="推进中" value="IMPLEMENTING" />
                    <el-option label="已落地" value="LANDED" />
                    <el-option label="暂停" value="PAUSED" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="当前阶段">
                  <el-input v-model="progressForm.stage" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="完成度">
                  <el-input v-model.number="progressForm.percent" type="number" :min="0" :max="100" style="width:120px">
                    <template slot="append">%</template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="20">
                <el-form-item label="进度描述">
                  <el-input v-model="progressForm.desc" type="textarea" :rows="2" placeholder="请描述本次进度内容" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="doProgress">提交进度</el-button>
            </el-form-item>
          </el-form>
        </template>

        <!-- 成果公示 -->
        <template v-if="canPublishResult">
          <div class="ops-subtitle">成果公示 <small>（进度已达100%，可提交公示）</small></div>
          <el-button type="success" icon="el-icon-trophy" @click="publishDialogVisible = true">提交成果公示</el-button>
        </template>
      </div>
    </div>

    <!-- 成果公示弹框 -->
    <el-dialog title="提交成果公示" :visible.sync="publishDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="publishForm" label-width="90px" size="small">
        <el-form-item label="成果总结">
          <el-input v-model="publishForm.resultSummary" type="textarea" :rows="4" placeholder="请描述提案落地成果" />
        </el-form-item>
        <el-form-item label="激励积分">
          <el-input-number v-model="publishForm.incentivePoints" :min="0" :max="1000" />
          <span style="margin-left:8px;color:#909399;font-size:12px">将发放给提案人</span>
        </el-form-item>
        <el-form-item label="荣誉勋章">
          <el-input v-model="publishForm.honorBadge" placeholder="如：创新先锋" style="width:200px" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="publishing" @click="doPublish">确认公示</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getIdeaDetail, reviewIdea, addIdeaProgress, publishIdeaResult, getCurrentIdeaReviewPermission } from '@/api/bbs/idea'
import { mapGetters } from 'vuex'

const STATUS_LABEL = {
  DRAFT: '草稿', PENDING_L1: '待初审', L1_REVIEWING: '初审中', L1_PASSED: '初审通过',
  L2_REVIEWING: '复审中', L2_PASSED: '复审通过', L3_REVIEWING: '终审中',
  APPROVED: '已立项', IMPLEMENTING: '落地中', LANDED: '已落地', PUBLICIZED: '已公示', REJECTED: '已驳回',
}
const STATUS_TYPE = {
  REJECTED: 'danger', PUBLICIZED: 'success', LANDED: 'success',
  APPROVED: 'success', IMPLEMENTING: '', DRAFT: 'info',
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
      publishForm: { resultSummary: '', incentivePoints: 100, honorBadge: '创新先锋' },
      reviewRules: {
        stage: [{ required: true, message: '请选择评审环节', trigger: 'change' }],
        action: [{ required: true, message: '请选择动作', trigger: 'change' }],
        score: [{ required: true, message: '请填写评分', trigger: 'change' }],
        rejectReason: [{ required: true, message: '驳回必须填写原因', trigger: 'blur' }],
      },
    }
  },
  computed: {
    ...mapGetters(['id']),
    categoryLabel() {
      const map = { product: '产品创新', tech: '技术优化', manage: '管理提升', cost: '降本增效', other: '其他' }
      return this.idea ? (map[this.idea.category] || this.idea.category) : ''
    },
    isProposer() {
      return this.idea && String(this.idea.submitUserId) === String(this.id)
    },
    canUpdateProgress() {
      const postApprovalStatuses = ['APPROVED', 'IMPLEMENTING', 'LANDED']
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
    canReviewAny() {
      const reviewEndedStatuses = ['APPROVED', 'IMPLEMENTING', 'LANDED', 'PUBLICIZED', 'REJECTED']
      return !!this.permission.canReviewAny &&
        this.idea != null &&
        !reviewEndedStatuses.includes(this.idea.status)
    },
    canManageFlow() {
      return !!this.permission.canManageFlow
    },
    reviewStageOptions() {
      const stageMap = { L1: '初审', L2: '复审', L3: '终审' }
      return (this.permission.reviewStages || []).map(item => ({ value: item, label: stageMap[item] || item }))
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
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      Promise.all([getIdeaDetail(this.$route.params.ideaId), getCurrentIdeaReviewPermission()]).then(([detailRes, permissionRes]) => {
        this.idea = detailRes.data
        this.permission = permissionRes.data || { reviewStages: [], canReviewAny: false, canManageFlow: false }
        if ((this.permission.reviewStages || []).length > 0) {
          this.reviewForm.stage = this.permission.reviewStages[0]
        }
      }).finally(() => { this.loading = false })
    },
    doReview() {
      this.$refs.reviewFormRef.validate(valid => {
        if (!valid) return
        reviewIdea(this.idea.ideaId, this.reviewForm).then(() => {
          this.$message.success('评审成功')
          this.$refs.reviewFormRef.resetFields()
          this.load()
        })
      })
    },
    doProgress() {
      if (!this.progressForm.desc) {
        this.$message.warning('请填写进度描述')
        return
      }
      addIdeaProgress(this.idea.ideaId, this.progressForm).then(() => {
        this.$message.success('进度更新成功')
        this.load()
      })
    },
    doPublish() {
      if (!this.publishForm.resultSummary) {
        this.$message.warning('请填写成果总结')
        return
      }
      this.publishing = true
      publishIdeaResult(this.idea.ideaId, this.publishForm).then(() => {
        this.$message.success('成果公示已提交')
        this.publishDialogVisible = false
        this.load()
      }).finally(() => { this.publishing = false })
    },
    statusLabel(v) {
      return STATUS_LABEL[v] || v
    },
    statusType(v) {
      return STATUS_TYPE[v] || 'warning'
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

.review-item {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 10px;
  background: #fafafa;
}
.review-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.review-stage { font-size: 13px; color: #303133; font-weight: 500; }
.review-score { font-size: 12px; color: #909399; }
.review-user { font-size: 12px; color: #909399; margin-left: auto; }
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
