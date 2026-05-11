<template>
  <div class="back-link" @click="goBack">← 返回</div>
  <a-card :bordered="false">
    <template v-if="book">
      <a-grid :cols="2" :col-gap="24">
        <a-grid-item>
          <img v-if="book.coverImage" :src="book.coverImage" alt="cover" class="detail-cover" @error="handleCoverError" />
          <div v-else class="detail-cover-placeholder">暂无封面</div>
        </a-grid-item>
        <a-grid-item>
          <a-space direction="vertical" fill>
            <h2>{{ book.bookName }}</h2>
            <div>作者：{{ book.author || '-' }}</div>
            <div>出版社：{{ book.publisher || '-' }}</div>
            <div>ISBN：{{ book.isbn || '-' }}</div>
            <div class="price">¥{{ book.price }}</div>
            <a-space>
              <a-input-number v-model="quantity" :min="1" :max="book.stock" />
              <a-button type="primary" @click="addToCart">加入购物车</a-button>
            </a-space>
          </a-space>
        </a-grid-item>
      </a-grid>
      <a-divider />
      <div>{{ book.description || '暂无描述' }}</div>
    </template>
    <a-empty v-else description="暂无图书信息" />
  </a-card>

  <a-card class="recommend-card" :bordered="false">
    <div class="section-heading">
      <div>
        <div class="section-title">AI 看了又看</div>
        <div class="section-subtitle">结合当前图书、个人行为与热门趋势生成相似推荐</div>
      </div>
      <a-tag color="arcoblue">混合推荐</a-tag>
    </div>
    <a-grid v-if="recommendations.length > 0" :cols="4" :col-gap="16" :row-gap="16" style="margin-top: 16px">
      <a-grid-item v-for="item in recommendations" :key="item.id">
        <a-card hoverable class="recommend-book" @click="goRecommendDetail(item.id)">
          <template #cover>
            <img v-if="item.coverImage" :src="item.coverImage" alt="cover" class="recommend-cover" @error="handleRecommendCoverError(item)" />
            <div v-else class="recommend-cover-placeholder">暂无封面</div>
          </template>
          <div class="recommend-title">{{ item.bookName }}</div>
          <div class="recommend-meta">{{ item.author || '佚名' }} · ¥{{ item.price }}</div>
          <div class="recommend-strategy">{{ item.strategy }}</div>
          <div class="recommend-reason">{{ item.reason }}</div>
        </a-card>
      </a-grid-item>
    </a-grid>
    <a-empty v-else description="暂无相关推荐" />
  </a-card>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import frontBookApi from '../../api/front/book'
import frontCartApi from '../../api/front/cart'
import frontRecommendationApi from '../../api/front/recommendation'
import type { BookDetail, RecommendationBook } from '../../types/api'

const route = useRoute()
const router = useRouter()
const book = ref<BookDetail | null>(null)
const recommendations = ref<RecommendationBook[]>([])
const quantity = ref(1)

const goBack = () => {
  router.back()
}

const handleCoverError = () => {
  if (book.value) {
    book.value.coverImage = undefined
  }
}

const load = async () => {
  const id = route.params.id as string
  const data = await frontBookApi.detail(id)
  book.value = data as any
  await recordView(id)
  await loadRecommendations(id)
}

const addToCart = async () => {
  if (!book.value) return
  await frontCartApi.add({ bookId: book.value.id, quantity: quantity.value })
  Message.success('已加入购物车')
}

const goRecommendDetail = (id: number) => {
  router.push(`/book/${id}`)
}

const handleRecommendCoverError = (item: RecommendationBook) => {
  item.coverImage = undefined
}

const recordView = async (id: number | string) => {
  if (!localStorage.getItem('token')) return
  await frontRecommendationApi.record({ bookId: id, behaviorType: 'view' }).catch(() => undefined)
}

const loadRecommendations = async (id: number | string) => {
  recommendations.value = ((await frontRecommendationApi.list({ sceneBookId: id, limit: 4 }).catch(() => [])) as any) || []
}

onMounted(load)

watch(
  () => route.params.id,
  () => {
    quantity.value = 1
    load()
  }
)
</script>

<style scoped>
.back-link {
  position: fixed;
  left: 24px;
  top: 120px;
  padding: 8px 14px;
  border-radius: 999px;
  background: var(--brand-card);
  border: 1px solid var(--brand-border);
  box-shadow: var(--brand-shadow);
  color: var(--brand-primary);
  font-weight: 500;
  cursor: pointer;
  z-index: 10;
  transition: color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.back-link:hover {
  color: var(--brand-accent);
  transform: translateY(-1px);
}

.detail-cover {
  width: 100%;
  height: 320px;
  object-fit: cover;
}

.detail-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 320px;
  color: var(--brand-muted);
  background: linear-gradient(135deg, #edf2f7, #f8fafc);
  border-radius: 12px;
}

.price {
  font-size: 20px;
  color: #d93026;
  font-weight: 600;
}

.recommend-card {
  margin-top: 18px;
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--brand-primary);
}

.section-subtitle {
  margin-top: 6px;
  color: var(--brand-muted);
  font-size: 13px;
}

.recommend-book {
  height: 100%;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.recommend-book:hover {
  transform: translateY(-5px);
  box-shadow: var(--brand-shadow);
}

.recommend-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.recommend-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 160px;
  color: var(--brand-muted);
  background: linear-gradient(135deg, #edf2f7, #f8fafc);
}

.recommend-title {
  margin-top: 8px;
  font-weight: 700;
}

.recommend-meta {
  margin-top: 5px;
  color: var(--brand-muted);
  font-size: 13px;
}

.recommend-strategy {
  display: inline-flex;
  margin-top: 8px;
  padding: 3px 8px;
  border-radius: 999px;
  color: #2563eb;
  background: rgba(37, 99, 235, 0.08);
  font-size: 12px;
  font-weight: 600;
}

.recommend-reason {
  margin-top: 8px;
  color: var(--brand-muted);
  font-size: 12px;
  line-height: 1.5;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
