<template>
  <el-dialog
    :title="$t('bbs.anonymousKeyDialogTitle')"
    :visible.sync="dialogVisible"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    append-to-body
    class="anonymous-key-dialog"
    @close="handleClose"
    style="padding: 20px;"
  >
    <div class="warning-block">
      <i class="el-icon-warning-outline" style="font-size: 16px;"></i>
      {{ $t('bbs.anonymousKeyFirstTimeInfo') }}
    </div>
    <!-- Hint when key was set before but not found -->
    <!-- <div v-if="keyNotFoundHint" class="key-not-found-hint">
      {{ $t('bbs.anonymousKeyNotFoundHint') }}
    </div> -->

    <el-radio-group v-model="keySource" class="key-source-group">
      <div class="key-option" v-if="allowGenerate">
        <el-radio label="generate">
          {{ $t('bbs.anonymousKeyGenerate') }}
        </el-radio>
        <div class="key-option-tip">
          {{ $t('bbs.anonymousKeyGenerateHoverTip') }}
        </div>
      </div>

      <div class="key-option" v-if="hasLocalBrowserKey">
        <el-radio label="browser">
          {{ $t('bbs.anonymousKeyFromBrowser') }}
        </el-radio>
        <div class="key-option-tip">
          {{ $t('bbs.anonymousKeyFromBrowserHoverTip') }}
        </div>
      </div>

      <div class="key-option">
        <el-radio label="manual">{{ $t('bbs.anonymousKeyManual') }}</el-radio>
        <div class="key-option-tip">
          {{ $t('bbs.anonymousKeyManualHoverTip') }}
        </div>
      </div>
    </el-radio-group>



    <!-- Warning message for current option -->
    <!-- <div class="warning-block">
      <i class="el-icon-warning-outline" style="font-size: 16px;"></i>
      <template v-if="keySource === 'generate'">
        {{ $t('bbs.anonymousKeyGenerateWarning') }}
      </template>
      <template v-else>
        {{ $t('bbs.anonymousKeyManualWarning') }}
      </template>
    </div> -->

    <!-- Generate: show button first, then show generated key -->
    <div v-if="keySource === 'generate'" class="key-generate-section">
      <el-button
        v-if="!generatedKey"
        type="primary"
        plain
        @click="doGenerateKey"
      >
        {{ $t('bbs.anonymousKeyGenerateButton') }}
      </el-button>
      <div v-else class="generated-key-row">
        <span class="key-label">{{ $t('bbs.anonymousKeyYourKey') }}</span>
        <el-input
          :value="generatedKey"
          readonly
          size="medium"
          class="key-input-readonly"
        />
        <el-button size="small" icon="el-icon-document-copy" @click="copyKey">
          {{ $t('bbs.anonymousKeyCopy') }}
        </el-button>
      </div>
    </div>

    <!-- Manual: input field -->
    <div v-else class="key-manual-section">
      <el-form ref="manualForm" :model="manualForm" :rules="manualRules">
        <el-form-item prop="key">
          <el-input
            v-model="manualForm.key"
            :placeholder="$t('bbs.anonymousKeyPlaceholder')"
            maxlength="6"
            show-word-limit
            size="medium"
            @input="normalizeManualKey"
          />
        </el-form-item>
      </el-form>
    </div>
    <el-checkbox
      v-model="rememberInBrowser"
      class="remember-checkbox"
    >
      {{ $t('bbs.anonymousKeyRememberInThisBrowser') }}
    </el-checkbox>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">{{ $t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :disabled="!canConfirm"
        @click="handleConfirm"
      >
        {{ $t('common.confirm') }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import {
  generateRandomKey,
  isValidKeyFormat,
  getAnonymousKeyFromLocalStorage,
} from '@/utils/anonymousKey'

export default {
  name: 'AnonymousKeyDialog',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    /** Default selected option when dialog opens: 'generate' | 'manual' */
    defaultKeySource: {
      type: String,
      default: 'generate',
    },
    /** Whether to show the \"generate\" option (user center often needs manual only). */
    allowGenerate: {
      type: Boolean,
      default: true,
    },
    /** When true, show "秘钥未找到，请手动输入" hint */
    keyNotFoundHint: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      /** 与论坛其它弹窗一致，<768 视为手机端 */
      windowInnerWidth:
        typeof window !== 'undefined' ? window.innerWidth : 1200,
      keySource: 'generate',
      generatedKey: '',
      localBrowserKey: '',
      hasLocalBrowserKey: false,
      rememberInBrowser: true,
      manualForm: {
        key: '',
      },
      manualRules: [
        {
          required: true,
          message: this.$t ? this.$t('bbs.anonymousKeyRequired') : '请输入6位匿名秘钥',
          trigger: 'blur',
        },
        {
          validator: (rule, value, callback) => {
            if (!value || !value.trim()) {
              callback(new Error(this.$t ? this.$t('bbs.anonymousKeyRequired') : '请输入6位匿名秘钥'))
              return
            }
            if (!isValidKeyFormat(value)) {
              callback(new Error(this.$t ? this.$t('bbs.anonymousKeyFormatError') : '秘钥须为6位字母或数字'))
              return
            }
            callback()
          },
          trigger: 'blur',
        },
      ],
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      },
    },
    canConfirm() {
      if (this.keySource === 'generate') {
        return !!this.generatedKey
      }
      return isValidKeyFormat(this.manualForm.key)
    },
    dialogWidth() {
      return this.windowInnerWidth < 768 ? '98%' : '500px'
    },
  },
  mounted() {
    this._onResize = () => {
      this.windowInnerWidth = window.innerWidth
    }
    window.addEventListener('resize', this._onResize)
  },
  beforeDestroy() {
    if (this._onResize) {
      window.removeEventListener('resize', this._onResize)
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.localBrowserKey = getAnonymousKeyFromLocalStorage() || ''
        this.hasLocalBrowserKey = !!this.localBrowserKey
        // Default: if browser already has key, checkbox is checked.
        this.rememberInBrowser = this.hasLocalBrowserKey

        const preferredSource = this.allowGenerate
          ? this.defaultKeySource || 'generate'
          : 'manual'
        if (preferredSource === 'browser' && !this.hasLocalBrowserKey) {
          this.keySource = this.allowGenerate ? 'generate' : 'manual'
        } else {
          this.keySource = preferredSource
        }
      }
    },
    keySource() {
      this.generatedKey = ''
      if (this.keySource === 'browser') {
        this.manualForm.key = this.localBrowserKey || ''
      } else {
        this.manualForm.key = ''
      }
      this.$refs.manualForm && this.$refs.manualForm.clearValidate()
    },
  },
  methods: {
    doGenerateKey() {
      this.generatedKey = generateRandomKey()
    },
    normalizeManualKey() {
      const raw = this.manualForm.key
      const filtered = raw.replace(/[^A-Za-z0-9]/g, '').slice(0, 6)
      if (filtered !== raw) {
        this.$nextTick(() => {
          this.manualForm.key = filtered
        })
      }
    },
    copyKey() {
      if (!this.generatedKey) return
      try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(this.generatedKey).then(() => {
            this.$message.success(this.$t('bbs.anonymousKeyCopySuccess') || '已复制到剪贴板')
          }).catch(() => this.fallbackCopy())
        } else {
          this.fallbackCopy()
        }
      } catch (e) {
        this.fallbackCopy()
      }
    },
    fallbackCopy() {
      const input = document.createElement('input')
      input.value = this.generatedKey
      document.body.appendChild(input)
      input.select()
      try {
        document.execCommand('copy')
        this.$message.success(this.$t('bbs.anonymousKeyCopySuccess') || '已复制到剪贴板')
      } catch (e) {}
      document.body.removeChild(input)
    },
    handleConfirm() {
      if (this.keySource === 'generate') {
        if (!this.generatedKey) {
          this.$message.warning(this.$t('bbs.anonymousKeyGenerateFirst') || '请先生成秘钥')
          return
        }
        this.$emit('confirm', this.generatedKey, this.rememberInBrowser)
        this.dialogVisible = false
        return
      }
      this.$refs.manualForm.validate((valid) => {
        if (!valid) return
        const key = this.manualForm.key.trim()
        if (!isValidKeyFormat(key)) {
          this.$message.warning(this.$t('bbs.anonymousKeyFormatError') || '秘钥须为6位字母或数字')
          return
        }
        this.$emit('confirm', key, this.rememberInBrowser)
        this.dialogVisible = false
      })
    },
    handleCancel() {
      this.$emit('cancel')
      this.dialogVisible = false
    },
    handleClose() {
      this.keySource = this.allowGenerate ? 'generate' : 'manual'
      this.generatedKey = ''
      this.manualForm.key = ''
      this.$refs.manualForm && this.$refs.manualForm.clearValidate()
    },
  },
}
</script>

<style scoped lang="scss">
.key-not-found-hint {
  color: #e6a23c;
  font-size: 13px;
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #fdf6ec;
  border-radius: 4px;
}

.key-source-group {
  display: flex;
  margin-bottom: 16px;
  flex-direction: column;
  gap: 20px;
  .el-radio {
    margin-bottom: 10px;
    margin-right: 0;
  }

  .recommended-tag {
    font-size: 12px;
    color: #67c23a;
    margin-left: 6px;
  }
}

.key-option {
  display: flex;
  flex-direction: column;
}

.key-option-tip {
  padding-left: 24px; // align with radio text
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.warning-block {
  font-size: 14px;
  color: #E72626;
  line-height: 1.6;
  margin-bottom: 30px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.key-generate-section {
  margin-bottom: 10px;

  .generated-key-row {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;

    .key-label {
      font-size: 14px;
      color: #606266;
      flex-shrink: 0;
    }

    .key-input-readonly {
      flex: 1;
      min-width: 140px;

      ::v-deep input {
        font-family: monospace;
        letter-spacing: 2px;
      }
    }
  }
}

.key-manual-section {
  margin-bottom: 10px;
}

.remember-checkbox {
  margin-bottom: 10px;
}
</style>
