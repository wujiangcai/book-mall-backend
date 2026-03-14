<template>
  <a-space direction="vertical" fill size="large">
    <a-carousel autoplay indicator-type="dot" class="hero-carousel">
      <div v-for="banner in banners" :key="banner.id" class="banner-item">
        <img :src="banner.imageUrl" :alt="String(banner.id)" class="banner-img" />
      </div>
    </a-carousel>

    <a-card class="card-glass" :bordered="false">
      <div class="section-title">分类快捷入口</div>
      <a-space wrap size="medium" style="margin-top: 12px">
        <a-tag
          v-for="cat in categories"
          :key="cat.id"
          color="arcoblue"
          class="category-tag"
          @click="goCategory(cat.id)"
        >
          {{ cat.categoryName }}
        </a-tag>
      </a-space>
    </a-card>

    <a-card class="card-glass" :bordered="false">
      <div class="section-title">推荐图书</div>
      <a-skeleton v-if="loading" :animation="true" :rows="3" />
      <template v-else>
        <a-grid :cols="4" :col-gap="16" :row-gap="16" style="margin-top: 16px">
          <a-grid-item v-for="book in books" :key="book.id">
            <a-card hoverable class="book-card" @click="goDetail(book.id)">
              <template #cover>
                <img :src="book.coverImage" alt="cover" class="book-cover" />
              </template>
              <div class="book-name">{{ book.bookName }}</div>
              <div class="book-meta">作者：{{ book.author || '佚名' }}</div>
              <div class="book-meta">¥{{ book.price }}</div>
              <div class="book-rating">评分：{{ getRating(book.id) }}</div>
            </a-card>
          </a-grid-item>
        </a-grid>
        <a-empty v-if="books.length === 0" description="暂无推荐图书" />
      </template>
    </a-card>

    <a-card class="card-glass" :bordered="false">
      <div class="section-title">畅销榜</div>
      <div class="best-seller">
        <div v-for="book in books" :key="`hot-${book.id}`" class="best-item" @click="goDetail(book.id)">
          <img :src="book.coverImage" alt="cover" />
          <div>
            <div class="book-name">{{ book.bookName }}</div>
            <div class="book-meta">¥{{ book.price }}</div>
          </div>
        </div>
      </div>
    </a-card>
  </a-space>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import frontBannerApi from '../../api/front/banner'
import frontBookApi from '../../api/front/book'
import frontCategoryApi from '../../api/front/category'
import type { Banner, BookListItem, CategoryTreeItem } from '../../types/api'

const router = useRouter()
const banners = ref<Banner[]>([])
const books = ref<BookListItem[]>([])
const categories = ref<CategoryTreeItem[]>([])
const loading = ref(false)

const goDetail = (id: number) => {
  router.push(`/book/${id}`)
}

const goCategory = (id: number) => {
  router.push({ path: '/books', query: { categoryId: String(id) } })
}

const getRating = (id: number) => {
  return (4 + (id % 10) / 10).toFixed(1)
}

const load = async () => {
  loading.value = true
  const [bannerData, bookData, categoryData] = await Promise.all([
    frontBannerApi.list().catch(() => []),
    frontBookApi.list({ page: 1, pageSize: 8 }).catch(() => ({ list: [] } as any)),
    frontCategoryApi.list().catch(() => []),
  ])
  banners.value = (bannerData as any) || []
  books.value = (bookData as any)?.list || []
  categories.value = (categoryData as any) || []
  loading.value = false
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.hero-carousel {
  border-radius: 16px;
  overflow: hidden;
}

.banner-item {
  width: 100%;
  height: 280px;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 280px;
  object-fit: cover;
}

.category-tag {
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

.book-name {
  font-weight: 600;
  margin-top: 8px;
}

.book-meta {
  color: var(--brand-muted);
  margin-top: 4px;
}

.book-rating {
  margin-top: 6px;
  color: var(--brand-accent);
  font-weight: 600;
}

.best-seller {
  margin-top: 12px;
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.best-item {
  min-width: 220px;
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  background: var(--brand-card);
  border: 1px solid var(--brand-border);
  cursor: pointer;
  transition: transform 0.3s ease;
}

.best-item:hover {
  transform: translateY(-4px);
}

.best-item img {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}
</style>
