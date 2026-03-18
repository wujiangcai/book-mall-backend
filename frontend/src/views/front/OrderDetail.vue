<template>
  <div class="back-link" @click="goBack">← 返回</div>
  <a-card title="订单详情" :bordered="false">
    <template v-if="detail">
      <a-descriptions :column="2">
        <a-descriptions-item label="订单号">{{ detail.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(detail.status) }}</a-descriptions-item>
        <a-descriptions-item label="金额">¥{{ detail.totalAmount }}</a-descriptions-item>
        <a-descriptions-item label="下单时间">{{ formatTime(detail.createTime) }}</a-descriptions-item>
        <a-descriptions-item label="收货人">{{ detail.address?.receiverName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ detail.address?.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="收货地址" :span="2">{{ detail.address?.fullAddress || '-' }}</a-descriptions-item>
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
        <template v-if="detail?.status === OrderStatus.UNPAID">
          <a-button type="primary" @click="pay">支付</a-button>
          <a-button status="danger" @click="cancel">取消</a-button>
        </template>
        <template v-else>
          <a-button @click="viewLogistics">查看物流</a-button>
          <a-button @click="applyAfterSales">申请售后</a-button>
        </template>
      </a-space>
    </template>
    <a-empty v-else description="暂无订单详情" />
  </a-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import frontOrderApi from '../../api/front/order'
import { OrderStatus } from '../../types/enums'
import type { OrderDetail } from '../../types/api'

const route = useRoute()
const router = useRouter()
const detail = ref<OrderDetail | null>(null)

const goBack = () => {
  router.back()
}

const formatTime = (value?: string) => {
  if (!value) return '-'
  return value.replace('T', ' ')
}

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

const viewLogistics = () => {
  Message.info('物流功能暂未开放')
}

const applyAfterSales = () => {
  Message.info('售后功能暂未开放')
}

const pay = async () => {
  if (!detail.value) return
  const formHtml = await frontOrderApi.pay(detail.value.orderId)
  if (!formHtml) return
  const container = document.createElement('div')
  container.innerHTML = formHtml
  document.body.appendChild(container)
  const form = container.querySelector('form') as HTMLFormElement | null
  if (form) {
    form.submit()
  }
}

const cancel = async () => {
  if (!detail.value) return
  await frontOrderApi.cancel(detail.value.orderId)
  Message.success('订单已取消')
  await load()
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
</style>
