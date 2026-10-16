<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import JudgeStatus from '@/components/JudgeStatus.vue'
import { getSubmissionList } from '@/api'
import { isAdmin, useLoginState } from '@/store/user'
import { formatDateTime } from '@/utils/format'
import { languageOptions } from '@/utils/language'

const router = useRouter()
const loginState = useLoginState()

const loading = ref(false)
const records = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  scope: 'all',
  status: null,
  page: 1,
  size: 10,
})

const statusOptions = [
  { value: 2, label: 'Accepted' },
  { value: 3, label: 'Wrong Answer' },
  { value: 4, label: 'Time Limit Exceeded' },
  { value: 5, label: 'Runtime Error' },
  { value: 6, label: 'Compile Error' },
]

function languageLabel(value) {
  return languageOptions.find((item) => item.value === value)?.label || value
}

function formatTime(value) {
  return value === null || value === undefined ? '—' : `${value} ms`
}

async function load() {
  loading.value = true
  try {
    const data = await getSubmissionList(query)
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
  query.status = null
  query.page = 1
  load()
}

function handlePage(page) {
  query.page = page
  load()
}

/** 提交详情仅本人与管理员可见，他人的记录不进入详情 */
function canOpen(row) {
  return isAdmin() || row.userId === loginState.user?.id
}

function rowClassName({ row }) {
  return canOpen(row) ? '' : 'row-locked'
}

function openSubmission(row) {
  if (!canOpen(row)) {
    ElMessage.info('只能查看自己的提交详情')
    return
  }
  router.push(`/submissions/${row.id}`)
}

function openProblem(row, column, event) {
  event?.stopPropagation()
  router.push(`/problems/${row.problemId}`)
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h1 class="page-title">提交记录</h1>
      <span class="muted">
        {{ query.scope === 'mine' ? '我的提交' : '全部提交' }}共 {{ total }} 条记录
      </span>
    </div>

    <div class="panel filter">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="提交者">
          <el-input
            v-model="query.keyword"
            placeholder="昵称或学号"
            clearable
            :prefix-icon="Search"
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="范围">
          <el-radio-group v-model="query.scope" @change="handleSearch">
            <el-radio-button value="all">全部提交</el-radio-button>
            <el-radio-button value="mine">我的提交</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="判题状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 200px">
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
      <el-table
        v-loading="loading"
        :data="records"
        row-key="id"
        :row-class-name="rowClassName"
        @row-click="openSubmission"
      >
        <el-table-column label="编号" width="90">
          <template #default="{ $index }">
            <span class="mono muted">{{ (query.page - 1) * query.size + $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交者" width="110">
          <template #default="{ row }">
            <span>{{ row.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题目" min-width="200">
          <template #default="{ row }">
            <span class="title-link" @click="openProblem(row, null, $event)">{{ row.problemTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column label="语言" width="100">
          <template #default="{ row }">
            <span class="mono">{{ languageLabel(row.language) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="190">
          <template #default="{ row }">
            <JudgeStatus :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ formatTime(row.timeUsed) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="代码长度" width="100" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.codeLength }} B</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180" class-name="time-cell">
          <template #default="{ row }">
            <span class="mono muted time-text">{{ formatDateTime(row.submitTime) }}</span>
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

.el-table :deep(.el-table__row) {
  cursor: pointer;
}

.el-table :deep(.el-table__row:hover) .title-link {
  color: var(--ssoj-primary);
}

/* 他人的提交记录不能进入详情，去掉可点击的手型光标 */
.el-table :deep(.row-locked) {
  cursor: default;
}

.time-text {
  white-space: nowrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
}
</style>
