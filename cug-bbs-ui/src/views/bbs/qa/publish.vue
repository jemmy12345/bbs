<template>
  <div class="qa-publish-page">
    <div class="qa-publish-wrap">
      <div class="qa-publish-header">
        <el-button icon="el-icon-arrow-left" type="text" @click="$router.back()">返回</el-button>
        <h2>发起悬赏提问</h2>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="qa-publish-form">
        <!-- 标题 -->
        <el-form-item label="问题标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请用一句话描述你的问题，方便他人搜索（5-100字）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <!-- 分类 -->
        <el-form-item label="问题分类" prop="category">
          <el-radio-group v-model="form.category">
            <el-radio-button v-for="cat in categories" :key="cat.value" :label="cat.value">
              {{ cat.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 标签 -->
        <el-form-item label="关键词标签">
          <div class="qa-tags-input">
            <el-tag
              v-for="tag in tagList"
              :key="tag"
              closable
              size="small"
              @close="removeTag(tag)"
            ># {{ tag }}</el-tag>
            <el-input
              v-if="tagInputVisible"
              ref="tagInput"
              v-model="tagInputVal"
              class="qa-tag-new-input"
              size="mini"
              @keyup.enter.native="confirmTag"
              @blur="confirmTag"
            />
            <el-button v-else-if="tagList.length < 5" size="mini" type="dashed" @click="showTagInput">
              + 添加标签
            </el-button>
          </div>
          <div class="form-tip">最多5个标签，每个不超过10字</div>
        </el-form-item>

        <!-- 问题内容 -->
        <el-form-item label="问题详情" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="详细描述你的问题背景、已尝试的方案和期望的结果，帮助他人快速理解并解答……"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <!-- 悬赏积分 -->
        <el-form-item label="悬赏积分" prop="rewardPoints">
          <el-input-number
            v-model="form.rewardPoints"
            :min="0"
            :max="500"
            :step="10"
          />
          <span class="qa-points-tip">
            <i class="el-icon-coin"></i>
            当前积分：<strong>{{ myPoints }}</strong>（提交后立即冻结，采纳最佳答案后发放给被采纳者）
          </span>
        </el-form-item>

        <!-- 可见范围 -->
        <el-form-item label="可见范围">
          <el-radio-group v-model="form.visibilityScope">
            <el-radio label="all">全员可见</el-radio>
            <el-radio label="dept">仅本部门</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 有效期 -->
        <el-form-item label="有效期">
          <el-date-picker
            v-model="form.expiryTime"
            type="datetime"
            placeholder="选择问题有效期（可选）"
            :picker-options="datePickerOptions"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
          <div class="form-tip">超过有效期未被采纳时，问题自动关闭并退还积分（留空表示无期限）</div>
        </el-form-item>

        <!-- 提交 -->
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" style="width: 160px;">
            发布问题
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { createQaQuestion } from '@/api/bbs/qa'
import { getInfo } from '@/api/login'

export default {
  name: 'BbsQaPublish',
  data() {
    const validatePoints = (rule, value, callback) => {
      if (value < 0) return callback(new Error('积分不能为负数'))
      if (value > this.myPoints) return callback(new Error('积分不足，当前可用积分 ' + this.myPoints))
      callback()
    }
    return {
      myPoints: 0,
      submitting: false,
      tagInputVisible: false,
      tagInputVal: '',
      tagList: [],
      form: {
        title: '',
        category: 'tech',
        content: '',
        rewardPoints: 20,
        visibilityScope: 'all',
        expiryTime: null,
      },
      categories: [
        { value: 'tech', label: '技术研发' },
        { value: 'product', label: '产品设计' },
        { value: 'hr', label: '人事行政' },
        { value: 'admin', label: '运营管理' },
        { value: 'other', label: '其他' },
      ],
      rules: {
        title: [
          { required: true, message: '请填写问题标题', trigger: 'blur' },
          { min: 5, max: 100, message: '标题长度在5到100字之间', trigger: 'blur' },
        ],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        content: [
          { required: true, message: '请填写问题详情', trigger: 'blur' },
          { min: 10, message: '问题详情至少10个字', trigger: 'blur' },
        ],
        rewardPoints: [
          { required: true, message: '请填写悬赏积分', trigger: 'blur' },
          { validator: validatePoints, trigger: 'blur' },
        ],
      },
      datePickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now()
        },
      },
    }
  },
  created() {
    this.loadMyPoints()
  },
  methods: {
    loadMyPoints() {
      getInfo().then(res => {
        this.myPoints = (res.user && res.user.bbsPoints) ? res.user.bbsPoints : 0
      }).catch(() => {})
    },
    showTagInput() {
      this.tagInputVisible = true
      this.$nextTick(() => {
        this.$refs.tagInput && this.$refs.tagInput.focus()
      })
    },
    confirmTag() {
      const val = (this.tagInputVal || '').trim()
      if (val && val.length <= 10 && !this.tagList.includes(val)) {
        this.tagList.push(val)
      }
      this.tagInputVisible = false
      this.tagInputVal = ''
    },
    removeTag(tag) {
      this.tagList = this.tagList.filter(t => t !== tag)
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.form.rewardPoints > this.myPoints) {
          this.$message.error('积分不足！当前积分：' + this.myPoints)
          return
        }
        this.submitting = true
        const payload = {
          ...this.form,
          tags: this.tagList.join(',') || undefined,
        }
        createQaQuestion(payload)
          .then(res => {
            this.$message.success('问题发布成功！')
            const questionId = res.data
            this.$router.replace({ path: `/qa/detail/${questionId}` })
          })
          .catch(err => {
            this.$message.error(err.message || '发布失败，请重试')
          })
          .finally(() => { this.submitting = false })
      })
    },
  },
}
</script>

<style scoped>
.qa-publish-page { max-width: 900px; margin: 0 auto; padding: 16px; }
.qa-publish-wrap { background: #fff; border-radius: 10px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
.qa-publish-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.qa-publish-header h2 { margin: 0; font-size: 20px; color: #303133; }
.qa-publish-form { max-width: 800px; }

.qa-tags-input { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
.qa-tag-new-input { width: 100px; }

.qa-points-tip { margin-left: 16px; font-size: 13px; color: #606266; }
.qa-points-tip strong { color: #e6a23c; }

.form-tip { font-size: 12px; color: #909399; margin-top: 6px; }
</style>
