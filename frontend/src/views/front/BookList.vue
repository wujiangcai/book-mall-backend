<template>
  <div class="list-layout">
    <a-card class="card-glass filter-panel" :bordered="false">
      <div class="section-title">筛选</div>
      <a-space direction="vertical" fill style="margin-top: 12px">
        <a-input v-model="query.keyword" placeholder="搜索图书" allow-clear />
        <a-select v-model="query.categoryId" placeholder="分类" allow-clear>
          <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.categoryName }}
          </a-option>
        </a-select>
        <a-input-number v-model="query.minPrice" placeholder="最低价" :min="0" />
        <a-input-number v-model="query.maxPrice" placeholder="最高价" :min="0" />
        <a-button type="primary" @click="search">应用筛选</a-button>
      </a-space>
    </a-card>

    <div class="list-main">
      <a-card class="card-glass" :bordered="false">
        <div class="list-search">
          <a-input-search
            v-model="query.keyword"
            placeholder="请输入图书名称、作者等"
            allow-clear
            @search="search"
          />
        </div>
        <a-space wrap class="category-tabs">
          <a-tag
            v-for="cat in categories"
            :key="`tab-${cat.id}`"
            class="tab-item"
            :color="query.categoryId === cat.id ? 'arcoblue' : undefined"
            @click="selectCategory(cat.id)"
          >
            {{ cat.categoryName }}
          </a-tag>
        </a-space>
      </a-card>

      <a-card class="card-glass" :bordered="false" style="margin-top: 16px">
        <a-skeleton v-if="loading" :animation="true" :rows="4" />
        <template v-else>
          <a-grid :cols="4" :col-gap="16" :row-gap="16">
            <a-grid-item v-for="book in list" :key="book.id">
              <a-card hoverable class="book-card" @click="goDetail(book.id)">
                <template #cover>
                  <img v-if="book.coverImage" :src="book.coverImage" alt="cover" class="book-cover" @error="handleBookCoverError(book)" />
                  <div v-else class="book-cover-placeholder">暂无封面</div>
                </template>
                <div class="book-name">{{ book.bookName }}</div>
                <div class="book-meta">作者：{{ book.author || '佚名' }}</div>
                <div class="book-meta">¥{{ book.price }}</div>
              </a-card>
            </a-grid-item>
          </a-grid>
          <a-empty v-if="list.length === 0" description="暂无符合条件的图书" />
        </template>

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
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import frontBookApi from '../../api/front/book'
import frontCategoryApi from '../../api/front/category'
import type { BookListItem, CategoryTreeItem } from '../../types/api'

const router = useRouter()
const route = useRoute()
const list = ref<BookListItem[]>([])
const categories = ref<CategoryTreeItem[]>([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  page: 1,
  pageSize: 10,
  categoryId: undefined as number | undefined,
  keyword: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
})

const loadCategories = async () => {
  const data = (await frontCategoryApi.list().catch(() => [])) as any
  categories.value = data || []
}

const loadList = async () => {
  loading.value = true
  const data = (await frontBookApi.list({
    page: query.page,
    pageSize: query.pageSize,
    categoryId: query.categoryId,
    keyword: query.keyword || undefined,
    minPrice: query.minPrice,
    maxPrice: query.maxPrice,
  })) as any
  list.value = data?.list || []
  total.value = data?.total || 0
  loading.value = false
}

const search = () => {
  query.page = 1
  loadList()
}

const selectCategory = (id: number) => {
  query.categoryId = id
  search()
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

const handleBookCoverError = (book: BookListItem) => {
  book.coverImage = undefined
}

watch(
  () => route.query,
  (q) => {
    if (q.keyword) {
      query.keyword = q.keyword as string
      search()
    }
    if (q.categoryId) {
      query.categoryId = Number(q.categoryId)
      search()
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadCategories()
  loadList()
})
</script>

<style scoped>
.list-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
}

.filter-panel {
  position: sticky;
  top: 96px;
  height: fit-content;
}

.list-search {
  margin-bottom: 12px;
}

.list-search :deep(.arco-input-search) {
  max-width: 420px;
}

.category-tabs {
  gap: 8px;
}

.tab-item {
  cursor: pointer;
}

.book-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.book-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--brand-shadow);
}

.book-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.book-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 180px;
  color: var(--brand-muted);
  background: linear-gradient(135deg, #edf2f7, #f8fafc);
}

.book-name {
  font-weight: 600;
  margin-top: 8px;
}

.book-meta {
  color: var(--brand-muted);
  margin-top: 4px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 992px) {
  .list-layout {
    grid-template-columns: 1fr;
  }

  .filter-panel {
    position: static;
  }
}
</style>
