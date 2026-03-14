<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="订单管理" :bordered="false">
      <a-space wrap>
        <a-input v-model="query.orderNo" placeholder="订单号" allow-clear style="width: 220px" />
        <a-input-number v-model="query.userId" placeholder="用户ID" :min="0" />
        <a-select v-model="query.status" placeholder="状态" allow-clear style="width: 160px">
          <a-option :value="0">待支付</a-option>
          <a-option :value="1">已支付</a-option>
          <a-option :value="2">待发货</a-option>
          <a-option :value="3">已发货</a-option>
          <a-option :value="4">已完成</a-option>
          <a-option :value="5">已取消</a-option>
        </a-select>
        <a-button type="primary" @click="load">搜索</a-button>
        <a-button status="success" :disabled="selectedIds.length === 0" @click="batchShip">批量发货</a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false">
      <a-table
        row-key="id"
        :data="list"
        :pagination="false"
        :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      >
        <template #columns>
          <a-table-column title="订单号" data-index="orderNo" />
          <a-table-column title="用户" data-index="username" />
          <a-table-column title="金额" :width="120">
            <template #cell="{ record }">¥{{ record.totalAmount }}</template>
          </a-table-column>
          <a-table-column title="状态" :width="120">
            <template #cell="{ record }">{{ statusText(record.status) }}</template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column title="操作" :width="180">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" @click="openDetail(record.id)">详情</a-button>
                <a-button size="mini" type="primary" @click="ship(record.id)">发货</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>

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

    <a-modal v-model:visible="detailVisible" title="订单详情" :footer="false">
      <a-descriptions :column="1" v-if="detail">
        <a-descriptions-item label="订单号">{{ detail.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="用户">{{ detail.username || detail.userId }}</a-descriptions-item>
        <a-descriptions-item label="金额">¥{{ detail.totalAmount }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(detail.status) }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detail.createTime }}</a-descriptions-item>
        <a-descriptions-item label="支付时间">{{ detail.payTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="发货时间">{{ detail.shipTime || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-table :data="detail?.items" row-key="bookId" :pagination="false" style="margin-top: 12px">
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
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import adminOrderApi from '../../api/admin/order'
import { OrderStatus } from '../../types/enums'
import type { AdminOrderDetail, AdminOrderListItem } from '../../types/api'

const list = ref<AdminOrderListItem[]>([])
const total = ref(0)
const detail = ref<AdminOrderDetail | null>(null)
const detailVisible = ref(false)
const selectedRowKeys = ref<number[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  orderNo: '',
  userId: undefined as number | undefined,
})

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
  const data = (await adminOrderApi.list({
    page: query.page,
    pageSize: query.pageSize,
    status: query.status,
    orderNo: query.orderNo || undefined,
    userId: query.userId,
  })) as any
  list.value = data?.list || []
  total.value = data?.total || 0
}

const handlePageChange = (page: number) => {
  query.page = page
  load()
}

const handlePageSizeChange = (pageSize: number) => {
  query.pageSize = pageSize
  query.page = 1
  load()
}

const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const openDetail = async (id: number) => {
  detail.value = (await adminOrderApi.detail(id)) as any
  detailVisible.value = true
}

const ship = async (id: number) => {
  await adminOrderApi.ship(id)
  await load()
}

const batchShip = async () => {
  const ids = selectedRowKeys.value
  await Promise.all(ids.map((id) => adminOrderApi.ship(id)))
  selectedRowKeys.value = []
  await load()
}

const selectedIds = selectedRowKeys

onMounted(load)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
