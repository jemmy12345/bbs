<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="环节" prop="stageCode">
        <el-select v-model="queryParams.stageCode" clearable placeholder="评审环节">
          <el-option label="初审" value="L1" />
          <el-option label="复审" value="L2" />
          <el-option label="终审" value="L3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="状态">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['bbs:ideaStageConfig:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['bbs:ideaStageConfig:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['bbs:ideaStageConfig:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置ID" align="center" prop="configId" width="100" />
      <el-table-column label="环节编码" align="center" prop="stageCode" width="100" />
      <el-table-column label="环节名称" align="center" prop="stageName" width="120" />
      <el-table-column label="角色标识" align="center" prop="roleKeys" min-width="240" :show-overflow-tooltip="true" />
      <el-table-column label="指定处理人" align="center" min-width="220" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ reviewerUserLabel(scope.row.reviewerUserIds) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['bbs:ideaStageConfig:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['bbs:ideaStageConfig:remove']">删除</el-button>
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

    <el-dialog :title="title" :visible.sync="open" width="620px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="评审环节" prop="stageCode">
          <el-select v-model="form.stageCode" style="width:100%" :disabled="form.configId !== undefined" @change="handleStageChange">
            <el-option label="初审" value="L1" />
            <el-option label="复审" value="L2" />
            <el-option label="终审" value="L3" />
          </el-select>
        </el-form-item>
        <el-form-item label="候选角色">
          <el-tag type="info">{{ stageRoleLabel(form.stageCode) }}</el-tag>
          <div class="form-tip">候选处理人来自该角色；如需新增候选人，请先在用户管理或角色管理中分配对应评审角色。</div>
        </el-form-item>
        <el-form-item label="指定处理人" prop="reviewerUserIdArray">
          <el-select
            v-model="form.reviewerUserIdArray"
            multiple
            filterable
            style="width:100%"
            :disabled="!form.stageCode"
            :placeholder="form.stageCode ? '请选择本环节实际处理人' : '请先选择评审环节'"
          >
            <el-option
              v-for="user in candidateUserOptions"
              :key="user.userId"
              :label="user.nickName + '（' + user.userName + '）'"
              :value="String(user.userId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIdeaStageConfig, getIdeaStageConfig, addIdeaStageConfig, updateIdeaStageConfig, delIdeaStageConfig, listReviewerOptions } from '@/api/bbs/ideaStageConfig'
import { listUser } from '@/api/system/user'

export default {
  name: 'BbsIdeaStageConfig',
  data() {
    return {
      loading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      configList: [],
      allUserOptions: [],
      candidateUserOptions: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        stageCode: undefined,
        status: undefined,
      },
      form: {},
      rules: {
        stageCode: [{ required: true, message: '环节编码不能为空', trigger: 'change' }],
        stageName: [{ required: true, message: '环节名称不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
      },
    }
  },
  created() {
    this.getList()
    this.loadAllUserOptions()
  },
  methods: {
    getList() {
      this.loading = true
      listIdeaStageConfig(this.queryParams).then(response => {
        this.configList = response.rows || []
        this.total = response.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    loadAllUserOptions() {
      listUser({ pageNum: 1, pageSize: 200, status: '0' }).then(response => {
        this.allUserOptions = response.rows || []
      })
    },
    loadReviewerOptions(stageCode) {
      this.candidateUserOptions = []
      if (!stageCode) return Promise.resolve([])
      return listReviewerOptions(stageCode).then(response => {
        const users = response.data || []
        this.candidateUserOptions = users
        const allowed = users.map(user => String(user.userId))
        this.form.reviewerUserIdArray = (this.form.reviewerUserIdArray || []).filter(id => allowed.includes(String(id)))
        return users
      })
    },
    reviewerUserLabel(userIds) {
      if (!userIds) return '-'
      const userMap = {}
      this.allUserOptions.forEach(user => {
        userMap[String(user.userId)] = user.nickName + '（' + user.userName + '）'
      })
      return userIds.split(',').map(id => {
        const key = id.trim()
        return userMap[key] || key
      }).join('，')
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        configId: undefined,
        stageCode: undefined,
        stageName: undefined,
        roleKeys: undefined,
        reviewerUserIds: undefined,
        reviewerUserIdArray: [],
        status: '0',
        remark: undefined,
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增提案评审配置'
    },
    handleUpdate(row) {
      this.reset()
      const configId = row.configId || this.ids[0]
      getIdeaStageConfig(configId).then(response => {
        const data = response.data || {}
        this.form = {
          configId: data.configId,
          stageCode: data.stageCode,
          stageName: data.stageName,
          roleKeys: data.roleKeys,
          reviewerUserIds: data.reviewerUserIds,
          reviewerUserIdArray: this.toArray(data.reviewerUserIds),
          status: data.status || '0',
          remark: data.remark
        }
        this.loadReviewerOptions(this.form.stageCode)
        this.open = true
        this.title = '修改提案评审配置'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (!this.form.reviewerUserIdArray || this.form.reviewerUserIdArray.length === 0) {
          this.$modal.msgError('请至少选择一名' + this.stageName(this.form.stageCode) + '处理人')
          return
        }
        const payload = {
          ...this.form,
          stageName: this.stageName(this.form.stageCode),
          roleKeys: this.stageRoleKey(this.form.stageCode),
          reviewerUserIds: (this.form.reviewerUserIdArray || []).join(',')
        }
        if (payload.configId !== undefined) {
          updateIdeaStageConfig(payload).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addIdeaStageConfig(payload).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleStageChange(stageCode) {
      this.form.stageName = this.stageName(stageCode)
      this.form.roleKeys = this.stageRoleKey(stageCode)
      this.form.reviewerUserIdArray = []
      this.loadReviewerOptions(stageCode)
    },
    stageName(stageCode) {
      const map = { L1: '初审', L2: '复审', L3: '终审' }
      return map[stageCode] || '评审'
    },
    stageRoleKey(stageCode) {
      const map = { L1: 'idea_l1_reviewer', L2: 'idea_l2_reviewer', L3: 'idea_l3_reviewer' }
      return map[stageCode] || ''
    },
    stageRoleLabel(stageCode) {
      const map = {
        L1: '初审员（idea_l1_reviewer）',
        L2: '复审员（idea_l2_reviewer）',
        L3: '终审员（idea_l3_reviewer）'
      }
      return map[stageCode] || '请选择评审环节'
    },
    toArray(value) {
      return value ? value.split(',').map(item => item.trim()).filter(Boolean) : []
    },
    handleDelete(row) {
      const configIds = row.configId || this.ids
      this.$modal.confirm('是否确认删除提案评审配置编号为"' + configIds + '"的数据项？').then(() => {
        return delIdeaStageConfig(configIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
  },
}
</script>

<style scoped>
.form-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>
