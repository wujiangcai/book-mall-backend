<template>
  <div class="auth-page">
    <a-card title="前台注册" :bordered="false" class="auth-card">
      <a-form :model="form" layout="vertical" @submit="handleRegister">
        <a-form-item label="用户名">
          <a-input v-model="form.username" placeholder="用户名" autocomplete="username" @blur="() => validateField('username')" />
          <div class="helper" :class="helperClass('username')">{{ helperText('username') }}</div>
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="form.password" placeholder="密码" autocomplete="new-password" @blur="() => validateField('password')" />
          <div class="helper" :class="helperClass('password')">{{ helperText('password') }}</div>
        </a-form-item>
        <a-form-item label="确认密码">
          <a-input-password v-model="form.confirmPassword" placeholder="确认密码" autocomplete="new-password" @blur="() => validateField('confirmPassword')" />
          <div class="helper" :class="helperClass('confirmPassword')">{{ helperText('confirmPassword') }}</div>
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model="form.phone" placeholder="手机号" @blur="() => validateField('phone')" />
          <div class="helper" :class="helperClass('phone')">{{ helperText('phone') }}</div>
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model="form.nickname" placeholder="昵称（可选）" @blur="() => validateField('nickname')" />
          <div class="helper" :class="helperClass('nickname')">{{ helperText('nickname') }}</div>
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model="form.email" placeholder="邮箱（可选）" @blur="() => validateField('email')" />
          <div class="helper" :class="helperClass('email')">{{ helperText('email') }}</div>
        </a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" :disabled="!isFormValid" long>注册</a-button>
      </a-form>
      <a-alert v-if="error" type="error" :title="error" class="error" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import frontAuthApi from '../../api/front/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  nickname: '',
  email: '',
})

const touched = reactive({
  username: false,
  password: false,
  confirmPassword: false,
  phone: false,
  nickname: false,
  email: false,
})

const validators = {
  username: (value: string) => {
    if (!value) return '4-20 位字母、数字或下划线'
    if (!/^[A-Za-z0-9_]{4,20}$/.test(value)) return '格式不正确'
    return ''
  },
  password: (value: string) => {
    if (!value) return '6-20 位，需包含字母和数字'
    if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/.test(value)) return '密码必须包含字母和数字'
    return ''
  },
  confirmPassword: (value: string) => {
    if (!value) return '请再次输入密码'
    if (value !== form.password) return '两次密码不一致'
    return ''
  },
  phone: (value: string) => {
    if (!value) return '11 位手机号'
    if (!/^1\d{10}$/.test(value)) return '手机号格式错误'
    return ''
  },
  nickname: (value: string) => {
    if (!value) return '2-20 个字符，可选'
    if (value.length < 2 || value.length > 20) return '昵称需 2-20 个字符'
    return ''
  },
  email: (value: string) => {
    if (!value) return '请输入邮箱，可选'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return '邮箱格式不正确'
    return ''
  },
}

const optionalFields: Array<keyof typeof form> = ['nickname', 'email']

const validateField = (field: keyof typeof form) => {
  touched[field] = true
}

const helperState = (field: keyof typeof form) => {
  const value = String(form[field] ?? '')
  const isOptional = optionalFields.includes(field)
  if (isOptional && !touched[field]) return { text: '', className: '' }
  if (!value) {
    return { text: validators[field](value), className: 'is-muted' }
  }
  const message = validators[field](value)
  if (message) return { text: message, className: 'is-error' }
  return { text: '✅', className: 'is-success' }
}

const helperText = (field: keyof typeof form) => helperState(field).text

const helperClass = (field: keyof typeof form) => helperState(field).className

const requiredFields: Array<keyof typeof form> = ['username', 'password', 'confirmPassword', 'phone']

const isFormValid = computed(() => {
  return (Object.keys(validators) as Array<keyof typeof validators>).every((key) => {
    const value = String(form[key] ?? '')
    if (!requiredFields.includes(key) && !value) return true
    return !validators[key](value)
  })
})

const validateAll = () => {
  ;(Object.keys(touched) as Array<keyof typeof touched>).forEach((key) => {
    touched[key] = true
  })
  return isFormValid.value
}

const handleRegister = async () => {
  error.value = ''
  if (!validateAll()) return
  loading.value = true
  try {
    await frontAuthApi.register({
      username: form.username,
      password: form.password,
      phone: form.phone,
      nickname: form.nickname || undefined,
      email: form.email || undefined,
    })
    await router.replace('/login')
  } catch (err: any) {
    error.value = err?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.auth-card {
  width: 360px;
}

.error {
  margin-top: 12px;
}

.helper {
  font-size: 12px;
  margin-top: 6px;
}

.helper.is-muted {
  color: #9ca3af;
}

.helper.is-error {
  color: #dc2626;
}

.helper.is-success {
  color: #16a34a;
}
</style>
