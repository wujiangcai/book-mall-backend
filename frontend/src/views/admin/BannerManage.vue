<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="轮播管理" :bordered="false">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增轮播</a-button>
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
          <a-table-column title="图片">
            <template #cell="{ record }">
              <img :src="record.imageUrl" alt="banner" class="banner-img" />
            </template>
          </a-table-column>
          <a-table-column title="链接" data-index="linkUrl" />
          <a-table-column title="排序" data-index="sortOrder" :width="100" />
          <a-table-column title="状态" :width="120">
            <template #cell="{ record }">
              <a-switch
                :checked="record.status === 1"
                checked-text="启用"
                unchecked-text="禁用"
                @change="(val) => updateStatus(record, val ? 1 : 0)"
              />
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="200">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" @click="openEdit(record)">编辑</a-button>
                <a-button size="mini" status="danger" @click="remove(record.id)">删除</a-button>
                <a-button size="mini" @click="updateSort(record)">更新排序</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-if="list.length === 0" description="暂无轮播" />
    </a-card>

    <a-modal v-model:visible="visible" :title="editing ? '编辑轮播' : '新增轮播'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="图片 URL">
          <a-input v-model="form.imageUrl" />
        </a-form-item>
        <a-form-item label="跳转链接">
          <a-input v-model="form.linkUrl" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model="form.sortOrder" :min="0" />
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
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import adminBannerApi from '../../api/admin/banner'
import type { Banner } from '../../types/api'

const list = ref<Banner[]>([])
const visible = ref(false)
const editing = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])

const form = reactive({
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1,
})

const load = async () => {
  list.value = (await adminBannerApi.list().catch(() => [])) as any
}

const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const openCreate = () => {
  editing.value = false
  editingId.value = null
  Object.assign(form, {
    imageUrl: '',
    linkUrl: '',
    sortOrder: 0,
    status: 1,
  })
  visible.value = true
}

const openEdit = (record: Banner) => {
  editing.value = true
  editingId.value = record.id
  Object.assign(form, {
    imageUrl: record.imageUrl,
    linkUrl: record.linkUrl || '',
    sortOrder: record.sortOrder,
    status: record.status,
  })
  visible.value = true
}

const submit = async () => {
  if (editing.value && editingId.value) {
    await adminBannerApi.update(editingId.value, { ...form })
    Message.success('轮播已更新')
  } else {
    await adminBannerApi.create({ ...form })
    Message.success('轮播已创建')
  }
  visible.value = false
  await load()
}

const updateStatus = async (record: Banner, status: number) => {
  await adminBannerApi.update(record.id, {
    imageUrl: record.imageUrl,
    linkUrl: record.linkUrl,
    sortOrder: record.sortOrder,
    status,
  })
  Message.success('状态已更新')
  await load()
}

const updateSort = async (record: Banner) => {
  await adminBannerApi.updateSort(record.id, { sortOrder: record.sortOrder })
  Message.success('排序已更新')
  await load()
}

const remove = async (id: number) => {
  await adminBannerApi.remove(id)
  Message.success('轮播已删除')
  await load()
}

const batchRemove = async () => {
  await Promise.all(selectedRowKeys.value.map((id) => adminBannerApi.remove(id)))
  Message.success('批量删除完成')
  selectedRowKeys.value = []
  await load()
}

const selectedIds = selectedRowKeys

onMounted(load)
</script>

<style scoped>
.banner-img {
  width: 120px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}
</style>
