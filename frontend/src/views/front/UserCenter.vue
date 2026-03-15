<template>
  <a-card title="用户中心" :bordered="false">
    <template v-if="authStore.userInfo">
      <a-descriptions :column="1">
        <a-descriptions-item label="用户名">{{ authStore.userInfo.username }}</a-descriptions-item>
        <a-descriptions-item label="昵称">{{ authStore.userInfo.nickname || '-' }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ authStore.userInfo.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ authStore.userInfo.email || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-space style="margin-top: 16px">
        <a-button type="primary" @click="openEdit">编辑资料</a-button>
        <a-button @click="go('/address')">管理地址</a-button>
        <a-button @click="go('/orders')">我的订单</a-button>
        <a-button status="danger" @click="handleLogout">退出登录</a-button>
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
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useAuthStore } from '../../store/auth'
import frontUserApi from '../../api/front/user'

const router = useRouter()
const authStore = useAuthStore()
const visible = ref(false)
const submitting = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
})

const go = (path: string) => {
  router.push(path)
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

const handleLogout = async () => {
  authStore.clear()
  Message.success('已退出登录')
  await router.replace('/login')
}
</script>

