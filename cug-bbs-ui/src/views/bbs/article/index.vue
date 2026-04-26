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
      <el-form-item label="文章标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入文章标题"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select
          v-model="queryParams.categoryId"
          placeholder="请选择分类"
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
      <el-form-item label="审核状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择审核状态"
          clearable
          style="width: 200px"
        >
          <el-option label="已审核" value="0" />
          <el-option label="待审核" value="2" />
          <el-option label="审核不通过" value="3" />
          <el-option label="已关闭" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
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
          >审核通过</el-button
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
          >审核不通过</el-button
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
          >批量下架</el-button
        >
      </el-col>
      <el-col :span="6" style="text-align: right">
        <span style="margin-right: 10px; line-height: 28px">审核开关：</span>
        <el-switch
          v-model="auditEnabled"
          active-text="开启"
          inactive-text="关闭"
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
        label="文章ID"
        align="center"
        prop="postId"
        width="100"
      />
      <el-table-column
        label="文章标题"
        align="left"
        prop="title"
        :show-overflow-tooltip="true"
        min-width="200"
      />
      <el-table-column
        label="分类"
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
      <el-table-column label="作者" align="center" prop="nickName" width="120">
        <template slot-scope="scope">
          <span v-if="scope.row.isAnonymous === '1'">匿名用户</span>
          <span v-else>{{ scope.row.nickName || "未知用户" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="150">
        <template slot-scope="scope">
          <el-tag
            v-if="scope.row.status === '2'"
            type="info"
            size="mini"
            style="margin-right: 5px"
            >待审核</el-tag
          >
          <el-tag
            v-else-if="scope.row.status === '1'"
            type="danger"
            size="mini"
            style="margin-right: 5px"
            >已关闭</el-tag
          >
          <el-tag
            v-else-if="scope.row.status === '0'"
            type="success"
            size="mini"
            style="margin-right: 5px"
            >已审核</el-tag
          >
          <el-tag
            v-else-if="scope.row.status === '3'"
            type="danger"
            size="mini"
            style="margin-right: 5px"
            >审核不通过</el-tag
          >
          <el-tag
            v-if="scope.row.isTop === '1'"
            type="danger"
            size="mini"
            style="margin-right: 5px"
            >置顶</el-tag
          >
          <el-tag v-if="scope.row.isEssence === '1'" type="warning" size="mini"
            >精华</el-tag
          >
        </template>
      </el-table-column>
      <el-table-column
        label="浏览量"
        align="center"
        prop="viewCount"
        width="100"
      />
      <el-table-column
        label="点赞数"
        align="center"
        prop="likeCount"
        width="100"
      />
      <el-table-column
        label="评论数"
        align="center"
        prop="commentCount"
        width="100"
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
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
            >查看</el-button
          >
          <el-button
            v-if="scope.row.status === '2'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            >审核通过</el-button
          >
          <el-button
            v-if="scope.row.status === '2'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
            >审核不通过</el-button
          >
          <el-button
            size="mini"
            :type="scope.row.isTop === '1' ? 'warning' : 'text'"
            :icon="scope.row.isTop === '1' ? 'el-icon-top' : 'el-icon-bottom'"
            @click="handleToggleTop(scope.row)"
            >{{ scope.row.isTop === "1" ? "取消置顶" : "置顶" }}</el-button
          >
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '1'"
            size="mini"
            type="text"
            :icon="scope.row.status === '1' ? 'el-icon-top' : 'el-icon-bottom'"
            @click="handleDelete('single', scope.row)"
            :style="{color: scope.row.status === '1' ? 'green' : 'red'}"
            >{{ scope.row.status === '1' ? '重新上架' : '下架' }}</el-button
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
      title="文章详情"
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
                >置顶</el-tag
              >
              <el-tag
                v-if="previewPost.isEssence === '1'"
                type="warning"
                size="small"
                style="margin-right: 5px"
                >精华</el-tag
              >
              <!-- <span style="margin-right: 10px;">分类：{{ previewPost.categoryName }}</span> -->
              <span style="margin-right: 10px"
                >作者：{{
                  previewPost.isAnonymous === "1"
                    ? "匿名用户"
                    : previewPost.nickName || "未知用户"
                }}</span
              >
              <span style="margin-right: 10px"
                >发布时间：{{ parseTime(previewPost.createTime) }}</span
              >
            </div>
          </div>
          <div class="preview-stats">
            <span style="margin-right: 15px"
              >浏览量：{{ previewPost.viewCount }}</span
            >
            <span style="margin-right: 15px"
              >点赞数：{{ previewPost.likeCount }}</span
            >
            <span>评论数：{{ previewPost.commentCount }}</span>
          </div>
          <div class="preview-body" v-html="previewPost.content"></div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">关 闭</el-button>
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
        share: "分享",
        suggestion: "建议",
        opinion: "意见",
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
          this.$modal.msgError("获取文章详情失败");
        });
    },
    /** 下架/上架按钮操作 */
    handleDelete(type,row) {
      const postIds = type === 'single' ? [row.postId] : this.ids;
      const isOffline = row ? row.status === '1' : false;
      let message = "";
      let title = "提示";
      if (type === 'single') {
        if (isOffline) {
          message = '是否确认重新上架文章"' + row.title + '"？上架后文章将重新显示在前台。';
          title = "重新上架文章";
        } else {
          message = '是否确认下架文章"' + row.title + '"？下架后文章将不再显示在前台。';
          title = "下架文章";
        }
      } else {
        message =
          "是否确认下架选中的" +
          postIds.length +
          "篇文章？下架后文章将不再显示在前台。";
        title = "批量下架文章";
      }
      this.$confirm(message, title, {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
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
          const msg = response && response.msg ? response.msg : (isOffline ? "上架成功" : "下架成功");
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
          ? "开启审核后，用户发布的文章将需要审核通过才能显示。是否确认开启？"
          : "关闭审核后，用户发布的文章将直接发布。是否确认关闭？",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(() => {
          return setAuditEnabled(value);
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(value ? "审核开关已开启" : "审核开关已关闭");
          } else {
            this.auditEnabled = !value; // 恢复原状态
            this.$modal.msgError(response.msg || "设置失败");
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
        this.$modal.msgWarning("请选择待审核的文章");
        return;
      }

      let message = "";
      if (row.postId) {
        message = '是否确认审核通过文章"' + row.title + '"？';
      } else {
        message = "是否确认审核通过选中的" + postIds.length + "篇待审核文章？";
      }

      this.$confirm(message, "审核通过", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(() => {
          return approvePosts(postIds);
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(response.msg || "审核通过成功");
            this.getList();
          } else {
            this.$modal.msgError(response.msg || "审核失败");
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
        this.$modal.msgWarning("请选择待审核的文章");
        return;
      }

      let message = "";
      if (row.postId) {
        message = '是否确认审核不通过文章"' + row.title + '"？';
      } else {
        message = "是否确认审核不通过选中的" + postIds.length + "篇待审核文章？";
      }

      this.$confirm(message, "审核不通过", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          return rejectPosts(postIds);
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(response.msg || "审核不通过成功");
            this.getList();
          } else {
            this.$modal.msgError(response.msg || "操作失败");
          }
        })
        .catch(() => {
          // 用户取消
        });
    },
    /** 置顶/取消置顶 */
    handleToggleTop(row) {
      const action = row.isTop === "1" ? "取消置顶" : "置顶";
      this.$confirm("是否确认" + action + '文章"' + row.title + '"？', action, {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(() => {
          return toggleTop(row.postId);
        })
        .then((response) => {
          if (response.code === 200) {
            this.$modal.msgSuccess(response.msg || action + "成功");
            this.getList();
          } else {
            this.$modal.msgError(response.msg || action + "失败");
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
