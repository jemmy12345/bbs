<template>
  <div class="usercenter-container">
    <!-- 上方个人信息区域 -->
    <div class="user-header">
      <div class="header-content">
        <div class="avatar-section">
          <el-avatar :src="defaultAvatar" :size="100"></el-avatar>
        </div>
        <div class="info-section">
          <h2 class="user-name">
            {{ $t('common.my') }}
          </h2>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-label">{{ $t('bbs.userCenter.published') }}</span>
              <span class="stat-value">{{ stats.postCount || 0 }}</span>
              <span class="stat-label">{{ $t('bbs.userCenter.posts') }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">{{ $t('bbs.userCenter.received') }}</span>
              <span class="stat-value">{{ stats.commentCount || 0 }}</span>
              <span class="stat-label">{{ $t('bbs.userCenter.replies') }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 下方内容区域 -->
    <div class="user-content">
      <!-- 左侧成就区域 -->
      <aside class="achievement-aside">
        <div class="achievement-card">
          <h3 class="achievement-title">{{ $t('bbs.userCenter.myAchievement') }}</h3>
          <div class="achievement-list">
            <div class="achievement-item">
              <img src="@/assets/images/uc-zan.png" alt="like" class="achievement-icon">
              <div class="achievement-info">
                <div class="achievement-label">{{ $t('bbs.userCenter.receivedLikes') }}</div>
                <div class="achievement-value">{{ stats.likeCount || 0 }}</div>
                <div class="achievement-label">{{ $t('bbs.userCenter.likes') }}</div>
              </div>
            </div>
            <div class="achievement-item">
              <img src="@/assets/images/uc-comment.png" alt="comment" class="achievement-icon">
              <div class="achievement-info">
                <div class="achievement-label">{{ $t('bbs.userCenter.contentActivity') }}</div>
                <div class="achievement-value">
                  {{ stats.commentCount || 0 }}
                </div>
                <div class="achievement-label">{{ $t('bbs.userCenter.comments') }}</div>
              </div>
            </div>
            <div class="achievement-item">
              <img src="@/assets/images/uc-collect.png" alt="collect" class="achievement-icon">
              <div class="achievement-info">
                <div class="achievement-label">{{ $t('bbs.userCenter.receivedCollects') }}</div>
                <div class="achievement-value">
                  {{ stats.collectCount || 0 }}
                </div>
                <div class="achievement-label">{{ $t('bbs.userCenter.collects') }}</div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧Tab内容区域 -->
      <div class="content-main">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('bbs.userCenter.myPosts')" name="posts">
            <div class="post-list" v-loading="loading">
              <div v-for="post in postList" :key="post.postId" class="post-item" @click="handlePostClick(post.postId)">
                <div class="post-content">
                  <div class="post-header">
                    <h3 class="post-title">{{ truncateTitle(post.title) }}</h3>
                    <div class="post-meta post-meta-pc">
                      <span class="time">{{
                        post.createTime
                      }}</span>
                      <span class="post-delete-action" @click.stop="deleteMyPost(post.postId)">
                        <i class="el-icon-delete"></i> {{ $t("common.delete") }}
                      </span>
                      <!-- 审核状态显示 -->
                      <span v-if="auditEnabled" class="divider">·</span>
                      <span v-if="auditEnabled && post.status === '2'" class="audit-status audit-pending">
                        <i class="el-icon-time"></i> {{ $t('bbs.userCenter.pendingReview') }}
                      </span>
                      <span v-else-if="auditEnabled && post.status === '3'" class="audit-status audit-rejected">
                        <i class="el-icon-close"></i> {{ $t('bbs.userCenter.reviewRejected') }}
                      </span>
                      <span v-else-if="auditEnabled && post.status === '0'" class="audit-status audit-approved">
                        <i class="el-icon-check"></i> {{ $t('bbs.userCenter.reviewApproved') }}
                      </span>
                    </div>
                  </div>
                  <!-- 手机端时间显示 -->
                  <div class="post-meta post-meta-mobile">
                    <span class="time">{{
                      post.createTime
                    }}</span>
                    <span class="post-delete-action" @click.stop="deleteMyPost(post.postId)">
                      <i class="el-icon-delete"></i> {{ $t("common.delete") }}
                    </span>
                    <!-- 审核状态显示 -->
                    <span v-if="auditEnabled" class="divider">·</span>
                    <span v-if="auditEnabled && post.status === '2'" class="audit-status audit-pending">
                      <i class="el-icon-time"></i> {{ $t('bbs.userCenter.pendingReview') }}
                    </span>
                    <span v-else-if="auditEnabled && post.status === '3'" class="audit-status audit-rejected">
                      <i class="el-icon-close"></i> {{ $t('bbs.userCenter.reviewRejected') }}
                    </span>
                    <span v-else-if="auditEnabled && post.status === '0'" class="audit-status audit-approved">
                      <i class="el-icon-check"></i> {{ $t('bbs.userCenter.reviewApproved') }}
                    </span>
                  </div>
                  <!-- 审核不通过原因 -->
                  <div v-if="auditEnabled && post.status === '3'" class="audit-reason">
                    <el-alert :title="$t('bbs.userCenter.auditReason', { user: fromUser })" type="info"
                      :closable="false" show-icon style="margin: 8px 0;" />
                  </div>
                  <div class="post-stats">
                    <span class="stat-item">
                      <i class="el-icon-view"></i> {{ post.viewCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="el-icon-chat-line-round"></i>
                      {{ post.commentCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="postList.length === 0 && !loading" class="empty-state">
                <i class="el-icon-document"></i>
                <p>{{ $t('bbs.userCenter.noData') }}</p>
              </div>
            </div>
            <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
              :limit.sync="queryParams.pageSize" @pagination="handlePagination" />
          </el-tab-pane>

          <el-tab-pane :label="$t('bbs.userCenter.myDrafts')" name="drafts">
            <div class="post-list" v-loading="loading">
              <div v-for="post in postList" :key="post.postId" class="post-item">
                <div class="post-content">
                  <div class="post-header">
                    <h3 class="post-title">{{ truncateTitle(post.title || $t('bbs.userCenter.noTitle')) }}</h3>
                    <div class="post-meta post-meta-pc">
                      <span class="time">{{
                        post.createTime
                      }}</span>
                      <span class="divider">·</span>
                      <span class="draft-status" style="color: #409EFF;cursor: pointer;" @click.stop="editDraft(post)">
                        <i class="el-icon-edit"></i> {{ $t('bbs.userCenter.draft') }}
                      </span>
                      <span class="draft-status" style="color: red; cursor: pointer;"
                        @click.stop="deleteDraft(post.postId)">
                        <i class="el-icon-delete"></i> {{ $t('common.delete') }}
                      </span>
                    </div>
                  </div>
                  <!-- 手机端时间显示 -->
                  <div class="post-meta post-meta-mobile">
                    <span class="time">{{
                      post.createTime
                    }}</span>
                    <span class="divider">·</span>
                    <span class="draft-status" style="color: #409EFF;cursor: pointer;" @click.stop="editDraft(post)">
                      <i class="el-icon-edit"></i> {{ $t('bbs.userCenter.draft') }}
                    </span>
                    <span class="draft-status" style="color: red; cursor: pointer;"
                      @click.stop="deleteDraft(post.postId)">
                      <i class="el-icon-delete"></i> {{ $t('common.delete') }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="postList.length === 0 && !loading" class="empty-state">
                <i class="el-icon-document"></i>
                <p>{{ $t('bbs.userCenter.noDrafts') }}</p>
              </div>
            </div>
            <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
              :limit.sync="queryParams.pageSize" @pagination="handlePagination" />
          </el-tab-pane>

          <el-tab-pane :label="$t('bbs.userCenter.myCollects')" name="collects">
            <div class="post-list" v-loading="loading">
              <div v-for="post in postList" :key="post.postId" class="post-item" @click="handlePostClick(post.postId)">
                <div class="post-content">
                  <h3 class="post-title">{{ truncateTitle(post.title) }}</h3>
                  <div class="post-meta post-meta-pc">
                    <span class="author">{{
                      post.nickName || $t('bbs.unknownUser')
                    }}</span>
                    <span class="divider">·</span>
                    <span class="time">{{
                      post.createTime
                    }}</span>
                  </div>
                  <!-- 手机端时间显示 -->
                  <div class="post-meta post-meta-mobile">
                    <span class="time">{{
                      post.createTime
                    }}</span>
                  </div>
                  <div class="post-stats">
                    <span class="stat-item">
                      <i class="el-icon-view"></i> {{ post.viewCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="el-icon-chat-line-round"></i>
                      {{ post.commentCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="postList.length === 0 && !loading" class="empty-state">
                <i class="el-icon-document"></i>
                <p>{{ $t('bbs.userCenter.noCollects') }}</p>
              </div>
            </div>
            <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
              :limit.sync="queryParams.pageSize" @pagination="handlePagination" />
          </el-tab-pane>

          <el-tab-pane :label="$t('bbs.userCenter.myLikes')" name="likes">
            <div class="post-list" v-loading="loading">
              <div v-for="post in postList" :key="post.postId" class="post-item" @click="handlePostClick(post.postId)">
                <div class="post-content">
                  <h3 class="post-title">{{ truncateTitle(post.title) }}</h3>
                  <div class="post-meta post-meta-pc">
                    <span class="author">{{
                      post.nickName || $t('bbs.unknownUser')
                    }}</span>
                    <span class="divider">·</span>
                    <span class="time">{{
                      post.createTime
                    }}</span>
                  </div>
                  <!-- 手机端时间显示 -->
                  <div class="post-meta post-meta-mobile">
                    <span class="time">{{
                      post.createTime
                    }}</span>
                  </div>
                  <div class="post-stats">
                    <span class="stat-item">
                      <i class="el-icon-view"></i> {{ post.viewCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="el-icon-chat-line-round"></i>
                      {{ post.commentCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="postList.length === 0 && !loading" class="empty-state">
                <i class="el-icon-document"></i>
                <p>{{ $t('bbs.userCenter.noLikes') }}</p>
              </div>
            </div>
            <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
              :limit.sync="queryParams.pageSize" @pagination="handlePagination" />
          </el-tab-pane>
        </el-tabs>

        <!-- 匿名秘钥输入（查询我的匿名） -->
        <AnonymousKeyDialog :visible.sync="keyDialogVisible" :key-not-found-hint="keyNotFoundHint"
          :allow-generate="false" default-key-source="manual" @confirm="onAnonymousKeyConfirm"
          @cancel="onAnonymousKeyCancel" />
      </div>
    </div>
  </div>
</template>

<script>
import {
  getUserCenterStats,
  getMyPosts,
  getMyCollects,
  getMyLikes,
  listAnonymousPostsByKey,
  listAnonymousRepliesByKey,
} from "@/api/bbs/usercenter";
import { getAuditEnabled, listDrafts, delPost, delByPersonal } from "@/api/bbs/post";
import { mapGetters } from "vuex";
import Pagination from "@/components/Pagination";
import AnonymousKeyDialog from "@/components/AnonymousKeyDialog";
import {
  getAnonymousKey,
  hasAnonymousKey,
  hasAnonymousKeyInLocalStorage,
  hasEverSetAnonymousKey,
  hashAnonymousKeyForUser,
  setAnonymousKey,
} from "@/utils/anonymousKey";

export default {
  name: "UserCenter",
  components: {
    Pagination,
    AnonymousKeyDialog,
  },
  data() {
    return {
      stats: {
        postCount: 0,
        commentCount: 0,
        likeCount: 0,
        collectCount: 0,
      },
      activeTab: "posts",
      postList: [],
      loading: false,
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      defaultAvatar:
        "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
      auditEnabled: false,
      keyDialogVisible: false,
      keyNotFoundHint: false,
      pendingAnonymousLoad: false,
      /** 我的匿名：posts | comments */
      anonymousSubType: "posts",
      anonymousCommentList: [],
    };
  },
  computed: {
    ...mapGetters(["user", "id"]),
    userInfo() {
      return this.$store.state.user || {};
    },
  },
  created() {
    this.loadStats();
    this.loadPostList();
    this.getAuditEnabled();
  },
  methods: {
    // 截取标题（手机端30个字）
    truncateTitle(title) {
      if (!title) return '';
      // 检测是否为移动端
      if (typeof window !== 'undefined') {
        const isMobile = window.innerWidth <= 768;
        if (isMobile && title.length > 30) {
          return title.substring(0, 30) + '...';
        }
      }
      return title;
    },
    // 获取审核开关状态
    getAuditEnabled() {
      getAuditEnabled()
        .then((response) => {
          if (response.code === 200) {
            this.auditEnabled = response.data || false;
          }
        })
        .catch(() => {
          this.auditEnabled = false;
        });
    },
    // 加载统计信息
    loadStats() {
      getUserCenterStats()
        .then((response) => {
          if (response.code === 200) {
            this.stats = response.data || {};
          }
        })
        .catch(() => {
          this.$message.error(this.$t('bbs.userCenter.loadStatsFailed'));
        });
    },
    // 加载帖子列表
    loadPostList() {
      this.loading = true;
      let apiMethod;
      if (this.activeTab === "posts") {
        apiMethod = getMyPosts;
      } else if (this.activeTab === "anonymousPosts") {
        // 匿名帖子：需要秘钥hash
        this.loading = false;
        this.loadAnonymousPostList();
        return;
      } else if (this.activeTab === "collects") {
        apiMethod = getMyCollects;
      } else if (this.activeTab === "likes") {
        apiMethod = getMyLikes;
      } else if (this.activeTab === "drafts") {
        apiMethod = listDrafts;
      }

      if (apiMethod) {
        apiMethod(this.queryParams)
          .then((response) => {
            this.loading = false;
            if (response.code === 200) {
              this.postList = response.rows.filter(item => {
                return item.status != '1';
              });
              console.log('this.postList', this.postList);
              this.total = response.total || 0;
            }
          })
          .catch(() => {
            this.loading = false;
            this.$message.error(this.$t('bbs.userCenter.loadListFailed'));
          });
      } else {
        this.loading = false;
      }
    },
    loadAnonymousPostList() {
      // 如果浏览器 localStorage 中已有秘钥：仍弹出秘钥设置框，让用户选择“从浏览器获取”或手动/生成。
      if (hasAnonymousKeyInLocalStorage()) {
        this.keyNotFoundHint = false;
        this.pendingAnonymousLoad = true;
        this.keyDialogVisible = true;
        this.postList = [];
        this.anonymousCommentList = [];
        this.total = 0;
        return;
      }

      // 无 localStorage 秘钥：如果当前仅有“会话内秘钥”，则直接查询；否则弹框提示输入。
      if (!hasAnonymousKey()) {
        this.keyNotFoundHint = hasEverSetAnonymousKey();
        this.pendingAnonymousLoad = true;
        this.keyDialogVisible = true;
        this.postList = [];
        this.anonymousCommentList = [];
        this.total = 0;
        return;
      }

      const rawKey = getAnonymousKey();
      this.loading = true;
      hashAnonymousKeyForUser(rawKey, this.id)
        .then((keyHash) => this.fetchAnonymousByHash(keyHash))
        .then(() => {
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
          this.$message.error(this.$t("bbs.userCenter.loadListFailed"));
        });
    },
    /**
     * 已得到 hashCode 时，按当前 anonymousSubType 请求列表
     */
    fetchAnonymousByHash(keyHash) {
      if (!keyHash) {
        return Promise.reject(new Error("anonymous key hash missing"));
      }
      if (this.anonymousSubType === "posts") {
        return listAnonymousPostsByKey(keyHash, { ...this.queryParams }).then(
          (response) => {
            if (response && response.code === 200) {
              this.postList = response.rows.filter(item => {
                return item.status != '1';
              });
              console.log('this.postList', this.postList);
              this.total = this.postList.length || 0;
              this.anonymousCommentList = [];
            }
          }
        );
      }
      if (this.anonymousSubType === "comments") {
        return listAnonymousRepliesByKey(keyHash, { ...this.queryParams }).then(
          (response) => {
            if (response && response.code === 200) {
              this.anonymousCommentList = response.rows || response.data || [];
              this.total = response.total || 0;
              this.postList = [];
            }
          }
        );
      }
      return Promise.resolve();
    },
    onAnonymousSubTypeChange() {
      if (this.activeTab !== "anonymousPosts") return;
      this.queryParams.pageNum = 1;
      this.loadAnonymousPostList();
    },
    onAnonymousKeyConfirm(key, rememberInBrowser) {
      setAnonymousKey(key, { rememberInBrowser });
      this.anonymousSubType = "posts";
      this.queryParams.pageNum = 1;
      this.loading = true;
      hashAnonymousKeyForUser(key, this.id)
        .then((keyHash) => this.fetchAnonymousByHash(keyHash))
        .then(() => {
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
          this.$message.error(this.$t("bbs.userCenter.loadListFailed"));
        })
        .finally(() => {
          this.pendingAnonymousLoad = false;
        });
    },
    truncateAnonymousContent(row) {
      const text =
        row.content ||
        row.commentContent ||
        row.replyContent ||
        row.text ||
        "";
      if (!text) return "";
      const plain = String(text).replace(/<[^>]+>/g, "");
      if (typeof window !== "undefined") {
        const isMobile = window.innerWidth <= 768;
        const max = isMobile ? 80 : 200;
        if (plain.length > max) return plain.substring(0, max) + "...";
      }
      return plain.length > 200 ? plain.substring(0, 200) + "..." : plain;
    },
    anonymousCommentPostTitle(row) {
      return row.postTitle || row.title || row.topicTitle || "";
    },
    handleAnonymousCommentRowClick(row) {
      const postId = row.postId || row.topicId;
      if (postId) this.handlePostClick(postId);
    },
    onAnonymousKeyCancel() {
      this.pendingAnonymousLoad = false;
      // 保持在匿名帖子tab，但不发请求
    },
    // Tab切换
    handleTabClick() {
      this.queryParams.pageNum = 1;
      this.loadPostList();
    },
    // 分页
    handlePagination() {
      this.loadPostList();
    },
    // 点击帖子
    handlePostClick(postId) {
      this.$router.push({
        path: "/post/" + postId,
      });
    },
    // 编辑草稿
    editDraft(post) {
      // 跳转到发布页面，并传递草稿ID
      this.$router.push({
        path: "/index",
        query: {
          draftId: post.postId,
          publish: "true"
        }
      });
    },
    // 删除草稿
    deleteDraft(postId) {
      this.$modal.confirm(this.$t('bbs.userCenter.confirmDeleteDraft')).then(() => {
        delPost(postId).then(() => {
          this.$modal.msgSuccess(this.$t('bbs.userCenter.deleteSuccess'));
          this.loadPostList();
        }).catch(() => {
          this.$modal.msgError(this.$t('bbs.userCenter.deleteFailed'));
        });
      }).catch(() => { });
    },
    // 删除我的帖子
    deleteMyPost(postId) {
      this.$modal
        .confirm(this.$t("bbs.userCenter.confirmDeletePost"))
        .then(() => {
          delByPersonal(postId)
            .then(() => {
              this.$modal.msgSuccess(this.$t("bbs.userCenter.deleteSuccess"));
              this.loadPostList();
              this.loadStats();
            })
            .catch(() => {
              this.$modal.msgError(this.$t("bbs.userCenter.deleteFailed"));
            });
        })
        .catch(() => { });
    },
  },
};
</script>

<style lang="scss" scoped>
.usercenter-container {
  min-height: calc(100vh - 60px);
  background: #f5f5f5;
  padding: 20px;
}

.user-header {
  background: white;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .header-content {
    display: flex;
    align-items: center;
    gap: 30px;
  }

  .avatar-section {
    flex-shrink: 0;
  }

  .info-section {
    flex: 1;

    .user-name {
      margin: 0 0 20px 0;
      font-size: 24px;
      color: #333;
    }

    .user-stats {
      display: flex;
      gap: 40px;

      .stat-item {
        display: flex;
        align-items: baseline;
        gap: 5px;

        .stat-label {
          font-size: 14px;
          color: #666;
        }

        .stat-value {
          font-size: 20px;
          font-weight: bold;
          color: #1890ff;
        }
      }
    }
  }
}

.user-content {
  display: flex;
  gap: 20px;
}

.achievement-aside {
  width: 250px;
  flex-shrink: 0;
  padding: 8px 0 !important;
}

.achievement-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .achievement-title {
    margin: 0 0 20px 0;
    font-size: 18px;
    color: #333;
    padding-bottom: 15px;
    border-bottom: 2px solid #1890ff;
  }

  .achievement-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .achievement-item {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    transition: all 0.3s;

    &:hover {
      background: #e6f7ff;
    }

    .achievement-icon {
      font-size: 32px;
      color: #1890ff;
      width: 22px;
      height: 22px;
    }

    .achievement-info {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 5px;

      .achievement-label {
        font-size: 12px;
        color: #999;
        margin-bottom: 5px;
      }

      .achievement-value {
        font-size: 24px;
        font-weight: bold;
        color: #1890ff;
        line-height: 1.2;
      }
    }
  }
}

.content-main {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  ::v-deep .el-tabs__header {
    margin-bottom: 20px;
  }

  .post-list {
    min-height: 400px;
  }

  .post-item {
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #f8f9fa;
    }

    &:last-child {
      border-bottom: none;
    }

    .post-content {
      .post-title {
        margin: 0 0 10px 0;
        font-size: 16px;
        color: #333;
        font-weight: 500;
        line-height: 1.5;
      }

      .draft-status {
        color: #909399;
        font-size: 12px;
      }

      .draft-actions {
        margin-top: 10px;
        display: flex;
        gap: 10px;
      }

      .post-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 10px;
        font-size: 13px;
        color: #999;

        .category {
          color: #1890ff;
        }

        .author {
          color: #666;
        }

        .divider {
          color: #ddd;
        }

        .post-delete-action {
          color: #f56c6c;
          cursor: pointer;
        }
      }

      // PC端显示，手机端隐藏
      .post-meta-pc {
        display: flex;
      }

      // 手机端显示，PC端隐藏
      .post-meta-mobile {
        display: none;
      }

      .post-stats {
        display: flex;
        gap: 20px;
        font-size: 13px;
        color: #999;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 5px;

          i {
            font-size: 14px;
          }
        }
      }

      .audit-status {
        font-size: 12px;
        margin-left: 8px;

        &.audit-pending {
          color: #409EFF;
        }

        &.audit-approved {
          color: #67C23A;
        }

        &.audit-rejected {
          color: #F56C6C;
        }
      }

      .audit-reason {
        margin-top: 8px;
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #999;

    i {
      font-size: 48px;
      margin-bottom: 15px;
      display: block;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }
}

// 移动端适配
@media screen and (max-width: 768px) {
  .usercenter-container {
    padding: 10px;
  }

  .user-header {
    padding: 20px;

    .header-content {
      flex-direction: column;
      text-align: center;
      gap: 20px;
    }

    .user-stats {
      justify-content: center;
      flex-wrap: wrap;
    }
  }

  .user-content {
    flex-direction: column;
  }

  .achievement-aside {
    width: 100%;
  }

  .achievement-list {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .achievement-item {
    flex: 1;
    min-width: 45%;
  }

  // 手机端布局调整
  .post-item {
    .post-content {
      .post-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }

      .post-title {
        width: 100%;
        margin-bottom: 0;
      }

      // 手机端显示时间在标题下方
      .post-meta-pc {
        display: none;
      }

      .post-meta-mobile {
        display: flex;
        margin-top: 8px;
        margin-bottom: 10px;
      }
    }
  }
}

.post-header {
  display: flex;
  justify-content: space-between;
}

.anonymous-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  .anonymous-toolbar-label {
    font-size: 14px;
    color: #606266;
  }

  .anonymous-type-select {
    min-width: 200px;
  }
}

.anonymous-comment-excerpt {
  margin: 0;
  font-size: 15px;
  color: #333;
  line-height: 1.5;
  font-weight: 500;
  text-align: left;
  flex: 1;
  padding-right: 12px;
}

.anonymous-comment-post-ref {
  font-size: 13px;
  color: #1890ff;
  margin-top: 8px;
}
</style>
