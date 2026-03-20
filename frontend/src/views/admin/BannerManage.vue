<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="轮播管理" :bordered="false">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增轮播</a-button>
        <a-button status="danger" :disabled="selectedIds.length === 0" @click="batchRemove">批量删除</a-button>
      </a-space>
    </a-card>

    <a-card v-if="isCoupon" :bordered="false" class="notice-panel">
      <div class="notice-title">优惠券管理</div>
      <div class="notice-desc">配置优惠券规则与发放策略。</div>
      <a-space>
        <a-button type="primary">新建优惠券</a-button>
        <a-button>导出规则</a-button>
      </a-space>
    </a-card>

    <a-card v-if="isNotice" :bordered="false" class="notice-panel">
      <div class="notice-title">公告管理</div>
      <div class="notice-desc">发布后台公告与运营通知。</div>
      <a-space>
        <a-button type="primary">新建公告</a-button>
        <a-button>查看记录</a-button>
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
                :model-value="Number(record.status) === 1"
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

    <a-modal v-model:visible="visible" :title="editing ? '编辑轮播' : '新增轮播'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="图片">
          <div class="upload-panel">
            <input
              ref="fileInputRef"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              class="hidden-file-input"
              @change="handleUpload"
            />
            <a-button class="upload-trigger" @click="triggerFileSelect">选择图片</a-button>
            <div class="file-name">{{ selectedFileName || '未选择文件' }}</div>
            <a-input v-model="form.imageUrl" placeholder="上传后自动填充" />
            <img v-if="form.imageUrl" :src="form.imageUrl" alt="banner-preview" class="upload-preview" />
          </div>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import adminBannerApi from '../../api/admin/banner'
import type { Banner } from '../../types/api'

const route = useRoute()
const list = ref<Banner[]>([])
const total = ref(0)
const visible = ref(false)
const editing = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedFileName = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)

const query = reactive({
  page: 1,
  pageSize: 10,
})

const form = reactive({
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1,
})

const load = async () => {
  const data = (await adminBannerApi.list({ page: query.page, pageSize: query.pageSize }).catch(() => null)) as any
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

const resetUploadState = () => {
  selectedFileName.value = ''
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

const openCreate = () => {
  editing.value = false
  editingId.value = null
  resetUploadState()
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
  resetUploadState()
  Object.assign(form, {
    imageUrl: record.imageUrl,
    linkUrl: record.linkUrl || '',
    sortOrder: record.sortOrder,
    status: record.status,
  })
  visible.value = true
}

const handleUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  selectedFileName.value = file.name
  try {
    const url = (await adminBannerApi.upload(file)) as unknown as string
    form.imageUrl = url
    Message.success('上传成功')
  } catch (error: any) {
    selectedFileName.value = ''
    Message.error(error?.message || '上传失败')
  } finally {
    target.value = ''
  }
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
  resetUploadState()
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
const isCoupon = computed(() => route.path.endsWith('/coupon'))
const isNotice = computed(() => route.path.endsWith('/notice'))

onMounted(load)
</script>

<style scoped>
.banner-img {
  width: 120px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.notice-panel {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.notice-desc {
  margin: 8px 0 16px;
  color: #64748b;
}

.upload-panel {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  width: 100%;
}

.upload-trigger {
  min-width: 96px;
}

.hidden-file-input {
  display: none;
}

.file-name {
  width: 100%;
  min-height: 22px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
}

.upload-preview {
  width: 180px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.08);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
