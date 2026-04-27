<template>
  <div class="ssologin">
    <div class="loading-container">
      <div class="loading-content">
        <!-- 主加载动画 -->
        <div class="spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-ring"></div>
          <div class="spinner-ring"></div>
        </div>
        <!-- 加载文字 -->
        <div class="loading-text">
          <span class="loading-dot">正在登录中</span>
          <span class="dot dot1">.</span>
          <span class="dot dot2">.</span>
          <span class="dot dot3">.</span>
        </div>
        <!-- 副标题 -->
        <div class="loading-subtitle">请稍候，正在为您跳转</div>
      </div>
    </div>
  </div>
</template>

<script>
import { login, getInfo } from "@/api/login";
import { setToken,removeToken } from "@/utils/auth";
import { getWXtoken, getWXuserid } from "@/api/wechat";
import store from "@/store";
export default {
  name: "AutoLogin",
  data() {
    return {
      loginParams: {
        username: "",
        loginType: "sso",
      },
      redirect: undefined,
      agentid: process.env.NODE_ENV === "production" ? '1000063' :'1000165',
      accessToken: "",
      appid: "wx786a96dd52ea3edb",
    };
  },

  created() {
    //  获取企微认证token
    const busiToken = this.$route.query.busiToken;
    const code = this.getCode();
    if (busiToken) {
      removeToken();
      setTimeout(() => {
        setToken(busiToken);
        this.getInfo();
      }, 1000);
    } else if (!code) {
      console.log('没有code，跳转到企业微信授权链接')
      // 如果没有 code，说明是直接访问的，需要跳转到企业微信授权链接
      this.redirectToWeChatAuth();
    } else {
      console.log('没有code，获取accessToken')
      this.getAccessToken();
    }
  },
  methods: {
    /**
     * @name 获取企业微信用户信息
     * @step1 获取跳转链接的CODE
     * @step2 获取accessToken
     * @step3 获取企微USERID
     */
    getCode() {
      // 从路由 query 参数获取（code 是一次性的，不需要保存）
      const code = this.$route.query.code;
      if (code) {
        return code;
      }
      // 从 URL 中提取（兼容旧逻辑）
      const urlParams = new URLSearchParams(window.location.search);
      const urlCode = urlParams.get("code");
      if (urlCode) {
        return urlCode;
      }
      return "";
    },
    /**
     * @name 跳转到企业微信授权链接
     */
    redirectToWeChatAuth() {
      // 构建 redirect_uri，包含原始的 redirect 参数（如果有）
      const redirect = this.$route.query.redirect;
      let redirectUri = `${window.location.origin}/bbs/ssologin`;
      if (redirect) {
        redirectUri += `?redirect=${encodeURIComponent(redirect)}`;
      }

      // 构建企业微信授权链接
      const authUrl = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${
        this.appid
      }&redirect_uri=${encodeURIComponent(
        redirectUri
      )}&response_type=code&scope=snsapi_base&agentid=${
        this.agentid
      }&state=STATE#wechat_redirect`;

      // 跳转到企业微信授权页面
      window.location.href = authUrl;
    },
    getAccessToken() {
      const params = {
        appid: this.agentid,
      };
      getWXtoken(params).then((res) => {
        this.accessToken = res.data.access_token;
        this.getUserid();
      });
    },
    getUserid() {
      const code = this.getCode();
      const params = {
        access_token: this.accessToken,
        code: code,
      };
      getWXuserid(params).then((res) => {
        this.loginParams.username = res.data.UserId;
        this.handleLogin();
      });
    },

    handleLogin() {
      login(this.loginParams).then(
        (res) => {
          console.log('登录成功',res)
          setToken(res.token);
          setTimeout(() => {
            this.getInfo();
          }, 1000);
        },
        (err) => {
          this.$router.push({
            path: "/500",
          });
        }
      );
    },
    getInfo() {
      store.dispatch("GetInfo").then((res) => {
        // 触发全局事件：用户信息加载完成
        window.dispatchEvent(
          new CustomEvent("user-info-loaded", {
            detail: {
              user: res.user,
              userName: res.user?.userName,
              nickName: res.user?.nickName,
            },
          })
        );
        setTimeout(() => {
          // 如果有 redirect 参数，跳转到原始页面；否则跳转到首页
          const redirect = this.$route.query.redirect;
          if (redirect) {
            this.$router.push({
              path: redirect,
            });
          } else {
            this.$router.push({
              path: "/index",
            });
          }
        }, 1000);
      });
      // getInfo().then(
      //   (res) => {
      //     alert(1)
      //     // 触发全局事件：用户信息加载完成
      //     window.dispatchEvent(new CustomEvent('user-info-loaded', {
      //       detail: {
      //         user: res.user,
      //         userName: res.user?.userName,
      //         nickName: res.user?.nickName,
      //       }
      //     }));

      //     setTimeout(() => {
      //       this.$router.push({
      //         path: "/index",
      //       });
      //     }, 1000);
      //   },
      //   (err) => {
      //     this.$router.push({
      //       path: "/500",
      //     });
      //   }
      // );
    },
  },
};
</script>

<style scoped>
.ssologin {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景动画 */
.ssologin::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.1) 1px,
    transparent 1px
  );
  background-size: 50px 50px;
  animation: backgroundMove 20s linear infinite;
}

@keyframes backgroundMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.loading-container {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-content {
  text-align: center;
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 主加载动画 - 三个旋转的圆环 */
.spinner {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 30px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid transparent;
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: -0.45s;
  border-top-color: rgba(255, 255, 255, 0.9);
}

.spinner-ring:nth-child(2) {
  animation-delay: -0.3s;
  border-top-color: rgba(255, 255, 255, 0.7);
  width: 70px;
  height: 70px;
  top: 5px;
  left: 5px;
}

.spinner-ring:nth-child(3) {
  animation-delay: -0.15s;
  border-top-color: rgba(255, 255, 255, 0.5);
  width: 60px;
  height: 60px;
  top: 10px;
  left: 10px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 加载文字 */
.loading-text {
  font-size: 20px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.loading-dot {
  display: inline-block;
}

.dot {
  display: inline-block;
  animation: dotPulse 1.4s infinite;
  opacity: 0;
}

.dot1 {
  animation-delay: 0s;
}

.dot2 {
  animation-delay: 0.2s;
}

.dot3 {
  animation-delay: 0.4s;
}

@keyframes dotPulse {
  0%,
  60%,
  100% {
    opacity: 0;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 副标题 */
.loading-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 10px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .spinner {
    width: 60px;
    height: 60px;
  }

  .spinner-ring:nth-child(2) {
    width: 50px;
    height: 50px;
  }

  .spinner-ring:nth-child(3) {
    width: 40px;
    height: 40px;
  }

  .loading-text {
    font-size: 18px;
  }

  .loading-subtitle {
    font-size: 12px;
  }
}
</style>
