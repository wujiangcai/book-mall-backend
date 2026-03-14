<template>
  <a-card :bordered="false" v-if="book">
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
  </a-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import frontBookApi from '../../api/front/book'
import frontCartApi from '../../api/front/cart'
import type { BookDetail } from '../../types/api'

const route = useRoute()
const book = ref<BookDetail | null>(null)
const quantity = ref(1)

const load = async () => {
  const id = route.params.id as string
  const data = await frontBookApi.detail(id)
  book.value = data as any
}

const addToCart = async () => {
  if (!book.value) return
  await frontCartApi.add({ bookId: book.value.id, quantity: quantity.value })
}

onMounted(load)
</script>

<style scoped>
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
