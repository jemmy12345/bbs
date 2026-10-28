<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="88px"
    >
      <el-form-item label="帖子标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="所属帖子标题"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="评论内容" prop="commentContent">
        <el-input
          v-model="queryParams.commentContent"
          placeholder="模糊搜索评论内容"
          clearable
          style="width: 220px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="commentList">
      <el-table-column label="评论ID" align="center" prop="commentId" width="90" />
      <el-table-column label="所属帖子ID" align="center" prop="postId" width="110" />
      <el-table-column
        label="所属帖子标题"
        align="left"
        prop="title"
        min-width="180"
        :show-overflow-tooltip="true"
      >
        <template slot-scope="scope">
          <router-link
            v-if="scope.row.postId"
            class="link-type"
            :to="'/post/' + scope.row.postId"
            target="_blank"
            >{{ scope.row.title || "—" }}</router-link
          >
          <span v-else>{{ scope.row.postTitle || "—" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="评论人ID" align="center" prop="commentUserId" width="120" :show-overflow-tooltip="true">
      </el-table-column>
      <el-table-column label="评论内容" align="left" prop="commentContent" min-width="220" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ plainCommentPreview(scope.row.commentContent) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="isCommentDeleted(scope.row)" type="info" size="mini">已删除</el-tag>
          <el-tag v-else type="success" size="mini">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="170"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            v-if="!isCommentDeleted(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-delete"
            style="color: #f56c6c"
            @click="handleDelete(scope.row)"
            >删除</el-button
          >
          <el-button
            v-else
            size="mini"
            type="text"
            icon="el-icon-refresh-left"
            @click="handleRestore(scope.row)"
            >恢复</el-button
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
  </div>
</template>

<script>
import { listCommentAdmin, setCommentDelFlag } from "@/api/bbs/comment";

export default {
  name: "BbsCommentAdmin",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      commentList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        commentContent: undefined,
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    plainCommentPreview(html) {
      if (!html || typeof html !== "string") return "—";
      const text = html
        .replace(/<[^>]+>/g, "")
        .replace(/&nbsp;/gi, " ")
        .trim();
      const s = text.length > 200 ? text.slice(0, 200) + "…" : text;
      return s || "—";
    },
    isCommentDeleted(row) {
      const d = String(row.commentDelFlag ?? row.delFlag ?? "");
      return d === "2";
    },
    getList() {
      this.loading = true;
      listCommentAdmin(this.queryParams)
        .then((response) => {
          this.commentList = response.rows || response.data || [];
          this.total = response.total || 0;
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleDelete(row) {
      this.$modal
        .confirm('是否确认删除该评论？删除后前台将不再展示，可在列表中恢复。')
        .then(() =>
          setCommentDelFlag({ commentId: row.commentId, commentDelFlag: 2 })
        )
        .then(() => {
          this.$modal.msgSuccess("删除成功");
          this.getList();
        })
        .catch(() => {});
    },
    handleRestore(row) {
      this.$modal
        .confirm("是否确认恢复该评论？恢复后将重新在前台展示。")
        .then(() =>
          setCommentDelFlag({ commentId: row.commentId, commentDelFlag: 0 })
        )
        .then(() => {
          this.$modal.msgSuccess("恢复成功");
          this.getList();
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped>
.link-type {
  color: #409eff;
}
.link-type:hover {
  text-decoration: underline;
}
</style>
