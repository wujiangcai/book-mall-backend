<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="用户管理" :bordered="false">
      <a-space wrap>
        <a-input v-model="query.keyword" placeholder="关键字" allow-clear style="width: 220px" />
        <a-button type="primary" @click="load">搜索</a-button>
        <a-button status="success" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(1)">
          批量启用
        </a-button>
        <a-button status="warning" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(0)">
          批量禁用
        </a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false">
      <a-table
        row-key="id"
        :data="list"
        :pagination="false"
        :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
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
                :checked="record.status === 1"
                checked-text="启用"
                unchecked-text="禁用"
                @change="(val) => updateStatus(record.id, val ? 1 : 0)"
              />
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </a-space>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import adminUserApi from '../../api/admin/user'
import type { AdminUserListItem } from '../../types/api'

const list = ref<AdminUserListItem[]>([])
const selectedRowKeys = ref<number[]>([])

const query = reactive({
  keyword: '',
})

const load = async () => {
  const data = (await adminUserApi.list({ keyword: query.keyword || undefined })) as any
  list.value = data?.list || []
}

const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const updateStatus = async (id: number, status: number) => {
  await adminUserApi.updateStatus(id, { status })
  await load()
}

const batchUpdateStatus = async (status: number) => {
  await Promise.all(selectedRowKeys.value.map((id) => adminUserApi.updateStatus(id, { status })))
  selectedRowKeys.value = []
  await load()
}

const selectedIds = selectedRowKeys

onMounted(load)
</script>
