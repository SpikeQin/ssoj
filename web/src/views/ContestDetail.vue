<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import DifficultyTag from '@/components/DifficultyTag.vue'
import { getContestDetail, getContestRank } from '@/api'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const contest = ref(null)
const activeTab = ref('problems')
const rankLoading = ref(false)
const rankLabels = ref([])
const rankRecords = ref([])

const statusText = { before: '未开始', running: '进行中', finished: '已结束' }
const statusType = { before: 'primary', running: 'success', finished: 'info' }

function cellText(cell) {
  if (!cell || !cell.tries) return '·'
  if (cell.accepted) return cell.tries > 1 ? `+${cell.tries}` : '+'
  return `-${cell.tries}`
}

function cellClass(cell) {
  if (!cell || !cell.tries) return 'cell--none'
  return cell.accepted ? 'cell--ac' : 'cell--wa'
}

async function loadRank() {
  rankLoading.value = true
  try {
    const data = await getContestRank(route.params.id)
    rankLabels.value = data.labels
    rankRecords.value = data.records
  } finally {
    rankLoading.value = false
  }
}

async function loadContest(id) {
  loading.value = true
  try {
    contest.value = await getContestDetail(id)
    await loadRank()
  } catch (error) {
    ElMessage.error(error.message)
    router.replace('/contests')
  } finally {
    loading.value = false
  }
}

function openProblem(row) {
  // 带上竞赛编号，比赛期间的提交会计入本场竞赛的榜单
  router.push({ path: `/problems/${row.problemId}`, query: { contestId: contest.value.id } })
}

onMounted(() => loadContest(route.params.id))
watch(
  () => route.params.id,
  (id) => {
    if (id) loadContest(id)
  },
)
</script>

<template>
  <div v-loading="loading">
    <div class="back">
      <el-button link :icon="ArrowLeft" @click="router.push('/contests')">返回竞赛列表</el-button>
    </div>

    <template v-if="contest">
      <div class="detail-head">
        <h1 class="page-title">
          {{ contest.title }}
          <el-tag size="small" effect="light" :type="statusType[contest.status]">
            {{ statusText[contest.status] }}
          </el-tag>
        </h1>
        <div class="meta">
          <span class="mono">{{ formatDateTime(contest.startTime) }} ~ {{ formatDateTime(contest.endTime) }}</span>
          <span class="sep">·</span>
          <span>{{ contest.problems.length }} 道题目</span>
          <span class="sep">·</span>
          <span>{{ contest.participants }} 人参加</span>
          <span class="sep">·</span>
          <span>按通过题数与罚时排名</span>
        </div>
        <p class="desc muted">{{ contest.description }}</p>
      </div>

      <div class="panel tabs-panel">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="题目" name="problems">
            <el-table :data="contest.problems" row-key="label" @row-click="openProblem">
              <el-table-column label="编号" width="90">
                <template #default="{ row }">
                  <span class="label-cell">{{ row.label }}</span>
                </template>
              </el-table-column>
              <el-table-column label="题目" min-width="220">
                <template #default="{ row }">
                  <span class="title-link">{{ row.title }}</span>
                </template>
              </el-table-column>
              <el-table-column label="难度" width="100">
                <template #default="{ row }">
                  <DifficultyTag :difficulty="row.difficulty" />
                </template>
              </el-table-column>
              <el-table-column label="通过人数" width="110" align="right">
                <template #default="{ row }">
                  <span class="mono muted">{{ row.solved }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="榜单" name="rank">
            <el-table v-loading="rankLoading" :data="rankRecords" row-key="rank">
              <el-table-column label="排名" width="80" align="center">
                <template #default="{ row }">
                  <span class="rank-no" :class="{ 'rank-no--top': row.rank <= 3 }">
                    {{ row.rank }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="参赛者" min-width="140">
                <template #default="{ row }">
                  <span class="nickname">{{ row.nickname }}</span>
                </template>
              </el-table-column>
              <el-table-column label="通过题数" width="100" align="right">
                <template #default="{ row }">
                  <span class="mono solved">{{ row.solved }}</span>
                </template>
              </el-table-column>
              <el-table-column label="罚时" width="100" align="right">
                <template #default="{ row }">
                  <span class="mono muted">{{ row.penalty }}</span>
                </template>
              </el-table-column>
              <el-table-column
                v-for="label in rankLabels"
                :key="label"
                :label="label"
                width="70"
                align="center"
              >
                <template #default="{ row }">
                  <span class="cell" :class="cellClass(row.cells[label])">
                    {{ cellText(row.cells[label]) }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </div>
</template>

<style scoped>
.back {
  margin-bottom: 4px;
}

.back :deep(.el-button) {
  padding-left: 0;
  font-size: 13px;
}

.detail-head {
  margin-bottom: 16px;
}

.detail-head .page-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--ssoj-text-2);
}

.sep {
  color: var(--ssoj-text-3);
}

.desc {
  margin: 10px 0 0;
  max-width: 900px;
  font-size: 13px;
  line-height: 1.7;
}

.tabs-panel {
  padding: 4px 16px 8px;
}

.label-cell {
  font-weight: 600;
  color: var(--ssoj-text);
}

.title-link {
  font-weight: 500;
}

.nickname {
  font-weight: 500;
}

.solved {
  font-weight: 600;
  color: var(--ssoj-text);
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

.cell {
  font-family: 'Cascadia Mono', Consolas, 'Courier New', monospace;
  font-size: 13px;
  font-weight: 600;
}

.cell--ac {
  color: var(--ssoj-ac);
}

.cell--wa {
  color: var(--ssoj-wa);
}

.cell--none {
  color: var(--ssoj-text-3);
  font-weight: 400;
}

.el-table :deep(.el-table__row) {
  cursor: pointer;
}
</style>
