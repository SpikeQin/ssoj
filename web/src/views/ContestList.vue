<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getContestList } from '@/api'
import { formatTimeRange } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const records = ref([])

const statusText = {
  before: '未开始',
  running: '进行中',
  finished: '已结束',
}

const statusType = {
  before: 'primary',
  running: 'success',
  finished: 'info',
}

async function load() {
  loading.value = true
  try {
    records.value = await getContestList()
  } finally {
    loading.value = false
  }
}

function openContest(row) {
  router.push(`/contests/${row.id}`)
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h1 class="page-title">竞赛</h1>
      <span class="muted">共 {{ records.length }} 场竞赛</span>
    </div>

    <div class="panel table-panel">
      <el-table v-loading="loading" :data="records" row-key="id" @row-click="openContest">
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="statusType[row.status]">
              {{ statusText[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="竞赛名称" min-width="260">
          <template #default="{ row }">
            <span class="title-link">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="起止时间" width="340">
          <template #default="{ row }">
            <span class="mono muted time-text">{{ formatTimeRange(row.startTime, row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题目数" width="90" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.problemCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="参赛人数" width="110" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.participants }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.table-panel {
  padding-top: 4px;
}

.title-link {
  font-weight: 500;
}

.time-text {
  white-space: nowrap;
}

.el-table :deep(.el-table__row) {
  cursor: pointer;
}

.el-table :deep(.el-table__row:hover) .title-link {
  color: var(--ssoj-primary);
}
</style>
