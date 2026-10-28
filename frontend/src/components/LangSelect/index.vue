<template>
  <el-dropdown trigger="click" @command="handleSetLanguage">
    <div class="lang-select">
      <img src="@/assets/images/icon-lang.png" alt="language" class="lang-icon">
      <span class="lang-text">{{ currentLangText }}</span>
    </div>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item 
        v-for="item of languages" 
        :key="item.value" 
        :disabled="language===item.value" 
        :command="item.value"
      >
        {{ item.label }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import locale from 'element-ui/lib/locale'
import enLocale from 'element-ui/lib/locale/lang/en'
import zhLocale from 'element-ui/lib/locale/lang/zh-CN'

export default {
  name: 'LangSelect',
  data() {
    return {
      languages: [
        { label: '中文', value: 'zh' },
        { label: 'English', value: 'en' }
      ]
    }
  },
  computed: {
    language() {
      return this.$store.getters.language
    },
    currentLangText() {
      return this.language === 'zh' ? '中' : 'EN'
    }
  },
  methods: {
    handleSetLanguage(lang) {
      this.$store.dispatch('app/setLanguage', lang).then(() => {
        this.$i18n.locale = lang
        // 更新Element UI的语言
        if (lang === 'en') {
          locale.use(enLocale)
        } else {
          locale.use(zhLocale)
        }
        // this.$message({
        //   message: this.$t('common.success'),
        //   type: 'success'
        // })
      })
    }
  }
}
</script>

<style scoped>
.lang-select {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  color: #333;
  min-width: 50px;
}

.lang-select:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: #1890ff;
}

.lang-select i {
  font-size: 16px;
}

.lang-text {
  font-size: 13px;
  font-weight: 500;
  user-select: none;
}
.lang-icon {
  background: #1890ff;
  width: 16px;
  height: 16px;
}
</style>
