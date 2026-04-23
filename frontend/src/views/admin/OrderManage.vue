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
        <a-button type="primary" @click="openCreate">新增订单</a-button>
        <a-button status="success" :disabled="!canBatchShip" @click="batchShip">批量发货</a-button>
        <a-button status="danger" :disabled="selectedIds.length === 0" @click="batchCancel">批量取消</a-button>
      </a-space>
    </a-card>

    <a-card v-if="isRefund" :bordered="false" class="sub-panel">
      <div class="sub-title">退款处理</div>
      <div class="sub-desc">展示待退款订单与审核记录。</div>
      <a-space>
        <a-button type="primary">批量审核</a-button>
        <a-button>导出退款单</a-button>
      </a-space>
    </a-card>

    <a-card v-if="isLogistics" :bordered="false" class="sub-panel">
      <div class="sub-title">物流跟踪</div>
      <div class="sub-desc">追踪发货进度与物流状态。</div>
      <a-space>
        <a-button type="primary">同步物流</a-button>
        <a-button>查看异常</a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false">
      <a-table
        row-key="id"
        :data="list"
        :pagination="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true, selectedRowKeys }"
        @selection-change="onSelectChange"
      >
        <template #columns>
          <a-table-column title="订单号" data-index="orderNo" />
          <a-table-column title="用户">
            <template #cell="{ record }">{{ formatOrderUser(record) }}</template>
          </a-table-column>
          <a-table-column title="金额" :width="120">
            <template #cell="{ record }">¥{{ record.totalAmount }}</template>
          </a-table-column>
          <a-table-column title="状态" :width="120">
            <template #cell="{ record }">{{ statusText(record.status) }}</template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column title="操作" :width="240">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" @click="openDetail(record.id)">详情</a-button>
                <a-button size="mini" @click="openEdit(record)">编辑</a-button>
                <a-button size="mini" type="primary" :disabled="record.status !== OrderStatus.PENDING_SHIP" @click="ship(record.id)">发货</a-button>
                <a-button size="mini" status="danger" @click="cancelOrder(record.id)">取消</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-if="list.length === 0" description="暂无订单" />

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
        <a-descriptions-item label="收货信息">
          {{ detail.address ? `${detail.address.receiverName} ${detail.address.phone} ${detail.address.fullAddress}` : '-' }}
        </a-descriptions-item>
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

    <a-modal v-model:visible="visible" :title="editing ? '编辑订单' : '新增订单'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item v-if="!editing" label="下单用户">
          <a-select
            v-model="form.userId"
            placeholder="输入用户名/手机号搜索用户"
            allow-search
            :filter-option="false"
            :loading="userLoading"
            @search="searchUsers"
            @change="handleUserChange"
          >
            <a-option v-for="user in userOptions" :key="user.id" :value="user.id">
              {{ formatUserOption(user) }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收货地址">
          <a-select v-model="form.addressId" placeholder="请选择收货地址">
            <a-option v-for="address in addressOptions" :key="address.id" :value="address.id">
              {{ formatAddressOption(address) }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="editing" label="状态">
          <a-select v-model="form.status">
            <a-option :value="0">待支付</a-option>
            <a-option :value="1">已支付</a-option>
            <a-option :value="2">待发货</a-option>
            <a-option :value="3">已发货</a-option>
            <a-option :value="4">已完成</a-option>
            <a-option :value="5">已取消</a-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="!editing" label="商品项">
          <a-space direction="vertical" fill>
            <div v-for="(item, index) in form.items" :key="index" class="order-item-row">
              <a-select
                v-model="item.bookId"
                placeholder="输入书名/作者/ISBN搜索图书"
                allow-search
                :filter-option="false"
                :loading="bookLoading"
                style="width: 360px"
                @search="searchBooks"
              >
                <a-option v-for="book in bookOptions" :key="book.id" :value="book.id">
                  {{ formatBookOption(book) }}
                </a-option>
              </a-select>
              <a-input-number v-model="item.quantity" :min="1" placeholder="数量" />
              <a-button size="mini" status="danger" @click="removeItem(index)">移除</a-button>
            </div>
            <a-button size="mini" @click="addItem">添加商品</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import adminBookApi from '../../api/admin/book'
import adminOrderApi from '../../api/admin/order'
import adminUserApi from '../../api/admin/user'
import { OrderStatus } from '../../types/enums'
import type { AdminAddress, AdminBookListItem, AdminOrderDetail, AdminOrderListItem, AdminUserListItem } from '../../types/api'

const route = useRoute()
const list = ref<AdminOrderListItem[]>([])
const total = ref(0)
const detail = ref<AdminOrderDetail | null>(null)
const detailVisible = ref(false)
const visible = ref(false)
const editing = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const userOptions = ref<AdminUserListItem[]>([])
const addressOptions = ref<AdminAddress[]>([])
const bookOptions = ref<AdminBookListItem[]>([])
const userLoading = ref(false)
const bookLoading = ref(false)
let userSearchTimer: ReturnType<typeof setTimeout> | null = null
let bookSearchTimer: ReturnType<typeof setTimeout> | null = null

const query = reactive({
  page: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  orderNo: '',
  userId: undefined as number | undefined,
})

const form = reactive({
  userId: undefined as number | undefined,
  addressId: undefined as number | undefined,
  status: OrderStatus.UNPAID,
  items: [{ bookId: undefined as number | undefined, quantity: 1 }],
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

const formatOrderUser = (record: AdminOrderListItem) => {
  return record.username ? `${record.username}（ID: ${record.userId}）` : `用户ID: ${record.userId}`
}

const addItem = () => {
  form.items.push({ bookId: undefined, quantity: 1 })
}

const removeItem = (index: number) => {
  form.items.splice(index, 1)
}

const load = async () => {
  const data = (await adminOrderApi.list({
    page: query.page,
    pageSize: query.pageSize,
    status: query.status,
    orderNo: query.orderNo || undefined,
    userId: query.userId,
  })) as any
  const rawList = data?.list || []
  list.value = rawList.map((item: any) => ({ ...item, id: item.id ?? item.orderId }))
  total.value = data?.total || 0
}

const loadUserOptions = async (keyword?: string) => {
  userLoading.value = true
  try {
    const data = (await adminUserApi.list({ page: 1, pageSize: 20, keyword: keyword || undefined }).catch(() => null)) as any
    userOptions.value = data?.list || []
  } finally {
    userLoading.value = false
  }
}

const loadBookOptions = async (keyword?: string) => {
  bookLoading.value = true
  try {
    const data = (await adminBookApi.list({ page: 1, pageSize: 20, keyword: keyword || undefined }).catch(() => null)) as any
    bookOptions.value = data?.list || []
  } finally {
    bookLoading.value = false
  }
}

const loadUserAddresses = async (userId?: number) => {
  if (!userId) {
    addressOptions.value = []
    form.addressId = undefined
    return
  }
  addressOptions.value = ((await adminUserApi.addresses(userId).catch(() => [])) as any) || []
  if (form.addressId && !addressOptions.value.some((item) => item.id === form.addressId)) {
    form.addressId = undefined
  }
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

const loadDetail = async (id: number) => {
  detail.value = (await adminOrderApi.detail(id)) as any
}

const openCreate = () => {
  editing.value = false
  editingId.value = null
  Object.assign(form, {
    userId: undefined,
    addressId: undefined,
    status: OrderStatus.UNPAID,
    items: [{ bookId: undefined, quantity: 1 }],
  })
  addressOptions.value = []
  visible.value = true
}

const openEdit = async (record: AdminOrderListItem) => {
  editing.value = true
  editingId.value = record.id
  await loadDetail(record.id)
  Object.assign(form, {
    userId: record.userId,
    addressId: detail.value?.addressId,
    status: record.status,
    items: [{ bookId: undefined, quantity: 1 }],
  })
  await loadUserAddresses(record.userId)
  visible.value = true
}

const handleUserChange = async (userId: number) => {
  await loadUserAddresses(userId)
}

const searchUsers = (keyword: string) => {
  if (userSearchTimer) {
    clearTimeout(userSearchTimer)
  }
  userSearchTimer = setTimeout(() => loadUserOptions(keyword), 300)
}

const searchBooks = (keyword: string) => {
  if (bookSearchTimer) {
    clearTimeout(bookSearchTimer)
  }
  bookSearchTimer = setTimeout(() => loadBookOptions(keyword), 300)
}

const formatUserOption = (user: AdminUserListItem) => {
  const name = user.nickname ? `${user.username} / ${user.nickname}` : user.username
  const phone = user.phone ? ` / ${user.phone}` : ''
  return `${name}${phone}（ID: ${user.id}）`
}

const formatAddressOption = (address: AdminAddress) => {
  const defaultText = Number(address.isDefault) === 1 ? '【默认】' : ''
  return `${defaultText}${address.receiverName} ${address.phone} ${address.fullAddress}`
}

const formatBookOption = (book: AdminBookListItem) => {
  const author = book.author ? ` / ${book.author}` : ''
  const isbn = book.isbn ? ` / ISBN: ${book.isbn}` : ''
  return `${book.bookName}${author}${isbn}（库存: ${book.stock} / ¥${book.price} / ID: ${book.id}）`
}

const submit = async () => {
  if (editing.value && editingId.value) {
    if (!form.addressId) return
    await adminOrderApi.update(editingId.value, {
      status: form.status,
      addressId: form.addressId,
    })
    Message.success('订单已更新')
  } else {
    if (!form.userId || !form.addressId) return
    const items = form.items
      .filter((item) => item.bookId && item.quantity)
      .map((item) => ({ bookId: item.bookId as number, quantity: item.quantity as number }))
    await adminOrderApi.create({
      userId: form.userId,
      addressId: form.addressId,
      items,
    })
    Message.success('订单已创建')
  }
  visible.value = false
  await load()
}

const ship = async (id: number) => {
  await adminOrderApi.ship(id)
  Message.success('发货成功')
  await load()
}

const cancelOrder = async (id: number) => {
  await adminOrderApi.cancel(id)
  Message.success('订单已取消')
  await load()
}

const batchShip = async () => {
  const ids = selectedRowKeys.value
  await Promise.all(ids.map((id) => adminOrderApi.ship(id)))
  Message.success('批量发货完成')
  selectedRowKeys.value = []
  await load()
}

const batchCancel = async () => {
  const ids = selectedRowKeys.value
  await Promise.all(ids.map((id) => adminOrderApi.cancel(id)))
  Message.success('批量取消完成')
  selectedRowKeys.value = []
  await load()
}

const selectedIds = selectedRowKeys
const canBatchShip = computed(
  () =>
    selectedRowKeys.value.length > 0 &&
    selectedRowKeys.value.every((id) => list.value.find((item) => item.id === id)?.status === OrderStatus.PENDING_SHIP),
)
const isRefund = computed(() => route.path.endsWith('/refund'))
const isLogistics = computed(() => route.path.endsWith('/logistics'))

onMounted(load)
onMounted(loadUserOptions)
onMounted(loadBookOptions)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.sub-panel {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.sub-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.sub-desc {
  margin: 8px 0 16px;
  color: #64748b;
}

.order-item-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
