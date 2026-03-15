<template>
  <a-card class="admin-card" :bordered="false">
    <div class="page-title">基础配置</div>
    <div class="page-desc">在此维护系统基础配置项。</div>
    <a-space direction="vertical" fill size="medium" class="config-list">
      <div class="config-item">站点名称：书城管理系统</div>
      <div class="config-item">默认货币：CNY</div>
      <div class="config-item">订单自动关闭：30 分钟</div>
    </a-space>
  </a-card>
</template>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.page-desc {
  margin: 8px 0 16px;
  color: #64748b;
}

.config-list {
  margin-top: 8px;
}

.config-item {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px 16px;
  color: #475569;
}
</style>
