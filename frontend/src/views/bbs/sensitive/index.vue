<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="敏感词" prop="word">
        <el-input
          v-model="queryParams.word"
          placeholder="请输入敏感词"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="敏感词类型" prop="wordType">
        <el-select v-model="queryParams.wordType" placeholder="敏感词类型" clearable>
          <el-option label="政治敏感" value="1" />
          <el-option label="色情低俗" value="2" />
          <el-option label="暴力血腥" value="3" />
          <el-option label="广告营销" value="4" />
          <el-option label="其他" value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['bbs:sensitive:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['bbs:sensitive:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['bbs:sensitive:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['bbs:sensitive:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="sensitiveWordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="敏感词ID" align="center" prop="wordId" width="100" />
      <el-table-column label="敏感词内容" align="center" prop="word" :show-overflow-tooltip="true" />
      <el-table-column label="敏感词类型" align="center" prop="wordType" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.wordType === '1'" type="danger">政治敏感</el-tag>
          <el-tag v-else-if="scope.row.wordType === '2'" type="warning">色情低俗</el-tag>
          <el-tag v-else-if="scope.row.wordType === '3'" type="danger">暴力血腥</el-tag>
          <el-tag v-else-if="scope.row.wordType === '4'" type="info">广告营销</el-tag>
          <el-tag v-else-if="scope.row.wordType === '5'">其他</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bbs:sensitive:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bbs:sensitive:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改敏感词对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="敏感词内容" prop="word">
          <el-input v-model="form.word" placeholder="请输入敏感词内容" />
        </el-form-item>
        <el-form-item label="敏感词类型" prop="wordType">
          <el-select v-model="form.wordType" placeholder="请选择敏感词类型" style="width: 100%">
            <el-option label="政治敏感" value="1" />
            <el-option label="色情低俗" value="2" />
            <el-option label="暴力血腥" value="3" />
            <el-option label="广告营销" value="4" />
            <el-option label="其他" value="5" />
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
import { listSensitiveWord, getSensitiveWord, delSensitiveWord, addSensitiveWord, updateSensitiveWord } from "@/api/bbs/sensitive"

export default {
  name: "SensitiveWord",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 敏感词表格数据
      sensitiveWordList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        word: undefined,
        wordType: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        word: [
          { required: true, message: "敏感词内容不能为空", trigger: "blur" }
        ],
        wordType: [
          { required: true, message: "敏感词类型不能为空", trigger: "change" }
        ],
        status: [
          { required: true, message: "状态不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询敏感词列表 */
    getList() {
      this.loading = true
      listSensitiveWord(this.queryParams).then(response => {
          this.sensitiveWordList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        wordId: undefined,
        word: undefined,
        wordType: undefined,
        status: "0",
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加敏感词"
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.wordId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const wordId = row.wordId || this.ids
      getSensitiveWord(wordId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改敏感词"
      })
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.wordId != undefined) {
            updateSensitiveWord(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addSensitiveWord(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const wordIds = row.wordId || this.ids
      this.$modal.confirm('是否确认删除敏感词编号为"' + wordIds + '"的数据项？').then(function() {
        return delSensitiveWord(wordIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bbs/sensitive/export', {
        ...this.queryParams
      }, `sensitive_word_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
