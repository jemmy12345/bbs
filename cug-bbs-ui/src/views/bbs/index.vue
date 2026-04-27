<template>
  <div class="bbs-content">
    <!-- 分类标题和统计 -->
    <div class="category-header" v-if="activeCategory">
      <h2 class="category-title">{{ currentCategoryName }}</h2>
      <div class="category-count">
        {{ $t("bbs.totalPosts", { count: total }) }}
      </div>
    </div>

    <!-- 移动端分类选择器 -->
    <!-- <div class="mobile-category-selector" v-if="isMobile">
      <el-select v-model="activeCategory" placeholder="选择分类" @change="handleCategoryChange" style="width: 100%;" size="small">
        <el-option label="全部" :value="null"></el-option>
        <el-option
          v-for="category in categoryList"
          :key="category.categoryId"
          :label="category.categoryName"
          :value="category.categoryId"
        />
      </el-select>
    </div> -->

    <!-- 排序选项 -->
    <div class="sort-options" v-if="!isMobile">
      <el-radio-group v-model="sortType" @change="handleQuery" size="medium">
        <el-radio-button label="latest">{{
          $t("bbs.latestPosts")
        }}</el-radio-button>
        <el-radio-button label="hot">{{ $t("bbs.hotPosts") }}</el-radio-button>
        <!-- <el-radio-button label="essence">精华</el-radio-button> -->
      </el-radio-group>
    </div>

    <div v-if="hotPostList.length || radarTopics.length || pointRankList.length" class="forum-intelligence">
      <!-- 全站热议榜 -->
      <div class="intelligence-card hot-card">
        <div class="intelligence-title">🔥 全站热议榜</div>
        <div v-if="hotPostList.length" class="hot-list">
          <div
            v-for="(post, index) in hotPostList.slice(0, 5)"
            :key="post.postId"
            class="hot-item"
            @click="handlePostClick(post.postId)"
          >
            <span class="hot-rank">{{ index + 1 }}</span>
            <div class="hot-body">
              <div class="hot-title">{{ truncateTitle(post.title, 30) }}</div>
              <div class="hot-meta">{{ post.viewCount || 0 }} 浏览 · {{ post.commentCount || 0 }} 评论</div>
            </div>
          </div>
        </div>
        <div v-else class="intelligence-empty">暂无热议帖子</div>
      </div>

      <!-- 话题雷达 -->
      <div class="intelligence-card radar-card">
        <div class="intelligence-title">📡 话题雷达</div>
        <div v-if="radarTopics.length" class="topic-chip-list">
          <span
            v-for="(topic, index) in radarTopics"
            :key="index"
            class="topic-chip"
            @click="handleSearch(topic.word)"
          >
            <span>{{ topic.word }}</span>
            <span class="topic-count">{{ topic.count }}</span>
          </span>
        </div>
        <div v-else class="intelligence-empty">暂无话题数据</div>
      </div>

      <!-- 积分榜 Top5 -->
      <div class="intelligence-card point-rank-card">
        <div class="intelligence-title">🏆 积分榜 TOP 5</div>
        <div class="point-rank-subtitle" style="margin-bottom:12px;">发布帖子/评论可累计积分</div>
        <div v-if="pointRankList.length" class="point-rank-list">
          <div
            v-for="(item, index) in pointRankList"
            :key="item.userId || index"
            class="point-rank-item"
          >
            <div class="point-rank-left">
              <span class="point-rank-no">{{ index + 1 }}</span>
              <el-avatar :size="32" :src="item.avatar">
                {{ (item.nickName || '用').slice(0, 1) }}
              </el-avatar>
              <span class="point-rank-name">{{ item.nickName || '未知用户' }}</span>
            </div>
            <span class="point-rank-score">{{ item.points || 0 }} 分</span>
          </div>
        </div>
        <div v-else class="intelligence-empty">暂无积分数据</div>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list" v-loading="loading">
      <div
        v-for="post in postList"
        :key="post.postId"
        class="post-item"
        @click="handlePostClick(post.postId)"
      >
        <div class="post-header">
          <!-- <el-tag 
            :type="getCategoryTagType(post.categoryName)" 
            size="small"
            class="category-tag"
          >
            {{ post.categoryName }}
          </el-tag> -->

          <!-- <el-tag v-if="post.isEssence === '1'" type="warning" size="mini">精华</el-tag> -->
        </div>
        <div class="post-content post-wrapper">
          <div class="post-title-wrapper">
            <h3 class="post-title">
              <el-tag v-if="post.isTop === '1'" type="danger" size="mini"
                >{{ $t("bbs.top") }}</el-tag
              >
              <el-tag
                v-if="post.postType"
                :type="getPostTypeTagType(post.postType)"
                size="mini"
                style="margin-right: 5px"
              >
                {{ getPostTypeName(post.postType) }}
              </el-tag>

              {{ truncateTitle(post.title) }}
            </h3>
            <div v-if="post.tags && post.tags.length" class="post-tag-list">
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
            <span
              v-if="
                (post.postType === 'suggestion' ||
                  post.postType === 'opinion') &&
                post.responseDeptName &&
                post.responseDeptName.trim()
              "
              type="info"
              size="small"
              style="margin-right: 5px; font-size: 12px; color: #909399"
            >
              <span>{{ $t("bbs.responseDeptPrefix") }}</span>
              {{ post.responseDeptName }}
            </span>
          </div>
          <div class="post-info-column">
            <div class="post-meta">
              <span class="author">
                <span v-if="post.isAnonymous === '1'">{{
                  $t("bbs.anonymousUser")
                }}</span>
                <span v-else>
                  <span v-if="!shouldShowAdminTag(post)">{{
                    post.nickName || $t("bbs.unknownUser")
                  }}</span>
                  <el-tag
                    v-if="shouldShowAdminTag(post)"
                    size="mini"
                    type="danger"
                    style="margin-left: 5px"
                    >{{ $t("bbs.admin") }}</el-tag
                  >
                </span>
              </span>
              <el-tag
                v-if="post.isAnonymous === '1'"
                size="mini"
                type="info"
                style="margin-left: 5px"
                >{{ $t("bbs.anonymous") }}</el-tag
              >
              <span class="divider">·</span>
              <span class="time">{{
                parseTime(post.createTime, "{y}-{m}-{d}")
              }}</span>
            </div>
            <div class="post-stats">
              <span class="stat-item">
                <i class="el-icon-view"></i> {{ post.viewCount }}
              </span>
              <span class="stat-item">
                <img
                  :src="require('@/assets/images/dianzan.svg')"
                  :alt="$t('bbs.like')"
                  class="like-icon"
                  :class="{ 'liked-icon': post.isLiked }"
                />
                {{ post.likeCount }}
              </span>
              <span class="stat-item">
                <i class="el-icon-chat-line-square"></i>
                {{ post.commentCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="postList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-document"></i>
        <p>{{ $t("bbs.noPosts") }}</p>
      </div>
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="handlePagination"
    />

    <!-- 发布帖子对话框 -->
    <el-dialog
      :title="$t('bbs.publishPost')"
      :visible.sync="dialogVisible"
      :width="isMobile ? '95%' : '850px'"
      :fullscreen="isMobile"
      :close-on-click-modal="false"
      style="max-height: 95vh; overflow-y: auto"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" :label-position="isMobile?'top':'right'">
        <!-- <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="category in categoryList"
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item> -->
        <el-form-item :label="$t('bbs.postTypeLabel')" prop="postType">
          <el-select
            v-model="form.postType"
            :placeholder="$t('bbs.selectPostType')"
            style="width: 100%"
          >
            <el-option :label="$t('bbs.postTypeShare')" value="share" />
            <el-option :label="$t('bbs.postTypeSuggestion')" value="suggestion" />
            <el-option :label="$t('bbs.postTypeOpinion')" value="opinion" />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('bbs.responseDeptLabel')"
          prop="responseDeptId"
          v-if="form.postType === 'suggestion' || form.postType === 'opinion'"
        >
          <treeselect
            v-model="form.responseDeptId"
            :options="deptList"
            :normalizer="normalizer"
            :placeholder="$t('bbs.selectDept')"
            :disable-branch-nodes="true"
            :default-expand-level="Infinity"
          />

          <!-- <el-select
            v-model="form.responseDeptId"
            placeholder="请选择回应部门"
            style="width: 100%"
            filterable
            @change="handleDeptChange"
          >
            <el-option
              v-for="dept in deptList"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            />
          </el-select> -->
        </el-form-item>
        <el-form-item :label="$t('bbs.postTitle')" prop="title">
          <el-input
            v-model="form.title"
            :placeholder="$t('bbs.postTitle')"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            clearable
            style="width: 100%"
            placeholder="可输入标签并回车，例如：流程优化、知识库、体验改进"
          >
            <el-option
              v-for="tag in tagOptions"
              :key="tag.tagId || tag.tagName"
              :label="tag.tagName"
              :value="tag.tagName"
            />
          </el-select>
          <div style="font-size: 12px; color: #999; margin-top: 5px">最多建议 5 个标签，便于形成专题沉淀。</div>
        </el-form-item>
        <el-form-item label="AI助写">
          <div class="ai-assist-row">
            <el-input
              v-model="aiKeywords"
              placeholder="请输入关键词，例如：食堂改进建议、流程优化、技术分享"
              clearable
            />
            <el-button type="success" :loading="aiGenerating" @click="handleAiGenerate">AI生成内容</el-button>
          </div>
          <div style="font-size: 12px; color: #999; margin-top: 6px;">AI生成后你仍可继续编辑优化。</div>
        </el-form-item>
        <el-form-item label="灵感胶囊">
          <div class="inspiration-panel">
            <div class="assist-section">
              <div class="assist-section-title">热点关键词</div>
              <div class="capsule-list">
                <button
                  v-for="topic in inspirationTopics"
                  :key="topic.word"
                  type="button"
                  class="capsule-button"
                  @click="applyTopicToAi(topic)"
                >
                  {{ topic.word }}
                </button>
              </div>
            </div>
            <div class="assist-section">
              <div class="assist-section-title">结构模板</div>
              <div class="capsule-list">
                <button
                  v-for="template in templateOptions"
                  :key="template.type"
                  type="button"
                  class="capsule-button capsule-button-ghost"
                  @click="insertTemplate(template.type)"
                >
                  {{ template.label }}
                </button>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="发布质量">
          <div class="quality-panel">
            <div class="quality-score-panel">
              <el-progress
                type="dashboard"
                :percentage="publishAdvisor.score"
                :color="publishAdvisor.color"
                :width="isMobile ? 110 : 128"
              />
              <div class="quality-level">{{ publishAdvisor.level }}</div>
              <div class="quality-meta">
                预计阅读 {{ publishAdvisor.minutes }} 分钟 · 正文 {{ publishAdvisor.contentLength }} 字
              </div>
            </div>
            <div class="quality-detail-panel">
              <div class="quality-block">
                <div class="quality-block-title">当前亮点</div>
                <div class="quality-tag-list">
                  <el-tag
                    v-for="item in publishAdvisor.highlights"
                    :key="item"
                    size="mini"
                    type="success"
                  >
                    {{ item }}
                  </el-tag>
                </div>
              </div>
              <div class="quality-block">
                <div class="quality-block-title">建议补强</div>
                <div class="quality-suggestion-list">
                  <div
                    v-for="item in publishAdvisor.tips"
                    :key="item"
                    class="quality-suggestion-item"
                  >
                    {{ item }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('bbs.postContent')" prop="content">
          <editor
            v-model="form.content"
            :min-height="isMobile?450:350"
            :placeholder="$t('bbs.forumNotice')"
          />
        </el-form-item>
        <el-form-item :label="$t('bbs.publishMethod')">
          <el-radio-group v-model="form.isAnonymous">
            <el-radio label="1">{{ $t("bbs.anonymousPublish") }}</el-radio>
            <el-radio label="0">{{ $t("bbs.realNamePublish") }}</el-radio>
          </el-radio-group>
          <div style="font-size: 12px; color: #999; margin-top: 5px">
            {{ $t("bbs.anonymousTip") }}
          </div>
        </el-form-item>
        <el-form-item v-if="isCurrentUserAdmin">
          <el-checkbox
            v-model="form.isAdminPush"
            true-label="1"
            false-label="0"
          >
            {{ $t("bbs.showAsAdminPost") }}
          </el-checkbox>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{
          $t("common.cancel")
        }}</el-button>
        <el-button v-if="form.isAnonymous === '0'" @click="saveDraft">{{
          $t("bbs.saveDraft")
        }}</el-button>
        <el-button type="primary" @click="submitForm">{{
          $t("common.confirm")
        }}</el-button>
      </div>
    </el-dialog>

    <!-- 匿名秘钥设置对话框（匿名发帖且本地无秘钥时弹出） -->
    <AnonymousKeyDialog
      :visible.sync="keyDialogVisible"
      :key-not-found-hint="keyNotFoundHint"
      @confirm="onAnonymousKeyConfirm"
      @cancel="onAnonymousKeyCancel"
    />
  </div>
</template>

<script>
import { listPost, addPost, getPost, updatePost, generatePostByAi, getHotPosts, getHotTags, getPointRank } from "@/api/bbs/post";
import { listCategory } from "@/api/bbs/category";
import { checkSensitiveWords } from "@/api/bbs/sensitive";
import { listDeptForPost, listDept } from "@/api/system/dept";
import { mapGetters } from "vuex";
import {
  setAnonymousKey,
  hasEverSetAnonymousKey,
  hashAnonymousKeyForUser,
} from "@/utils/anonymousKey";
import Editor from "@/components/Editor";
import AnonymousKeyDialog from "@/components/AnonymousKeyDialog";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "BbsIndex",
  components: {
    Editor,
    Treeselect,
    AnonymousKeyDialog,
  },
  data() {
    return {
      loading: false,
      postList: [],
      categoryList: [],
      deptList: [],
      total: 0,
      activeCategory: null,
      sortType: "latest",
      currentNav: "home",
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        categoryId: null,
      },
      dialogVisible: false,
      form: {
        postId: null,
        categoryId: null,
        title: "",
        content: "",
        isAnonymous: "1",
        isAdminPush: "0",
        postType: "",
        responseDeptId: null,
        responseDeptName: null,
        tags: [],
      },
      isMobile: false,
      keyDialogVisible: false,
      keyNotFoundHint: false,
      pendingPostData: null,
      aiKeywords: "",
      aiGenerating: false,
      hotPostList: [],
      pointRankList: [],
      tagOptions: [],
    };
  },
  computed: {
    ...mapGetters(["id", "permissions", "roles"]),
    isCurrentUserAdmin() {
      const permissions = this.permissions || [];
      const roles = this.roles || [];
      return permissions.includes("*:*:*") || roles.includes("admin");
    },
    currentCategoryName() {
      if (!this.activeCategory) return "";
      const category = this.categoryList.find(
        (c) => c.categoryId === this.activeCategory
      );
      return category ? category.categoryName : "";
    },
    radarTopics() {
      return this.extractTopicKeywords([
        ...(this.hotPostList || []),
        ...(this.postList || []),
      ]).slice(0, 8);
    },
    inspirationTopics() {
      return this.radarTopics.slice(0, 5);
    },
    templateOptions() {
      return [
        { type: "share", label: "经验分享模板" },
        { type: "suggestion", label: "建议闭环模板" },
        { type: "opinion", label: "观点讨论模板" },
      ];
    },
    publishAdvisor() {
      const titleText = this.extractPlainText(this.form.title);
      const contentText = this.extractPlainText(this.form.content);
      const contentLength = contentText.length;
      const suggestions = [];
      const highlights = [];
      let score = 0;

      if (titleText.length >= 8 && titleText.length <= 40) {
        score += 25;
        highlights.push("标题长度合适");
      } else {
        suggestions.push("标题建议控制在 8 到 40 个字，便于阅读与检索。");
      }

      if (this.form.postType) {
        score += 15;
        highlights.push("帖子类型已明确");
      } else {
        suggestions.push("先选择帖子类型，系统才能给出更准确的治理与协同建议。");
      }

      if (contentLength >= 200) {
        score += 35;
        highlights.push("正文信息量充足");
      } else if (contentLength >= 80) {
        score += 25;
        highlights.push("正文已具备基本表达");
      } else if (contentLength >= 30) {
        score += 15;
      } else {
        suggestions.push("正文偏短，建议补充背景、现状、问题和期望结果。");
      }

      if (
        this.form.postType === "share" ||
        ((this.form.postType === "suggestion" || this.form.postType === "opinion") &&
          this.form.responseDeptId)
      ) {
        score += 15;
        highlights.push("协同对象已明确");
      } else if (
        this.form.postType === "suggestion" ||
        this.form.postType === "opinion"
      ) {
        suggestions.push("建议/意见类帖子最好指定回应部门，便于闭环处理。");
      }

      if (/(问题|建议|方案|收益|背景|现状|一、|二、|1\.|2\.)/.test(contentText)) {
        score += 10;
        highlights.push("正文结构清晰");
      } else {
        suggestions.push("可以按“背景 - 问题 - 建议 - 预期收益”结构组织内容。");
      }

      const safeScore = Math.min(100, score);
      let level = "待完善";
      let color = "#f56c6c";
      if (safeScore >= 85) {
        level = "可直接发布";
        color = "#67c23a";
      } else if (safeScore >= 65) {
        level = "质量较好";
        color = "#409eff";
      } else if (safeScore >= 45) {
        level = "建议优化";
        color = "#e6a23c";
      }

      if (!highlights.length) {
        highlights.push("已开启 AI 助写，可先生成初稿再细化。");
      }
      if (!suggestions.length) {
        suggestions.push("内容已较完整，建议再检查措辞是否具体、可执行。");
      }

      return {
        score: safeScore,
        level,
        color,
        minutes: Math.max(1, Math.ceil(Math.max(contentLength, 1) / 260)),
        contentLength,
        highlights,
        tips: suggestions,
      };
    },
    rules() {
      const rules = {
        postType: [
          { required: true, message: this.$t("bbs.selectPostType"), trigger: "change" },
        ],
        title: [
          {
            required: true,
            message: this.$t("bbs.postTitle"),
            trigger: "blur",
          },
        ],
        content: [
          {
            required: true,
            message: this.$t("bbs.postContent"),
            trigger: "blur",
          },
        ],
      };
      // 如果选择了建议或意见，回应部门必填
      if (
        this.form.postType === "suggestion" ||
        this.form.postType === "opinion"
      ) {
        rules.responseDeptId = [
          { required: true, message: this.$t("bbs.selectResponseDept"), trigger: "change" },
        ];
      }
      return rules;
    },
  },
  created() {
    this.getCategoryList();
    this.getDeptList();
    this.getHotPostList();
    this.getPointRankList();
    this.getTagOptions();
    this.checkMobile();
    window.addEventListener("resize", this.checkMobile);

    // 监听路由参数变化
    if (this.$route.query.categoryId) {
      this.activeCategory = parseInt(this.$route.query.categoryId);
    }
    if (this.$route.query.nav) {
      this.currentNav = this.$route.query.nav;
    }
    if (this.$route.query.keyword) {
      this.queryParams.title = this.$route.query.keyword;
    }
    // 恢复页数
    if (this.$route.query.page) {
      this.queryParams.pageNum = parseInt(this.$route.query.page) || 1;
    }

    // 检查是否需要打开发布对话框
    if (this.$route.query.publish === "true") {
      this.$nextTick(() => {
        // 检查是否有草稿ID
        if (this.$route.query.draftId) {
          this.loadDraft(this.$route.query.draftId);
        } else {
          this.handleAdd();
        }
        // 清除query参数
        this.$router.replace({
          path: this.$route.path,
          query: {
            ...this.$route.query,
            publish: undefined,
            draftId: undefined,
          },
        });
      });
    }

    this.getList();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.checkMobile);
  },
  watch: {
    "form.postType"(newVal, oldVal) {
      // 当帖子类型改变时，如果不是建议或意见，清空回应部门
      if (newVal !== "suggestion" && newVal !== "opinion") {
        this.form.responseDeptId = null;
        this.form.responseDeptName = null;
      }
      // 清除验证状态
      if (this.$refs.form) {
        this.$refs.form.clearValidate("responseDeptId");
      }
    },
    $route(to) {
      if (to.query.categoryId) {
        this.activeCategory = parseInt(to.query.categoryId);
      } else {
        this.activeCategory = null;
      }
      if (to.query.nav) {
        this.currentNav = to.query.nav;
      } else {
        this.currentNav = "home";
      }
      if (to.query.keyword) {
        this.queryParams.title = to.query.keyword;
      } else {
        this.queryParams.title = null;
      }
      // 恢复页数
      if (to.query.page) {
        this.queryParams.pageNum = parseInt(to.query.page) || 1;
      }
      // 检查是否需要打开发布对话框
      if (to.query.publish === "true") {
        this.$nextTick(() => {
          this.handleAdd();
          // 清除query参数，避免刷新时重复触发
          this.$router.replace({
            path: to.path,
            query: {
              ...to.query,
              publish: undefined,
            },
          });
        });
      }
      this.getList();
    },
  },
  methods: {
    normalizer(node) {
      // 基础映射
      const normalized = {
        id: node.deptId,
        label: node.deptName,
        children: node.children,
      };

      return normalized;
    },
    removeEmptyChildren(nodes) {
      return nodes.map((node) => {
        const newNode = { ...node };
        newNode.authorityValue = node.id;
        if (newNode.children) {
          // 递归处理子节点
          newNode.children = this.removeEmptyChildren(newNode.children);
          // 如果子节点为空数组，则删除 `children` 字段
          if (newNode.children.length === 0) {
            delete newNode.children;
          }
        }
        return newNode;
      });
    },
    // 根据 isAdminPush 决定是否显示管理员标识
    shouldShowAdminTag(post) {
      return (
        post &&
        (post.isAdminPush === "1" ||
          post.isAdminPush === 1 ||
          post.isAdminPush === true)
      );
    },
    checkMobile() {
      this.isMobile = window.innerWidth < 768;
    },
    getList() {
      this.loading = true;
      this.queryParams.categoryId = this.activeCategory;

      // 根据导航切换排序
      if (this.currentNav === "hot") {
        this.sortType = "hot";
      } else if (this.currentNav === "latest") {
        this.sortType = "latest";
      }

      // 普通用户只能看到已发布的文章（status=0）
      listPost({ ...this.queryParams, sortType: this.sortType, status: "0" })
        .then((response) => {
          this.postList = response.rows || response.data || [];
          this.total = response.total || 0;
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    getHotPostList() {
      getHotPosts(5)
        .then((response) => {
          this.hotPostList = response.data || [];
        })
        .catch(() => {
          this.hotPostList = [];
        });
    },
    getPointRankList() {
      getPointRank(5)
        .then((response) => {
          this.pointRankList = response.data || [];
        })
        .catch(() => {
          this.pointRankList = [];
        });
    },
    getTagOptions() {
      getHotTags(50)
        .then((response) => {
          this.tagOptions = response.data || [];
        })
        .catch(() => {
          this.tagOptions = [];
        });
    },
    handlePagination(pagination) {
      // 更新页数和每页条数
      if (pagination.page !== undefined) {
        this.queryParams.pageNum = pagination.page;
      }
      if (pagination.limit !== undefined) {
        this.queryParams.pageSize = pagination.limit;
      }
      // 更新路由参数，保存当前页数
      this.$router
        .push({
          path: "/index",
          query: {
            ...this.$route.query,
            page:
              this.queryParams.pageNum > 1
                ? this.queryParams.pageNum
                : undefined,
            categoryId: this.activeCategory || undefined,
            nav: this.currentNav === "home" ? undefined : this.currentNav,
            keyword: this.queryParams.title || undefined,
          },
        })
        .then(() => {
          this.getList();
        });
    },
    getCategoryList() {
      listCategory().then((response) => {
        this.categoryList = response.data || response.rows || [];
      });
    },
    getDeptList() {
      // 使用listDeptForPost接口，不需要权限
      listDeptForPost()
        .then((response) => {
          if (response.code === 200 && response.data) {
            // 去除response.data里deptId为850098678、2250805007、2250805011的节点
            response.data = response.data.filter(
              (item) =>
                item.deptId !== 850098678 &&
                item.deptId != 2250805007 &&
                item.deptId != 2250805011
            );
            const root = response.data[0];
            if (root && root.children && root.children.length) {
              root.children = root.children.filter(
                (c) => c.deptId !== 850090579
              );
            }
            this.deptList = this.removeEmptyChildren(response.data);
          }
        })
        .catch(() => {
          // 忽略错误
        });
    },

    handleCategoryChange(categoryId) {
      this.activeCategory = categoryId;
      this.queryParams.pageNum = 1;
      // 更新路由参数
      this.$router
        .push({
          path: "/index",
          query: {
            ...this.$route.query,
            categoryId: categoryId || undefined,
            nav: this.currentNav === "home" ? undefined : this.currentNav,
            page: undefined, // 切换分类时重置页数
          },
        })
        .then(() => {
          this.getList();
        });
    },
    handleNavClick(nav) {
      this.currentNav = nav;
      this.queryParams.pageNum = 1;
      if (nav === "home") {
        this.sortType = "latest";
      } else if (nav === "hot") {
        this.sortType = "hot";
      } else if (nav === "latest") {
        this.sortType = "latest";
      }
      // 更新路由参数
      this.$router
        .push({
          path: "/index",
          query: {
            ...this.$route.query,
            nav: nav === "home" ? undefined : nav,
            categoryId: this.activeCategory || undefined,
            page: undefined, // 切换导航时重置页数
          },
        })
        .then(() => {
          this.getList();
        });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      // 更新路由参数
      this.$router
        .push({
          path: "/index",
          query: {
            ...this.$route.query,
            keyword: this.queryParams.title || undefined,
            page: undefined, // 搜索时重置页数
          },
        })
        .then(() => {
          this.getList();
        });
    },
    handlePostClick(postId) {
      // 保存当前页数到路由参数，以便返回时恢复
      this.$router.push({
        path: "/post/" + postId,
        query: {
          page:
            this.queryParams.pageNum > 1 ? this.queryParams.pageNum : undefined,
          categoryId: this.activeCategory || undefined,
          nav: this.currentNav === "home" ? undefined : this.currentNav,
          keyword: this.queryParams.title || undefined,
        },
      });
    },
    handleAdd() {
      this.dialogVisible = true;
      this.resetForm();
    },
    // 加载草稿
    loadDraft(postId) {
      getPost(postId)
        .then((response) => {
          if (response.code === 200 && response.data) {
            const draft = response.data;
            this.form = {
              postId: draft.postId,
              categoryId: draft.categoryId,
              title: draft.title || "",
              content: draft.content || "",
              isAnonymous: draft.isAnonymous || "1",
              isAdminPush: draft.isAdminPush ? String(draft.isAdminPush) : "0",
              postType: draft.postType || "",
              responseDeptId: draft.responseDeptId || null,
              responseDeptName: draft.responseDeptName || null,
              tags: (draft.tags || [])
                .map((item) => item.tagName)
                .filter((name) => !!name),
            };
            this.dialogVisible = true;
          } else {
            this.$modal.msgError(this.$t("bbs.loadDraftFailed"));
          }
        })
        .catch(() => {
          this.$modal.msgError(this.$t("bbs.loadDraftFailed"));
        });
    },
    submitForm() {
      this.$refs["form"].validate((valid) => {
        // const summary = this.form.content.replace(/<[^>]+>/g, "").substring(0, 200);
        //     const data = {
        //       ...this.form,
        //       summary: summary
        //     };
        // addPost(data).then(response => {
        //       this.$modal.msgSuccess("发布成功");
        //       this.dialogVisible = false;
        //       this.getList();
        //       this.getCategoryList();
        //       // 通知父组件更新左侧分类数目
        //       this.$root.$emit('bbs:post-published', this.form.categoryId);
        //     }).catch(error => {
        //       // 后端也会检测，这里处理后端返回的敏感词错误
        //       if (error && error.includes && error.includes("敏感词")) {
        //         this.$modal.msgError(error);
        //       } else if (error && typeof error === 'string') {
        //         this.$modal.msgError(error);
        //       } else {
        //         this.$modal.msgError("发布失败，请稍后重试");
        //       }
        //     });
        if (valid) {
          // 先检测敏感词（去除HTML标签，只检测纯文本）
          const titleText = (this.form.title || "").replace(/<[^>]+>/g, "");
          const contentText = (this.form.content || "").replace(/<[^>]+>/g, "");
          const checkText = titleText + contentText;
          
          // 检查内容是否包含图片
          const hasImage = (this.form.content || "").includes("<img");
          
          // 如果标题和内容都为空，且没有图片，则不允许发布
          if (!checkText.trim() && !hasImage) {
            this.$modal.msgWarning(this.$t("bbs.enterTitleOrContent"));
            return;
          }
          
          // 如果只有标题，没有内容和图片，也不允许发布
          if (titleText.trim() && !contentText.trim() && !hasImage) {
            this.$modal.msgWarning(this.$t("bbs.enterPostContent"));
            return;
          }

          // 显示检测中提示
          const loading = this.$loading({
            lock: true,
            text: this.$t("bbs.checkingContent"),
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)",
          });

          checkSensitiveWords({ text: checkText })
            .then((response) => {
              const hit = !!response.hit;
              const words = response.words || [];
              if (hit) {
                const message = words.length
                  ? `检测到敏感词：${words.join("、")}。请修改后再发布。`
                  : "内容命中敏感词，请修改后再发布。";
                this.$alert(message, "风险提示", {
                  type: "warning",
                  confirmButtonText: "我知道了",
                });
                loading.close();
                return;
              }
              loading.close();
              // 如果检测通过，继续提交
              const summary = this.form.content
                .replace(/<[^>]+>/g, "")
                .substring(0, 200);
              this.form.categoryId = "1";
              if (!this.isCurrentUserAdmin) {
                this.form.isAdminPush = "0";
              }
              const data = {
                ...this.form,
                summary: summary,
                tags: (this.form.tags || [])
                  .filter((name) => !!name)
                  .slice(0, 5)
                  .map((name) => ({ tagName: String(name).trim() })),
              };

              // 匿名发帖：需要本地秘钥并提交秘钥哈希
              if (this.form.isAnonymous === "1") {
                // 无条件弹出秘钥设置框：每次匿名发帖都需要用户确认
                this.keyNotFoundHint = hasEverSetAnonymousKey();
                this.pendingPostData = data;
                this.keyDialogVisible = true;
                return;
              }

              this.doSubmitPost(data);
            })
            .catch((error) => {
              loading.close();
              // 检测到敏感词，显示错误信息
              // request拦截器会在code !== 200时返回Promise.reject，并显示Notification
              // 这里不需要再次显示错误，但可以添加额外的处理
              // 阻止表单提交
              return;
            });
        }
      });
    },
    /** 实际提交帖子（含匿名秘钥哈希，若为匿名） */
    doSubmitPost(data) {
      const apiMethod = data.postId ? updatePost : addPost;
      apiMethod(data)
        .then((response) => {
          const message = response.msg || this.$t("bbs.publishSuccess");
          this.$modal.msgSuccess(message);
          this.dialogVisible = false;
          this.resetForm();
          this.getList();
          this.getHotPostList();
          this.getPointRankList();
          this.getCategoryList();
          this.$root.$emit("bbs:post-published", this.form.categoryId);
        })
        .catch((error) => {
          if (error && error.includes && error.includes("敏感词")) {
            this.$modal.msgError(error);
          } else if (error && typeof error === "string") {
            this.$modal.msgError(error);
          } else {
            this.$modal.msgError(this.$t("bbs.publishFailed"));
          }
        });
    },
    handleAiGenerate() {
      if (!this.aiKeywords || !this.aiKeywords.trim()) {
        this.$modal.msgWarning("请先输入关键词");
        return;
      }
      this.aiGenerating = true;
      generatePostByAi({
        keywords: this.aiKeywords.trim(),
        postType: this.form.postType,
      })
        .then((response) => {
          const generatedText = typeof response.data === "string" ? response.data : "";
          if (!generatedText) {
            this.$modal.msgWarning("AI未返回可用内容，请稍后重试");
            return;
          }
          const htmlText = generatedText
            .replace(/\n\n/g, "<br/><br/>")
            .replace(/\n/g, "<br/>");
          this.form.content = this.form.content
            ? `${this.form.content}<p><br/></p><p>${htmlText}</p>`
            : `<p>${htmlText}</p>`;
          this.$modal.msgSuccess("AI助写内容已生成，可继续编辑后发布");
        })
        .finally(() => {
          this.aiGenerating = false;
        });
    },
    extractPlainText(content) {
      return String(content || "")
        .replace(/<[^>]+>/g, " ")
        .replace(/&nbsp;/g, " ")
        .replace(/\s+/g, " ")
        .trim();
    },
    calculateHotScore(post) {
      const viewCount = Number(post.viewCount || 0);
      const likeCount = Number(post.likeCount || 0);
      const commentCount = Number(post.commentCount || 0);
      const topBonus = post.isTop === "1" ? 20 : 0;
      return viewCount + likeCount * 5 + commentCount * 8 + topBonus;
    },
    extractTopicKeywords(posts) {
      const stopWords = [
        "大家",
        "我们",
        "这个",
        "那个",
        "关于",
        "建议",
        "意见",
        "分享",
        "讨论",
        "论坛",
        "内容",
        "帖子",
        "工作",
        "进行",
        "可以",
        "需要",
        "以及",
        "the",
        "with",
        "from",
      ];
      const keywordMap = {};
      (posts || []).forEach((post) => {
        const sourceText = this.extractPlainText(
          `${post.title || ""} ${post.summary || ""}`
        );
        const words = sourceText.match(/[\u4e00-\u9fa5]{2,6}|[A-Za-z]{4,}/g) || [];
        words.forEach((word) => {
          const normalized = word.toLowerCase();
          if (stopWords.includes(normalized) || stopWords.includes(word)) {
            return;
          }
          keywordMap[word] = (keywordMap[word] || 0) + 1;
        });
        if (post.postType) {
          const postTypeLabel = this.getPostTypeName(post.postType);
          if (postTypeLabel) {
            keywordMap[postTypeLabel] = (keywordMap[postTypeLabel] || 0) + 1;
          }
        }
      });
      return Object.keys(keywordMap)
        .map((word) => ({ word, count: keywordMap[word] }))
        .sort((left, right) => right.count - left.count)
        .slice(0, 12);
    },
    getHotReason(post) {
      const hotScore = this.calculateHotScore(post);
      const commentCount = Number(post.commentCount || 0);
      const likeCount = Number(post.likeCount || 0);
      if (commentCount > 0) {
        return `${commentCount} 条讨论 · 热度 ${hotScore}`;
      }
      if (likeCount > 0) {
        return `${likeCount} 次点赞 · 热度 ${hotScore}`;
      }
      return `${post.viewCount || 0} 次浏览 · 热度 ${hotScore}`;
    },
    applyTopicToAi(topic) {
      this.aiKeywords = topic.word;
      this.$modal.msgSuccess(`已将“${topic.word}”填入 AI 助写关键词`);
    },
    insertTemplate(templateType) {
      const templateMap = {
        share: {
          title: "经验分享：",
          keywords: "经验复盘、最佳实践、避坑总结",
          content:
            "<p><strong>背景</strong></p><p>本次分享适用于哪些场景？先交代清楚问题背景。</p><p><strong>实践过程</strong></p><p>拆分为 2 到 3 个关键动作，说明怎么做、为什么这么做。</p><p><strong>结果与建议</strong></p><p>总结收益、踩坑点，以及可复用的建议。</p>",
        },
        suggestion: {
          title: "优化建议：",
          keywords: "流程优化、效率提升、员工体验",
          content:
            "<p><strong>现状描述</strong></p><p>当前流程或场景中遇到了什么具体问题？</p><p><strong>影响分析</strong></p><p>问题对效率、体验或协同造成了哪些影响？</p><p><strong>建议方案</strong></p><p>建议的调整动作、执行方式和优先级。</p><p><strong>预期收益</strong></p><p>如果落地，预计会带来哪些改善？</p>",
        },
        opinion: {
          title: "观点讨论：",
          keywords: "制度优化、组织协同、管理改进",
          content:
            "<p><strong>观点结论</strong></p><p>先用 1 到 2 句话明确你的核心观点。</p><p><strong>支撑依据</strong></p><p>结合事实、案例或数据说明原因。</p><p><strong>开放问题</strong></p><p>列出希望大家一起讨论或决策的问题点。</p>",
        },
      };
      const template = templateMap[templateType];
      if (!template) {
        return;
      }
      this.form.postType = templateType;
      if (!this.form.title) {
        this.form.title = template.title;
      }
      if (!this.aiKeywords) {
        this.aiKeywords = template.keywords;
      }
      this.form.content = this.extractPlainText(this.form.content)
        ? `${this.form.content}<p><br/></p>${template.content}`
        : template.content;
      this.$modal.msgSuccess("已插入结构模板，可继续编辑或结合 AI 扩写。");
    },
    /** 匿名秘钥对话框确认：保存秘钥并提交待发帖子 */
    onAnonymousKeyConfirm(key, rememberInBrowser) {
      setAnonymousKey(key, { rememberInBrowser });
      hashAnonymousKeyForUser(key, this.id)
        .then((keyHash) => {
          if (!this.pendingPostData) return;
          this.pendingPostData.userId = keyHash;
          this.doSubmitPost(this.pendingPostData);
          this.pendingPostData = null;
        })
        .catch((err) => {
          this.$modal.msgError(this.$t("bbs.publishFailed"));
          console.warn("anonymous key hash failed", err);
          this.pendingPostData = null;
        });
    },
    /** 匿名秘钥对话框取消：放弃本次发帖 */
    onAnonymousKeyCancel() {
      this.pendingPostData = null;
    },
    saveDraft() {
      // 保存草稿不需要验证表单，但需要至少有一个标题或内容
      const titleText = (this.form.title || "").replace(/<[^>]+>/g, "").trim();
      const contentText = (this.form.content || "")
        .replace(/<[^>]+>/g, "")
        .trim();

      if (!titleText && !contentText) {
        this.$modal.msgWarning(this.$t("bbs.draftEmptyTip"));
        return;
      }

      // 生成摘要
      const summary = this.form.content
        .replace(/<[^>]+>/g, "")
        .substring(0, 200);
      this.form.categoryId = "1";
      if (!this.isCurrentUserAdmin) {
        this.form.isAdminPush = "0";
      }
      const data = {
        ...this.form,
        summary: summary,
        status: "4", // 草稿状态
        tags: (this.form.tags || [])
          .filter((name) => !!name)
          .slice(0, 5)
          .map((name) => ({ tagName: String(name).trim() })),
      };

      // 如果是编辑草稿，使用update接口
      const apiMethod = this.form.postId ? updatePost : addPost;
      apiMethod(data)
        .then((response) => {
          this.$modal.msgSuccess(response.msg || this.$t("bbs.draftSaveSuccess"));
          this.dialogVisible = false;
          this.resetForm();
        })
        .catch((error) => {
          if (error && typeof error === "string") {
            this.$modal.msgError(error);
          } else {
            this.$modal.msgError(this.$t("bbs.draftSaveFailed"));
          }
        });
    },
    handleDeptChange(deptId) {
      // 根据选择的部门ID，找到对应的部门名称
      if (deptId) {
        const selectedDept = this.deptList.find(
          (dept) => dept.deptId === deptId
        );
        if (selectedDept) {
          this.form.responseDeptName = selectedDept.deptName;
        }
      } else {
        this.form.responseDeptName = null;
      }
    },
    resetForm() {
      this.form = {
        postId: null,
        categoryId: null,
        title: "",
        content: "",
        isAnonymous: "1",
        isAdminPush: "0",
        postType: "",
        responseDeptId: null,
        responseDeptName: null,
        tags: [],
      };
      this.aiKeywords = "";
      // 重置表单验证
      if (this.$refs.form) {
        this.$refs.form.clearValidate();
      }
    },
    getCategoryTagType(categoryName) {
      const typeMap = {
        技术分享: "primary",
        产品讨论: "success",
        职场交流: "warning",
        生活杂谈: "info",
      };
      return typeMap[categoryName] || "primary";
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
    truncateTitle(title) {
      if (!title) return "";
      const maxLength = this.isMobile ? 20 : 35;
      if (title.length > maxLength) {
        return title.substring(0, maxLength) + "...";
      }
      return title;
    },
  },
};
</script>

<style scoped lang="scss">
.bbs-content {
  background: white;
  border-radius: 4px;
  padding: 20px;
  min-height: 500px;
  width: 100%;
  box-sizing: border-box;
}

.ai-assist-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.inspiration-panel,
.quality-panel {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 14px;
  background: #fafcff;
}

.assist-section + .assist-section,
.quality-block + .quality-block {
  margin-top: 12px;
}

.assist-section-title,
.quality-block-title,
.quality-level {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.quality-panel {
  display: flex;
  gap: 16px;
  align-items: center;
}

.quality-score-panel {
  min-width: 160px;
  text-align: center;
}

.quality-detail-panel {
  flex: 1;
}

.quality-suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quality-suggestion-item {
  position: relative;
  padding-left: 14px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.quality-suggestion-item::before {
  content: "";
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
}

.category-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.category-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.category-count {
  font-size: 14px;
  color: #999;
}

.mobile-category-selector {
  margin-bottom: 15px;
}

.sort-options {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.point-rank-board {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
}

.point-rank-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.point-rank-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.point-rank-subtitle {
  font-size: 12px;
  color: #909399;
}

.point-rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.point-rank-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #eef1f6;
  border-radius: 10px;
  background: #fff;
  padding: 10px 12px;
}

.point-rank-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.point-rank-no {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 12px;
  text-align: center;
  line-height: 22px;
  font-weight: 700;
}

.point-rank-name {
  font-size: 14px;
  color: #303133;
}

.point-rank-score {
  font-size: 14px;
  font-weight: 600;
  color: #e67e22;
}

.forum-intelligence {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.intelligence-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 8px 24px rgba(31, 45, 61, 0.06);
}

.intelligence-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  border: 1px solid #f0f3f7;
}

.metric-value {
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
  color: #1f2d3d;
}

.metric-label {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}

.metric-tip,
.radar-tip,
.quality-meta,
.hot-meta,
.intelligence-empty {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hot-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  cursor: pointer;
}

.hot-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.hot-body {
  min-width: 0;
}

.hot-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
}

.topic-chip-list,
.capsule-list,
.quality-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-chip,
.capsule-button {
  border: none;
  border-radius: 999px;
  padding: 8px 12px;
  background: #eef5ff;
  color: #2f5bea;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.capsule-button-ghost {
  background: #fff7ed;
  color: #d97706;
}

.topic-count {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(47, 91, 234, 0.12);
  font-size: 12px;
  line-height: 18px;
}

.post-list {
  min-height: 400px;
}

.post-item {
  padding: 10px 20px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover {
  background: #f9f9f9;
}

.post-item:last-child {
  border-bottom: none;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-tag-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.category-tag {
  margin-right: 8px;
}

.post-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  margin: 0;
}

.post-content {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2; // standard property for compatibility
  -webkit-box-orient: vertical;
}

.post-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 13px;
  color: #999;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author {
  color: #409eff;
  font-weight: 500;
}

.divider {
  color: #ccc;
}

.post-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 10px;
}
.editor {
  min-height: 350px;
}
/* 移动端适配 */
@media screen and (max-width: 768px) {
  .point-rank-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .forum-intelligence {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: 1fr 1fr;
  }

  .quality-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .quality-score-panel {
    min-width: 0;
  }

  .ai-assist-row {
    flex-direction: column;
    align-items: stretch;
  }

  .editor {
    min-height: 450px;
  }
  .bbs-content {
    padding: 10px !important;
  }

  .post-item {
    padding: 15px 10px !important;
  }

  .post-title {
    font-size: 16px !important;
  }

  .post-content {
    font-size: 14px;
  }

  .post-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;

    .post-title-wrapper {
      width: 100%;
      margin-bottom: 0;
    }

    .post-info-column {
      width: 100%;
      flex-direction: row !important;
      align-items: center;
      justify-content: space-between;
      margin-top: 0;
    }

    .post-meta {
      display: flex;
      align-items: center;
      gap: 5px;
      flex-wrap: wrap;
      flex: 1;
    }

    .post-stats {
      display: flex;
      gap: 15px;
      flex-shrink: 0;
    }
  }

  .post-footer {
    display: none;
  }

  .sort-options {
    margin-bottom: 15px;
  }

  .mobile-category-selector {
    margin-bottom: 15px;
  }
}
.post-wrapper {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  justify-content: space-between;

  .post-title-wrapper {
    flex: 1;
    display: flex;
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .post-info-column {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
    flex-shrink: 0;
  }

  .author {
    color: #409eff;
    font-weight: 500;
    font-size: 13px;
  }

  .divider {
    color: #ccc;
  }

  .post-stats {
    display: flex;
    gap: 15px;
    justify-content: flex-end;
  }
  .time {
    font-size: 12px;
  }
  .stat-item {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    .like-icon {
      width: 15px;
      height: 15px;
    }
  }
}
</style>
