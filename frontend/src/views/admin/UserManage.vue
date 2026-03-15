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
                :model-value="Number(record.status) === 1"
                checked-text="启用"
                unchecked-text="禁用"
                @change="(val) => updateStatus(record.id, val ? 1 : 0)"
              />
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
  </a-space>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import adminUserApi from '../../api/admin/user'
import type { AdminUserListItem } from '../../types/api'

const route = useRoute()
const list = ref<AdminUserListItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
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

const batchUpdateStatus = async (status: number) => {
  await Promise.all(selectedRowKeys.value.map((id) => adminUserApi.updateStatus(id, { status })))
  Message.success('批量状态已更新')
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
