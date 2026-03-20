<template>
  <a-space direction="vertical" fill size="large">
    <a-card class="card-glass hero-banner-card" :bordered="false">
      <div v-if="banners.length > 0" class="hero-carousel">
        <div
          v-for="(banner, index) in banners"
          v-show="index === activeBannerIndex"
          :key="banner.id"
          class="banner-item"
          @click="openBanner(banner.linkUrl)"
        >
          <div class="banner-image-layer" :style="{ backgroundImage: `url(${banner.imageUrl})` }"></div>
          <div class="banner-overlay">
            <div class="banner-badge">BOOK MALL</div>
            <div class="banner-title">沉浸式阅读与学习空间</div>
            <div class="banner-desc">在安静留白中发现文学、编程与知识探索的灵感</div>
          </div>
        </div>
        <div v-if="banners.length > 1" class="hero-dots">
          <button
            v-for="(banner, index) in banners"
            :key="`dot-${banner.id}`"
            type="button"
            class="hero-dot"
            :class="{ 'hero-dot--active': index === activeBannerIndex }"
            @click.stop="activeBannerIndex = index"
          ></button>
        </div>
      </div>
      <a-empty v-else description="暂无轮播图" />
    </a-card>

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
import { onBeforeUnmount, onMounted, ref } from 'vue'
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
const activeBannerIndex = ref(0)
let bannerTimer: number | null = null

const goDetail = (id: number) => {
  router.push(`/book/${id}`)
}

const goCategory = (id: number) => {
  router.push({ path: '/books', query: { categoryId: String(id) } })
}

const openBanner = (linkUrl?: string) => {
  if (!linkUrl) return
  window.open(linkUrl, '_blank')
}

const getRating = (id: number) => {
  return (4 + (id % 10) / 10).toFixed(1)
}

const startBannerTimer = () => {
  if (bannerTimer) {
    window.clearInterval(bannerTimer)
  }
  if (banners.value.length <= 1) {
    bannerTimer = null
    return
  }
  bannerTimer = window.setInterval(() => {
    activeBannerIndex.value = (activeBannerIndex.value + 1) % banners.value.length
  }, 4000)
}

const load = async () => {
  loading.value = true
  const [bannerData, bookData, categoryData] = await Promise.all([
    frontBannerApi.list().catch(() => []),
    frontBookApi.list({ page: 1, pageSize: 8 }).catch(() => ({ list: [] } as any)),
    frontCategoryApi.list().catch(() => []),
  ])
  banners.value = (bannerData as any) || []
  activeBannerIndex.value = 0
  startBannerTimer()
  books.value = (bookData as any)?.list || []
  categories.value = (categoryData as any) || []
  loading.value = false
}

onMounted(() => {
  load()
})

onBeforeUnmount(() => {
  if (bannerTimer) {
    window.clearInterval(bannerTimer)
  }
})
</script>

<style scoped>
.hero-banner-card {
  padding: 0;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(246, 248, 251, 0.92));
}

.hero-carousel {
  position: relative;
  width: 100%;
  height: 360px;
  background: linear-gradient(135deg, rgba(246, 248, 251, 0.82), rgba(238, 243, 250, 0.76));
}

.hero-dots {
  position: absolute;
  left: 50%;
  bottom: 18px;
  z-index: 3;
  display: flex;
  gap: 8px;
  transform: translateX(-50%);
}

.hero-dot {
  width: 10px;
  height: 10px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 2px 8px rgba(30, 58, 95, 0.18);
  cursor: pointer;
}

.hero-dot--active {
  width: 24px;
  background: linear-gradient(90deg, #6f8fb4, #d4a574);
}

.banner-item {
  position: relative;
  width: 100%;
  height: 360px;
  overflow: hidden;
  cursor: pointer;
  background: #eef3fa;
}

.banner-item:hover .banner-image-layer {
  transform: scale(1.03);
}

.banner-image-layer {
  position: absolute;
  inset: 0;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  filter: saturate(0.92) brightness(1.02);
  transition: transform 0.45s ease;
}


.banner-overlay {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: 20px;
  max-width: 360px;
  padding: 14px 16px;
  color: var(--brand-primary);
  background: rgba(253, 251, 247, 0.76);
  border: 1px solid rgba(30, 58, 95, 0.08);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 22px rgba(30, 58, 95, 0.08);
}

.banner-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(73, 118, 167, 0.12);
  color: #4976a7;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.banner-title {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
  color: #1e3a5f;
}

.banner-desc {
  margin-top: 6px;
  color: #5f6f82;
  font-size: 13px;
  line-height: 1.6;
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

@media (max-width: 768px) {
  .hero-carousel {
    height: 300px;
  }

  .banner-item {
    height: 300px;
  }

  .banner-overlay {
    left: 14px;
    right: 14px;
    bottom: 14px;
    max-width: 300px;
    padding: 12px 14px;
  }

  .banner-title {
    font-size: 17px;
  }

  .banner-desc {
    font-size: 12px;
  }
}
</style>
