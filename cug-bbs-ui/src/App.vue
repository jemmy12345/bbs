<template>
  <div id="app">
    <router-view />
    <theme-picker />
    <watermark />
  </div>
</template>

<script>
import ThemePicker from "@/components/ThemePicker"
import Watermark from "@/components/Watermark"
import { getToken } from '@/utils/auth'

export default {
  name: "App",
  components: { 
    ThemePicker,
    Watermark
  },
  mounted() {
    // 检查是否有 token 但没有用户信息，如果有则主动加载用户信息
    // 这可以解决多个标签页打开时，第二个标签页没有加载用户信息的问题
    const token = getToken()
    const hasUserInfo = this.$store.state.user && this.$store.state.user.name
    
    if (token && !hasUserInfo) {
      // 如果有 token 但没有用户信息，主动调用 GetInfo
      this.$store.dispatch('GetInfo').catch(() => {
        // 如果获取用户信息失败，静默处理（可能是 token 已过期）
        console.warn('Failed to get user info on app mount')
      })
    }
  }
}
</script>
<style scoped>
#app .theme-picker {
  display: none;
}
</style>
