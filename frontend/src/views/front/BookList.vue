<template>
  <a-space direction="vertical" fill size="large">
    <a-card :bordered="false">
      <a-space>
        <a-select v-model="query.categoryId" placeholder="分类" allow-clear style="width: 180px">
          <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.categoryName }}
          </a-option>
        </a-select>
        <a-input v-model="query.keyword" placeholder="搜索图书" allow-clear style="width: 240px" />
        <a-button type="primary" @click="search">搜索</a-button>
      </a-space>
    </a-card>

    <a-card title="图书列表" :bordered="false">
      <a-grid :cols="4" :col-gap="16" :row-gap="16">
        <a-grid-item v-for="book in list" :key="book.id">
          <a-card hoverable @click="goDetail(book.id)">
            <template #cover>
              <img :src="book.coverImage" alt="cover" class="book-cover" />
            </template>
            <div class="book-name">{{ book.bookName }}</div>
            <div class="book-meta">¥{{ book.price }}</div>
          </a-card>
        </a-grid-item>
      </a-grid>

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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import frontBookApi from '../../api/front/book'
import frontCategoryApi from '../../api/front/category'
import type { BookListItem, CategoryTreeItem } from '../../types/api'

const router = useRouter()
const list = ref<BookListItem[]>([])
const categories = ref<CategoryTreeItem[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
  categoryId: undefined as number | undefined,
  keyword: '',
})

const loadCategories = async () => {
  const data = (await frontCategoryApi.list().catch(() => [])) as any
  categories.value = data || []
}

const loadList = async () => {
  const data = (await frontBookApi.list({
    page: query.page,
    pageSize: query.pageSize,
    categoryId: query.categoryId,
    keyword: query.keyword || undefined,
  })) as any
  list.value = data?.list || []
  total.value = data?.total || 0
}

const search = () => {
  query.page = 1
  loadList()
}

const handlePageChange = (page: number) => {
  query.page = page
  loadList()
}

const handlePageSizeChange = (pageSize: number) => {
  query.pageSize = pageSize
  query.page = 1
  loadList()
}

const goDetail = (id: number) => {
  router.push(`/book/${id}`)
}

onMounted(() => {
  loadCategories()
  loadList()
})
</script>

<style scoped>
.book-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.book-name {
  font-weight: 600;
  margin-top: 8px;
}

.book-meta {
  color: #666;
  margin-top: 4px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
