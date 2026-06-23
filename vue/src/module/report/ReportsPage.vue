<script setup lang="ts">
/**
 * Страница отчётов.
 * Содержит два раздела: CFD (Cumulative Flow Diagram) и Lead Time.
 * Для визуализации используются встроенные SVG-графики (без Chart.js).
 */
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useReportStore } from './store'
import ProjectLayout from '../../component/ProjectLayout.vue'

const route = useRoute()
const reportStore = useReportStore()
const { cfdData, leadTimeData, loading, error } = storeToRefs(reportStore)

const activeTab = ref<'cfd' | 'leadtime'>('cfd')

const projectId = computed(() => {
  const id = route.params['id']
  return Array.isArray(id) ? id[0] : id
})

const cfdFrom = ref('')
const cfdTo = ref('')
const cfdInterval = ref<'DAY' | 'WEEK' | 'MONTH'>('DAY')

const ltFrom = ref('')
const ltTo = ref('')

const now = new Date()
const defaultFrom = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
const defaultTo = now.toISOString().slice(0, 10)

onMounted(() => {
  cfdFrom.value = defaultFrom
  cfdTo.value = defaultTo
  ltFrom.value = defaultFrom
  ltTo.value = defaultTo
})

async function loadCfdChart() {
  await reportStore.loadCfd({
    projectId: projectId.value,
    from: cfdFrom.value || defaultFrom,
    to: cfdTo.value || defaultTo,
    interval: cfdInterval.value,
  })
}

async function loadLeadTimeChart() {
  await reportStore.loadLeadTime({
    projectId: projectId.value,
    from: ltFrom.value || defaultFrom,
    to: ltTo.value || defaultTo,
  })
}

const SVG_WIDTH = 600
const SVG_HEIGHT = 300
const PAD_LEFT = 50
const PAD_RIGHT = 20
const PAD_TOP = 20
const PAD_BOTTOM = 40
const PLOT_W = SVG_WIDTH - PAD_LEFT - PAD_RIGHT
const PLOT_H = SVG_HEIGHT - PAD_TOP - PAD_BOTTOM

const COLORS = [
  '#4f46e5', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6',
  '#06b6d4', '#ec4899', '#14b8a6', '#f97316', '#6366f1',
]

const columnIds = computed(() => {
  const ids = new Set<string>()
  for (const d of cfdData.value) {
    ids.add(d.columnId)
  }
  return Array.from(ids)
})

interface ColumnSeries {
  columnId: string
  columnName: string
  color: string
  points: { date: string; count: number }[]
}

const cfdSeries = computed((): ColumnSeries[] => {
  return columnIds.value.map((colId, idx) => {
    const name =
      cfdData.value.find((d) => d.columnId === colId)?.columnName ?? colId
    const points = cfdData.value
      .filter((d) => d.columnId === colId)
      .map((d) => ({ date: d.date, count: d.count }))
    return {
      columnId: colId,
      columnName: name,
      color: COLORS[idx % COLORS.length],
      points,
    }
  })
})

const allDateLabels = computed(() => {
  const set = new Set<string>()
  for (const d of cfdData.value) {
    set.add(d.date)
  }
  return Array.from(set).sort()
})

const maxCfdCount = computed(() => {
  let max = 0
  for (const d of cfdData.value) {
    if (d.count > max) max = d.count
  }
  return max || 1
})

function cfdX(date: string): number {
  const idx = allDateLabels.value.indexOf(date)
  if (idx < 0) return PAD_LEFT
  return PAD_LEFT + (idx / Math.max(allDateLabels.value.length - 1, 1)) * PLOT_W
}

function cfdY(count: number): number {
  return PAD_TOP + PLOT_H - (count / maxCfdCount.value) * PLOT_H
}

function buildCfdLinePath(points: { date: string; count: number }[]): string {
  if (points.length === 0) return ''
  return points
    .map((p, i) => {
      const x = cfdX(p.date)
      const y = cfdY(p.count)
      return `${i === 0 ? 'M' : 'L'}${x},${y}`
    })
    .join(' ')
}

const yTicks = computed(() => {
  const ticks: number[] = []
  const step = Math.max(1, Math.floor(maxCfdCount.value / 5))
  for (let i = 0; i <= maxCfdCount.value; i += step) {
    ticks.push(i)
  }
  return ticks
})

const avgLeadTime = computed(() => {
  if (leadTimeData.value.length === 0) return 0
  const sum = leadTimeData.value.reduce((acc, d) => acc + d.leadTimeHours, 0)
  return sum / leadTimeData.value.length
})

const avgLabel = computed(() => avgLeadTime.value.toFixed(1))

const ltMaxHours = computed(() => {
  if (leadTimeData.value.length === 0) return 1
  return Math.max(...leadTimeData.value.map((d) => d.leadTimeHours), 1)
})

function ltX(index: number): number {
  return PAD_LEFT + (index / Math.max(leadTimeData.value.length - 1, 1)) * PLOT_W
}

function ltY(hours: number): number {
  return PAD_TOP + PLOT_H - (hours / ltMaxHours.value) * PLOT_H
}

function ltBarHeight(hours: number): number {
  return (hours / ltMaxHours.value) * PLOT_H
}

const ltYAxisLabels = computed(() => {
  const step = Math.max(1, Math.floor(ltMaxHours.value / 5))
  const labels: number[] = []
  for (let i = 0; i <= ltMaxHours.value; i += step) {
    labels.push(i)
  }
  return labels
})
</script>

<template>
  <ProjectLayout v-if="projectId" :project-id="projectId">
    <div class="reports-page">

    <div class="reports-page__tabs">
      <button
        :class="['reports-page__tab', { 'reports-page__tab--active': activeTab === 'cfd' }]"
        @click="activeTab = 'cfd'"
      >
        CFD Chart
      </button>
      <button
        :class="['reports-page__tab', { 'reports-page__tab--active': activeTab === 'leadtime' }]"
        @click="activeTab = 'leadtime'"
      >
        Lead Time
      </button>
    </div>

    <div v-if="error" class="reports-page__error">{{ error }}</div>

    <!-- CFD Chart -->
    <div v-if="activeTab === 'cfd'" class="reports-page__section">
      <div class="reports-page__filters">
        <label>
          From
          <input v-model="cfdFrom" type="date" />
        </label>
        <label>
          To
          <input v-model="cfdTo" type="date" />
        </label>
        <label>
          Interval
          <select v-model="cfdInterval">
            <option value="DAY">Day</option>
            <option value="WEEK">Week</option>
            <option value="MONTH">Month</option>
          </select>
        </label>
        <button class="reports-page__load-btn" :disabled="loading" @click="loadCfdChart">
          Load
        </button>
      </div>

      <div v-if="loading" class="reports-page__loading">Loading...</div>

      <div v-else-if="cfdData.length === 0" class="reports-page__empty">
        No CFD data. Adjust filters and click Load.
      </div>

      <div v-else class="reports-page__chart-wrapper">
        <svg :viewBox="`0 0 ${SVG_WIDTH} ${SVG_HEIGHT}`" class="reports-page__svg">
          <!-- Y axis grid lines -->
          <line
            v-for="t in yTicks"
            :key="'grid-' + t"
            :x1="PAD_LEFT"
            :y1="cfdY(t)"
            :x2="PAD_LEFT + PLOT_W"
            :y2="cfdY(t)"
            stroke="var(--color-border)"
            stroke-width="1"
          />
          <!-- Y axis labels -->
          <text
            v-for="t in yTicks"
            :key="'yl-' + t"
            :x="PAD_LEFT - 8"
            :y="cfdY(t) + 4"
            text-anchor="end"
            font-size="10"
            fill="var(--color-text-secondary)"
          >
            {{ t }}
          </text>
          <!-- X axis labels (show every nth label) -->
          <text
            v-for="(label, i) in allDateLabels"
            v-show="allDateLabels.length <= 20 || i % Math.ceil(allDateLabels.length / 10) === 0"
            :key="'xl-' + i"
            :x="cfdX(label)"
            :y="SVG_HEIGHT - 8"
            text-anchor="middle"
            font-size="9"
            fill="var(--color-text-secondary)"
          >
            {{ label.slice(5) }}
          </text>
          <!-- Lines -->
          <path
            v-for="series in cfdSeries"
            :key="series.columnId"
            :d="buildCfdLinePath(series.points)"
            :stroke="series.color"
            stroke-width="2"
            fill="none"
          />
          <!-- Legend -->
          <g v-for="(series, i) in cfdSeries" :key="'leg-' + series.columnId">
            <rect
              :x="10"
              :y="SVG_HEIGHT - 70 + i * 18"
              width="10"
              height="10"
              :fill="series.color"
              rx="2"
            />
            <text
              :x="24"
              :y="SVG_HEIGHT - 61 + i * 18"
              font-size="10"
              fill="var(--color-text-secondary)"
            >
              {{ series.columnName }}
            </text>
          </g>
        </svg>
      </div>
    </div>

    <!-- Lead Time Chart -->
    <div v-if="activeTab === 'leadtime'" class="reports-page__section">
      <div class="reports-page__filters">
        <label>
          From
          <input v-model="ltFrom" type="date" />
        </label>
        <label>
          To
          <input v-model="ltTo" type="date" />
        </label>
        <button class="reports-page__load-btn" :disabled="loading" @click="loadLeadTimeChart">
          Load
        </button>
      </div>

      <div v-if="loading" class="reports-page__loading">Loading...</div>

      <div v-else-if="leadTimeData.length === 0" class="reports-page__empty">
        No lead time data. Adjust filters and click Load.
      </div>

      <div v-else class="reports-page__chart-wrapper">
        <div class="reports-page__avg">Average lead time: {{ avgLabel }} hours</div>
        <svg :viewBox="`0 0 ${SVG_WIDTH} ${SVG_HEIGHT}`" class="reports-page__svg">
          <!-- Y axis grid -->
          <line
            v-for="t in ltYAxisLabels"
            :key="'ltg-' + t"
            :x1="PAD_LEFT"
            :y1="ltY(t)"
            :x2="PAD_LEFT + PLOT_W"
            :y2="ltY(t)"
            stroke="var(--color-border)"
            stroke-width="1"
          />
          <!-- Y axis labels -->
          <text
            v-for="t in ltYAxisLabels"
            :key="'lty-' + t"
            :x="PAD_LEFT - 8"
            :y="ltY(t) + 4"
            text-anchor="end"
            font-size="10"
            fill="var(--color-text-secondary)"
          >
            {{ t }}
          </text>
          <!-- Average line -->
          <line
            :x1="PAD_LEFT"
            :y1="ltY(avgLeadTime)"
            :x2="PAD_LEFT + PLOT_W"
            :y2="ltY(avgLeadTime)"
            stroke="#ef4444"
            stroke-width="1.5"
            stroke-dasharray="4,4"
          />
          <text
            :x="PAD_LEFT + PLOT_W - 4"
            :y="ltY(avgLeadTime) - 4"
            text-anchor="end"
            font-size="10"
            fill="#ef4444"
          >
            Avg: {{ avgLabel }}h
          </text>
          <!-- Bars -->
          <rect
            v-for="(d, i) in leadTimeData"
            :key="d.taskId"
            :x="ltX(i) - 3"
            :y="ltY(d.leadTimeHours)"
            width="6"
            :height="ltBarHeight(d.leadTimeHours)"
            fill="var(--color-primary)"
            opacity="0.7"
            rx="1"
          />
        </svg>
      </div>
    </div>
  </div>
</ProjectLayout>
</template>

<style scoped>
.reports-page {
  max-width: 64rem;
  margin: 0 auto;
}
.reports-page__tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}
.reports-page__tab {
  padding: 0.5rem 1.25rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  color: var(--color-text);
  font-weight: 500;
}
.reports-page__tab--active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.reports-page__error {
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius);
}
.reports-page__section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.reports-page__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: flex-end;
  padding: 1rem;
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.reports-page__filters label {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}
.reports-page__filters input,
.reports-page__filters select {
  padding: 0.4rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-background);
  color: var(--color-text);
}
.reports-page__load-btn {
  padding: 0.4rem 1rem;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
}
.reports-page__load-btn:disabled {
  opacity: 0.5;
}
.reports-page__loading,
.reports-page__empty {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius);
}
.reports-page__chart-wrapper {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1rem;
  overflow: auto;
}
.reports-page__svg {
  width: 100%;
  height: auto;
  display: block;
}
.reports-page__avg {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  margin-bottom: 0.5rem;
}
</style>
