<template>
  <el-dialog
    :title="$t('bbsExtra.changePassword.title')"
    :visible.sync="dialogVisible"
    :width="dialogWidth"
    :close-on-click-modal="false"
    append-to-body
    @closed="resetForm"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item :label="$t('bbsExtra.changePassword.oldPassword')" prop="oldPassword">
        <el-input v-model="form.oldPassword" :placeholder="$t('bbsExtra.changePassword.oldPasswordPh')" type="password" show-password />
      </el-form-item>
      <el-form-item :label="$t('bbsExtra.changePassword.newPassword')" prop="newPassword">
        <el-input v-model="form.newPassword" :placeholder="$t('bbsExtra.changePassword.newPasswordPh')" type="password" show-password />
      </el-form-item>
      <el-form-item :label="$t('bbsExtra.changePassword.confirmPassword')" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          :placeholder="$t('bbsExtra.changePassword.confirmPasswordPh')"
          type="password"
          show-password
          @keyup.enter.native="submit"
        />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">{{ $t('common.confirm') }}</el-button>
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
    return {
      submitting: false,
      dialogWidth: window.innerWidth <= 768 ? "90%" : "420px",
      form: {
        oldPassword: undefined,
        newPassword: undefined,
        confirmPassword: undefined
      }
    };
  },
  computed: {
    rules() {
      const equalToPassword = (rule, value, callback) => {
        if (this.form.newPassword !== value) {
          callback(new Error(this.$t('bbsExtra.changePassword.mismatch')));
        } else {
          callback();
        }
      };
      return {
        oldPassword: [
          { required: true, message: this.$t('bbsExtra.changePassword.oldRequired'), trigger: "blur" }
        ],
        newPassword: [
          { required: true, message: this.$t('bbsExtra.changePassword.newRequired'), trigger: "blur" },
          { min: 6, max: 20, message: this.$t('bbsExtra.changePassword.lengthRange'), trigger: "blur" },
          { pattern: /^[^<>"'|\\]+$/, message: this.$t('bbsExtra.changePassword.illegalChars'), trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: this.$t('bbsExtra.changePassword.confirmRequired'), trigger: "blur" },
          { validator: equalToPassword, trigger: "blur" }
        ]
      };
    },
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
            this.$modal.msgSuccess(this.$t('bbsExtra.changePassword.success'));
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
