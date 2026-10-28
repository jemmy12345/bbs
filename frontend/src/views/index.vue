<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card class="intro-card">
          <h2>智慧论坛与提案管理系统</h2>
          <p>
            本系统面向企业内部沟通、经验分享、意见反馈和创新提案管理场景，提供论坛互动、匿名表达、内容风控、提案评审、结果公示和后台治理等能力。
          </p>
          <p>
            管理端用于用户、角色、菜单、内容审核、敏感词库和提案流程配置；前台用于发帖、评论、回复、提案投稿、评审协作和成果查看。
          </p>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="intro-card">
          <h3>核心模块</h3>
          <ul>
            <li>论坛发帖、评论、回复、点赞、收藏与分享</li>
            <li>浏览器端匿名标识与服务端匿名数据存储</li>
            <li>企业敏感词库、本地规则与智能审核兜底</li>
            <li>提案投稿、初审、复审、终审、进度跟踪与成果公示</li>
            <li>管理员后台配置、角色权限和运行数据维护</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="mt20">
      <el-col :xs="24" :md="8">
        <el-card class="feature-card">
          <h3>内容治理</h3>
          <p>统一处理普通用户、管理员和评审人员的发帖、评论、回复风控，降低不当内容发布风险。</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="feature-card">
          <h3>提案流程</h3>
          <p>支持提案从投稿、评审、立项、推进到公示的闭环管理，适合毕业设计演示完整业务链路。</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="feature-card">
          <h3>后台管理</h3>
          <p>提供用户、角色、菜单、日志、字典和论坛业务配置，便于系统维护和权限控制。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Index'
}
</script>

<style scoped lang="scss">
.home {
  line-height: 1.8;

  .intro-card,
  .feature-card {
    min-height: 180px;
  }

  h2,
  h3 {
    margin-top: 0;
    color: #303133;
  }

  p,
  li {
    color: #606266;
    font-size: 14px;
  }

  .mt20 {
    margin-top: 20px;
  }
}
</style>
