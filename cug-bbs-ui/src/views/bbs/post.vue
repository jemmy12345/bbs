<template>
  <div class="bbs-post-detail">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <div class="post-detail" v-loading="loading">
          <!-- 帖子内容 -->
          <div class="post-content-card" v-if="post">
            <div class="post-header">
              <!-- <el-tag v-if="post.isTop === '1'" type="danger" size="small">{{ $t('bbs.top') }}</el-tag>
              <el-tag v-if="post.isEssence === '1'" type="warning" size="small">{{ $t('bbs.essence') }}</el-tag> -->
              <h1 class="post-title">
                <el-tag
                  v-if="post.postType"
                  :type="getPostTypeTagType(post.postType)"
                  size="mini"
                  style="margin-right: 5px"
                >
                  {{ getPostTypeName(post.postType) }}
                </el-tag>
                <span>{{ post.title }}</span>
              </h1>
              <div v-if="post.tags && post.tags.length" class="post-tags-inline">
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
            </div>
            <div class="post-meta">
              <el-avatar
                :src="getPostAvatar(post)"
                :size="40"
              >
                <i
                  v-if="post.isAnonymous === '1'"
                  class="el-icon-user-solid"
                ></i>
              </el-avatar>
              <div class="meta-info">
                <div class="author">
                  {{
                    post.isAnonymous === "1"
                      ? $t("bbs.anonymousUser")
                      : shouldShowAdminTag(post)
                      ? $t("bbs.admin")
                      : post.nickName || $t("bbs.unknownUser")
                  }}
                </div>
                <div class="meta-detail">
                  <!-- <span>{{ post.categoryName }}</span> -->
                 
                  
                  <span>{{
                    parseTime(post.createTime, "{y}-{m}-{d} {h}:{i}")
                  }}</span>
                  <span
                    v-if="
                      (post.postType === 'suggestion' ||
                        post.postType === 'opinion') &&
                      post.responseDeptName &&
                      post.responseDeptName.trim()
                    "
                    style="margin-right: 5px; color: #909399; font-size: 12px"
                  >
                    {{ $t("bbs.responseDeptPrefix") }} {{ post.responseDeptName }}
                  </span>
                  <span>{{ $t("bbs.view") }} {{ post.viewCount }}</span>
                  <span style="margin-left: 8px">
                    <img
                      :src="require('@/assets/images/dianzan.svg')"
                      :alt="$t('bbs.like')"
                      class="like-icon-small"
                      :class="{ 'liked-icon': post.isLiked }"
                    />
                    {{ post.likeCount }}
                  </span>
                  <el-tag
                    v-if="post.isAnonymous === '1'"
                    size="mini"
                    type="info"
                    style="margin-left: 5px"
                    >{{ $t("bbs.anonymous") }}</el-tag
                  >
                </div>
              </div>
            </div>
            
            <div
              class="post-body"
              v-html="post.content"
              @click="handleCommentImageClick($event)"
            ></div>
            <div
              v-if="showFollowupSystemNotice"
              class="post-system-notice"
            >
              <i class="el-icon-info notice-icon"></i>
              <div class="notice-content">
                <div class="notice-title">
                  {{ $t("bbs.systemNoticeTitle") }}
                </div>
                <div class="notice-text">
                  {{ followupSystemNoticeText }}
                </div>
              </div>
            </div>
            <div v-if="showFollowupSystemNotice" class="followup-status-card">
              <div class="followup-status-header">
                <div>
                  <div class="followup-status-title">建议闭环状态</div>
                  <div class="followup-status-desc">
                    面向建议/意见类帖子，用于展示部门受理和处理进度。
                  </div>
                </div>
                <el-tag :type="followupMeta.tagType" size="small">
                  {{ followupMeta.label }}
                </el-tag>
              </div>
              <div v-if="followupMeta.note" class="followup-note">
                {{ followupMeta.note }}
              </div>
              <div v-if="followupLatest" class="followup-note">
                责任人：{{ followupLatest.ownerUserName || followupLatest.ownerUserId || '未指定' }}
                <span v-if="followupLatest.handleTime">，处理时间：{{ parseTime(followupLatest.handleTime, '{y}-{m}-{d} {h}:{i}') }}</span>
              </div>
              <div v-if="canManageFollowup" class="followup-form">
                <el-select
                  v-model="followupForm.followupStatus"
                  size="small"
                  style="width: 180px"
                >
                  <el-option label="已受理" value="accepted" />
                  <el-option label="处理中" value="processing" />
                  <el-option label="已反馈" value="feedback" />
                  <el-option label="已解决" value="resolved" />
                </el-select>
                <el-input
                  v-model="followupForm.followupNote"
                  size="small"
                  maxlength="120"
                  show-word-limit
                  placeholder="可填写处理说明、结论或下一步安排"
                />
                <el-input
                  v-model="followupForm.ownerUserName"
                  size="small"
                  maxlength="50"
                  placeholder="责任人名称（可选）"
                />
                <el-button
                  type="primary"
                  size="small"
                  :loading="followupSubmitting"
                  @click="handleFollowupUpdate"
                >
                  更新状态
                </el-button>
              </div>
              <div v-if="followupHistory && followupHistory.length" class="followup-history">
                <div class="followup-history-title">处理记录</div>
                <div
                  v-for="item in followupHistory.slice(0, 5)"
                  :key="item.recordId"
                  class="followup-history-item"
                >
                  <span class="history-status">{{ parseFollowupRecordMeta(item).label }}</span>
                  <span class="history-owner">{{ item.ownerUserName || item.ownerUserId || '未指定责任人' }}</span>
                  <span class="history-time">{{ parseTime(item.handleTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                  <span class="history-note">{{ item.processNote || '-' }}</span>
                </div>
              </div>
            </div>
            <div class="post-actions">
              <el-button
                :type="post.isLiked ? 'primary' : ''"
                @click="handleLike"
                :size="isMobile ? 'small' : 'medium'"
                :class="{
                  'liked-button': post.isLiked,
                  'like-button-clicked': isLikeClicked,
                }"
              >
                <img
                  :src="require('@/assets/images/dianzan.svg')"
                  :alt="$t('bbs.like')"
                  class="like-icon"
                  :class="{ 'liked-icon': post.isLiked }"
                />
                <span
                  >{{ post.isLiked ? $t("bbs.liked") : $t("bbs.like") }} ({{
                    post.likeCount
                  }})</span
                >
              </el-button>
              <el-button
                :type="post.isCollected ? 'warning' : ''"
                icon="el-icon-collection"
                @click="handleCollect"
                :size="isMobile ? 'small' : 'medium'"
              >
                {{ post.isCollected ? $t("bbs.collected") : $t("bbs.collect") }}
                ({{ post.collectCount }})
              </el-button>
            </div>
          </div>

          <!-- 评论区域 -->
          <div class="comment-section" id="comment-section">
            <div class="comment-title">
              {{ $t("bbs.commentTitle") }} ({{ commentList.length }})
            </div>

            <!-- 发表评论 -->
            <div class="comment-form">
              <div class="comment-editor-wrapper">
                <editor
                  v-model="commentForm.content"
                  :min-height="isMobile ? 150 : 200"
                  :file-size="5"
                  type="url"
                  :read-only="false"
                  ref="commentEditor"
                />
              </div>
              <div class="comment-form-options">
                <el-radio-group v-model="commentForm.isAnonymous" size="small">
                  <el-radio label="1" :disabled="disableAnonymous">{{
                    $t("bbs.anonymousPublish")
                  }}</el-radio>
                  <el-radio label="0">{{ $t("bbs.realNamePublish") }}</el-radio>
                </el-radio-group>
                <span
                  v-if="disableAnonymous"
                  style="margin-left: 10px; color: #909399; font-size: 12px"
                >
                  {{ $t("bbs.deptContactTip") }}
                </span>
              </div>
              <div class="comment-actions">
                <el-button
                  type="primary"
                  @click="submitComment"
                  :size="isMobile ? 'small' : 'medium'"
                >
                  {{ $t("bbs.publishComment") }}
                </el-button>
              </div>
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
              <div
                v-for="comment in commentList"
                :key="comment.commentId"
                class="comment-item"
                :class="{ 'official-reply-item': isOfficialReply(comment) }"
              >
                <div class="comment-avatar-wrapper">
                  <el-avatar
                    :src="comment.isAnonymous === '1' ? '' : comment.avatar || ''"
                    :size="40"
                  >
                    <i
                      v-if="comment.isAnonymous === '1'"
                      class="el-icon-user-solid"
                    ></i>
                  </el-avatar>
                  <span v-if="isOfficialReply(comment)" class="official-reply-badge">{{
                    $t("bbs.officialReply")
                  }}</span>
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{
                      comment.isAnonymous === "1"
                        ? $t("bbs.anonymousUser")
                        : comment.nickName || $t("bbs.unknownUser")
                    }}</span>
                    <span
                      v-if="comment.isAnonymous === '0' && comment.deptName"
                      class="comment-dept"
                      style="margin-left: 8px; color: #909399; font-size: 12px"
                      >{{ comment.deptName }}</span
                    >
                    <el-tag
                      v-if="comment.isAnonymous === '1'"
                      size="mini"
                      type="info"
                      style="margin-left: 5px"
                      >{{ $t("bbs.anonymous") }}</el-tag
                    >
                    <span class="comment-time">{{
                      parseTime(comment.createTime, "{y}-{m}-{d} {h}:{i}")
                    }}</span>
                  </div>
                  <div
                    class="comment-text"
                    v-html="comment.content"
                    @click="handleCommentImageClick($event)"
                  ></div>
                  <!-- 评论图片 -->
                  <div v-if="comment.images" class="comment-images">
                    <div
                      v-for="(image, index) in getCommentImages(comment.images)"
                      :key="index"
                      class="comment-image-item"
                      @click="previewImage(image)"
                    >
                      <img
                        :src="getImageUrl(image)"
                        :alt="$t('bbs.commentImage') + (index + 1)"
                      />
                    </div>
                  </div>
                  <div class="comment-actions">
                    <el-button
                      type="text"
                      size="small"
                      @click="handleReply(comment)"
                    >
                      {{ $t("bbs.reply") }}
                    </el-button>
                    <el-button
                      type="text"
                      size="small"
                      :class="{ liked: comment.isLiked }"
                      @click="handleCommentLike(comment)"
                    >
                      <img
                        :src="require('@/assets/images/dianzan.svg')"
                        :alt="$t('bbs.like')"
                        class="like-icon"
                        :class="{ 'liked-icon': comment.isLiked }"
                      />
                      {{ comment.likeCount }}
                    </el-button>
                    <el-button
                      type="text"
                      size="small"
                      :class="{ disliked: comment.isDisliked }"
                      @click="handleCommentDislike(comment)"
                    >
                    <img
                        :src="require('@/assets/images/diancai.svg')"
                        :alt="$t('bbs.like')"
                        class="like-icon"
                        :class="{ 'liked-icon': comment.isLiked }"
                      />
                     {{ comment.dislikeCount || 0 }}
                    </el-button>
                    <el-button
                      v-if="canDeleteComment(comment)"
                      type="text"
                      size="small"
                      class="comment-delete-btn"
                      @click="handleDeleteComment(comment)"
                    >
                      {{ $t("common.delete") }}
                    </el-button>
                  </div>

                  <!-- 回复输入框 -->
                  <div
                    v-if="replyingCommentId === comment.commentId"
                    class="reply-form"
                  >
                    <div class="reply-editor-wrapper">
                      <editor
                        v-model="replyForm.content"
                        :min-height="isMobile ? 120 : 150"
                        :file-size="5"
                        type="url"
                      />
                    </div>
                    <div class="reply-form-options">
                      <el-radio-group
                        v-model="replyForm.isAnonymous"
                        size="small"
                      >
                        <el-radio label="1" :disabled="disableAnonymous">{{
                          $t("bbs.anonymousPublish")
                        }}</el-radio>
                        <el-radio label="0">{{
                          $t("bbs.realNamePublish")
                        }}</el-radio>
                      </el-radio-group>
                      <span
                        v-if="disableAnonymous"
                        style="
                          margin-left: 10px;
                          color: #909399;
                          font-size: 12px;
                        "
                      >
                        {{ $t("bbs.deptContactTip") }}
                      </span>
                    </div>
                    <div class="reply-form-actions">
                      <el-button size="small" @click="cancelReply">{{
                        $t("common.cancel")
                      }}</el-button>
                      <el-button
                        type="primary"
                        size="small"
                        @click="submitReply(comment)"
                        >{{ $t("bbs.submitReply") }}</el-button
                      >
                    </div>
                  </div>

                  <!-- 子评论 -->
                  <div
                    v-if="comment.children && comment.children.length > 0"
                    class="sub-comments"
                  >
                    <div
                      v-for="subComment in comment.children"
                      :key="subComment.commentId"
                      class="sub-comment-item"
                      :class="{ 'official-reply-item': isOfficialReply(subComment) }"
                    >
                      <div class="comment-avatar-wrapper sub-avatar-wrapper">
                        <el-avatar
                          :src="
                            subComment.isAnonymous === '1'
                              ? ''
                              : subComment.avatar || ''
                          "
                          :size="30"
                        >
                          <i
                            v-if="subComment.isAnonymous === '1'"
                            class="el-icon-user-solid"
                          ></i>
                        </el-avatar>
                        <span
                          v-if="isOfficialReply(subComment)"
                          class="official-reply-badge"
                          >{{ $t("bbs.officialReply") }}</span
                        >
                      </div>
                      <div class="sub-comment-content">
                        <div class="sub-comment-header">
                          <span class="sub-comment-author">{{
                            subComment.isAnonymous === "1"
                              ? $t("bbs.anonymousUser")
                              : subComment.nickName || $t("bbs.unknownUser")
                          }}</span>
                          <span
                            v-if="
                              subComment.isAnonymous === '0' &&
                              subComment.deptName
                            "
                            class="comment-dept"
                            style="
                              margin-left: 8px;
                              color: #909399;
                              font-size: 12px;
                            "
                            >{{ subComment.deptName }}</span
                          >
                          <el-tag
                            v-if="subComment.isAnonymous === '1'"
                            size="mini"
                            type="info"
                            style="margin-left: 5px"
                            >{{ $t("bbs.anonymous") }}</el-tag
                          >
                          <span class="sub-comment-time">{{
                            parseTime(subComment.createTime, "{y}-{m}-{d} {h}:{i}")
                          }}</span>
                        </div>
                        <div
                          class="sub-comment-text"
                          v-html="subComment.content"
                          @click="handleCommentImageClick($event)"
                        ></div>
                        <!-- 子评论图片 -->
                        <div
                          v-if="subComment.images"
                          class="sub-comment-images"
                        >
                          <div
                            v-for="(image, index) in getCommentImages(
                              subComment.images
                            )"
                            :key="index"
                            class="sub-comment-image-item"
                            @click="previewImage(image)"
                          >
                            <img
                              :src="getImageUrl(image)"
                              :alt="$t('bbs.commentImage') + (index + 1)"
                            />
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图片预览对话框 -->
    <el-dialog
      :visible.sync="previewImageVisible"
      :title="$t('bbs.imagePreview')"
      width="800px"
      append-to-body
    >
      <img
        :src="previewImageUrl"
        style="width: 100%; max-height: 600px; object-fit: contain"
      />
    </el-dialog>

    <!-- 匿名秘钥设置对话框（匿名评论/回复且本地无秘钥时弹出） -->
    <AnonymousKeyDialog
      :visible.sync="keyDialogVisible"
      :key-not-found-hint="keyNotFoundHint"
      @confirm="onAnonymousKeyConfirm"
      @cancel="onAnonymousKeyCancel"
    />
  </div>
</template>

<script>
import {
  getPost,
  toggleLike,
  toggleCollect,
  updatePostFollowup,
  getPostFollowupLatest,
  getPostFollowupHistory,
} from "@/api/bbs/post";
import {
  listComment,
  addComment,
  delByPersonal,
  toggleLike as toggleCommentLike,
  toggleDislike as toggleCommentDislike,
} from "@/api/bbs/comment";
import { checkSensitiveWords } from "@/api/bbs/sensitive";
import { checkIsDeptContact } from "@/api/bbs/deptContact";
import { getToken } from "@/utils/auth";
import { mapGetters } from "vuex";
import {
  hasEverSetAnonymousKey,
  getAnonymousKey,
  hashAnonymousKeyForUser,
  setAnonymousKey,
} from "@/utils/anonymousKey";
import ImageUpload from "@/components/ImageUpload";
import Editor from "@/components/Editor";
import AnonymousKeyDialog from "@/components/AnonymousKeyDialog";

export default {
  name: "BbsPost",
  components: {
    ImageUpload,
    Editor,
    AnonymousKeyDialog,
  },
  data() {
    return {
      loading: false,
      post: null,
      commentList: [],
      commentForm: {
        content: "",
        images: "",
        parentId: 0,
        replyUserId: null,
        isAnonymous: "1",
      },
      replyComment: null,
      replyingCommentId: null,
      replyForm: {
        content: "",
        isAnonymous: "1",
      },
      isMobile: false,
      previewImageUrl: "",
      previewImageVisible: false,
      isLikeClicked: false,
      isDeptContact: false, // 当前用户是否是部门接口人
      disableAnonymous: false, // 是否禁用匿名评论
      keyDialogVisible: false,
      keyNotFoundHint: false,
      pendingAnonymousSubmit: null,
      pendingAnonymousPayload: null,
      currentAnonymousUserHash: "",
      followupSubmitting: false,
      followupLatest: null,
      followupHistory: [],
      followupForm: {
        followupStatus: "accepted",
        followupNote: "",
        ownerUserId: "",
        ownerUserName: "",
      },
    };
  },
  created() {
    this.initCurrentAnonymousUserHash();
    this.getPostDetail();
    this.getCommentList();
    this.checkMobile();
    window.addEventListener("resize", this.checkMobile);
  },
  mounted() {
    // 如果路由参数中有scrollToComment，滚动到评论区
    if (this.$route.query.scrollToComment === "true") {
      this.$nextTick(() => {
        this.scrollToComment();
      });
    }
    
    // 移动端：阻止评论编辑器自动聚焦，避免自动弹出输入法
    if (this.isMobile) {
      this.$nextTick(() => {
        // 延迟执行，确保编辑器已初始化
        setTimeout(() => {
          if (this.$refs.commentEditor && this.$refs.commentEditor.Quill) {
            // 移除编辑器的焦点
            this.$refs.commentEditor.Quill.blur();
            // 阻止编辑器自动聚焦
            const editorElement = this.$refs.commentEditor.$refs.editor;
            if (editorElement) {
              editorElement.setAttribute('readonly', 'true');
              // 当用户点击编辑器时，移除readonly属性
              editorElement.addEventListener('click', () => {
                editorElement.removeAttribute('readonly');
              }, { once: true });
            }
          }
        }, 500);
      });
    }
  },
  watch: {
    $route(to, from) {
      // 如果路由参数中有scrollToComment，滚动到评论区
      if (to.query.scrollToComment === "true") {
        this.$nextTick(() => {
          this.scrollToComment();
        });
      }
    },
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.checkMobile);
  },
  computed: {
    ...mapGetters(["id", "roles", "permissions"]),
    showFollowupSystemNotice() {
      return (
        this.post &&
        (this.post.postType === "suggestion" ||
          this.post.postType === "opinion") &&
        !!this.post.responseDeptId
      );
    },
    isCurrentUserAdmin() {
      const permissions = this.permissions || [];
      const roles = this.roles || [];
      return permissions.includes("*:*:*") || roles.includes("admin");
    },
    canManageFollowup() {
      return this.showFollowupSystemNotice && (this.isDeptContact || this.isCurrentUserAdmin);
    },
    followupSystemNoticeText() {
      if (!this.showFollowupSystemNotice) return "";
      const deptName =
        this.post && this.post.responseDeptName
          ? String(this.post.responseDeptName).trim()
          : "";
      if (deptName) {
        return this.$t("bbs.followupNoticeWithDept", { deptName });
      }
      return this.$t("bbs.followupNoticeWithoutDept");
    },
    followupMeta() {
      if (this.followupLatest) {
        return this.parseFollowupRecordMeta(this.followupLatest);
      }
      return this.parseFollowupMeta(this.post ? this.post.auditReason : "");
    },
  },
  methods: {
    getPostAvatar(post) {
      if (!post || post.isAnonymous === "1") return "";
      if (this.shouldShowAdminTag(post)) {
        return require("@/assets/images/bbs.png");
      }
      return post.avatar || "";
    },
    shouldShowAdminTag(post) {
      return (
        post &&
        (post.isAdminPush === "1" ||
          post.isAdminPush === 1 ||
          post.isAdminPush === true)
      );
    },
    initCurrentAnonymousUserHash() {
      const key = getAnonymousKey();
      if (!key || !this.id) {
        this.currentAnonymousUserHash = "";
        return;
      }
      hashAnonymousKeyForUser(key, this.id)
        .then((keyHash) => {
          this.currentAnonymousUserHash = keyHash;
        })
        .catch(() => {
          this.currentAnonymousUserHash = "";
        });
    },
    isOfficialReply(comment) {
      return (
        comment &&
        (comment.isOfficialReply === true ||
          comment.isOfficialReply === "1" ||
          comment.isOfficialReply === 1)
      );
    },
    canDeleteComment(comment) {
      if (!comment || !comment.userId) return false;
      const commentUserId = String(comment.userId);
      if (comment.isAnonymous === "1") {
        return (
          !!this.currentAnonymousUserHash &&
          commentUserId === String(this.currentAnonymousUserHash)
        );
      }
      return !!this.id && commentUserId === String(this.id);
    },
    handleDeleteComment(comment) {
      this.$confirm(
        this.$t("bbs.confirmDeleteComment"),
        this.$t("bbs.deleteComment"),
        {
          confirmButtonText: this.$t("common.confirm"),
          cancelButtonText: this.$t("common.cancel"),
          type: "warning",
        }
      )
        .then(() => {
          return delByPersonal(comment.commentId);
        })
        .then(() => {
          this.$modal.msgSuccess(this.$t("bbs.deleteCommentSuccess"));
          this.getCommentList();
          this.getPostDetail();
        })
        .catch(() => {});
    },
    /** 匿名提交拦截：确保本地秘钥存在并填充 hash 后再继续提交 */
    ensureAnonymousKeyAndSubmit(payload, submitFn) {
      // 无条件弹出秘钥设置框：每次匿名评论/回复都需要用户确认。
      this.keyNotFoundHint = hasEverSetAnonymousKey();
      this.pendingAnonymousPayload = payload;
      this.pendingAnonymousSubmit = submitFn;
      this.keyDialogVisible = true;
      return Promise.resolve();
    },
    onAnonymousKeyConfirm(key, rememberInBrowser) {
      setAnonymousKey(key, { rememberInBrowser });
      hashAnonymousKeyForUser(key, this.id)
        .then((keyHash) => {
          if (!this.pendingAnonymousPayload || !this.pendingAnonymousSubmit) return;
          this.pendingAnonymousPayload.userId = keyHash;
          this.pendingAnonymousSubmit(this.pendingAnonymousPayload);
        })
        .catch((err) => {
          console.warn("anonymous key hash failed", err);
          this.$modal.msgError(this.$t("bbs.publishFailed"));
        })
        .finally(() => {
          this.pendingAnonymousPayload = null;
          this.pendingAnonymousSubmit = null;
        });
    },
    onAnonymousKeyCancel() {
      this.pendingAnonymousPayload = null;
      this.pendingAnonymousSubmit = null;
    },
    checkMobile() {
      this.isMobile = window.innerWidth < 768;
    },
    getPostDetail() {
      this.loading = true;
      const postId = this.$route.params.postId;
      getPost(postId).then((response) => {
        this.post = response.data;
        this.loadFollowupRecords();
        this.loading = false;

        // 判断当前用户是否是部门接口人
        this.checkDeptContact();
      });
    },
    checkDeptContact() {
      // 如果帖子类型是建议或意见，且有回应部门ID
      if (
        this.post &&
        (this.post.postType === "suggestion" ||
          this.post.postType === "opinion") &&
        this.post.responseDeptId
      ) {
        checkIsDeptContact(this.post.responseDeptId)
          .then((response) => {
            this.isDeptContact = response.data === true;
            this.disableAnonymous = this.isDeptContact;

            // 如果是部门接口人，强制设置为实名评论
            if (this.isDeptContact) {
              this.commentForm.isAnonymous = "0";
              this.replyForm.isAnonymous = "0";
            }
          })
          .catch(() => {
            this.isDeptContact = false;
            this.disableAnonymous = false;
          });
      } else {
        this.isDeptContact = false;
        this.disableAnonymous = false;
      }
    },
    parseFollowupMeta(auditReason) {
      const defaultMeta = {
        code: "accepted",
        label: "待受理",
        note: "部门接口人或管理员可在这里维护处理进展。",
        tagType: "info",
      };
      const text = String(auditReason || "");
      if (!text.startsWith("[FOLLOWUP]")) {
        return defaultMeta;
      }
      const payload = text.substring("[FOLLOWUP]".length);
      const separatorIndex = payload.indexOf("|");
      const code = separatorIndex >= 0 ? payload.substring(0, separatorIndex) : payload;
      const note = separatorIndex >= 0 ? payload.substring(separatorIndex + 1) : "";
      const metaMap = {
        accepted: { label: "已受理", tagType: "info" },
        processing: { label: "处理中", tagType: "warning" },
        feedback: { label: "已反馈", tagType: "success" },
        resolved: { label: "已解决", tagType: "success" },
      };
      const target = metaMap[code] || metaMap.accepted;
      return {
        code,
        label: target.label,
        note: note || defaultMeta.note,
        tagType: target.tagType,
      };
    },
    parseFollowupRecordMeta(record) {
      if (!record) {
        return this.parseFollowupMeta("");
      }
      const metaMap = {
        accepted: { label: "已受理", tagType: "info" },
        processing: { label: "处理中", tagType: "warning" },
        feedback: { label: "已反馈", tagType: "success" },
        resolved: { label: "已解决", tagType: "success" },
      };
      const code = record.followupStatus || "accepted";
      const target = metaMap[code] || metaMap.accepted;
      return {
        code,
        label: target.label,
        note: record.processNote || "部门接口人或管理员可在这里维护处理进展。",
        tagType: target.tagType,
      };
    },
    loadFollowupRecords() {
      if (!this.post || !this.post.postId || !this.showFollowupSystemNotice) {
        this.followupLatest = null;
        this.followupHistory = [];
        this.syncFollowupForm();
        return;
      }

      getPostFollowupLatest(this.post.postId)
        .then((response) => {
          this.followupLatest = response.data || null;
          this.syncFollowupForm();
        })
        .catch(() => {
          this.followupLatest = null;
          this.syncFollowupForm();
        });

      getPostFollowupHistory(this.post.postId)
        .then((response) => {
          this.followupHistory = response.data || [];
        })
        .catch(() => {
          this.followupHistory = [];
        });
    },
    syncFollowupForm() {
      const meta = this.followupLatest
        ? this.parseFollowupRecordMeta(this.followupLatest)
        : this.parseFollowupMeta(this.post ? this.post.auditReason : "");
      this.followupForm.followupStatus = meta.code || "accepted";
      this.followupForm.followupNote = meta.note === "部门接口人或管理员可在这里维护处理进展。" ? "" : meta.note;
      this.followupForm.ownerUserId = this.followupLatest ? (this.followupLatest.ownerUserId || "") : "";
      this.followupForm.ownerUserName = this.followupLatest ? (this.followupLatest.ownerUserName || "") : "";
    },
    handleFollowupUpdate() {
      if (!this.post || !this.post.postId) {
        return;
      }
      this.followupSubmitting = true;
      updatePostFollowup({
        postId: String(this.post.postId),
        followupStatus: this.followupForm.followupStatus,
        followupNote: this.followupForm.followupNote,
        ownerUserId: this.followupForm.ownerUserId,
        ownerUserName: this.followupForm.ownerUserName,
      })
        .then((response) => {
          this.$modal.msgSuccess(response.msg || "闭环状态已更新");
          this.getPostDetail();
        })
        .finally(() => {
          this.followupSubmitting = false;
        });
    },
    getCommentList() {
      const postId = this.$route.params.postId;
      listComment({ postId: postId }).then((response) => {
        // 组织评论树结构
        const comments = response.data || [];
        const commentMap = {};
        const rootComments = [];

        comments.forEach((comment) => {
          comment.children = [];
          commentMap[comment.commentId] = comment;

          if (comment.parentId === 0 || !comment.parentId) {
            rootComments.push(comment);
          } else {
            const parent = commentMap[comment.parentId];
            if (parent) {
              parent.children.push(comment);
            }
          }
        });

        this.commentList = rootComments;
      });
    },
    submitComment() {
      // if (!getToken()) {
      //   this.$modal.msgWarning("请先登录");
      //   return;
      // }

      // 检查富文本内容是否为空（去除HTML标签后检查文本，同时检查是否包含图片）
      const contentText = (this.commentForm.content || "")
        .replace(/<[^>]+>/g, "")
        .trim();
      const hasImage = (this.commentForm.content || "").includes("<img");
      const hasContent = contentText || hasImage || this.commentForm.images;

      if (!hasContent) {
        this.$modal.msgWarning(this.$t("bbs.enterCommentContent"));
        return;
      }

      // 先检测敏感词（去除HTML标签，只检测纯文本）

      // 如果有文字内容，进行敏感词检测
      if (contentText.trim()) {
        // 显示检测中提示
        const loading = this.$loading({
          lock: true,
          text: this.$t("bbs.checkingContent"),
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });

        checkSensitiveWords({ text: contentText })
          .then((response) => {
            const hit = !!response.hit;
            const words = response.words || [];
            if (hit) {
              const message = words.length
                ? `检测到敏感词：${words.join("、")}。请修改后再提交。`
                : "内容命中敏感词，请修改后再提交。";
              this.$alert(message, "风险提示", {
                type: "warning",
                confirmButtonText: "我知道了",
              });
              loading.close();
              return;
            }
            loading.close();
            // 如果检测通过，继续提交
            this.doSubmitComment();
          })
          .catch((error) => {
            loading.close();
            // 检测到敏感词，request拦截器已经显示了Notification错误
            // 这里只需要阻止提交即可
            return;
          });
      } else {
        // 如果只有图片没有文字，直接提交
        this.doSubmitComment();
      }
    },
    doSubmitComment() {
      const data = {
        postId: this.$route.params.postId,
        content: this.commentForm.content || "",
        images: this.commentForm.images || "",
        parentId: this.commentForm.parentId || 0,
        replyUserId: this.commentForm.replyUserId,
        isAnonymous: this.commentForm.isAnonymous || "1",
      };

      const submit = (payload) => {
        addComment(payload)
          .then(() => {
            this.$modal.msgSuccess(this.$t("bbs.commentSuccess"));
            this.commentForm.content = "";
            this.commentForm.images = "";
            this.commentForm.parentId = 0;
            this.commentForm.replyUserId = null;
            this.commentForm.isAnonymous = "1";
            this.replyComment = null;
            this.getCommentList();
            this.getPostDetail();
          })
          .catch((error) => {
            // request拦截器已处理错误提示
            if (
              error &&
              typeof error === "string" &&
              !error.includes("敏感词")
            ) {
              // 可按需补充
            }
          });
      };

      if (data.isAnonymous === "1") {
        this.ensureAnonymousKeyAndSubmit(data, submit);
        return;
      }

      submit(data);
    },
    handleReply(comment) {
      // 如果点击的是当前正在回复的评论，则取消回复
      if (this.replyingCommentId === comment.commentId) {
        this.cancelReply();
        return;
      }
      // 设置正在回复的评论ID
      this.replyingCommentId = comment.commentId;
      // 初始化回复表单
      this.replyForm = {
        content: "",
        isAnonymous: this.disableAnonymous ? "0" : "1",
      };
    },
    cancelReply() {
      this.replyingCommentId = null;
      this.replyForm = {
        content: "",
        isAnonymous: this.disableAnonymous ? "0" : "1",
      };
    },
    submitReply(comment) {
      // 检查富文本内容是否为空（去除HTML标签后检查文本，同时检查是否包含图片）
      const contentText = (this.replyForm.content || "")
        .replace(/<[^>]+>/g, "")
        .trim();
      const hasImage = (this.replyForm.content || "").includes("<img");
      const hasContent = contentText || hasImage;

      if (!hasContent) {
        this.$modal.msgWarning(this.$t("bbs.enterCommentContent"));
        return;
      }

      // 先检测敏感词
      if (contentText) {
        const loading = this.$loading({
          lock: true,
          text: this.$t("bbs.checkingContent"),
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });

        checkSensitiveWords({ text: contentText })
          .then((response) => {
            const hit = !!response.hit;
            const words = response.words || [];
            if (hit) {
              const message = words.length
                ? `检测到敏感词：${words.join("、")}。请修改后再提交。`
                : "内容命中敏感词，请修改后再提交。";
              this.$alert(message, "风险提示", {
                type: "warning",
                confirmButtonText: "我知道了",
              });
              loading.close();
              return;
            }
            loading.close();
            this.doSubmitReply(comment);
          })
          .catch((error) => {
            loading.close();
            return;
          });
      } else {
        this.doSubmitReply(comment);
      }
    },
    doSubmitReply(comment) {
      const data = {
        postId: this.$route.params.postId,
        content: this.replyForm.content || "",
        images: "",
        parentId: comment.commentId,
        replyUserId: comment.userId,
        isAnonymous: this.replyForm.isAnonymous || "1",
      };

      const submit = (payload) => {
        addComment(payload)
          .then(() => {
            this.$modal.msgSuccess(this.$t("bbs.replySuccess"));
            this.cancelReply();
            this.getCommentList();
            this.getPostDetail();
          })
          .catch(() => {});
      };

      if (data.isAnonymous === "1") {
        this.ensureAnonymousKeyAndSubmit(data, submit);
        return;
      }

      submit(data);
    },
    handleLike() {
      if (!getToken()) {
        this.$modal.msgWarning(this.$t("bbs.pleaseLogin"));
        return;
      }
      // 添加点击高亮效果
      this.isLikeClicked = true;
      setTimeout(() => {
        this.isLikeClicked = false;
      }, 300);

      toggleLike(this.post.postId).then((response) => {
        this.getPostDetail();
      });
    },
    handleCollect() {
      if (!getToken()) {
        this.$modal.msgWarning(this.$t("bbs.pleaseLogin"));
        return;
      }
      toggleCollect(this.post.postId).then((response) => {
        this.getPostDetail();
      });
    },
    scrollToComment() {
      // 滚动到评论区
      this.$nextTick(() => {
        const commentSection = document.querySelector(".comment-section");
        if (commentSection) {
          commentSection.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      });
    },
    handleCommentLike(comment) {
      if (!getToken()) {
        this.$modal.msgWarning(this.$t("bbs.pleaseLogin"));
        return;
      }
      toggleCommentLike(comment.commentId).then((response) => {
        this.getCommentList();
      });
    },
    handleCommentDislike(comment) {
      if (!getToken()) {
        this.$modal.msgWarning(this.$t("bbs.pleaseLogin"));
        return;
      }
      toggleCommentDislike(comment.commentId).then(() => {
        this.getCommentList();
      });
    },
    // 获取评论图片列表
    getCommentImages(images) {
      if (!images) return [];
      return images.split(",").filter((img) => img.trim());
    },
    // 获取图片完整URL
    getImageUrl(image) {
      if (!image) return "";
      if (image.startsWith("http://") || image.startsWith("https://")) {
        return image;
      }
      return process.env.VUE_APP_BASE_API + image;
    },
    // 预览图片
    previewImage(image) {
      this.previewImageUrl = this.getImageUrl(image);
      this.previewImageVisible = true;
    },
    handleCommentImageClick(event) {
      // 如果点击的是图片，则预览图片
      if (event.target.tagName === "IMG") {
        this.previewImageUrl = event.target.src;
        this.previewImageVisible = true;
      }
    },
    getPostTypeName(postType) {
      const typeMap = {
        share: this.$t("bbs.postTypeShare"),
        suggestion: this.$t("bbs.postTypeSuggestion"),
        opinion: this.$t("bbs.postTypeOpinion"),
      };
      return typeMap[postType] || "";
    },
    getPostTypeTagType(postType) {
      const typeMap = {
        share: "success",
        suggestion: "warning",
        opinion: "danger",
      };
      return typeMap[postType] || "primary";
    },
  },
};
</script>

<style scoped lang="scss">
.bbs-post-detail {
  background: white;
  border-radius: 4px;
  padding: 30px;
  min-height: 500px;
  width: 100%;
  box-sizing: border-box;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .bbs-post-detail {
    padding: 10px;
  }

  .followup-status-header,
  .followup-form {
    display: flex;
    flex-direction: column;
    align-items: stretch;
  }

  .followup-history-item {
    grid-template-columns: 1fr;
  }

  .post-detail {
    padding: 15px !important;
  }

  .post-title {
    font-size: 20px !important;
  }

  .post-body {
    font-size: 14px;
    line-height: 1.6;
  }

  .post-actions {
    display: flex;
    // flex-direction: column;
    gap: 10px;
  }

  .post-actions .el-button {
    width: 100%;
  }

  .comment-section {
    margin-top: 20px;
  }

  .comment-form {
    margin-bottom: 20px;
  }

  .comment-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .comment-content {
    margin-left: 0;
    margin-top: 10px;
    width: 100%;
  }

  .comment-actions {
    margin-top: 10px;
  }

  .sub-comments {
    margin-top: 10px;
    padding-left: 10px;
  }

  .sub-comment-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .sub-comment-content {
    margin-left: 0;
    margin-top: 5px;
  }
}

.post-detail {
  background: white;
  border-radius: 4px;
  padding: 30px;
}

.post-content-card {
  margin-bottom: 30px;
}

.post-header {
  margin-bottom: 20px;
}

.post-title {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 10px 0;
  .el-tag {
    font-size: 11px;
  }
}

.post-tags-inline {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.post-meta {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.meta-info {
  margin-left: 15px;
}

.author {
  font-weight: bold;
  margin-bottom: 5px;
}

.meta-detail {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.meta-detail span {
  margin-right: 15px;
}

.like-icon-small {
  width: 12px;
  height: 12px;
  vertical-align: middle;
  margin-right: 3px;
  transition: all 0.3s ease;
}

.like-icon-small.liked-icon {
  filter: brightness(1.3) saturate(1.8);
}

.post-body {
  line-height: 1.8;
  color: #333;
  margin-bottom: 30px;
  min-height: 200px;
  word-break: break-word;
}

.post-system-notice {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 18px;
  padding: 12px 14px;
  border-radius: 4px;
  border: 1px solid #d9ecff;
  background: #f4f9ff;
}

.post-system-notice .notice-icon {
  margin-top: 2px;
  color: #409eff;
  font-size: 16px;
}

.post-system-notice .notice-content {
  flex: 1;
  min-width: 0;
}

.post-system-notice .notice-title {
  font-size: 13px;
  line-height: 1.4;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 2px;
}

.post-system-notice .notice-text {
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}

.followup-status-card {
  margin-bottom: 18px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.followup-status-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.followup-status-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.followup-status-desc,
.followup-note {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}

.followup-form {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 180px 1fr 220px auto;
  gap: 10px;
  align-items: center;
}

.followup-history {
  margin-top: 12px;
  border-top: 1px dashed #ebeef5;
  padding-top: 10px;
}

.followup-history-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.followup-history-item {
  display: grid;
  grid-template-columns: 80px 120px 140px 1fr;
  gap: 8px;
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
  padding: 4px 0;
}

.history-status {
  color: #409eff;
}

// 使用深度选择器确保样式能应用到 v-html 渲染的内容
::v-deep .post-body img {
  width: 100% !important;
  height: 500px !important;
  max-width: 100% !important;
  max-height: 500px !important;
  min-width: 100% !important;
  min-height: 500px !important;
  border-radius: 4px;
  margin: 8px 0;
  cursor: pointer;
  object-fit: contain;
  transition: all 0.3s;
  display: block;
}

::v-deep .post-body img:hover {
  opacity: 0.8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.post-actions {
  padding-top: 20px;
  border-top: 1px solid #eee;
  .el-button {
    border: none;
  }
}
::v-deep button .like-icon {
  vertical-align: middle;
  width: 11px;
  height: 12px;
}
.post-actions .like-icon {
  width: 17px;
  height: 17px;
  margin-right: 5px;
  transition: all 0.3s ease;
  vertical-align: middle;
  display: inline-block;
  width: 11px;
  height: 12px;
}

.post-actions .el-button .like-icon {
  width: 11px;
  height: 12px;
  filter: brightness(1);
  opacity: 0.8;
}

.post-actions .el-button:hover .like-icon {
  width: 11px;
  height: 12px;
  opacity: 1;
  transform: scale(1.05);
}

.post-actions .el-button .liked-icon {
  filter: brightness(1.3) saturate(1.8);
  transform: scale(1.15);
  opacity: 1;
}

.post-actions .liked-button .like-icon {
  width: 11px;
  height: 12px;
  vertical-align: text-top;
  filter: brightness(0) invert(1);
  opacity: 1;
}

.post-actions .like-button-clicked .like-icon {
  width: 11px;
  height: 12px;
  animation: likePulse 0.4s ease-in-out;
  filter: brightness(1.8) saturate(2.5);
  opacity: 1;
}

@keyframes likePulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.4);
  }
  100% {
    transform: scale(1.15);
  }
}

.comment-section {
  margin-top: 30px;
}

.comment-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 30px;
}

.comment-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.comment-editor-wrapper .ql-toolbar {
  border-top: none;
  border-left: none;
  border-right: none;
  border-bottom: 1px solid #dcdfe6;
}

.comment-editor-wrapper .ql-container {
  border: none;
  font-size: 14px;
}

.comment-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.comment-editor-wrapper .ql-toolbar {
  border-top: none;
  border-left: none;
  border-right: none;
  border-bottom: 1px solid #dcdfe6;
}

.comment-editor-wrapper .ql-container {
  border: none;
  font-size: 14px;
}

.comment-form-options {
  margin-top: 10px;
  margin-bottom: 10px;
}

.comment-form .comment-actions {
  margin-top: 10px;
  text-align: right;
}

.comment-item {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  border-radius: 6px;
}

.comment-item.official-reply-item,
.sub-comment-item.official-reply-item {
  background: #fff5f5;
  padding: 12px;
}

.comment-avatar-wrapper {
  position: relative;
  display: inline-flex;
}

.sub-avatar-wrapper {
  margin-top: 2px;
}

.official-reply-badge {
  position: absolute;
  top: -19px;
  left: 20px;
  background: #f56c6c;
  color: #fff;
  border-radius: 12px;
  padding: 0 8px;
  font-size: 10px;
  line-height: 20px;
  white-space: nowrap;
  z-index: 1;
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.35);
}

.official-reply-badge::after {
  content: "";
  position: absolute;
  left: 7px;
  bottom: -2px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  transform: rotate(45deg);
}

.comment-content {
  flex: 1;
  margin-left: 15px;
}

.comment-header {
  margin-bottom: 10px;
}

.comment-author {
  font-weight: bold;
  margin-right: 10px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  line-height: 1.6;
  margin-bottom: 10px;
  word-break: break-word;
}

// 使用深度选择器确保样式能应用到 v-html 渲染的内容
::v-deep .comment-text img {
  width: 200px !important;
  height: 200px !important;
  max-width: 200px !important;
  max-height: 200px !important;
  min-width: 200px !important;
  min-height: 200px !important;
  border-radius: 4px;
  margin: 8px 0;
  cursor: pointer;
  object-fit: contain;
  transition: all 0.3s;
  display: block;
}

::v-deep .comment-text img:hover {
  opacity: 0.8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.comment-text p {
  margin: 8px 0;
}

.comment-text p:first-child {
  margin-top: 0;
}

.comment-text p:last-child {
  margin-bottom: 0;
}

.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  margin-bottom: 10px;
}

.comment-image-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #eee;
  transition: all 0.3s;
}

.comment-image-item:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.comment-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sub-comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 5px;
}

.sub-comment-image-item {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #eee;
  transition: all 0.3s;
}

.sub-comment-image-item:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.sub-comment-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-actions .el-button {
  margin-right: 15px;
}

.comment-actions .liked {
  color: #409eff;
}

.comment-actions .disliked {
  color: #f56c6c;
}

.comment-content .comment-actions {
  display: flex;
  align-items: center;
}

.comment-delete-btn {
  margin-left: auto;
  color: #f56c6c;
}

.reply-form {
  margin-top: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.reply-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: white;
}

.reply-editor-wrapper .ql-toolbar {
  border-top: none;
  border-left: none;
  border-right: none;
  border-bottom: 1px solid #dcdfe6;
}

.reply-editor-wrapper .ql-container {
  border: none;
  font-size: 14px;
}

.reply-form-options {
  margin-top: 10px;
  margin-bottom: 10px;
}

.reply-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.sub-comments {
  margin-top: 15px;
  padding-left: 15px;
  border-left: 2px solid #eee;
}

.sub-comment-item {
  display: flex;
  margin-bottom: 10px;
}

.sub-comment-content {
  margin-left: 10px;
  font-size: 14px;
}

.sub-comment-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 4px;
  gap: 0 8px;
}

.sub-comment-time {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}

.sub-comment-text {
  color: #666;
  font-size: 13px;
  margin-top: 5px;
  word-break: break-word;
}

// 使用深度选择器确保样式能应用到 v-html 渲染的内容
::v-deep .sub-comment-text img {
  width: 150px !important;
  height: 150px !important;
  max-width: 150px !important;
  max-height: 150px !important;
  min-width: 150px !important;
  min-height: 150px !important;
  border-radius: 4px;
  margin: 8px 0;
  cursor: pointer;
  object-fit: contain;
  transition: all 0.3s;
  display: block;
}

::v-deep .sub-comment-text img:hover {
  opacity: 0.8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.sub-comment-text p {
  margin: 4px 0;
}

.sub-comment-text p:first-child {
  margin-top: 0;
}

.sub-comment-text p:last-child {
  margin-bottom: 0;
}

.sub-comment-author {
  font-weight: bold;
  margin-right: 5px;
}

.reply-to {
  color: #409eff;
  margin-right: 5px;
}

.post-sidebar {
  background: white;
  border-radius: 4px;
  padding: 20px;
}

.author-card {
  text-align: center;
}

.author-name {
  margin: 15px 0;
  font-weight: bold;
}
</style>
