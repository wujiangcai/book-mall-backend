<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="用户管理" :bordered="false">
      <a-space wrap>
        <a-input v-model="query.keyword" placeholder="关键字" allow-clear style="width: 220px" />
        <a-button type="primary" @click="load">搜索</a-button>
        <a-button type="primary" @click="openCreate">新增用户</a-button>
        <a-button status="success" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(1)">
          批量启用
        </a-button>
        <a-button status="warning" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(0)">
          批量禁用
        </a-button>
        <a-button status="danger" :disabled="selectedIds.length === 0" @click="batchDisable">
          批量禁用(删除)
        </a-button>
      </a-space>
    </a-card>

    <a-card v-if="isRoles" :bordered="false" class="roles-panel">
      <div class="roles-title">权限管理</div>
      <div class="roles-desc">配置管理员与用户权限等级。</div>
      <a-space>
        <a-button type="primary">新建角色</a-button>
        <a-button>导出权限</a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false">
      <a-table
        row-key="id"
        :data="list"
        :pagination="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true, selectedRowKeys }"
        @selection-change="onSelectChange"
      >
        <template #columns>
          <a-table-column title="用户名" data-index="username" />
          <a-table-column title="昵称" data-index="nickname" />
          <a-table-column title="手机号" data-index="phone" />
          <a-table-column title="角色" :width="120">
            <template #cell="{ record }">{{ record.role === 1 ? '管理员' : '用户' }}</template>
          </a-table-column>
          <a-table-column title="状态" :width="120">
            <template #cell="{ record }">
              <a-switch
                :model-value="Number(record.status) === 1"
                checked-text="启用"
                unchecked-text="禁用"
                @change="(val) => updateStatus(record.id, val ? 1 : 0)"
              />
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="180">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" @click="openEdit(record)">编辑</a-button>
                <a-button size="mini" status="warning" @click="resetPassword(record)">重置</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-if="list.length === 0" description="暂无用户" />

      <div class="pager">
        <a-pagination
          :current="query.page"
          :page-size="query.pageSize"
          :total="total"
          show-total
          show-jumper
          show-page-size
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </a-card>

    <a-modal v-model:visible="visible" :title="editing ? '编辑用户' : '新增用户'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="用户名">
          <a-input v-model="form.username" />
        </a-form-item>
        <a-form-item v-if="!editing" label="密码">
          <a-input-password v-model="form.password" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model="form.nickname" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model="form.phone" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model="form.email" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model="form.role">
            <a-option :value="1">管理员</a-option>
            <a-option :value="0">用户</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="form.status">
            <a-option :value="1">启用</a-option>
            <a-option :value="0">禁用</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import adminUserApi from '../../api/admin/user'
import type { AdminUserListItem } from '../../types/api'

const route = useRoute()
const list = ref<AdminUserListItem[]>([])
const total = ref(0)
const visible = ref(false)
const editing = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
})

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  role: 0,
  status: 1,
})

const load = async () => {
  const data = (await adminUserApi.list({
    page: query.page,
    pageSize: query.pageSize,
    keyword: query.keyword || undefined,
  })) as any
  list.value = data?.list || []
  total.value = data?.total || 0
}

const handlePageChange = (page: number) => {
  query.page = page
  load()
}

const handlePageSizeChange = (pageSize: number) => {
  query.pageSize = pageSize
  query.page = 1
  load()
}

const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const updateStatus = async (id: number, status: number) => {
  await adminUserApi.updateStatus(id, { status })
  Message.success('状态已更新')
  await load()
}

const openCreate = () => {
  editing.value = false
  editingId.value = null
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    phone: '',
    email: '',
    role: 0,
    status: 1,
  })
  visible.value = true
}

const openEdit = (record: AdminUserListItem) => {
  editing.value = true
  editingId.value = record.id
  Object.assign(form, {
    username: record.username,
    password: '',
    nickname: record.nickname || '',
    phone: record.phone || '',
    email: record.email || '',
    role: record.role,
    status: record.status,
  })
  visible.value = true
}

const submit = async () => {
  if (editing.value && editingId.value) {
    await adminUserApi.update(editingId.value, {
      username: form.username,
      nickname: form.nickname,
      phone: form.phone || undefined,
      email: form.email || undefined,
      role: form.role,
      status: form.status,
    })
    Message.success('用户已更新')
  } else {
    await adminUserApi.create({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone || undefined,
      email: form.email || undefined,
      role: form.role,
      status: form.status,
    })
    Message.success('用户已创建')
  }
  visible.value = false
  await load()
}

const resetPassword = async (record: AdminUserListItem) => {
  Modal.confirm({
    title: '确认重置密码',
    content: `确认将 ${record.username} 的密码重置为 123456 吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      await adminUserApi.resetPassword(record.id)
      Message.success('密码已重置为 123456')
      await load()
    },
  })
}

const batchUpdateStatus = async (status: number) => {
  await Promise.all(selectedRowKeys.value.map((id) => adminUserApi.updateStatus(id, { status })))
  Message.success('批量状态已更新')
  selectedRowKeys.value = []
  await load()
}

const batchDisable = async () => {
  await Promise.all(selectedRowKeys.value.map((id) => adminUserApi.updateStatus(id, { status: 0 })))
  Message.success('批量禁用完成')
  selectedRowKeys.value = []
  await load()
}

const selectedIds = selectedRowKeys
const isRoles = computed(() => route.path.endsWith('/roles'))

onMounted(load)
</script>

<style scoped>
.roles-panel {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.roles-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.roles-desc {
  margin: 8px 0 16px;
  color: #64748b;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
