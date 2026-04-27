<template>
  <div class="topic-page">
    <div class="topic-header">
      <h2>论坛专题</h2>
      <p>按标签沉淀知识与案例，快速查看同类讨论。</p>
    </div>

    <div class="topic-toolbar">
      <div class="tag-list">
        <el-tag
          :type="!activeTag ? 'primary' : 'info'"
          effect="plain"
          @click="handleTagChange('')"
        >
          全部专题
        </el-tag>

        <span
          v-for="tag in hotTags"
          :key="tag.tagId || tag.tagName"
          class="tag-follow-item"
        >
          <el-tag
            :type="activeTag === tag.tagName ? 'primary' : 'info'"
            effect="plain"
            @click="handleTagChange(tag.tagName)"
          >
            #{{ tag.tagName }}
          </el-tag>
          <span
            v-if="tag.tagId"
            class="tag-follow-icon"
            :title="followedTagIds.includes(tag.tagId) ? '取消订阅' : '订阅此标签'"
            @click.stop="handleToggleFollow(tag)"
          >
            <i
              :class="followedTagIds.includes(tag.tagId) ? 'el-icon-star-on' : 'el-icon-star-off'"
              :style="{ color: followedTagIds.includes(tag.tagId) ? '#e6a23c' : '#c0c4cc' }"
            ></i>
          </span>
        </span>
      </div>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索专题内帖子标题"
        class="search-input"
        clearable
        @keyup.enter.native="handleQuery"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleQuery" />
      </el-input>
    </div>

    <div class="topic-summary">
      <div class="summary-item">
        <div class="summary-value">{{ total }}</div>
        <div class="summary-label">专题帖子数</div>
      </div>
      <div class="summary-item">
        <div class="summary-value">{{ activeTag || '全部' }}</div>
        <div class="summary-label">当前专题</div>
      </div>
      <div class="summary-item">
        <div class="summary-value">{{ queryParams.pageNum }}</div>
        <div class="summary-label">当前页</div>
      </div>
    </div>

    <div class="post-list" v-loading="loading">
      <div
        v-for="post in postList"
        :key="post.postId"
        class="post-item"
        @click="handlePostClick(post.postId)"
      >
        <div class="post-title">{{ post.title }}</div>
        <div class="post-tags" v-if="post.tags && post.tags.length">
          <el-tag
            v-for="tag in post.tags"
            :key="tag.tagId || tag.tagName"
            size="mini"
            effect="plain"
            :style="{ borderColor: tag.tagColor || '#409EFF', color: tag.tagColor || '#409EFF' }"
          >
            #{{ tag.tagName }}
          </el-tag>
        </div>
        <div class="post-meta">
          <span>{{ parseTime(post.createTime, '{y}-{m}-{d}') }}</span>
          <span>浏览 {{ post.viewCount || 0 }}</span>
          <span>点赞 {{ post.likeCount || 0 }}</span>
          <span>评论 {{ post.commentCount || 0 }}</span>
        </div>
      </div>
      <el-empty v-if="!loading && postList.length === 0" description="暂无专题内容" />
    </div>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listPost, getHotTags, toggleTagFollow, getMyFollowedTagIds } from '@/api/bbs/post'

export default {
  name: 'BbsTopic',
  data() {
    return {
      loading: false,
      hotTags: [],
      postList: [],
      total: 0,
      activeTag: '',
      searchKeyword: '',
      followedTagIds: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
      },
    }
  },
  created() {
    this.activeTag = this.$route.query.tagName || ''
    this.searchKeyword = this.$route.query.keyword || ''
    this.queryParams.title = this.searchKeyword || null
    this.getHotTags()
    this.getList()
    this.loadFollowedTags()
  },
  watch: {
    $route(to) {
      this.activeTag = to.query.tagName || ''
      this.searchKeyword = to.query.keyword || ''
      this.queryParams.title = this.searchKeyword || null
      this.queryParams.pageNum = to.query.page ? parseInt(to.query.page, 10) || 1 : 1
      this.getList()
    },
  },
  methods: {
    getHotTags() {
      getHotTags(50)
        .then((response) => {
          this.hotTags = response.data || []
        })
        .catch(() => {
          this.hotTags = []
        })
    },
    getList() {
      this.loading = true
      listPost({
        ...this.queryParams,
        topicTagName: this.activeTag || null,
        status: '0',
        sortType: 'latest',
      })
        .then((response) => {
          this.postList = response.rows || []
          this.total = response.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleTagChange(tagName) {
      this.queryParams.pageNum = 1
      this.$router.push({
        path: '/topic',
        query: {
          ...this.$route.query,
          tagName: tagName || undefined,
          page: undefined,
        },
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.$router.push({
        path: '/topic',
        query: {
          ...this.$route.query,
          keyword: this.searchKeyword || undefined,
          page: undefined,
        },
      })
    },
    handlePostClick(postId) {
      this.$router.push({
        path: '/post/' + postId,
        query: {
          from: 'topic',
          tagName: this.activeTag || undefined,
        },
      })
    },
    loadFollowedTags() {
      getMyFollowedTagIds()
        .then((res) => {
          this.followedTagIds = res.data || []
        })
        .catch(() => {})
    },
    handleToggleFollow(tag) {
      if (!tag.tagId) return
      toggleTagFollow(tag.tagId)
        .then((res) => {
          const following = res.data && res.data.following
          if (following) {
            if (!this.followedTagIds.includes(tag.tagId)) {
              this.followedTagIds.push(tag.tagId)
            }
            this.$message.success(`已订阅标签 #${tag.tagName}，有新帖将收到通知`)
          } else {
            this.followedTagIds = this.followedTagIds.filter((id) => id !== tag.tagId)
            this.$message.info(`已取消订阅 #${tag.tagName}`)
          }
        })
        .catch(() => this.$message.error('操作失败'))
    },
  },
}
</script>

<style scoped>
.topic-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.topic-header h2 {
  margin: 0;
  font-size: 24px;
}

.topic-header p {
  margin-top: 8px;
  color: #909399;
}

.topic-toolbar {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  justify-content: space-between;
  align-items: flex-start;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-list .el-tag {
  cursor: pointer;
}

.tag-follow-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.tag-follow-icon {
  cursor: pointer;
  font-size: 14px;
  padding: 0 2px;
  transition: color 0.2s;
}

.tag-follow-icon:hover i {
  color: #e6a23c !important;
}

.search-input {
  width: 280px;
}

.topic-summary {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.summary-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.summary-label {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.post-list {
  margin-top: 16px;
}

.post-item {
  border: 1px solid #f0f2f5;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  cursor: pointer;
}

.post-item:hover {
  background: #fafcff;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.post-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.post-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .topic-page {
    padding: 12px;
  }

  .topic-toolbar {
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }

  .topic-summary {
    grid-template-columns: 1fr;
  }
}
</style>
