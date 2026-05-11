<template>
  <div class="ai-shell">
    <section class="ai-hero">
      <div>
        <div class="hero-eyebrow">AI STORE ANALYSIS</div>
        <h1>AI 店铺数据分析</h1>
        <p>{{ analysis?.summary || '点击“开始分析”后，系统会读取订单、库存、热销图书与用户行为数据生成经营洞察。' }}</p>
      </div>
      <a-space>
        <a-button type="primary" size="large" :loading="loading" @click="runAnalysis">{{ analysis ? '重新分析' : '开始分析' }}</a-button>
        <a-button size="large" @click="goDashboard">返回大屏</a-button>
      </a-space>
    </section>

    <a-alert v-if="analysis" class="source-alert" :type="sourceAlertType" show-icon>
      <template #title>{{ analysis.sourceLabel || '分析结果' }}</template>
      <div>生成时间：{{ formatTime(analysis.generatedAt) }}。{{ sourceTip }}</div>
    </a-alert>

    <a-alert v-else class="source-alert" type="info" show-icon>
      <template #title>等待手动分析</template>
      <div>进入页面不会自动请求模型，避免切换页面就触发长耗时分析。</div>
    </a-alert>

    <a-skeleton v-if="loading" :animation="true" :rows="8" />

    <template v-else-if="analysis">
      <section class="metric-grid">
        <a-card v-for="metric in analysis.metrics" :key="metric.label" class="metric-card" :bordered="false">
          <div class="metric-label">{{ metric.label }}</div>
          <div class="metric-value">{{ metric.value }}</div>
          <div class="metric-trend">{{ metric.trend }}</div>
          <div class="metric-hint">{{ metric.hint }}</div>
        </a-card>
      </section>

      <section class="analysis-grid">
        <a-card class="panel forecast-panel" :bordered="false">
          <template #title>经营预测</template>
          <div class="forecast-main">
            <div>
              <span>下周订单预测</span>
              <strong>{{ analysis.forecast.nextWeekOrders }}</strong>
            </div>
            <div>
              <span>下周营收预测</span>
              <strong>{{ formatCurrency(analysis.forecast.nextWeekRevenue) }}</strong>
            </div>
          </div>
          <div class="forecast-confidence">置信度：{{ analysis.forecast.confidence }}</div>
          <p class="forecast-basis">{{ analysis.forecast.basis }}</p>
        </a-card>

        <a-card class="panel" :bordered="false">
          <template #title>AI 行动建议</template>
          <div class="insight-list">
            <div v-for="item in analysis.insights" :key="item.title" class="insight-item" :class="`insight-${item.level}`">
              <div class="insight-head">
                <span>{{ item.title }}</span>
                <a-tag :color="tagColor(item.level)">{{ item.level }}</a-tag>
              </div>
              <p>{{ item.content }}</p>
              <div class="insight-action">建议：{{ item.action }}</div>
            </div>
          </div>
        </a-card>
      </section>

      <a-card class="panel" :bordered="false">
        <template #title>AI 重点运营图书</template>
        <div v-if="analysis.focusBooks.length > 0" class="focus-books">
          <div v-for="book in analysis.focusBooks" :key="book.bookId" class="focus-book">
            <img v-if="book.coverImage" :src="book.coverImage" alt="cover" @error="book.coverImage = undefined" />
            <div v-else class="focus-cover-placeholder">暂无封面</div>
            <div class="focus-info">
              <strong>{{ book.bookName }}</strong>
              <span>{{ book.author || '佚名' }}</span>
              <em>销量 {{ book.salesCount }} · 销售额 {{ formatCurrency(book.salesAmount) }}</em>
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无热销图书数据" />
      </a-card>
    </template>

    <a-empty v-else description="暂无 AI 分析数据，请点击开始分析" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import adminAiAnalysisApi from '../../api/admin/aiAnalysis'
import type { AdminAiAnalysis } from '../../types/api'

const CACHE_KEY = 'admin_ai_analysis_cache'

const router = useRouter()
const loading = ref(false)
const analysis = ref<AdminAiAnalysis | null>(null)

const sourceAlertType = computed(() => {
  if (analysis.value?.source === 'model') return 'success'
  if (analysis.value?.source === 'timeout') return 'warning'
  return 'info'
})

const sourceTip = computed(() => {
  if (analysis.value?.source === 'model') return '本次内容由大模型生成。'
  if (analysis.value?.source === 'timeout') return '模型请求超时，本次展示本地规则兜底结果。'
  return '本次展示本地规则分析结果。'
})

const restoreCache = () => {
  const cached = localStorage.getItem(CACHE_KEY)
  if (!cached) return
  try {
    analysis.value = JSON.parse(cached) as AdminAiAnalysis
  } catch {
    localStorage.removeItem(CACHE_KEY)
  }
}

const runAnalysis = async () => {
  loading.value = true
  try {
    const data = (await adminAiAnalysisApi.get()) as any
    analysis.value = data
    localStorage.setItem(CACHE_KEY, JSON.stringify(data))
  } finally {
    loading.value = false
  }
}

const goDashboard = () => {
  router.push('/admin/dashboard')
}

const formatCurrency = (value?: number) => {
  return `¥${Number(value || 0).toFixed(2)}`
}

const formatTime = (value?: string) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

const tagColor = (level: string) => {
  if (level === 'success') return 'green'
  if (level === 'warning') return 'orangered'
  return 'arcoblue'
}

onMounted(restoreCache)
</script>
<style scoped>
.ai-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ai-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  border-radius: 24px;
  color: #fff;
  background: radial-gradient(circle at top left, rgba(96, 165, 250, 0.42), transparent 32%), linear-gradient(135deg, #0f172a, #1e3a5f);
  box-shadow: 0 22px 50px rgba(15, 23, 42, 0.22);
}

.hero-eyebrow {
  color: rgba(219, 234, 254, 0.86);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.ai-hero h1 {
  margin: 10px 0;
  font-size: 32px;
}

.ai-hero p {
  max-width: 820px;
  margin: 0;
  color: rgba(241, 245, 249, 0.86);
  line-height: 1.8;
}

.source-alert {
  margin-top: -4px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card,
.panel {
  border-radius: 20px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.08);
}

.metric-label {
  color: #64748b;
  font-size: 13px;
}

.metric-value {
  margin-top: 10px;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
}

.metric-trend {
  margin-top: 8px;
  color: #2563eb;
  font-weight: 700;
}

.metric-hint {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.analysis-grid {
  display: grid;
  grid-template-columns: 0.85fr 1.15fr;
  gap: 16px;
}

.forecast-main {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.forecast-main div {
  padding: 18px;
  border-radius: 16px;
  background: #f8fafc;
}

.forecast-main span {
  display: block;
  color: #64748b;
}

.forecast-main strong {
  display: block;
  margin-top: 8px;
  color: #1e3a5f;
  font-size: 26px;
}

.forecast-confidence,
.forecast-basis {
  margin-top: 14px;
  color: #475569;
}

.insight-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.insight-item {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.insight-warning {
  border-color: rgba(249, 115, 22, 0.32);
  background: rgba(255, 247, 237, 0.88);
}

.insight-success {
  border-color: rgba(34, 197, 94, 0.28);
  background: rgba(240, 253, 244, 0.82);
}

.insight-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #0f172a;
  font-weight: 800;
}

.insight-item p {
  margin: 10px 0 0;
  color: #475569;
  line-height: 1.7;
}

.insight-action {
  margin-top: 10px;
  color: #1e3a5f;
  font-weight: 700;
}

.focus-books {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.focus-book {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 16px;
  background: #f8fafc;
}

.focus-book img,
.focus-cover-placeholder {
  width: 58px;
  height: 78px;
  flex: 0 0 auto;
  border-radius: 10px;
  object-fit: cover;
}

.focus-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  background: #e2e8f0;
  font-size: 12px;
}

.focus-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.focus-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #0f172a;
}

.focus-info span,
.focus-info em {
  color: #64748b;
  font-size: 12px;
  font-style: normal;
}

@media (max-width: 1100px) {
  .metric-grid,
  .focus-books {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analysis-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .ai-hero {
    flex-direction: column;
  }

  .metric-grid,
  .focus-books,
  .forecast-main {
    grid-template-columns: 1fr;
  }
}
</style>
