<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item :label="$t('bbsExtra.article.articleTitle')" prop="title">
        <el-input
          v-model="queryParams.title"
          :placeholder="$t('bbsExtra.article.articleTitlePh')"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('bbsExtra.article.category')" prop="categoryId">
        <el-select
          v-model="queryParams.categoryId"
          :placeholder="$t('bbsExtra.article.categoryPh')"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="category in categoryList"
            :key="category.categoryId"
            :label="category.categoryName"
            :value="category.categoryId"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('bbsExtra.article.auditStatus')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="$t('bbsExtra.article.auditStatusPh')"
          clearable
          style="width: 200px"
        >
          <el-option :label="$t('bbsExtra.article.stApproved')" value="0" />
          <el-option :label="$t('bbsExtra.article.stPending')" value="2" />
          <el-option :label="$t('bbsExtra.article.stRejected')" value="3" />
          <el-option :label="$t('bbsExtra.article.stClosed')" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >{{ $t('common.search') }}</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >{{ $t('common.reset') }}</el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple || !hasPendingPosts"
          @click="handleApprove"
          >{{ $t('bbsExtra.article.approve') }}</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          size="mini"
          :disabled="multiple || !hasPendingPosts"
          @click="handleReject"
          >{{ $t('bbsExtra.article.reject') }}</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-bottom"
          size="mini"
          :disabled="multiple"
          @click="handleDelete('batch')"
          >{{ $t('bbsExtra.article.batchOffline') }}</el-button
        >
      </el-col>
      <el-col :span="6" style="text-align: right">
        <span style="margin-right: 10px; line-height: 28px">{{ $t('bbsExtra.article.auditSwitch') }}</span>
        <el-switch
          v-model="auditEnabled"
          :active-text="$t('bbsExtra.article.on')"
          :inactive-text="$t('bbsExtra.article.off')"
          @change="handleAuditSwitchChange"
        ></el-switch>
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="postList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        :label="$t('bbsExtra.article.articleId')"
        align="center"
        prop="postId"
        width="100"
      />
      <el-table-column
        :label="$t('bbsExtra.article.articleTitle')"
        align="left"
        prop="title"
        :show-overflow-tooltip="true"
        min-width="200"
      />
      <el-table-column
        :label="$t('bbsExtra.article.category')"
        align="center"
        prop="postType"
        width="120"
      >
        <template slot-scope="scope">
            <el-tag :type="getPostTypeTagType(scope.row.postType)"
                size="mini"
                style="margin-right: 5px">
              {{ getPostTypeName(scope.row.postType) }}
            </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('bbsExtra.article.author')" align="center" prop="nickName" width="120">
        <template slot-scope="scope">
          <span v-if="scope.row.isAnonymous === '1'">{{ $t('bbs.anonymousUser') }}</span>
          <span v-else>{{ scope.row.nickName || $t("bbs.unknownUser") }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('bbsExtra.article.status')" align="center" width="150">
        <template slot-scope="scope">
          <el-tag
            v-if="scope.row.status === '2'"
            type="info"
            size="mini"
            style="margin-right: 5px"
            >{{ $t('bbsExtra.article.stPending') }}</el-tag
          >
          <el-tag
            v-else-if="scope.row.status === '1'"
            type="danger"
            size="mini"
            style="margin-right: 5px"
            >{{ $t('bbsExtra.article.stClosed') }}</el-tag
          >
          <el-tag
            v-else-if="scope.row.status === '0'"
            type="success"
            size="mini"
            style="margin-right: 5px"
            >{{ $t('bbsExtra.article.stApproved') }}</el-tag
          >
          <el-tag
            v-else-if="scope.row.status === '3'"
            type="danger"
            size="mini"
            style="margin-right: 5px"
            >{{ $t('bbsExtra.article.stRejected') }}</el-tag
          >
          <el-tag
            v-if="scope.row.isTop === '1'"
            type="danger"
            size="mini"
            style="margin-right: 5px"
            >{{ $t('bbsExtra.article.top') }}</el-tag
          >
          <el-tag v-if="scope.row.isEssence === '1'" type="warning" size="mini"
            >{{ $t('bbs.essence') }}</el-tag
          >
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('bbsExtra.article.rejectReason')"
        align="left"
        min-width="220"
        :show-overflow-tooltip="true"
      >
        <template slot-scope="scope">
          <span v-if="scope.row.status === '3'">{{ scope.row.auditReason || $t("bbsExtra.article.notFilled") }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('bbsExtra.article.views')"
        align="center"
        prop="viewCount"
        width="100"
      />
      <el-table-column
        :label="$t('bbsExtra.article.likes')"
        align="center"
        prop="likeCount"
        width="100"
      />
      <el-table-column
        :label="$t('bbsExtra.article.comments')"
        align="center"
        prop="commentCount"
        width="100"
      />
      <el-table-column
        :label="$t('bbsExtra.article.createTime')"
        align="center"
        prop="createTime"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('bbsExtra.article.actions')"
        align="center"
        class-name="small-padding fixed-width"
        width="320"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            >{{ $t('bbsExtra.article.view') }}</el-button
          >
          <el-button
            v-if="scope.row.status === '2'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            >{{ $t('bbsExtra.article.approve') }}</el-button
          >
          <el-button
            v-if="scope.row.status === '2'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
            >{{ $t('bbsExtra.article.reject') }}</el-button
          >
          <el-button
            size="mini"
            :type="scope.row.isTop === '1' ? 'warning' : 'text'"
            :icon="scope.row.isTop === '1' ? 'el-icon-top' : 'el-icon-bottom'"
            @click="handleToggleTop(scope.row)"
            >{{ scope.row.isTop === "1" ? $t("bbsExtra.article.unpin") : $t("bbsExtra.article.pin") }}</el-button
          >
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '1'"
            size="mini"
            type="text"
            :icon="scope.row.status === '1' ? 'el-icon-top' : 'el-icon-bottom'"
            @click="handleDelete('single', scope.row)"
            :style="{color: scope.row.status === '1' ? 'green' : 'red'}"
            >{{ scope.row.status === '1' ? $t('bbsExtra.article.relist') : $t('bbsExtra.article.takeDown') }}</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 文章详情预览对话框 -->
    <el-dialog
      :title="$t('bbsExtra.article.detailTitle')"
      :visible.sync="previewVisible"
      width="80%"
      :close-on-click-modal="false"
      append-to-body
    >
      <div v-loading="previewLoading" class="article-preview">
        <div v-if="previewPost" class="preview-content">
          <div class="preview-header">
            <h2 class="preview-title">{{ previewPost.title }}</h2>
            <div class="preview-meta">
              <el-tag
                v-if="previewPost.isTop === '1'"
                type="danger"
                size="small"
                style="margin-right: 5px"
                >{{ $t('bbsExtra.article.top') }}</el-tag
              >
              <el-tag
                v-if="previewPost.isEssence === '1'"
                type="warning"
                size="small"
                style="margin-right: 5px"
                >{{ $t('bbs.essence') }}</el-tag
              >
              <!-- <span style="margin-right: 10px;">分类：{{ previewPost.categoryName }}</span> -->
              <span style="margin-right: 10px"
                >{{ $t('bbsExtra.article.authorColon') }}{{
                  previewPost.isAnonymous === "1"
                    ? $t("bbs.anonymousUser")
                    : previewPost.nickName || $t("bbs.unknownUser")
                }}</span
              >
              <span style="margin-right: 10px"
                >{{ $t('bbsExtra.article.publishTime') }}{{ parseTime(previewPost.createTime) }}</span
              >
            </div>
          </div>
          <div class="preview-stats">
            <span style="margin-right: 15px"
              >{{ $t('bbsExtra.article.viewsColon') }}{{ previewPost.viewCount }}</span
            >
            <span style="margin-right: 15px"
              >{{ $t('bbsExtra.article.likesColon') }}{{ previewPost.likeCount }}</span
            >
            <span>{{ $t('bbsExtra.article.commentsColon') }}{{ previewPost.commentCount }}</span>
          </div>
          <div class="preview-body" v-html="previewPost.content"></div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">{{ $t('bbsExtra.article.close') }}</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  listPost,
  delPost,
  getPost,
  getAuditEnabled,
  setAuditEnabled,
  approvePosts,
  rejectPosts,
  toggleTop,
} from "@/api/bbs/post";
import { listCategory } from "@/api/bbs/category";

export default {
  name: "BbsArticle",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 文章表格数据
      postList: [],
      // 分类列表
      categoryList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        categoryId: undefined,
        status: undefined,
      },
      // 预览相关
      previewVisible: false,
      previewLoading: false,
      previewPost: null,
      // 审核开关
      auditEnabled: false,
    };
  },
  computed: {
    // 是否有待审核的文章
    hasPendingPosts() {
      return this.postList.some((post) => post.status === "2");
    },
  },
  created() {
    this.getList();
    this.getCategoryList();
    this.getAuditEnabled();
  },
  methods: {
    getPostTypeTagType(postType) {
      const typeMap = {
        share: "success",
        suggestion: "warning",
        opinion: "danger",
      };
      return typeMap[postType] || "primary";
    },
    getPostTypeName(postType) {
      const typeMap = {
        share: this.$t("bbs.postTypeShare"),
        suggestion: this.$t("bbs.postTypeSuggestion"),
        opinion: this.$t("bbs.postTypeOpinion"),
      };
      return typeMap[postType] || "";
    },
    /** 查询文章列表 */
    getList() {
      this.loading = true;
      listPost(this.queryParams)
        .then((response) => {
          this.postList = response.rows || response.data || [];
          this.total = response.total || 0;
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    /** 查询分类列表 */
    getCategoryList() {
      listCategory().then((response) => {
        this.categoryList = response.data || response.rows || [];
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.postId).filter(id => id != null);
      console.log('this.ids', this.ids);
      this.multiple = !selection.length;
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.previewVisible = true;
      this.previewLoading = true;
      this.previewPost = null;

      getPost(row.postId)
        .then((response) => {
          this.previewPost = response.data;
          this.previewLoading = false;
        })
        .catch(() => {
          this.previewLoading = false;
          this.$modal.msgError(this.$t("bbsExtra.article.loadDetailFailed"));
        });
    },
    /** 下架/上架按钮操作 */
    handleDelete(type,row) {
      const postIds = type === 'single' ? [row.postId] : this.ids;
      const isOffline = row ? row.status === '1' : false;
      let message = "";
      let title = this.$t("bbsExtra.article.tip");
      if (type === 'single') {
        if (isOffline) {
          message = this.$t("bbsExtra.article.confirmRelist", { title: row.title });
          title = this.$t("bbsExtra.article.relistTitle");
        } else {
          message = this.$t("bbsExtra.article.confirmTakeDown", { title: row.title });
          title = this.$t("bbsExtra.article.takeDownTitle");
        }
      } else {
        message = this.$t("bbsExtra.article.confirmBatchTakeDown", { count: postIds.length });
        title = this.$t("bbsExtra.article.batchTakeDownTitle");
      }
      this.$confirm(message, title, {
        confirmButtonText: this.$t("common.confirm"),
        cancelButtonText: this.$t("common.cancel"),
        type: "warning",
        dangerouslyUseHTMLString: false,
      })
        .then(() => {
          // 如果是批量操作，需要循环调用删除接口
          console.log('postIds', row);
          if (postIds.length === 1) {
            return delPost(postIds[0]);
          } else {
            // 批量下架，循环调用
            const deletePromises = this.ids.map((id) => delPost(id));
            return Promise.all(deletePromises);
          }
        })
        .then((response) => {
          this.getList();
          const msg = response && response.msg ? response.msg : (isOffline ? this.$t("bbsExtra.article.relistSuccess") : this.$t("bbsExtra.article.takeDownSuccess"));
          this.$modal.msgSuccess(msg);
        })
        .catch(() => {
          // 用户取消操作，不需要做任何操作
        });
    },
    /** 获取审核开关状态 */
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
    /** 审核开关变化 */
    handleAuditSwitchChange(value) {
      this.$confirm(
        value
          ? this.$t("bbsExtra.article.confirmAuditOn")
          : this.$t("bbsExtra.article.confirmAuditOff"),
        this.$t("bbsExtra.article.tip"),
        {
          confirmButtonText: this.$t("common.confirm"),
          cancelButtonText: this.$t("common.cancel"),
          type: "warning",
        }
      )
        .then(() => {
          return setAuditEnabled(value);
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(value ? this.$t("bbsExtra.article.auditOnDone") : this.$t("bbsExtra.article.auditOffDone"));
          } else {
            this.auditEnabled = !value; // 恢复原状态
            this.$modal.msgError(response.msg || this.$t("bbsExtra.article.settingFailed"));
          }
        })
        .catch(() => {
          this.auditEnabled = !value; // 恢复原状态
        });
    },
    /** 审核通过 */
    handleApprove(row) {
      let postIds = [];
      
      if (row.postId) {
        // 单个操作
          postIds = [row.postId];
      } else {
        // 批量操作
        console.log('this.ids', this.ids);
        postIds = this.ids
          .filter((id) => id != null) // 过滤掉 null 和 undefined
          .filter((id) => {
            const post = this.postList.find((p) => p.postId === id);
            return post && post.status === "2"; // 只选择待审核的文章
          });
      }

      if (postIds.length === 0) {
        this.$modal.msgWarning(this.$t("bbsExtra.article.selectPending"));
        return;
      }

      let message = "";
      if (row.postId) {
        message = this.$t("bbsExtra.article.confirmApprove", { title: row.title });
      } else {
        message = this.$t("bbsExtra.article.confirmApproveBatch", { count: postIds.length });
      }

      this.$confirm(message, this.$t("bbsExtra.article.approveTitle"), {
        confirmButtonText: this.$t("common.confirm"),
        cancelButtonText: this.$t("common.cancel"),
        type: "info",
      })
        .then(() => {
          return approvePosts(postIds);
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(response.msg || this.$t("bbsExtra.article.approveSuccess"));
            this.getList();
          } else {
            this.$modal.msgError(response.msg || this.$t("bbsExtra.article.approveFailed"));
          }
        })
        .catch(() => {
          // 用户取消
        });
    },
    /** 审核不通过 */
    handleReject(row) {
      let postIds = [];
      console.log('row', row);
      if (row.postId) {
        // 单个操作
          postIds = [row.postId];
      } else {
        // 批量操作
        console.log('this.ids', this.ids);
        postIds = this.ids
          .filter((id) => id != null) // 过滤掉 null 和 undefined
          .filter((id) => {
            const post = this.postList.find((p) => p.postId === id);
            return post && post.status === "2"; // 只选择待审核的文章
          });
      }

      if (postIds.length === 0) {
        this.$modal.msgWarning(this.$t("bbsExtra.article.selectPending"));
        return;
      }

      let message = "";
      if (row.postId) {
        message = this.$t("bbsExtra.article.confirmReject", { title: row.title });
      } else {
        message = this.$t("bbsExtra.article.confirmRejectBatch", { count: postIds.length });
      }

      this.$prompt(this.$t("bbsExtra.article.rejectPrompt"), this.$t("bbsExtra.article.reject"), {
        confirmButtonText: this.$t("common.confirm"),
        cancelButtonText: this.$t("common.cancel"),
        inputPattern: /\S+/,
        inputErrorMessage: this.$t("bbsExtra.article.rejectReasonRequired"),
        inputValue: this.$t("bbsExtra.article.defaultRejectReason"),
      })
        .then(({ value }) => {
          return rejectPosts({
            postIds,
            auditReason: value && value.trim ? value.trim() : value,
          });
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(response.msg || this.$t("bbsExtra.article.rejectSuccess"));
            this.getList();
          } else {
            this.$modal.msgError(response.msg || this.$t("bbsExtra.article.opFailed"));
          }
        })
        .catch(() => {
          // 用户取消
        });
    },
    /** 置顶/取消置顶 */
    handleToggleTop(row) {
      const isUnpin = row.isTop === "1";
      const action = isUnpin ? this.$t("bbsExtra.article.unpin") : this.$t("bbsExtra.article.pin");
      const confirmMsg = isUnpin
        ? this.$t("bbsExtra.article.confirmUnpin", { title: row.title })
        : this.$t("bbsExtra.article.confirmPin", { title: row.title });
      this.$confirm(confirmMsg, action, {
        confirmButtonText: this.$t("common.confirm"),
        cancelButtonText: this.$t("common.cancel"),
        type: "info",
      })
        .then(() => {
          return toggleTop(row.postId);
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(response.msg || this.$t(isUnpin ? "bbsExtra.article.unpinSuccess" : "bbsExtra.article.pinSuccess"));
            this.getList();
          } else {
            this.$modal.msgError(response.msg || this.$t(isUnpin ? "bbsExtra.article.unpinFailed" : "bbsExtra.article.pinFailed"));
          }
        })
        .catch(() => {
          // 用户取消
        });
    },
  },
};
</script>

<style scoped lang="scss">
.article-preview {
  min-height: 300px;
}

.preview-content {
  .preview-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;

    .preview-title {
      font-size: 24px;
      font-weight: bold;
      color: #333;
      margin: 0 0 15px 0;
    }

    .preview-meta {
      font-size: 14px;
      color: #666;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
    }
  }

  .preview-stats {
    margin-bottom: 20px;
    padding: 10px;
    background: #f5f5f5;
    border-radius: 4px;
    font-size: 14px;
    color: #666;
  }

  .preview-body {
    line-height: 1.8;
    color: #333;
    word-wrap: break-word;

    ::v-deep img {
      max-width: 100%;
      height: auto;
    }
  }
}
</style>
