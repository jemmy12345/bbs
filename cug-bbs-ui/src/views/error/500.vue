<template>
    <div class="errPage-container">
      <!-- <el-button icon="arrow-left" class="pan-back-btn" @click="back">
        返回
      </el-button> -->
      <el-row style="display: flex; justify-content: center; align-items: center;">
        <el-col :span="12" style="text-align: right;">
          <img
            :src="errGif"
            width="250"
            height="320"
            alt="Girl has dropped her ice cream."
          />
        </el-col>
        <el-col :span="12" style="text-align: left;">
          <h1 class="text-jumbo text-ginormous">500 错误</h1>
          <h6>抱歉，服务出了一些问题</h6>
        </el-col>
      </el-row>
    </div>
  </template>
  
  <script>
  import errGif from "@/assets/401_images/401.gif";
  
  export default {
    name: "Page500",
    data() {
      return {
        errGif: errGif + "?" + +new Date(),
        rediretURL: "",
      };
    },
    mounted() {
     
    },
    methods: {
      back() {
        this.$router.go(-1);
      },
    },
  };
  </script>
  
  <style lang="scss" scoped>
  .errPage-container {
    width: 800px;
    max-width: 100%;
    margin: 100px auto;
    .pan-back-btn {
      background: #008489;
      color: #fff;
      border: none !important;
    }
    .pan-gif {
      margin: 0 auto;
      display: block;
    }
    .pan-img {
      display: block;
      margin: 0 auto;
      width: 100%;
    }
    .text-jumbo {
      font-size: 32px;
      font-weight: 500;
      color: #484848;
    }
    .list-unstyled {
      font-size: 14px;
      li {
        padding-bottom: 5px;
      }
      a {
        color: #008489;
        text-decoration: none;
        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
  </style>
  