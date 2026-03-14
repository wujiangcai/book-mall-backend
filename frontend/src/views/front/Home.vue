<template>
  <a-space direction="vertical" fill size="large">
    <a-carousel autoplay indicator-type="dot">
      <div v-for="banner in banners" :key="banner.id" class="banner-item">
        <img :src="banner.imageUrl" :alt="String(banner.id)" class="banner-img" />
      </div>
    </a-carousel>

    <a-card title="推荐图书" :bordered="false">
      <a-grid :cols="4" :col-gap="16" :row-gap="16">
        <a-grid-item v-for="book in books" :key="book.id">
          <a-card hoverable @click="goDetail(book.id)">
            <template #cover>
              <img :src="book.coverImage" alt="cover" class="book-cover" />
            </template>
            <div class="book-name">{{ book.bookName }}</div>
            <div class="book-meta">¥{{ book.price }}</div>
          </a-card>
        </a-grid-item>
      </a-grid>
    </a-card>
  </a-space>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import frontBannerApi from '../../api/front/banner'
import frontBookApi from '../../api/front/book'
import type { Banner, BookListItem } from '../../types/api'

const router = useRouter()
const banners = ref<Banner[]>([])
const books = ref<BookListItem[]>([])

const goDetail = (id: number) => {
  router.push(`/book/${id}`)
}

const load = async () => {
  const [bannerData, bookData] = await Promise.all([
    frontBannerApi.list().catch(() => []),
    frontBookApi.list({ page: 1, pageSize: 8 }).catch(() => ({ list: [] } as any)),
  ])
  banners.value = (bannerData as any) || []
  books.value = (bookData as any)?.list || []
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.banner-item {
  width: 100%;
  height: 240px;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 240px;
  object-fit: cover;
}

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
</style>
