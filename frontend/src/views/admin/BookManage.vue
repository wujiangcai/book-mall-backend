<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="图书管理" :bordered="false">
      <a-space wrap>
        <a-input v-model="query.keyword" placeholder="关键字" allow-clear style="width: 200px" />
        <a-select v-model="query.categoryId" placeholder="分类" allow-clear style="width: 200px">
          <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.categoryName }}
          </a-option>
        </a-select>
        <a-button type="primary" @click="load">搜索</a-button>
        <a-button @click="openCreate">新增图书</a-button>
        <a-button status="success" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(1)">
          批量上架
        </a-button>
        <a-button status="warning" :disabled="selectedIds.length === 0" @click="batchUpdateStatus(0)">
          批量下架
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
          <a-table-column title="书名" data-index="bookName" />
          <a-table-column title="作者" data-index="author" />
          <a-table-column title="分类" data-index="categoryName" />
          <a-table-column title="价格" :width="120">
            <template #cell="{ record }">¥{{ record.price }}</template>
          </a-table-column>
          <a-table-column title="库存" data-index="stock" :width="100" />
          <a-table-column title="状态" :width="120">
            <template #cell="{ record }">
              <a-switch
                :checked="record.status === 1"
                checked-text="上架"
                unchecked-text="下架"
                @change="(val) => updateStatus(record.id, val ? 1 : 0)"
              />
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="180">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" @click="openEdit(record)">编辑</a-button>
                <a-button size="mini" status="danger" @click="remove(record.id)">删除</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-if="list.length === 0" description="暂无图书" />

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

    <a-card v-if="isStock" :bordered="false" class="stock-panel">
      <div class="stock-title">库存管理面板</div>
      <div class="stock-desc">根据库存预警与销量进行补货计划。</div>
      <a-space wrap>
        <a-button type="primary">生成补货建议</a-button>
        <a-button>导出库存报表</a-button>
      </a-space>
    </a-card>

    <a-modal v-model:visible="visible" :title="editing ? '编辑图书' : '新增图书'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="书名">
          <a-input v-model="form.bookName" />
        </a-form-item>
        <a-form-item label="作者">
          <a-input v-model="form.author" />
        </a-form-item>
        <a-form-item label="出版社">
          <a-input v-model="form.publisher" />
        </a-form-item>
        <a-form-item label="ISBN">
          <a-input v-model="form.isbn" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model="form.categoryId" placeholder="分类">
            <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.categoryName }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="价格">
          <a-input-number v-model="form.price" :min="0" />
        </a-form-item>
        <a-form-item label="库存">
          <a-input-number v-model="form.stock" :min="0" />
        </a-form-item>
        <a-form-item label="封面">
          <a-input v-model="form.coverImage" placeholder="封面 URL" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model="form.description" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="form.status">
            <a-option :value="1">上架</a-option>
            <a-option :value="0">下架</a-option>
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
import adminBookApi from '../../api/admin/book'
import adminCategoryApi from '../../api/admin/category'
import type { AdminBookListItem, CategoryAdminItem } from '../../types/api'

const route = useRoute()
const list = ref<AdminBookListItem[]>([])
const total = ref(0)
const categories = ref<CategoryAdminItem[]>([])
const visible = ref(false)
const editing = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  categoryId: undefined as number | undefined,
  keyword: '',
})

const form = reactive({
  bookName: '',
  author: '',
  publisher: '',
  isbn: '',
  categoryId: undefined as number | undefined,
  price: 0,
  stock: 0,
  coverImage: '',
  description: '',
  status: 1,
})

const loadCategories = async () => {
  categories.value = (await adminCategoryApi.list().catch(() => [])) as any
}

const load = async () => {
  const data = (await adminBookApi.list({
    page: query.page,
    pageSize: query.pageSize,
    categoryId: query.categoryId,
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

const openCreate = () => {
  editing.value = false
  editingId.value = null
  Object.assign(form, {
    bookName: '',
    author: '',
    publisher: '',
    isbn: '',
    categoryId: undefined,
    price: 0,
    stock: 0,
    coverImage: '',
    description: '',
    status: 1,
  })
  visible.value = true
}

const openEdit = (record: AdminBookListItem) => {
  editing.value = true
  editingId.value = record.id
  Object.assign(form, {
    bookName: record.bookName,
    author: record.author || '',
    publisher: record.publisher || '',
    isbn: record.isbn || '',
    categoryId: record.categoryId,
    price: record.price,
    stock: record.stock,
    coverImage: record.coverImage || '',
    description: '',
    status: record.status,
  })
  visible.value = true
}

const submit = async () => {
  if (!form.categoryId) return
  if (editing.value && editingId.value) {
    await adminBookApi.update(editingId.value, { ...form, categoryId: form.categoryId })
    Message.success('图书已更新')
  } else {
    await adminBookApi.create({ ...form, categoryId: form.categoryId })
    Message.success('图书已创建')
  }
  visible.value = false
  await load()
}

const updateStatus = async (id: number, status: number) => {
  await adminBookApi.updateStatus(id, { status })
  Message.success('状态已更新')
  await load()
}

const remove = async (id: number) => {
  await adminBookApi.remove(id)
  Message.success('图书已删除')
  await load()
}

const batchUpdateStatus = async (status: number) => {
  await Promise.all(selectedRowKeys.value.map((id) => adminBookApi.updateStatus(id, { status })))
  Message.success('批量状态已更新')
  selectedRowKeys.value = []
  await load()
}

const batchRemove = async () => {
  await Promise.all(selectedRowKeys.value.map((id) => adminBookApi.remove(id)))
  Message.success('批量删除完成')
  selectedRowKeys.value = []
  await load()
}

const selectedIds = selectedRowKeys
const isStock = computed(() => route.path.endsWith('/stock'))

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.stock-panel {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.stock-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.stock-desc {
  margin: 8px 0 16px;
  color: #64748b;
}
</style>
