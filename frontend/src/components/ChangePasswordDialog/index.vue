<template>
  <el-dialog
    title="修改密码"
    :visible.sync="dialogVisible"
    :width="dialogWidth"
    :close-on-click-modal="false"
    append-to-body
    @closed="resetForm"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" placeholder="请输入旧密码" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" placeholder="请输入新密码" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          placeholder="请确认新密码"
          type="password"
          show-password
          @keyup.enter.native="submit"
        />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { updateUserPwd } from "@/api/system/user";

export default {
  name: "ChangePasswordDialog",
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.form.newPassword !== value) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };
    return {
      submitting: false,
      dialogWidth: window.innerWidth <= 768 ? "90%" : "420px",
      form: {
        oldPassword: undefined,
        newPassword: undefined,
        confirmPassword: undefined
      },
      rules: {
        oldPassword: [
          { required: true, message: "旧密码不能为空", trigger: "blur" }
        ],
        newPassword: [
          { required: true, message: "新密码不能为空", trigger: "blur" },
          { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\ |", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "确认密码不能为空", trigger: "blur" },
          { validator: equalToPassword, trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      }
    }
  },
  methods: {
    submit() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return;
        }
        this.submitting = true;
        updateUserPwd(this.form.oldPassword, this.form.newPassword)
          .then(() => {
            this.$modal.msgSuccess("修改成功");
            this.dialogVisible = false;
          })
          .finally(() => {
            this.submitting = false;
          });
      });
    },
    resetForm() {
      this.form = { oldPassword: undefined, newPassword: undefined, confirmPassword: undefined };
      if (this.$refs.form) {
        this.$refs.form.clearValidate();
      }
    }
  }
};
</script>
