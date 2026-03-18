<template>
  <a-card title="订单列表" :bordered="false">
    <a-table :data="list" row-key="orderId" :pagination="false">
      <template #columns>
        <a-table-column title="订单号" data-index="orderNo" />
        <a-table-column title="金额" :width="120">
          <template #cell="{ record }">¥{{ record.totalAmount }}</template>
        </a-table-column>
        <a-table-column title="状态" :width="120">
          <template #cell="{ record }">{{ statusText(record.status) }}</template>
        </a-table-column>
        <a-table-column title="创建时间" data-index="createTime" />
        <a-table-column title="操作" :width="200">
          <template #cell="{ record }">
            <a-space>
              <a-button size="mini" @click="goDetail(record.orderId)">详情</a-button>
              <template v-if="record.status === OrderStatus.UNPAID">
                <a-button size="mini" type="primary" @click="pay(record.orderId)">支付</a-button>
                <a-button size="mini" status="danger" @click="cancel(record.orderId)">取消</a-button>
              </template>
              <template v-else>
                <a-button size="mini" @click="viewLogistics">查看物流</a-button>
                <a-button size="mini" @click="applyAfterSales">申请售后</a-button>
              </template>
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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import frontOrderApi from '../../api/front/order'
import { OrderStatus } from '../../types/enums'
import type { OrderListItem } from '../../types/api'

const router = useRouter()
const list = ref<OrderListItem[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
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
  const data = (await frontOrderApi.list({
    page: query.page,
    pageSize: query.pageSize,
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

const goDetail = (id: number) => {
  router.push(`/order/${id}`)
}

const viewLogistics = () => {
  Message.info('物流功能暂未开放')
}

const applyAfterSales = () => {
  Message.info('售后功能暂未开放')
}

const pay = async (id: number) => {
  const formHtml = await frontOrderApi.pay(id)
  if (!formHtml) return
  const container = document.createElement('div')
  container.innerHTML = formHtml
  document.body.appendChild(container)
  const form = container.querySelector('form') as HTMLFormElement | null
  if (form) {
    form.submit()
  }
}

const cancel = async (id: number) => {
  await frontOrderApi.cancel(id)
  Message.success('订单已取消')
  await load()
}

onMounted(load)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
