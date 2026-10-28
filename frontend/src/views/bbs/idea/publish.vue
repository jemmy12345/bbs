<template>
  <div class="idea-publish-page">
    <div class="idea-panel">
      <div class="header">
        <el-button type="text" icon="el-icon-arrow-left" @click="$router.back()">返回</el-button>
        <h2>{{ isResubmit ? '修改并重新提交' : editIdeaId ? '编辑草稿' : '创意提案投稿' }}</h2>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="提案名称" prop="title"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="提案分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="产品创新" value="product" />
            <el-option label="技术优化" value="tech" />
            <el-option label="管理提升" value="manage" />
            <el-option label="降本增效" value="cost" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="提案背景" prop="backgroundText"><el-input v-model="form.backgroundText" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="实施方案" prop="planText"><el-input v-model="form.planText" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="预期价值" prop="expectedValue"><el-input v-model="form.expectedValue" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="可见范围" prop="visibilityScope">
          <el-radio-group v-model="form.visibilityScope">
            <el-radio label="all">全员</el-radio>
            <el-radio label="review_group">评审可见</el-radio>
            <el-radio label="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="附件资料">
          <FileUpload
            v-model="form.attachmentUrls"
            :limit="5"
            :file-size="10"
            :is-show-tip="true"
            :drag="false"
          />
          <div class="upload-hint">选择文件后会在下方显示已上传附件</div>
        </el-form-item>
        <el-form-item label="相关案例"><el-input v-model="form.relatedCase" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="提案团队"><el-input v-model="form.teamName" /></el-form-item>
        <el-form-item label="业务价值"><el-input v-model="form.bizValue" type="textarea" :rows="2" maxlength="2000" show-word-limit /></el-form-item>

        <el-form-item>
          <el-button v-if="!isResubmit" :loading="saving" @click="saveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">{{ isResubmit ? '重新提交' : '提交提案' }}</el-button>
          <span class="draft-tip" v-if="!isResubmit">保存草稿后会留在当前页面，方便继续编辑</span>
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
      rules: {
        title: [{ required: true, message: '请输入提案名称', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        backgroundText: [{ required: true, message: '请输入提案背景', trigger: 'blur' }],
        planText: [{ required: true, message: '请输入实施方案', trigger: 'blur' }],
        expectedValue: [{ required: true, message: '请输入预期价值', trigger: 'blur' }],
        visibilityScope: [{ required: true, message: '请选择可见范围', trigger: 'change' }],
      },
    }
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
            visibilityScope: d.visibilityScope || 'review_group',
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
        this.$message.success('草稿保存成功')
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
          : submitIdea(this.form)
        req.then(() => {
          this.$message.success(this.isResubmit ? '提案已重新提交' : '提案提交成功')
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
    if (window.confirm('当前提案内容尚未保存，确定离开吗？')) {
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
