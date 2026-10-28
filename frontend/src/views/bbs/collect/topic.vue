<template>
  <div class="collect-topic" v-loading="loading">
    <div v-if="data.activity" class="hero">
      <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/collect')">返回列表</el-button>
      <h2>{{ data.activity.title }}</h2>
      <p>{{ data.activity.ruleText || '暂无规则说明' }}</p>
      <div class="meta">
        <span>开始：{{ data.activity.startTime || '-' }}</span>
        <span>结束：{{ data.activity.endTime || '-' }}</span>
        <el-tag size="mini" type="success">{{ data.activity.status }}</el-tag>
      </div>
    </div>

    <div class="grid">
      <div class="panel">
        <h4>活动内容合集（问答 + 提案 + 帖子）</h4>
        <div v-for="item in (data.contentList || [])" :key="item.id" class="item">
          <div class="t">[{{ item.contentType }}] {{ item.contentTitle || ('内容#' + item.contentId) }}</div>
          <div class="m">作者：{{ item.authorNickName }}，得分：{{ item.score }}，点赞：{{ item.likeCount }}，评论：{{ item.commentCount }}</div>
        </div>
      </div>

      <div class="panel">
        <h4>参与榜单</h4>
        <el-table :data="data.rankList || []" size="mini" border>
          <el-table-column type="index" label="排名" width="70" />
          <el-table-column prop="nickName" label="用户" />
          <el-table-column prop="totalScore" label="总分" width="90" />
          <el-table-column prop="likeCount" label="获赞" width="90" />
        </el-table>
      </div>
    </div>

    <div class="panel">
      <h4>活动结算与归档</h4>
      <el-input type="textarea" :rows="3" v-model="summaryText" placeholder="填写活动总结，归档后沉淀为历史专题" />
      <div style="margin-top: 8px;"><el-button type="warning" @click="archiveTopicNow">结束并归档</el-button></div>
    </div>
  </div>
</template>

<script>
import { getCollectTopicDetail, archiveCollectTopic } from '@/api/bbs/collect'

export default {
  name: 'BbsCollectTopic',
  data() {
    return {
      loading: false,
      data: {},
      summaryText: '',
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      getCollectTopicDetail(this.$route.params.activityId).then(res => {
        this.data = res.data || {}
      }).finally(() => {
        this.loading = false
      })
    },
    archiveTopicNow() {
      archiveCollectTopic(this.$route.params.activityId, this.summaryText).then(() => {
        this.$message.success('活动已归档')
        this.load()
      })
    },
  },
}
</script>

<style scoped>
.collect-topic { max-width: 1100px; margin: 0 auto; padding: 16px; }
.hero { background: linear-gradient(120deg, #f6d365, #fda085); border-radius: 10px; padding: 18px; margin-bottom: 12px; }
.hero h2 { margin: 4px 0 8px; }
.meta { display: flex; gap: 12px; align-items: center; color: #606266; }
.grid { display: grid; grid-template-columns: 1fr 360px; gap: 10px; }
.panel { background: #fff; border-radius: 8px; padding: 12px; box-shadow: 0 1px 5px rgba(0,0,0,.08); margin-bottom: 10px; }
.item { border: 1px solid #f3f3f3; border-radius: 6px; padding: 8px; margin-bottom: 8px; }
.t { font-weight: 600; }
.m { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
