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
import { listPost, addPost, getPost, updatePost } from "@/api/bbs/post";
import { listCategory } from "@/api/bbs/category";
import { checkSensitiveWords } from "@/api/bbs/sensitive";
import { listDeptForPost, listDept } from "@/api/system/dept";
import { getToken } from "@/utils/auth";
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
      },
      isMobile: false,
      keyDialogVisible: false,
      keyNotFoundHint: false,
      pendingPostData: null,
    };
  },
  computed: {
    ...mapGetters(["id", "permissions"]),
    isCurrentUserAdmin() {
      return this.permissions && this.permissions.length > 0;;
    },
    currentCategoryName() {
      if (!this.activeCategory) return "";
      const category = this.categoryList.find(
        (c) => c.categoryId === this.activeCategory
      );
      return category ? category.categoryName : "";
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
      };
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
