<template>
  <div class="back-link" @click="goBack">← 返回</div>
  <a-card :bordered="false">
    <template v-if="book">
      <a-grid :cols="2" :col-gap="24">
        <a-grid-item>
          <img :src="book.coverImage" alt="cover" class="detail-cover" />
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
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import frontBookApi from '../../api/front/book'
import frontCartApi from '../../api/front/cart'
import type { BookDetail } from '../../types/api'

const route = useRoute()
const router = useRouter()
const book = ref<BookDetail | null>(null)
const quantity = ref(1)

const goBack = () => {
  router.back()
}

const load = async () => {
  const id = route.params.id as string
  const data = await frontBookApi.detail(id)
  book.value = data as any
}

const addToCart = async () => {
  if (!book.value) return
  await frontCartApi.add({ bookId: book.value.id, quantity: quantity.value })
  Message.success('已加入购物车')
}

onMounted(load)
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

.price {
  font-size: 20px;
  color: #d93026;
  font-weight: 600;
}
</style>
