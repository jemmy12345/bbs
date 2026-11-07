<template>
  <div class="qa-publish-page">
    <div class="qa-publish-wrap">
      <div class="qa-publish-header">
        <el-button icon="el-icon-arrow-left" type="text" @click="$router.back()">{{ $t('qa.publish.back') }}</el-button>
        <h2>{{ $t('qa.publish.title') }}</h2>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="qa-publish-form">
        <!-- 标题 -->
        <el-form-item :label="$t('qa.publish.fieldTitle')" prop="title">
          <el-input
            v-model="form.title"
            :placeholder="$t('qa.publish.titlePlaceholder')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <!-- 分类 -->
        <el-form-item :label="$t('qa.publish.fieldCategory')" prop="category">
          <el-radio-group v-model="form.category">
            <el-radio-button v-for="cat in categories" :key="cat.value" :label="cat.value">
              {{ cat.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 标签 -->
        <el-form-item :label="$t('qa.publish.fieldTags')">
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
              {{ $t('qa.publish.addTag') }}
            </el-button>
          </div>
          <div class="form-tip">{{ $t('qa.publish.tagTip') }}</div>
        </el-form-item>

        <!-- 问题内容 -->
        <el-form-item :label="$t('qa.publish.fieldContent')" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            :placeholder="$t('qa.publish.contentPlaceholder')"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <!-- 悬赏积分 -->
        <el-form-item :label="$t('qa.publish.fieldReward')" prop="rewardPoints">
          <el-input-number
            v-model="form.rewardPoints"
            :min="1"
            :max="500"
            :step="10"
            :precision="0"
          />
          <span class="qa-points-tip">
            <i class="el-icon-coin"></i>
            <i18n path="qa.publish.pointsTip" tag="span">
              <strong place="n">{{ myPoints }}</strong>
            </i18n>
          </span>
        </el-form-item>

        <!-- 有效期 -->
        <el-form-item :label="$t('qa.publish.fieldExpiry')">
          <el-date-picker
            v-model="form.expiryTime"
            type="datetime"
            :placeholder="$t('qa.publish.expiryPlaceholder')"
            :picker-options="datePickerOptions"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
          <div class="form-tip">{{ $t('qa.publish.expiryTip') }}</div>
        </el-form-item>

        <!-- 提交 -->
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" style="width: 160px;">
            {{ $t('qa.publish.submit') }}
          </el-button>
          <el-button @click="$router.back()">{{ $t('qa.publish.cancel') }}</el-button>
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
      if (value == null || value <= 0) return callback(new Error(this.$t('qa.publish.rulePointsPositive')))
      if (value > this.myPoints) return callback(new Error(this.$t('qa.publish.rulePointsInsufficient', { n: this.myPoints })))
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
        expiryTime: null,
      },
      validatePoints,
      datePickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now()
        },
      },
    }
  },
  computed: {
    categories() {
      return ['tech', 'product', 'hr', 'admin', 'other'].map(v => ({ value: v, label: this.$t('qa.cat.' + v) }))
    },
    rules() {
      return {
        title: [
          { required: true, message: this.$t('qa.publish.ruleTitleRequired'), trigger: 'blur' },
          { min: 5, max: 100, message: this.$t('qa.publish.ruleTitleLength'), trigger: 'blur' },
        ],
        category: [{ required: true, message: this.$t('qa.publish.ruleCategory'), trigger: 'change' }],
        content: [
          { required: true, message: this.$t('qa.publish.ruleContentRequired'), trigger: 'blur' },
          { min: 10, message: this.$t('qa.publish.ruleContentMin'), trigger: 'blur' },
        ],
        rewardPoints: [
          { required: true, message: this.$t('qa.publish.ruleRewardRequired'), trigger: 'change' },
          { validator: this.validatePoints, trigger: 'change' },
        ],
      }
    },
  },
  created() {
    this.loadMyPoints()
  },
  methods: {
    loadMyPoints() {
      getInfo().then(res => {
        this.myPoints = (res.user && res.user.bbsPoints) ? res.user.bbsPoints : 0
      }).catch(() => {
        this.$message.error(this.$t('qa.publish.loadPointsFail'))
      })
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
          this.$message.error(this.$t('qa.publish.insufficient', { n: this.myPoints }))
          return
        }
        this.submitting = true
        const payload = {
          ...this.form,
          tags: this.tagList.join(',') || undefined,
        }
        createQaQuestion(payload)
          .then(res => {
            this.$message.success(this.$t('qa.publish.success'))
            const questionId = res.data
            this.$router.replace({ path: `/qa/detail/${questionId}` })
          })
          .catch(err => {
            this.$message.error(err.message || this.$t('qa.publish.fail'))
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
