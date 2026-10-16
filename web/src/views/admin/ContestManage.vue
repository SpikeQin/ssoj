<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deleteContest, getContestDetail, getContestList, getProblemOptions, saveContest } from '@/api'
import { formatDateTime, formatTimeRange } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const records = ref([])
const problemOptions = ref([])

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  title: '',
  description: '',
  timeRange: [],
  problemIds: [],
  hideProblems: 1,
})

const statusText = { before: '未开始', running: '进行中', finished: '已结束' }
const statusType = { before: 'primary', running: 'success', finished: 'info' }

const rules = {
  title: [{ required: true, message: '请输入竞赛名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择起止时间', trigger: 'change' }],
  problemIds: [{ required: true, message: '请至少选择一道题目', trigger: 'change' }],
}

async function load() {
  loading.value = true
  try {
    records.value = await getContestList()
    problemOptions.value = await getProblemOptions()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, {
    id: null,
    title: '',
    description: '',
    timeRange: [],
    problemIds: [],
    hideProblems: 1,
  })
  dialogVisible.value = true
}

async function openEdit(row) {
  const detail = await getContestDetail(row.id)
  Object.assign(form, {
    id: row.id,
    title: detail.title,
    description: detail.description || '',
    timeRange: [formatDateTime(detail.startTime), formatDateTime(detail.endTime)],
    problemIds: detail.problems.map((item) => item.problemId),
    hideProblems: detail.hideProblems === 0 ? 0 : 1,
  })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await saveContest({
        id: form.id,
        title: form.title,
        description: form.description,
        startTime: form.timeRange[0],
        endTime: form.timeRange[1],
        problemIds: form.problemIds,
        hideProblems: form.hideProblems,
      })
      ElMessage.success(form.id ? '竞赛已更新' : '竞赛已创建')
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除竞赛《${row.title}》？删除后竞赛与其榜单不再展示，已有的提交记录仍会保留。`,
      '删除竞赛',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await deleteContest(row.id)
    ElMessage.success('竞赛已删除')
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
      <h1 class="page-title">竞赛管理</h1>
      <span class="muted">共 {{ records.length }} 场竞赛</span>
    </div>

    <div class="panel table-panel">
      <div class="toolbar">
        <span class="muted">竞赛按通过题数与罚时排名，题目在竞赛内使用 A、B、C 编号</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">创建竞赛</el-button>
      </div>

      <el-table v-loading="loading" :data="records" row-key="id">
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="statusType[row.status]">
              {{ statusText[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="竞赛名称" min-width="200" />
        <el-table-column label="起止时间" width="340">
          <template #default="{ row }">
            <span class="mono muted time-text">{{ formatTimeRange(row.startTime, row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题目数" width="80" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.problemCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="参赛人数" width="100" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.participants }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="router.push(`/contests/${row.id}`)">
              查看
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑竞赛' : '创建竞赛'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="竞赛名称" prop="title">
          <el-input v-model="form.title" placeholder="例如：2026 秋季程序设计热身赛" />
        </el-form-item>
        <el-form-item label="起止时间" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="竞赛题目" prop="problemIds">
          <el-select
            v-model="form.problemIds"
            multiple
            filterable
            placeholder="选择参加竞赛的题目，顺序即 A、B、C 编号顺序"
            style="width: 100%"
          >
            <el-option
              v-for="item in problemOptions"
              :key="item.id"
              :label="`${item.id} ${item.title}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="竞赛说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="填写竞赛规则、语言限制与注意事项"
          />
        </el-form-item>
        <el-form-item label="赛题隐藏">
          <div class="switch-row">
            <el-switch v-model="form.hideProblems" :active-value="1" :inactive-value="0" />
            <span class="muted">竞赛结束前赛题不出现在学生题库，只能从竞赛页面进入</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">
          {{ form.id ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.table-panel {
  padding: 4px 0 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  font-size: 13px;
}

.time-text {
  white-space: nowrap;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
</style>
