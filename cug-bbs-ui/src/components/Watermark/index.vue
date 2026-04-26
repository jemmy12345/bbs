<template>
  <div class="watermark-container" ref="watermarkContainer"></div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Watermark',
  computed: {
    ...mapGetters(['name', 'nickName','id']),
    userName() {
      // 优先使用昵称，如果没有则使用用户名
      return this.nickName || this.name || ''
    }
  },
  data() {
    return {
      checkTimer: null,
      isChecking: false,
      userInfoLoadedHandler: null
    }
  },
  watch: {
    userName: {
      handler(newVal, oldVal) {
        // 当用户名从空变为有值时，创建水印
        if (newVal) {
          this.$nextTick(() => {
            this.createWatermark()
          })
        } else if (!newVal) {
          this.removeWatermark()
        }
      },
      immediate: false
    }
  },
  mounted() {
    // 监听全局事件：用户信息加载完成
    this.userInfoLoadedHandler = (event) => {
      this.$nextTick(() => {
        if (this.userName) {
          this.createWatermark()
        }
      })
    }
    window.addEventListener('user-info-loaded', this.userInfoLoadedHandler)
    
    // 立即检查并创建水印，确保用户信息已加载
    this.checkAndCreateWatermark()
    // 监听窗口大小变化，重新生成水印
    window.addEventListener('resize', this.handleResize)
    // 使用 MutationObserver 防止水印被删除
    this.observeWatermark()
  },
  beforeDestroy() {
    // 移除全局事件监听
    if (this.userInfoLoadedHandler) {
      window.removeEventListener('user-info-loaded', this.userInfoLoadedHandler)
    }
    
    window.removeEventListener('resize', this.handleResize)
    if (this.observer) {
      this.observer.disconnect()
    }
    if (this.checkTimer) {
      clearTimeout(this.checkTimer)
    }
  },
  methods: {
    checkAndCreateWatermark() {
      // 如果已经有用户信息，直接创建水印
      if (this.userName) {
        this.$nextTick(() => {
          this.createWatermark()
        })
        return
      }
      
      // 检查是否有 token
      const token = this.$store.state.user.token
      
      // 如果有 token 但没有用户信息，主动调用 GetInfo
      if (token && !this.userName && !this.isChecking) {
        this.isChecking = true
        this.$store.dispatch('GetInfo').then(() => {
          this.isChecking = false
          this.$nextTick(() => {
            if (this.userName) {
              this.createWatermark()
            }
          })
        }).catch(() => {
          this.isChecking = false
        })
        return
      }
      
      // 延迟检查用户信息是否已加载，最多检查15次，每次间隔200ms
      let checkCount = 0
      const maxChecks = 15
      const checkInterval = 200
      
      const check = () => {
        if (this.userName) {
          // 用户信息已加载，创建水印
          this.$nextTick(() => {
            this.createWatermark()
          })
        } else if (checkCount < maxChecks) {
          // 用户信息还未加载，继续检查
          checkCount++
          this.checkTimer = setTimeout(check, checkInterval)
        } else {
          // 如果检查了多次还是没有用户信息，但有 token，尝试主动获取
          if (token && !this.userName && !this.isChecking) {
            this.isChecking = true
            this.$store.dispatch('GetInfo').then(() => {
              this.isChecking = false
              this.$nextTick(() => {
                if (this.userName) {
                  this.createWatermark()
                }
              })
            }).catch(() => {
              this.isChecking = false
            })
          }
        }
      }
      
      // 立即检查一次
      check()
    },
    createWatermark() {
      if (!this.userName ) {
        // 未登录时不显示水印
        this.removeWatermark()
        return
      }

      const container = this.$refs.watermarkContainer
      if (!container) return

      // 清除旧的水印
      container.innerHTML = ''

      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      
      // 设置 canvas 尺寸
      const width = 160
      const height = 80
      canvas.width = width
      canvas.height = height

      // 设置文字样式
      ctx.font = '13px Arial'
      ctx.fillStyle = 'rgba(0, 0, 0, 0.15)'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      
      // 旋转画布
      ctx.translate(width / 2, height / 2)
      ctx.rotate(-Math.PI / 6) // -30度
      ctx.translate(-width / 2, -height / 2)

      // 绘制水印文字
      const watermarkText = this.id.indexOf('@') !== -1 ? `${this.id.split('@')[0]} ${this.userName}` : this.userName
      ctx.fillText(watermarkText, width / 2, height / 2)

      // 将 canvas 转为 base64
      const base64Url = canvas.toDataURL()
      
      // 创建水印容器
      const watermarkDiv = document.createElement('div')
      watermarkDiv.className = 'watermark'
      watermarkDiv.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;
        z-index: 9999;
        background-image: url(${base64Url});
        background-repeat: repeat;
        background-size: ${width}px ${height}px;
      `
      
      container.appendChild(watermarkDiv)
    },
    removeWatermark() {
      const container = this.$refs.watermarkContainer
      if (container) {
        container.innerHTML = ''
      }
    },
    handleResize() {
      // 防抖处理
      clearTimeout(this.resizeTimer)
      this.resizeTimer = setTimeout(() => {
        this.createWatermark()
      }, 200)
    },
    observeWatermark() {
      const container = this.$refs.watermarkContainer
      if (!container) return

      this.observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          if (mutation.type === 'childList') {
            // 如果水印被删除，重新创建
            const hasWatermark = container.querySelector('.watermark')
            if (!hasWatermark && this.userName ) {
              this.createWatermark()
            }
          }
        })
      })

      this.observer.observe(container, {
        childList: true,
        subtree: true
      })
    }
  }
}
</script>

<style scoped>
.watermark-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
}
</style>
