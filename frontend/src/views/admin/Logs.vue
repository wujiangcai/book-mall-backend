<template>
  <a-card class="admin-card" :bordered="false">
    <div class="page-title">操作日志</div>
    <div class="page-desc">查看后台关键操作记录。</div>
    <a-space direction="vertical" fill size="medium" class="log-list">
      <div class="log-item">[2026-03-15 10:20] 管理员 admin 更新图书库存</div>
      <div class="log-item">[2026-03-15 09:12] 管理员 admin 审核退款订单</div>
      <div class="log-item">[2026-03-14 18:05] 管理员 admin 发布公告</div>
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

.log-list {
  margin-top: 8px;
}

.log-item {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px 16px;
  color: #475569;
}
</style>
