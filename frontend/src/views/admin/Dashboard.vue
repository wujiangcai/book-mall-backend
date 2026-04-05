<template>
  <div class="dashboard-shell">
    <section class="hero-panel">
      <div class="hero-copy">
        <div class="hero-eyebrow">ADMIN DATA SCREEN</div>
        <h1>书城运营态势总览</h1>
        <p>
          欢迎回来，{{ authStore.userInfo?.username || '管理员' }}。
          当前大屏统计已接入真实数据库，聚焦订单趋势、库存预警、分类结构与热销图书。
        </p>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="load">刷新数据</a-button>
          <a-button size="large" @click="go('/admin/orders')">查看订单</a-button>
        </div>
      </div>

      <div class="hero-metrics">
        <div class="hero-metric">
          <span>今日订单</span>
          <strong>{{ formatNumber(data.overview.todayOrders) }}</strong>
        </div>
        <div class="hero-metric">
          <span>今日营收</span>
          <strong>{{ formatCurrency(data.overview.todayRevenue) }}</strong>
        </div>
        <div class="hero-metric">
          <span>待发货</span>
          <strong>{{ formatNumber(data.overview.pendingShipOrders) }}</strong>
        </div>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="card in overviewCards" :key="card.title" class="stat-card">
        <div class="stat-head">
          <span class="stat-dot" :style="{ background: card.color }"></span>
          <span class="stat-title">{{ card.title }}</span>
        </div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-sub">{{ card.sub }}</div>
      </article>
    </section>

    <a-card class="panel verify-panel" :bordered="false">
      <template #title>数据校验</template>
      <div class="verify-grid">
        <div class="verify-item">
          <span class="verify-label">最近刷新</span>
          <strong>{{ lastLoadedAtText }}</strong>
        </div>
        <div class="verify-item">
          <span class="verify-label">近 7 天趋势条数</span>
          <strong>{{ formatNumber(safeOrderTrend.length) }}</strong>
        </div>
        <div class="verify-item">
          <span class="verify-label">状态汇总单量</span>
          <strong>{{ formatNumber(statusOrderTotal) }}</strong>
        </div>
        <div class="verify-item">
          <span class="verify-label">低库存列表条数</span>
          <strong>{{ formatNumber(safeLowStockBooks.length) }}</strong>
        </div>
      </div>
      <div class="verify-list">
        <div v-for="item in verificationItems" :key="item.label" class="verify-row">
          <div>
            <div class="verify-row-label">{{ item.label }}</div>
            <div class="verify-row-meta">页面值 {{ item.display }} · 推导值 {{ item.derived }}</div>
          </div>
          <a-tag :color="item.match ? 'green' : 'orange'">{{ item.match ? '一致' : '待核对' }}</a-tag>
        </div>
      </div>
      <a-alert
        v-if="hasVerificationMismatch"
        type="warning"
        show-icon
        class="verify-alert"
      >
        当前页面存在需要人工核对的汇总差异，请对照 /api/admin/dashboard 响应确认后端返回值。
      </a-alert>
    </a-card>

    <section class="analytics-grid">
      <a-card class="panel panel-trend" :bordered="false">
        <template #title>近 7 天订单趋势</template>
        <template v-if="trendPoints.length > 0">
          <div class="trend-header">
            <div>
              <div class="trend-value">{{ formatNumber(maxOrderCount) }}</div>
              <div class="trend-label">单日订单峰值</div>
            </div>
            <div>
              <div class="trend-value">{{ formatCurrency(weekRevenue) }}</div>
              <div class="trend-label">近 7 天营收</div>
            </div>
          </div>
          <svg class="trend-chart" viewBox="0 0 720 280" preserveAspectRatio="none">
            <defs>
              <linearGradient id="trendArea" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="rgba(40, 112, 255, 0.38)" />
                <stop offset="100%" stop-color="rgba(40, 112, 255, 0.02)" />
              </linearGradient>
            </defs>
            <path :d="trendAreaPath" fill="url(#trendArea)" />
            <path :d="trendLinePath" class="trend-line" />
            <circle
              v-for="point in trendPoints"
              :key="point.label"
              :cx="point.x"
              :cy="point.y"
              r="6"
              class="trend-dot"
            />
            <text
              v-for="point in trendPoints"
              :key="`label-${point.label}`"
              :x="point.x"
              y="258"
              text-anchor="middle"
              class="trend-axis-label"
            >
              {{ point.label }}
            </text>
          </svg>
        </template>
        <a-empty v-else description="暂无趋势数据" />
      </a-card>

      <a-card class="panel panel-ring" :bordered="false">
        <template #title>订单状态分布</template>
        <template v-if="statusSegments.length > 0">
          <div class="ring-layout">
            <div class="ring-chart" :style="{ background: ringGradient }">
              <div class="ring-inner">
                <strong>{{ formatNumber(data.overview.totalOrders) }}</strong>
                <span>订单总量</span>
              </div>
            </div>
            <div class="ring-legend">
              <div v-for="item in statusSegments" :key="item.label" class="legend-item">
                <span class="legend-swatch" :style="{ background: item.color }"></span>
                <div>
                  <div class="legend-title">{{ item.label }}</div>
                  <div class="legend-meta">{{ item.count }} 单 · {{ item.percent }}%</div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <a-empty v-else description="暂无状态数据" />
      </a-card>
    </section>

    <section class="details-grid">
      <a-card class="panel" :bordered="false">
        <template #title>分类图书分布</template>
        <div v-if="categoryBars.length > 0" class="category-bars">
          <div v-for="item in categoryBars" :key="item.categoryName" class="category-row">
            <div class="category-meta">
              <span>{{ item.categoryName }}</span>
              <strong>{{ item.bookCount }}</strong>
            </div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: `${item.percent}%` }"></div>
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无分类数据" />
      </a-card>

      <a-card class="panel" :bordered="false">
        <template #title>低库存预警</template>
        <div v-if="safeLowStockBooks.length > 0" class="warning-list">
          <div v-for="item in safeLowStockBooks" :key="item.id" class="warning-item">
            <div>
              <div class="warning-title">{{ item.bookName }}</div>
              <div class="warning-sub">{{ item.author || '作者未填写' }}</div>
            </div>
            <div class="warning-stock" :class="{ danger: item.stock <= 3 }">
              {{ item.stock }}
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无低库存图书" />
      </a-card>
    </section>

    <section class="details-grid">
      <a-card class="panel" :bordered="false">
        <template #title>热销图书 TOP 5</template>
        <div v-if="safeHotBooks.length > 0" class="hot-book-list">
          <div v-for="(book, index) in safeHotBooks" :key="book.bookId" class="hot-book-item">
            <div class="hot-rank">{{ index + 1 }}</div>
            <img v-if="book.coverImage" :src="book.coverImage" alt="cover" class="hot-cover" @error="book.coverImage = undefined" />
            <div v-else class="hot-cover hot-cover--empty">封面</div>
            <div class="hot-info">
              <div class="hot-name">{{ book.bookName }}</div>
              <div class="hot-sub">{{ book.author || '作者未填写' }}</div>
            </div>
            <div class="hot-sales">
              <strong>{{ book.salesCount }}</strong>
              <span>{{ formatCurrency(book.salesAmount) }}</span>
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无热销数据" />
      </a-card>

      <a-card class="panel" :bordered="false">
        <template #title>最近订单动态</template>
        <div v-if="safeRecentOrders.length > 0" class="recent-order-list">
          <div v-for="order in safeRecentOrders" :key="order.id" class="recent-order-item">
            <div>
              <div class="recent-order-no">{{ order.orderNo }}</div>
              <div class="recent-order-meta">{{ order.username || '未知用户' }} · {{ order.createTime }}</div>
            </div>
            <div class="recent-order-side">
              <span class="status-chip" :style="{ background: statusColor(order.status) }">
                {{ statusText(order.status) }}
              </span>
              <strong>{{ formatCurrency(order.totalAmount) }}</strong>
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无订单数据" />
      </a-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import adminDashboardApi from '../../api/admin/dashboard'
import { useAuthStore } from '../../store/auth'
import type {
  AdminDashboard,
  AdminDashboardCategoryStat,
  AdminDashboardOrderStatus,
  AdminDashboardTrendPoint,
} from '../../types/api'

type TrendSvgPoint = {
  label: string
  x: number
  y: number
}

const router = useRouter()
const authStore = useAuthStore()

const emptyDashboard = (): AdminDashboard => ({
  overview: {
    customerCount: 0,
    adminCount: 0,
    totalBooks: 0,
    onShelfBooks: 0,
    totalOrders: 0,
    pendingShipOrders: 0,
    lowStockBooks: 0,
    activeBanners: 0,
    todayOrders: 0,
    totalRevenue: 0,
    todayRevenue: 0,
  },
  orderTrend: [],
  orderStatus: [],
  categoryStats: [],
  hotBooks: [],
  lowStockBooks: [],
  recentOrders: [],
})

const mergeDashboard = (dashboard?: Partial<AdminDashboard> | null): AdminDashboard => ({
  ...emptyDashboard(),
  ...(dashboard || {}),
  overview: {
    ...emptyDashboard().overview,
    ...(dashboard?.overview || {}),
  },
  orderTrend: (dashboard?.orderTrend || []).filter(Boolean),
  orderStatus: (dashboard?.orderStatus || []).filter(Boolean),
  categoryStats: (dashboard?.categoryStats || []).filter(Boolean),
  hotBooks: (dashboard?.hotBooks || []).filter(Boolean),
  lowStockBooks: (dashboard?.lowStockBooks || []).filter(Boolean),
  recentOrders: (dashboard?.recentOrders || []).filter(Boolean),
})

const normalizeDashboardResponse = async () => {
  const result = await adminDashboardApi.get()
  const payload = (result as any)?.data ?? result
  return mergeDashboard(payload as Partial<AdminDashboard> | null)
}

const data = reactive<AdminDashboard>(emptyDashboard())
const lastLoadedAt = ref('')

const palette = ['#2870ff', '#00a6a6', '#ff8a00', '#ff5f5f', '#8856ff', '#20b15a']

const load = async () => {
  try {
    const dashboard = await normalizeDashboardResponse()
    Object.assign(data, dashboard)
    lastLoadedAt.value = new Date().toLocaleString('zh-CN')
  } catch (error: any) {
    Object.assign(data, emptyDashboard())
    Message.error(error?.message || '加载大屏数据失败')
  }
}

const go = (path: string) => {
  router.push(path)
}

const formatNumber = (value?: number) => Number(value || 0).toLocaleString('zh-CN')

const formatCurrency = (value?: number) => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

const statusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '待发货',
    3: '已发货',
    4: '已完成',
    5: '已取消',
  }
  return map[status] || '未知'
}

const statusColor = (status: number) => {
  const map: Record<number, string> = {
    0: '#6c7a89',
    1: '#4f7cff',
    2: '#ff9f1a',
    3: '#00a870',
    4: '#6f42c1',
    5: '#ef476f',
  }
  return map[status] || '#6c7a89'
}

const overviewCards = computed(() => [
  {
    title: '用户总量',
    value: formatNumber(data.overview.customerCount),
    sub: `管理员 ${formatNumber(data.overview.adminCount)} 人`,
    color: '#2870ff',
  },
  {
    title: '图书总量',
    value: formatNumber(data.overview.totalBooks),
    sub: `上架中 ${formatNumber(data.overview.onShelfBooks)} 本`,
    color: '#00a6a6',
  },
  {
    title: '订单总量',
    value: formatNumber(data.overview.totalOrders),
    sub: `待发货 ${formatNumber(data.overview.pendingShipOrders)} 单`,
    color: '#ff8a00',
  },
  {
    title: '累计营收',
    value: formatCurrency(data.overview.totalRevenue),
    sub: `今日 ${formatCurrency(data.overview.todayRevenue)}`,
    color: '#ff5f5f',
  },
  {
    title: '轮播启用',
    value: formatNumber(data.overview.activeBanners),
    sub: '当前首页可展示内容',
    color: '#8856ff',
  },
  {
    title: '低库存预警',
    value: formatNumber(data.overview.lowStockBooks),
    sub: '安全阈值 10 本以内',
    color: '#20b15a',
  },
])

const safeOrderTrend = computed(() => (data.orderTrend as AdminDashboardTrendPoint[]).filter(Boolean))
const safeHotBooks = computed(() => (data.hotBooks || []).filter(Boolean))
const safeLowStockBooks = computed(() => (data.lowStockBooks || []).filter(Boolean))
const safeRecentOrders = computed(() => (data.recentOrders || []).filter(Boolean))
const statusOrderTotal = computed(() => statusSegments.value.reduce((sum, item) => sum + Number(item.count || 0), 0))
const lastLoadedAtText = computed(() => lastLoadedAt.value || '尚未刷新')

const verificationItems = computed(() => [
  {
    label: '订单总量',
    display: Number(data.overview.totalOrders || 0),
    derived: statusOrderTotal.value,
    match: Number(data.overview.totalOrders || 0) === statusOrderTotal.value,
  },
  {
    label: '低库存预警',
    display: Number(data.overview.lowStockBooks || 0),
    derived: safeLowStockBooks.value.length,
    match: Number(data.overview.lowStockBooks || 0) === safeLowStockBooks.value.length,
  },
  {
    label: '热销图书条数',
    display: Math.min(5, safeHotBooks.value.length),
    derived: safeHotBooks.value.length,
    match: safeHotBooks.value.length <= 5,
  },
  {
    label: '最近订单条数',
    display: safeRecentOrders.value.length,
    derived: safeRecentOrders.value.length,
    match: true,
  },
])

const hasVerificationMismatch = computed(() => verificationItems.value.some((item) => !item.match))

const maxOrderCount = computed(() => Math.max(...safeOrderTrend.value.map((item) => Number(item.orderCount || 0)), 0))
const weekRevenue = computed(() => safeOrderTrend.value.reduce((sum, item) => sum + Number(item.revenue || 0), 0))

const trendPoints = computed<TrendSvgPoint[]>(() => {
  const trend = safeOrderTrend.value
  if (trend.length === 0) return []
  const width = 720
  const height = 220
  const left = 36
  const right = 24
  const top = 24
  const bottom = 40
  const step = trend.length === 1 ? 0 : (width - left - right) / (trend.length - 1)
  const maxValue = Math.max(...trend.map((item) => Number(item.orderCount || 0)), 1)
  return trend.map((item, index) => {
    const x = left + step * index
    const ratio = Number(item.orderCount || 0) / maxValue
    const y = top + (height - top - bottom) * (1 - ratio)
    return { label: item.day || '', x, y }
  })
})

const trendLinePath = computed(() => {
  if (trendPoints.value.length === 0) return ''
  return trendPoints.value
    .map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`)
    .join(' ')
})

const trendAreaPath = computed(() => {
  if (trendPoints.value.length === 0) return ''
  const first = trendPoints.value[0]
  const last = trendPoints.value[trendPoints.value.length - 1]
  return `${trendLinePath.value} L ${last.x} 240 L ${first.x} 240 Z`
})

const statusSegments = computed(() => {
  const list = (data.orderStatus as AdminDashboardOrderStatus[]).filter(Boolean)
  const total = list.reduce((sum, item) => sum + Number(item.count || 0), 0) || 1
  return list.map((item, index) => ({
    ...item,
    color: palette[index % palette.length],
    percent: Math.round((Number(item.count || 0) / total) * 100),
  }))
})

const ringGradient = computed(() => {
  if (statusSegments.value.length === 0) return '#e7edf5'
  let current = 0
  const slices = statusSegments.value.map((item) => {
    const start = current
    current += item.percent
    return `${item.color} ${start}% ${current}%`
  })
  return `conic-gradient(${slices.join(', ')})`
})

const categoryBars = computed(() => {
  const list = (data.categoryStats as AdminDashboardCategoryStat[]).filter(Boolean)
  const maxValue = Math.max(...list.map((item) => item.bookCount || 0), 1)
  return list.map((item, index) => ({
    ...item,
    percent: Math.max(12, Math.round((Number(item.bookCount || 0) / maxValue) * 100)),
    color: palette[index % palette.length],
  }))
})

onMounted(load)
</script>

<style scoped>
.dashboard-shell {
  --screen-bg: linear-gradient(180deg, #edf4ff 0%, #f7f9fc 48%, #eef5f0 100%);
  --panel-bg: rgba(255, 255, 255, 0.9);
  --panel-border: rgba(116, 140, 171, 0.16);
  --panel-shadow: 0 18px 40px rgba(22, 39, 68, 0.08);
  --text-main: #17324d;
  --text-sub: #5f7288;
  --blue: #2870ff;
  --teal: #00a6a6;
  --amber: #ff9f1a;
  --rose: #ef476f;
  --violet: #8856ff;
  --green: #20b15a;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 6px 2px 14px;
  background: var(--screen-bg);
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(280px, 1fr);
  gap: 20px;
  padding: 28px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(40, 112, 255, 0.18), transparent 28%),
    radial-gradient(circle at left bottom, rgba(32, 177, 90, 0.16), transparent 25%),
    linear-gradient(135deg, #10243a 0%, #183a5b 48%, #1c4f60 100%);
  color: #f3f7fb;
  box-shadow: 0 26px 60px rgba(16, 36, 58, 0.2);
}

.hero-eyebrow {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.hero-copy h1 {
  margin: 16px 0 10px;
  color: #fff;
  font-size: 34px;
  line-height: 1.1;
}

.hero-copy p {
  max-width: 700px;
  margin: 0;
  color: rgba(243, 247, 251, 0.82);
  font-size: 15px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
}

.hero-metrics {
  display: grid;
  gap: 14px;
}

.hero-metric {
  padding: 18px 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
}

.hero-metric span {
  display: block;
  color: rgba(243, 247, 251, 0.76);
  font-size: 13px;
}

.hero-metric strong {
  display: block;
  margin-top: 8px;
  color: #fff;
  font-size: 30px;
  line-height: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  min-height: 148px;
  padding: 18px;
  border: 1px solid var(--panel-border);
  border-radius: 20px;
  background: var(--panel-bg);
  box-shadow: var(--panel-shadow);
}

.stat-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.stat-title {
  color: var(--text-sub);
  font-size: 13px;
}

.stat-value {
  margin-top: 18px;
  color: var(--text-main);
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
}

.stat-sub {
  margin-top: 10px;
  color: #7b8ea6;
  font-size: 12px;
}

.analytics-grid,
.details-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr);
  gap: 20px;
}

.panel {
  border-radius: 24px;
  background: var(--panel-bg);
  box-shadow: var(--panel-shadow);
}

.verify-panel {
  padding-bottom: 4px;
}

.verify-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.verify-item {
  padding: 16px 18px;
  border: 1px solid rgba(30, 58, 95, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.verify-label {
  display: block;
  color: var(--text-sub);
  font-size: 12px;
}

.verify-item strong {
  display: block;
  margin-top: 8px;
  color: var(--text-main);
  font-size: 22px;
  line-height: 1.2;
}

.verify-list {
  display: grid;
  gap: 12px;
}

.verify-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-top: 1px dashed rgba(95, 114, 136, 0.22);
}

.verify-row:first-child {
  border-top: none;
}

.verify-row-label {
  color: var(--text-main);
  font-weight: 600;
}

.verify-row-meta {
  margin-top: 4px;
  color: var(--text-sub);
  font-size: 13px;
}

.verify-alert {
  margin-top: 16px;
}

.panel :deep(.arco-card-header) {
  border-bottom: none;
  padding-bottom: 0;
}

.panel :deep(.arco-card-header-title) {
  color: var(--text-main);
  font-size: 16px;
  font-weight: 700;
}

.trend-header {
  display: flex;
  gap: 30px;
  margin-bottom: 8px;
}

.trend-value {
  color: var(--text-main);
  font-size: 28px;
  font-weight: 700;
}

.trend-label {
  margin-top: 2px;
  color: var(--text-sub);
  font-size: 12px;
}

.trend-chart {
  width: 100%;
  height: 280px;
}

.trend-line {
  fill: none;
  stroke: var(--blue);
  stroke-width: 4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.trend-dot {
  fill: #fff;
  stroke: var(--blue);
  stroke-width: 3;
}

.trend-axis-label {
  fill: #73869d;
  font-size: 12px;
}

.ring-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 18px;
  align-items: center;
  min-height: 280px;
}

.ring-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  margin: 0 auto;
}

.ring-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(99, 126, 154, 0.08);
}

.ring-inner strong {
  color: var(--text-main);
  font-size: 28px;
  line-height: 1;
}

.ring-inner span {
  margin-top: 6px;
  color: var(--text-sub);
  font-size: 12px;
}

.ring-legend {
  display: grid;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: #f4f7fb;
}

.legend-swatch {
  width: 12px;
  height: 12px;
  border-radius: 999px;
}

.legend-title {
  color: var(--text-main);
  font-size: 13px;
  font-weight: 600;
}

.legend-meta {
  margin-top: 2px;
  color: var(--text-sub);
  font-size: 12px;
}

.category-bars,
.warning-list,
.hot-book-list,
.recent-order-list {
  display: grid;
  gap: 12px;
}

.category-row {
  padding: 12px 14px;
  border-radius: 16px;
  background: #f7f9fc;
}

.category-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: var(--text-main);
  font-size: 13px;
}

.bar-track {
  height: 10px;
  border-radius: 999px;
  background: #e6edf5;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2870ff 0%, #67c4ff 100%);
}

.warning-item,
.recent-order-item,
.hot-book-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #f7f9fc;
}

.warning-title,
.recent-order-no,
.hot-name {
  color: var(--text-main);
  font-weight: 600;
}

.warning-sub,
.recent-order-meta,
.hot-sub {
  margin-top: 4px;
  color: var(--text-sub);
  font-size: 12px;
}

.warning-stock {
  min-width: 46px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(255, 159, 26, 0.12);
  color: #c06a00;
  font-weight: 700;
  text-align: center;
}

.warning-stock.danger {
  background: rgba(239, 71, 111, 0.12);
  color: var(--rose);
}

.hot-rank {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: linear-gradient(135deg, #10243a, #2870ff);
  color: #fff;
  font-weight: 700;
  flex-shrink: 0;
}

.hot-cover {
  width: 48px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.hot-cover--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8edf4, #f8fafc);
  color: var(--text-sub);
  font-size: 12px;
}

.hot-info {
  flex: 1;
  min-width: 0;
}

.hot-sales {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  color: var(--text-main);
}

.hot-sales strong {
  font-size: 18px;
  line-height: 1;
}

.hot-sales span {
  margin-top: 6px;
  color: var(--text-sub);
  font-size: 12px;
}

.recent-order-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  color: var(--text-main);
}

.status-chip {
  padding: 5px 10px;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1080px) {
  .hero-panel,
  .analytics-grid,
  .details-grid {
    grid-template-columns: 1fr;
  }

  .ring-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-panel {
    padding: 20px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-actions {
    flex-wrap: wrap;
  }
}
</style>
