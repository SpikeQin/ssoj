<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Search } from '@element-plus/icons-vue'
import AnnouncementBoard from '@/components/AnnouncementBoard.vue'
import { getProblemList } from '@/api'
import { difficultyText } from '@/utils/dict'

const router = useRouter()

const loading = ref(false)
const records = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  difficulty: null,
  status: null,
  page: 1,
  size: 10,
})

const difficultyOptions = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
]

const statusOptions = [
  { value: 'ac', label: '已通过' },
  { value: 'wa', label: '未通过' },
  { value: 'none', label: '未尝试' },
]

function difficultyType(difficulty) {
  return difficulty === 1 ? 'success' : difficulty === 2 ? 'warning' : 'danger'
}

function myStatusClass(status) {
  if (status === 'ac') return 'status--ac'
  if (status === 'wa') return 'status--wa'
  return 'status--idle'
}

function myStatusText(status) {
  if (status === 'ac') return '已通过'
  if (status === 'wa') return '未通过'
  return '未尝试'
}

async function load() {
  loading.value = true
  try {
    const data = await getProblemList(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  load()
}

function handleReset() {
  query.keyword = ''
  query.difficulty = null
  query.status = null
  query.page = 1
  load()
}

function handlePage(page) {
  query.page = page
  load()
}

function openProblem(row) {
  router.push(`/problems/${row.id}`)
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h1 class="page-title">题库</h1>
      <span class="muted">共 {{ total }} 道题目</span>
    </div>

    <AnnouncementBoard />

    <div class="panel filter">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="题目名称或编号"
            clearable
            :prefix-icon="Search"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="query.difficulty" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in difficultyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="我的状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="panel table-panel">
      <el-table v-loading="loading" :data="records" row-key="id" @row-click="openProblem">
        <el-table-column label="状态" width="92">
          <template #default="{ row }">
            <span class="status" :class="myStatusClass(row.myStatus)">
              <i class="dot"></i>{{ myStatusText(row.myStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="编号" width="80">
          <template #default="{ row }">
            <span class="mono muted">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题目" min-width="200">
          <template #default="{ row }">
            <span class="title-link">{{ row.title }}</span>
            <el-tag v-if="row.contestTitle" size="small" type="warning" effect="light" class="contest-tag">
              {{ row.contestTitle }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="88">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="difficultyType(row.difficulty)">
              {{ difficultyText[row.difficulty] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="170">
          <template #default="{ row }">
            <span v-for="tag in row.tags" :key="tag" class="tag">{{ tag }}</span>
          </template>
        </el-table-column>
        <el-table-column label="通过率" width="150">
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
        <el-table-column label="提交" width="90" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.submitCount }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :current-page="query.page"
          :page-size="query.size"
          @current-change="handlePage"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter {
  padding: 16px 16px 0;
  margin-bottom: 12px;
}

.filter :deep(.el-form-item) {
  margin-bottom: 16px;
}

.table-panel {
  padding-top: 4px;
}

.title-link {
  font-weight: 500;
}

.contest-tag {
  margin-left: 8px;
}

.el-table :deep(.el-table__row) {
  cursor: pointer;
}

.el-table :deep(.el-table__row:hover) .title-link {
  color: var(--ssoj-primary);
}

.tag {
  display: inline-block;
  padding: 1px 8px;
  margin-right: 6px;
  border-radius: 4px;
  background: #f2f4f7;
  color: var(--ssoj-text-2);
  font-size: 12px;
  line-height: 18px;
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

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
}
</style>
