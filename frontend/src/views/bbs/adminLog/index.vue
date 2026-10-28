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
      <el-form-item label="管理员" prop="adminName">
        <el-input
          v-model="queryParams.adminName"
          placeholder="管理员姓名"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作模块" prop="operationModule">
        <el-input
          v-model="queryParams.operationModule"
          placeholder="操作模块"
          clearable
          style="width: 200px"
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

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="日志编号" align="center" prop="logId" width="88" />
      <el-table-column label="操作时间" align="center" prop="createdTime" width="168">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="管理员"
        align="center"
        prop="adminName"
        width="100"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="管理员账号"
        align="center"
        prop="adminId"
        min-width="160"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="操作类型" align="center" prop="operationType" width="100">
        <template slot-scope="scope">
          <span>{{ operationTypeLabel(scope.row.operationType) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作模块"
        align="center"
        prop="operationModule"
        min-width="120"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="操作描述"
        align="left"
        prop="operationDesc"
        min-width="220"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="请求地址"
        align="left"
        prop="requestUrl"
        min-width="200"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="请求参数"
        align="left"
        prop="requestParams"
        min-width="160"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="响应结果"
        align="left"
        prop="responseResult"
        min-width="160"
        :show-overflow-tooltip="true"
      />
      
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.status === undefined || scope.row.status === null">—</span>
          <el-tag v-else-if="isSuccessStatus(scope.row.status)" type="success" size="mini"
            >成功</el-tag
          >
          <el-tag v-else type="danger" size="mini">失败</el-tag>
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
import { listBbsAdminLog } from "@/api/bbs/adminLog";

/** 与后端 BbsAdminLogOperationType 枚举一致 */
const OPERATION_TYPE_LABELS = {
  ADD: "新增",
  UPDATE: "修改",
  DELETE: "删除",
  QUERY: "查询",
  EXPORT: "导出",
  IMPORT: "导入",
  APPROVE: "审核通过",
  REJECT: "审核驳回",
  OFFLINE: "下架",
  ONLINE: "上架",
  TOP: "置顶",
};

export default {
  name: "BbsAdminLog",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      logList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        adminName: undefined,
        operationModule: undefined,
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    operationTypeLabel(type) {
      if (type == null || type === "") return "—";
      const key = String(type).trim();
      return OPERATION_TYPE_LABELS[key] || key;
    },
    /** 接口示例 success 时 status 为 1；若贵司约定相反请再改此处 */
    isSuccessStatus(status) {
      return status === 1 || status === "1";
    },
    getList() {
      this.loading = true;
      listBbsAdminLog(this.queryParams)
        .then((response) => {
          this.logList = response.rows || response.data || [];
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
  },
};
</script>
