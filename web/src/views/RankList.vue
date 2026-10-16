<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getGlobalRank } from '@/api'

const router = useRouter()
const loading = ref(false)
const records = ref([])

async function load() {
  loading.value = true
  try {
    records.value = await getGlobalRank()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h1 class="page-title">榜单</h1>
      <span class="muted">按通过题数排名，题数相同时按最后通过时间</span>
    </div>

    <div class="panel table-panel">
      <el-table v-loading="loading" :data="records" row-key="rank">
        <el-table-column label="排名" width="90" align="center">
          <template #default="{ row }">
            <span class="rank-no" :class="{ 'rank-no--top': row.rank <= 3 }">{{ row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user">
              <el-avatar :size="24" class="user-avatar">{{ row.nickname.slice(0, 1) }}</el-avatar>
              <span class="user-name" :class="{ 'is-me': row.nickname === '演示学生' }">
                {{ row.nickname }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="通过题数" width="120" align="right">
          <template #default="{ row }">
            <span class="mono solved">{{ row.solved }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交次数" width="120" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.submitCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="通过率" min-width="200">
          <template #default="{ row }">
            <div class="rate">
              <el-progress
                :percentage="row.acceptRate"
                :stroke-width="4"
                color="#98a2b3"
                :show-text="false"
              />
              <span class="rate-text muted mono">{{ row.acceptRate }}%</span>
            </div>
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

.rank-no {
  display: inline-block;
  min-width: 22px;
  color: var(--ssoj-text-2);
}

.rank-no--top {
  color: var(--ssoj-primary);
  font-weight: 600;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background: #eef3fe;
  color: var(--ssoj-primary);
  font-size: 12px;
}

.user-name {
  font-weight: 500;
}

.user-name.is-me {
  color: var(--ssoj-primary);
}

.solved {
  font-weight: 600;
  color: var(--ssoj-text);
}

.rate {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rate :deep(.el-progress) {
  flex: 1 1 auto;
  min-width: 0;
}

.rate-text {
  flex: 0 0 auto;
  font-size: 12px;
}
</style>
