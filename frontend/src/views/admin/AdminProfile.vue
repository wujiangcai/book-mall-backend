<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="个人信息" :bordered="false">
      <template v-if="authStore.userInfo">
        <a-descriptions :column="1">
          <a-descriptions-item label="用户名">{{ authStore.userInfo.username }}</a-descriptions-item>
          <a-descriptions-item label="昵称">{{ authStore.userInfo.nickname || '-' }}</a-descriptions-item>
          <a-descriptions-item label="手机号">{{ authStore.userInfo.phone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ authStore.userInfo.email || '-' }}</a-descriptions-item>
        </a-descriptions>
        <a-space style="margin-top: 16px">
          <a-button type="primary" @click="openEdit">编辑资料</a-button>
          <a-button @click="openPassword">修改密码</a-button>
        </a-space>
      </template>
      <a-empty v-else description="暂无用户信息" />
    </a-card>

    <a-modal v-model:visible="visible" title="编辑资料" :ok-loading="submitting" :ok-button-props="{ disabled: submitting }" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="用户名">
          <a-input v-model="form.username" disabled />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model="form.nickname" placeholder="昵称" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model="form.phone" placeholder="11位手机号" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model="form.email" placeholder="邮箱" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="passwordVisible"
      title="修改密码"
      :ok-loading="passwordSubmitting"
      :ok-button-props="{ disabled: passwordSubmitting }"
      @ok="submitPassword"
    >
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item label="旧密码">
          <a-input-password v-model="passwordForm.oldPassword" placeholder="请输入旧密码" />
        </a-form-item>
        <a-form-item label="新密码">
          <a-input-password v-model="passwordForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认新密码">
          <a-input-password v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useAuthStore } from '../../store/auth'
import adminUserApi from '../../api/admin/user'
import frontUserApi from '../../api/front/user'

const authStore = useAuthStore()
const visible = ref(false)
const submitting = ref(false)
const passwordVisible = ref(false)
const passwordSubmitting = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const ensureUserInfo = async () => {
  if (!authStore.userInfo && authStore.isAuthed) {
    await authStore.fetchUserInfo()
  }
}

const openEdit = () => {
  const info = authStore.userInfo
  if (!info) {
    Message.warning('暂无可编辑的用户信息')
    return
  }
  Object.assign(form, {
    username: info.username || '',
    nickname: info.nickname || '',
    phone: info.phone || '',
    email: info.email || '',
  })
  visible.value = true
}

const openPassword = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisible.value = true
}

const validateForm = () => {
  const phone = form.phone?.trim()
  const email = form.email?.trim()
  if (phone && !/^[0-9]{11}$/.test(phone)) {
    Message.error('手机号需为11位数字')
    return false
  }
  if (email && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
    Message.error('邮箱格式不正确')
    return false
  }
  return true
}

const validatePassword = () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    Message.error('请填写完整密码信息')
    return false
  }
  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
    Message.error('新密码长度必须在6-20之间')
    return false
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    Message.error('两次输入的新密码不一致')
    return false
  }
  return true
}

const submit = async () => {
  if (!validateForm()) return
  submitting.value = true
  try {
    await frontUserApi.updateInfo({
      nickname: form.nickname?.trim() || undefined,
      phone: form.phone?.trim() || undefined,
      email: form.email?.trim() || undefined,
    })
    Message.success('资料已更新')
    visible.value = false
    await authStore.fetchUserInfo()
  } catch (err: any) {
    Message.error(err?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const submitPassword = async () => {
  if (!validatePassword()) return
  passwordSubmitting.value = true
  try {
    await adminUserApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    Message.success('密码已更新，请重新登录')
    passwordVisible.value = false
    authStore.clear()
    window.location.href = '/admin/login'
  } catch (err: any) {
    Message.error(err?.message || '更新失败')
  } finally {
    passwordSubmitting.value = false
  }
}

onMounted(ensureUserInfo)
</script>
