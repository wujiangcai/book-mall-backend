<template>
  <a-card title="订单详情" :bordered="false">
    <template v-if="detail">
      <a-descriptions :column="2">
        <a-descriptions-item label="订单号">{{ detail.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(detail.status) }}</a-descriptions-item>
        <a-descriptions-item label="金额">¥{{ detail.totalAmount }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detail.createTime }}</a-descriptions-item>
        <a-descriptions-item label="支付时间">{{ detail.payTime || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <a-table :data="detail.items" row-key="bookId" :pagination="false">
        <template #columns>
          <a-table-column title="书名" data-index="bookName" />
          <a-table-column title="价格" :width="120">
            <template #cell="{ record }">¥{{ record.price }}</template>
          </a-table-column>
          <a-table-column title="数量" data-index="quantity" :width="80" />
          <a-table-column title="小计" :width="120">
            <template #cell="{ record }">¥{{ record.totalPrice }}</template>
          </a-table-column>
        </template>
      </a-table>

      <a-space style="margin-top: 16px">
        <a-button type="primary" @click="pay">支付</a-button>
        <a-button status="danger" @click="cancel">取消</a-button>
      </a-space>
    </template>
    <a-empty v-else description="暂无订单详情" />
  </a-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import frontOrderApi from '../../api/front/order'
import { OrderStatus } from '../../types/enums'
import type { OrderDetail } from '../../types/api'

const route = useRoute()
const detail = ref<OrderDetail | null>(null)

const statusText = (status: number) => {
  switch (status) {
    case OrderStatus.UNPAID:
      return '待支付'
    case OrderStatus.PAID:
      return '已支付'
    case OrderStatus.PENDING_SHIP:
      return '待发货'
    case OrderStatus.SHIPPED:
      return '已发货'
    case OrderStatus.COMPLETED:
      return '已完成'
    case OrderStatus.CANCELLED:
      return '已取消'
    default:
      return '未知'
  }
}

const load = async () => {
  const id = route.params.id as string
  detail.value = (await frontOrderApi.detail(id)) as any
}

const pay = async () => {
  if (!detail.value) return
  await frontOrderApi.pay(detail.value.orderId)
  Message.success('支付成功')
  await load()
}

const cancel = async () => {
  if (!detail.value) return
  await frontOrderApi.cancel(detail.value.orderId)
  Message.success('订单已取消')
  await load()
}

onMounted(load)
</script>
