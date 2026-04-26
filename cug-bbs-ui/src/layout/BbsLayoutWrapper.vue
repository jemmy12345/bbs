<template>
  <component :is="layoutComponent" />
</template>

<script>
import { isMobileDevice } from '@/utils/device'
import BbsLayout from './BbsLayout'
import BbsMobileLayout from './BbsMobileLayout'

export default {
  name: 'BbsLayoutWrapper',
  data() {
    return {
      isMobile: isMobileDevice() // 初始化时就检测设备类型
    }
  },
  computed: {
    layoutComponent() {
      return this.isMobile ? BbsMobileLayout : BbsLayout
    }
  },
  created() {
    // 监听窗口大小变化，但只在桌面端和移动端之间切换时才更新
    // 避免频繁切换布局导致的问题
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleResize() {
      // 使用防抖，避免频繁切换
      if (this.resizeTimer) {
        clearTimeout(this.resizeTimer)
      }
      this.resizeTimer = setTimeout(() => {
        const newIsMobile = isMobileDevice()
        // 只在设备类型真正改变时才更新
        if (this.isMobile !== newIsMobile) {
          this.isMobile = newIsMobile
        }
      }, 300)
    }
  }
}
</script>
