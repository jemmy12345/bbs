<template>
  <div class="idea-publish-page">
    <div class="idea-panel">
      <div class="header">
        <el-button type="text" icon="el-icon-arrow-left" @click="$router.back()">{{ $t('idea.back') }}</el-button>
        <h2>{{ isResubmit ? $t('idea.publish.titleResubmit') : editIdeaId ? $t('idea.publish.titleEditDraft') : $t('idea.publish.titleNew') }}</h2>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item :label="$t('idea.publish.name')" prop="title"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item :label="$t('idea.publish.category')" prop="category">
          <el-select v-model="form.category" :placeholder="$t('idea.publish.selectCategory')">
            <el-option :label="$t('idea.cat.product')" value="product" />
            <el-option :label="$t('idea.cat.tech')" value="tech" />
            <el-option :label="$t('idea.cat.manage')" value="manage" />
            <el-option :label="$t('idea.cat.cost')" value="cost" />
            <el-option :label="$t('idea.cat.other')" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('idea.publish.background')" prop="backgroundText"><el-input v-model="form.backgroundText" type="textarea" :rows="3" /></el-form-item>
        <el-form-item :label="$t('idea.publish.plan')" prop="planText"><el-input v-model="form.planText" type="textarea" :rows="5" /></el-form-item>
        <el-form-item :label="$t('idea.publish.expectedValue')" prop="expectedValue"><el-input v-model="form.expectedValue" type="textarea" :rows="3" /></el-form-item>
        <el-form-item :label="$t('idea.publish.visibility')" prop="visibilityScope">
          <el-radio-group v-model="form.visibilityScope">
            <el-radio label="all">{{ $t('idea.publish.visAll') }}</el-radio>
            <el-radio label="review_group">{{ $t('idea.publish.visReview') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('idea.publish.attachments')">
          <FileUpload
            v-model="form.attachmentUrls"
            :limit="5"
            :file-size="10"
            :is-show-tip="true"
            :drag="false"
          />
          <div class="upload-hint">{{ $t('idea.publish.uploadHint') }}</div>
        </el-form-item>
        <el-form-item :label="$t('idea.publish.relatedCase')"><el-input v-model="form.relatedCase" type="textarea" :rows="2" /></el-form-item>
        <el-form-item :label="$t('idea.publish.team')"><el-input v-model="form.teamName" /></el-form-item>
        <el-form-item :label="$t('idea.publish.bizValue')"><el-input v-model="form.bizValue" type="textarea" :rows="2" maxlength="2000" show-word-limit /></el-form-item>

        <el-form-item>
          <el-button v-if="!isResubmit" :loading="saving" @click="saveDraft">{{ $t('idea.publish.saveDraft') }}</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">{{ isResubmit ? $t('idea.publish.resubmit') : $t('idea.publish.submit') }}</el-button>
          <span class="draft-tip" v-if="!isResubmit">{{ $t('idea.publish.draftTip') }}</span>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { saveIdeaDraft, updateIdeaDraft, submitIdea, getIdeaDetail, resubmitIdea } from '@/api/bbs/idea'

export default {
  name: 'BbsIdeaPublish',
  data() {
    return {
      editIdeaId: null,
      isResubmit: false,
      saving: false,
      submitting: false,
      lastSavedSnapshot: '',
      form: {
        title: '', category: 'product',
        backgroundText: '', planText: '', expectedValue: '',
        visibilityScope: 'review_group',
        attachmentUrls: '', relatedCase: '', teamName: '', bizValue: '', content: '',
      },
    }
  },
  computed: {
    rules() {
      return {
        title: [{ required: true, message: this.$t('idea.publish.ruleName'), trigger: 'blur' }],
        category: [{ required: true, message: this.$t('idea.publish.selectCategory'), trigger: 'change' }],
        backgroundText: [{ required: true, message: this.$t('idea.publish.ruleBackground'), trigger: 'blur' }],
        planText: [{ required: true, message: this.$t('idea.publish.rulePlan'), trigger: 'blur' }],
        expectedValue: [{ required: true, message: this.$t('idea.publish.ruleValue'), trigger: 'blur' }],
        visibilityScope: [{ required: true, message: this.$t('idea.publish.ruleVisibility'), trigger: 'change' }],
      }
    },
  },
  created() {
    this.lastSavedSnapshot = this.serializeForm(this.form)
    const id = this.$route.query.ideaId
    const resubmit = this.$route.query.resubmit === '1'
    if (id) {
      this.editIdeaId = Number(id)
      this.isResubmit = resubmit
      getIdeaDetail(this.editIdeaId).then(res => {
        const d = res.data
        if (d) {
          this.form = {
            title: d.title || '',
            category: d.category || 'product',
            backgroundText: d.backgroundText || '',
            planText: d.planText || '',
            expectedValue: d.expectedValue || '',
            visibilityScope: d.visibilityScope === 'all' ? 'all' : 'review_group',
            attachmentUrls: d.attachmentUrls || '',
            relatedCase: d.relatedCase || '',
            teamName: d.teamName || '',
            bizValue: d.bizValue || '',
            content: d.content || '',
          }
          this.lastSavedSnapshot = this.serializeForm(this.form)
        }
      })
    }
    this.beforeUnloadHandler = (event) => {
      if (!this.isFormDirty()) {
        return
      }
      event.preventDefault()
      event.returnValue = ''
    }
    window.addEventListener('beforeunload', this.beforeUnloadHandler)
  },
  beforeDestroy() {
    window.removeEventListener('beforeunload', this.beforeUnloadHandler)
  },
  methods: {
    serializeForm(form) {
      return JSON.stringify({
        title: form.title || '',
        category: form.category || '',
        backgroundText: form.backgroundText || '',
        planText: form.planText || '',
        expectedValue: form.expectedValue || '',
        visibilityScope: form.visibilityScope || '',
        attachmentUrls: form.attachmentUrls || '',
        relatedCase: form.relatedCase || '',
        teamName: form.teamName || '',
        bizValue: form.bizValue || '',
      })
    },
    isFormDirty() {
      return this.serializeForm(this.form) !== this.lastSavedSnapshot
    },
    markSaved() {
      this.lastSavedSnapshot = this.serializeForm(this.form)
    },
    fillContent() {
      this.form.content = `${this.form.backgroundText || ''}\n\n${this.form.planText || ''}\n\n${this.form.expectedValue || ''}`
    },
    saveDraft() {
      this.fillContent()
      this.saving = true
      const req = this.editIdeaId
        ? updateIdeaDraft(this.editIdeaId, this.form)
        : saveIdeaDraft(this.form)
      req.then((res) => {
        this.$message.success(this.$t('idea.publish.draftSaved'))
        if (!this.editIdeaId && res && res.data) {
          this.editIdeaId = Number(res.data)
          this.$router.replace({ path: '/idea/publish', query: { ideaId: this.editIdeaId } })
        }
        this.markSaved()
      }).finally(() => { this.saving = false })
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.fillContent()
        this.submitting = true
        const req = this.isResubmit
          ? resubmitIdea(this.editIdeaId, this.form)
          // 编辑草稿后提交时带上草稿编号，后端把该草稿转为正式提案，不再另建一条
          : submitIdea(this.editIdeaId ? { ...this.form, ideaId: this.editIdeaId } : this.form)
        req.then(() => {
          this.$message.success(this.isResubmit ? this.$t('idea.publish.resubmitted') : this.$t('idea.publish.submitted'))
          this.markSaved()
          this.$router.push('/idea')
        }).finally(() => { this.submitting = false })
      })
    },
  },
  beforeRouteLeave(to, from, next) {
    if ((this.saving || this.submitting) || !this.isFormDirty()) {
      next()
      return
    }
    if (window.confirm(this.$t('idea.publish.leaveConfirm'))) {
      next()
      return
    }
    next(false)
  },
}
</script>

<style scoped>
.idea-publish-page { max-width: 920px; margin: 0 auto; padding: 16px; }
.idea-panel { background:#fff; border-radius:10px; padding:24px; box-shadow:0 1px 6px rgba(0,0,0,.08); }
.header { display:flex; align-items:center; gap:8px; margin-bottom:12px; }
.header h2 { margin:0; font-size:20px; }
.draft-tip { margin-left: 12px; color: #909399; font-size: 12px; }
.upload-hint { margin-top: 6px; color: #909399; font-size: 12px; }
</style>
