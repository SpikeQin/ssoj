<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import DifficultyTag from '@/components/DifficultyTag.vue'
import {
  deleteProblem,
  getProblemCases,
  getProblemList,
  saveProblem,
  saveProblemCases,
  setProblemEnabled,
} from '@/api'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('新建题目')
const formRef = ref(null)
const saving = ref(false)
const form = reactive({
  id: null,
  title: '',
  difficulty: 1,
  tags: [],
  timeLimit: 1000,
  memoryLimit: 128,
  description: '',
})

const caseDialogVisible = ref(false)
const caseLoading = ref(false)
const caseSaving = ref(false)
const currentProblem = ref(null)
const caseList = ref([])

const formRules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题面描述', trigger: 'blur' }],
}

/** 常用算法标签，可直接选择也可以输入新的标签 */
const tagOptions = [
  '基础语法',
  '模拟',
  '字符串',
  '数学',
  '数论',
  '浮点数',
  '排序',
  '贪心',
  '前缀和',
  '差分',
  '二分',
  '双指针',
  '搜索',
  '动态规划',
  '记忆化搜索',
  '概率与期望',
  '图论',
]

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
  query.page = 1
  load()
}

function handlePage(page) {
  query.page = page
  load()
}

function openCreate() {
  dialogTitle.value = '新建题目'
  Object.assign(form, {
    id: null,
    title: '',
    difficulty: 1,
    tags: [],
    timeLimit: 1000,
    memoryLimit: 128,
    description: '',
  })
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑题目'
  Object.assign(form, {
    id: row.id,
    title: row.title,
    difficulty: row.difficulty,
    tags: [...(row.tags || [])],
    timeLimit: row.timeLimit,
    memoryLimit: row.memoryLimit,
    description: row.description,
  })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await saveProblem({ ...form, tags: form.tags })
      ElMessage.success(form.id ? '题目已更新' : '题目已创建')
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function openCases(row) {
  currentProblem.value = row
  caseDialogVisible.value = true
  caseLoading.value = true
  try {
    caseList.value = await getProblemCases(row.id)
  } finally {
    caseLoading.value = false
  }
}

function addCase() {
  caseList.value.push({
    caseId: caseList.value.length + 1,
    input: '',
    output: '',
    enabled: 1,
  })
}

function removeCase(index) {
  caseList.value.splice(index, 1)
}

async function submitCases() {
  const empty = caseList.value.some((item) => !item.input?.trim() || !item.output?.trim())
  if (empty) {
    ElMessage.warning('用例的输入与输出不能为空')
    return
  }
  caseSaving.value = true
  try {
    await saveProblemCases(currentProblem.value.id, caseList.value)
    ElMessage.success('测试用例已保存')
    caseDialogVisible.value = false
  } finally {
    caseSaving.value = false
  }
}

async function toggleEnabled(row) {
  const next = row.enabled === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      next === 1 ? `确认将《${row.title}》重新上线？` : `确认下线《${row.title}》？`,
      '题目状态变更',
      { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' },
    )
    await setProblemEnabled(row.id, next)
    ElMessage.success(next === 1 ? '题目已上线' : '题目已下线')
    load()
  } catch {
    // 取消操作时不做处理
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除题目《${row.title}》？删除后学生不再能看到该题，已有的提交记录仍会保留。`,
      '删除题目',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await deleteProblem(row.id)
    ElMessage.success('题目已删除')
    load()
  } catch {
    // 取消操作时不做处理
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h1 class="page-title">题目管理</h1>
      <span class="muted">共 {{ total }} 道题目</span>
    </div>

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
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建题目</el-button>
    </div>

    <div class="panel table-panel">
      <el-table v-loading="loading" :data="records" row-key="id">
        <el-table-column label="编号" width="80">
          <template #default="{ row }">
            <span class="mono muted">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="题目" min-width="200" />
        <el-table-column label="难度" width="96">
          <template #default="{ row }">
            <DifficultyTag :difficulty="row.difficulty" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="96">
          <template #default="{ row }">
            <span class="status" :class="row.enabled === 1 ? 'status--ac' : 'status--idle'">
              <i class="dot"></i>{{ row.enabled === 1 ? '已上线' : '已下线' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="提交数" width="96" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.submitCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="通过率" width="96" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.acceptRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openCases(row)">测试用例</el-button>
            <el-button link type="primary" @click="toggleEnabled(row)">
              {{ row.enabled === 1 ? '下线' : '上线' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="题目标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：A + B 问题" />
        </el-form-item>
        <el-form-item label="难度">
          <el-radio-group v-model="form.difficulty">
            <el-radio-button :value="1">简单</el-radio-button>
            <el-radio-button :value="2">中等</el-radio-button>
            <el-radio-button :value="3">困难</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时间限制">
          <el-input-number v-model="form.timeLimit" :min="100" :max="5000" :step="100" />
          <span class="unit muted">毫秒</span>
        </el-form-item>
        <el-form-item label="内存限制">
          <el-input-number v-model="form.memoryLimit" :min="16" :max="512" :step="16" />
          <span class="unit muted">MB</span>
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入算法标签，建议 1 到 2 个"
            style="width: 100%"
          >
            <el-option v-for="item in tagOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="题面" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="8"
            placeholder="使用 Markdown 编写题面，包含题目描述、输入输出格式与样例"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="caseDialogVisible"
      :title="`测试用例${currentProblem ? ' · ' + currentProblem.title : ''}`"
      width="860px"
    >
      <div v-loading="caseLoading">
        <div class="case-toolbar">
          <span class="muted">判题时按顺序逐个执行，输入与输出需一一对应</span>
          <el-button size="small" :icon="Plus" @click="addCase">新增用例</el-button>
        </div>
        <div v-for="(item, index) in caseList" :key="index" class="case-item">
          <div class="case-index mono muted">用例 {{ index + 1 }}</div>
          <el-input
            v-model="item.input"
            type="textarea"
            :rows="3"
            placeholder="输入数据"
            class="case-input"
          />
          <el-input
            v-model="item.output"
            type="textarea"
            :rows="3"
            placeholder="标准输出"
            class="case-input"
          />
          <div class="case-actions">
            <el-switch v-model="item.enabled" :active-value="1" :inactive-value="0" />
            <el-button link type="danger" @click="removeCase(index)">删除</el-button>
          </div>
        </div>
        <div v-if="!caseList.length" class="empty muted">暂无测试用例</div>
      </div>
      <template #footer>
        <el-button @click="caseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="caseSaving" @click="submitCases">保存用例</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 16px 0;
  margin-bottom: 12px;
}

.filter :deep(.el-form-item) {
  margin-bottom: 16px;
}

.table-panel {
  padding-top: 4px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
}

.unit {
  margin-left: 8px;
  font-size: 12px;
}

.case-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 13px;
}

.case-item {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) minmax(0, 1fr) 96px;
  gap: 10px;
  align-items: start;
  padding: 10px 0;
  border-top: 1px solid var(--ssoj-border);
}

.case-index {
  font-size: 12px;
  padding-top: 8px;
}

.case-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  padding-top: 6px;
}

.empty {
  padding: 12px 0;
  font-size: 13px;
}
</style>
