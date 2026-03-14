<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="购物车" :bordered="false">
      <a-table :data="list" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="选择" :width="80">
            <template #cell="{ record }">
              <a-checkbox v-model="record.checked" />
            </template>
          </a-table-column>
          <a-table-column title="书名" data-index="bookName" />
          <a-table-column title="价格" :width="120">
            <template #cell="{ record }">¥{{ record.price }}</template>
          </a-table-column>
          <a-table-column title="数量" :width="160">
            <template #cell="{ record }">
              <a-input-number
                v-model="record.quantity"
                :min="1"
                :max="record.stock"
                @change="updateQuantity(record)"
              />
            </template>
          </a-table-column>
          <a-table-column title="小计" :width="120">
            <template #cell="{ record }">¥{{ record.totalPrice }}</template>
          </a-table-column>
          <a-table-column title="操作" :width="120">
            <template #cell="{ record }">
              <a-button status="danger" size="mini" @click="removeItem(record)">删除</a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <div class="cart-actions">
        <a-checkbox v-model="checkAll" @change="toggleAll">全选</a-checkbox>
        <div class="total">合计：¥{{ totalPrice }}</div>
        <a-button type="primary" :disabled="selectedIds.length === 0" @click="openCheckout">去结算</a-button>
      </div>
    </a-card>

    <a-modal v-model:visible="addressVisible" title="选择收货地址" @ok="checkout">
      <a-select v-model="selectedAddressId" style="width: 100%">
        <a-option v-for="addr in addressList" :key="addr.id" :value="addr.id">
          {{ addr.receiverName }} {{ addr.phone }} {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
        </a-option>
      </a-select>
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import frontCartApi from '../../api/front/cart'
import frontOrderApi from '../../api/front/order'
import frontAddressApi from '../../api/front/address'
import type { Address, CartItem, OrderCreateResponse } from '../../types/api'

type CartItemChecked = CartItem & { checked?: boolean }

const router = useRouter()
const list = ref<CartItemChecked[]>([])
const checkAll = ref(false)
const addressVisible = ref(false)
const addressList = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)

const load = async () => {
  const data = (await frontCartApi.list().catch(() => [])) as any
  list.value = (data || []).map((item: any) => ({ ...item, checked: false }))
  checkAll.value = false
}

const loadAddresses = async () => {
  addressList.value = (await frontAddressApi.list().catch(() => [])) as any
  const defaultAddr = addressList.value.find((addr) => addr.isDefault === 1)
  selectedAddressId.value = defaultAddr?.id || addressList.value[0]?.id || null
}

const updateQuantity = async (record: CartItemChecked) => {
  await frontCartApi.update(record.id, { quantity: record.quantity })
  await load()
}

const removeItem = async (record: CartItemChecked) => {
  await frontCartApi.remove(record.id)
  await load()
}

const selectedIds = computed(() => list.value.filter((item) => item.checked).map((item) => item.id))

const totalPrice = computed(() =>
  list.value.filter((item) => item.checked).reduce((sum, item) => sum + item.totalPrice, 0)
)

const toggleAll = () => {
  list.value = list.value.map((item) => ({ ...item, checked: checkAll.value }))
}

const openCheckout = async () => {
  await loadAddresses()
  addressVisible.value = true
}

const checkout = async () => {
  const cartIds = selectedIds.value
  if (cartIds.length === 0 || !selectedAddressId.value) return
  const data = (await frontOrderApi.create({
    addressId: selectedAddressId.value,
    cartIds,
  })) as any as OrderCreateResponse
  addressVisible.value = false
  await router.push(`/order/${data.orderId}`)
}

onMounted(load)
</script>

<style scoped>
.cart-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.total {
  font-weight: 600;
}
</style>
