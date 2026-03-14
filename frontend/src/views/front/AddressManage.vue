<template>
  <a-space direction="vertical" fill size="large">
    <a-card title="地址管理" :bordered="false">
      <a-button type="primary" @click="openCreate">新增地址</a-button>
      <a-table :data="list" row-key="id" :pagination="false" style="margin-top: 16px">
        <template #columns>
          <a-table-column title="收件人" data-index="receiverName" />
          <a-table-column title="电话" data-index="phone" />
          <a-table-column title="地址">
            <template #cell="{ record }">
              {{ record.province }}{{ record.city }}{{ record.district }}{{ record.detailAddress }}
            </template>
          </a-table-column>
          <a-table-column title="默认" :width="80">
            <template #cell="{ record }">
              <a-tag v-if="record.isDefault === 1" color="arcoblue">默认</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="180">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" @click="openEdit(record)">编辑</a-button>
                <a-button size="mini" status="danger" @click="remove(record)">删除</a-button>
                <a-button size="mini" @click="setDefault(record)">设默认</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-if="list.length === 0" description="暂无地址" />
    </a-card>

    <a-modal v-model:visible="visible" :title="editing ? '编辑地址' : '新增地址'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="收件人">
          <a-input v-model="form.receiverName" />
        </a-form-item>
        <a-form-item label="电话">
          <a-input v-model="form.phone" />
        </a-form-item>
        <a-form-item label="省">
          <a-input v-model="form.province" />
        </a-form-item>
        <a-form-item label="市">
          <a-input v-model="form.city" />
        </a-form-item>
        <a-form-item label="区">
          <a-input v-model="form.district" />
        </a-form-item>
        <a-form-item label="详细地址">
          <a-input v-model="form.detailAddress" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import frontAddressApi from '../../api/front/address'
import type { Address } from '../../types/api'

const list = ref<Address[]>([])
const visible = ref(false)
const editing = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

const load = async () => {
  list.value = (await frontAddressApi.list().catch(() => [])) as any
}

const openCreate = () => {
  editing.value = false
  editingId.value = null
  Object.assign(form, {
    receiverName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
  })
  visible.value = true
}

const openEdit = (record: Address) => {
  editing.value = true
  editingId.value = record.id
  Object.assign(form, {
    receiverName: record.receiverName,
    phone: record.phone,
    province: record.province || '',
    city: record.city || '',
    district: record.district || '',
    detailAddress: record.detailAddress,
  })
  visible.value = true
}

const submit = async () => {
  if (editing.value && editingId.value) {
    await frontAddressApi.update(editingId.value, { ...form })
    Message.success('地址已更新')
  } else {
    await frontAddressApi.create({ ...form, isDefault: 0 })
    Message.success('地址已创建')
  }
  visible.value = false
  await load()
}

const remove = async (record: Address) => {
  await frontAddressApi.remove(record.id)
  Message.success('地址已删除')
  await load()
}

const setDefault = async (record: Address) => {
  await frontAddressApi.setDefault(record.id)
  Message.success('默认地址已更新')
  await load()
}

onMounted(load)
</script>
