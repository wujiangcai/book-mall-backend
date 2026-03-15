<template>
  <div class="admin-dashboard">
    <div class="welcome-card">
      <div class="welcome-title">后台数据概览</div>
      <div class="welcome-sub">欢迎回来，{{ authStore.userInfo?.username || '管理员' }}，请开始今日管理工作。</div>
      <div class="welcome-meta">
        <div class="meta-item">
          <span>今日订单</span>
          <strong>128</strong>
        </div>
        <div class="meta-item">
          <span>待处理退款</span>
          <strong>6</strong>
        </div>
        <div class="meta-item">
          <span>低库存预警</span>
          <strong>12</strong>
        </div>
      </div>
    </div>

    <div class="quick-grid">
      <a-card class="quick-card" :bordered="false">
        <div class="card-title">图书管理</div>
        <div class="card-desc">维护图书信息、分类与库存状态</div>
        <a-button type="primary" @click="go('/admin/books')">进入管理</a-button>
      </a-card>
      <a-card class="quick-card" :bordered="false">
        <div class="card-title">订单管理</div>
        <div class="card-desc">跟踪订单、退款与配送状态</div>
        <a-button @click="go('/admin/orders')">查看订单</a-button>
      </a-card>
      <a-card class="quick-card" :bordered="false">
        <div class="card-title">营销中心</div>
        <div class="card-desc">运营轮播、优惠券与公告内容</div>
        <a-button @click="go('/admin/banners')">进入中心</a-button>
      </a-card>
      <a-card class="quick-card" :bordered="false">
        <div class="card-title">用户管理</div>
        <div class="card-desc">管理用户信息与权限设置</div>
        <a-button @click="go('/admin/users')">查看用户</a-button>
      </a-card>
    </div>

    <a-card class="admin-section" :bordered="false">
      <div class="section-title">运营提醒</div>
      <a-space direction="vertical" fill size="medium" class="section-list">
        <div class="section-item">今日有 6 笔退款请求待处理</div>
        <div class="section-item">有 12 本图书库存低于安全阈值</div>
        <div class="section-item">营销中心新增 3 条待发布公告</div>
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const go = (path: string) => {
  router.push(path)
}

</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.welcome-title {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.welcome-sub {
  margin-top: 6px;
  color: #64748b;
}

.welcome-meta {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.meta-item {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #475569;
}

.meta-item strong {
  color: #1e3a5f;
  font-size: 20px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.quick-card {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.card-desc {
  margin: 8px 0 16px;
  color: #64748b;
  font-size: 13px;
}

.admin-section {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.section-list {
  margin-top: 12px;
}

.section-item {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px 16px;
  color: #475569;
}
</style>
