<script setup>
import { onMounted, ref } from 'vue'
import JudgeStatus from '@/components/JudgeStatus.vue'
import { getDashboardSummary } from '@/api'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const summary = ref(null)

async function load() {
  loading.value = true
  try {
    summary.value = await getDashboardSummary()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="page-head">
      <h1 class="page-title">管理首页</h1>
      <span class="muted">数据每 30 秒刷新一次</span>
    </div>

    <div v-if="summary" class="stat-grid">
      <div class="panel stat">
        <span class="stat-value mono">{{ summary.userCount }}</span>
        <span class="stat-label muted">用户总数</span>
      </div>
      <div class="panel stat">
        <span class="stat-value mono">{{ summary.problemCount }}</span>
        <span class="stat-label muted">题目总数</span>
      </div>
      <div class="panel stat">
        <span class="stat-value mono">{{ summary.submissionCount }}</span>
        <span class="stat-label muted">提交总数</span>
      </div>
      <div class="panel stat">
        <span class="stat-value mono">{{ summary.todaySubmission }}</span>
        <span class="stat-label muted">今日提交</span>
      </div>
      <div class="panel stat">
        <span class="stat-value mono" :class="{ 'is-warning': summary.pendingCount > 0 }">
          {{ summary.pendingCount }}
        </span>
        <span class="stat-label muted">待判题任务</span>
      </div>
      <div class="panel stat">
        <span class="stat-value mono">{{ summary.passRate }}%</span>
        <span class="stat-label muted">整体通过率</span>
      </div>
    </div>

    <section class="panel block">
      <header class="block-head"><h2>最近提交</h2></header>
      <el-table :data="summary?.recent || []" row-key="id">
        <el-table-column label="编号" width="90">
          <template #default="{ $index }">
            <span class="mono muted">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="problemTitle" label="题目" min-width="200" />
        <el-table-column label="状态" width="190">
          <template #default="{ row }">
            <JudgeStatus :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            <span class="mono muted">{{ formatDateTime(row.submitTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.3;
}

.stat-value.is-warning {
  color: var(--ssoj-tle);
}

.stat-label {
  font-size: 12px;
}

.block {
  padding: 14px 16px 16px;
}

.block-head {
  margin-bottom: 12px;
}

.block-head h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

@media (max-width: 1120px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
