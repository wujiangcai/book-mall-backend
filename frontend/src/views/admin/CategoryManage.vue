<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="分类管理" :bordered="false">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增分类</a-button>
        <a-button status="success" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(1)">
          批量启用
        </a-button>
        <a-button status="warning" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(0)">
          批量禁用
        </a-button>
        <a-button status="danger" :disabled="selectedIds.length === 0" @click="batchRemove">批量删除</a-button>
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
          <a-table-column title="分类名" data-index="categoryName" />
          <a-table-column title="父级" :width="120">
            <template #cell="{ record }">{{ record.parentId || '-' }}</template>
          </a-table-column>
          <a-table-column title="排序" data-index="sortOrder" :width="120" />
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
          <a-table-column title="操作" :width="160">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" @click="openEdit(record)">编辑</a-button>
                <a-button size="mini" status="danger" @click="remove(record.id)">删除</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-if="list.length === 0" description="暂无分类" />

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

    <a-modal v-model:visible="visible" :title="editing ? '编辑分类' : '新增分类'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="分类名">
          <a-input v-model="form.categoryName" />
        </a-form-item>
        <a-form-item label="父级ID">
          <a-input-number v-model="form.parentId" :min="0" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model="form.sortOrder" :min="0" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import adminCategoryApi from '../../api/admin/category'
import type { CategoryAdminItem } from '../../types/api'

const list = ref<CategoryAdminItem[]>([])
const total = ref(0)
const visible = ref(false)
const editing = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
})

const form = reactive({
  categoryName: '',
  parentId: 0,
  sortOrder: 0,
})

const load = async () => {
  const data = (await adminCategoryApi.list({ page: query.page, pageSize: query.pageSize }).catch(() => null)) as any
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

const openCreate = () => {
  editing.value = false
  editingId.value = null
  Object.assign(form, {
    categoryName: '',
    parentId: 0,
    sortOrder: 0,
  })
  visible.value = true
}

const openEdit = (record: CategoryAdminItem) => {
  editing.value = true
  editingId.value = record.id
  Object.assign(form, {
    categoryName: record.categoryName,
    parentId: record.parentId,
    sortOrder: record.sortOrder,
  })
  visible.value = true
}

const submit = async () => {
  if (editing.value && editingId.value) {
    await adminCategoryApi.update(editingId.value, { ...form })
    Message.success('分类已更新')
  } else {
    await adminCategoryApi.create({ ...form })
    Message.success('分类已创建')
  }
  visible.value = false
  await load()
}

const updateStatus = async (id: number, status: number) => {
  await adminCategoryApi.updateStatus(id, { status })
  Message.success('状态已更新')
  await load()
}

const remove = async (id: number) => {
  await adminCategoryApi.remove(id)
  Message.success('分类已删除')
  await load()
}

const batchUpdateStatus = async (status: number) => {
  await Promise.all(selectedRowKeys.value.map((id) => adminCategoryApi.updateStatus(id, { status })))
  Message.success('批量状态已更新')
  selectedRowKeys.value = []
  await load()
}

const batchRemove = async () => {
  await Promise.all(selectedRowKeys.value.map((id) => adminCategoryApi.remove(id)))
  Message.success('批量删除完成')
  selectedRowKeys.value = []
  await load()
}

const selectedIds = selectedRowKeys

onMounted(load)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
