<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  deleteAnnouncement,
  getAnnouncementList,
  getSystemConfig,
  saveAnnouncement,
  saveSystemConfig,
} from '@/api'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const announcements = ref([])
const configs = ref([])
const savingConfig = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, title: '', content: '' })

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    announcements.value = await getAnnouncementList()
    configs.value = await getSystemConfig()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, title: '', content: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, title: row.title, content: row.content })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await saveAnnouncement({ ...form })
      ElMessage.success(form.id ? '公告已更新' : '公告已发布')
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除公告《${row.title}》？`, '删除公告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteAnnouncement(row.id)
    ElMessage.success('公告已删除')
    load()
  } catch {
    // 取消操作时不做处理
  }
}

async function submitConfigs() {
  savingConfig.value = true
  try {
    await saveSystemConfig(configs.value)
    ElMessage.success('配置已保存')
  } finally {
    savingConfig.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="page-head">
      <h1 class="page-title">公告与配置</h1>
      <span class="muted">配置修改后刷新页面即可生效</span>
    </div>

    <section class="panel block">
      <header class="block-head">
        <h2>公告管理</h2>
        <el-button type="primary" size="small" :icon="Plus" @click="openCreate">
          发布公告
        </el-button>
      </header>
      <el-table :data="announcements" row-key="id">
        <el-table-column prop="title" label="公告标题" min-width="260" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            <span class="mono muted">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="panel block">
      <header class="block-head">
        <h2>系统配置</h2>
        <el-button type="primary" size="small" :loading="savingConfig" @click="submitConfigs">
          保存配置
        </el-button>
      </header>
      <div v-for="item in configs" :key="item.configKey" class="config-row">
        <div class="config-label">
          <span class="mono">{{ item.configKey }}</span>
          <span class="muted config-remark">{{ item.remark }}</span>
        </div>
        <el-input v-model="item.configValue" style="width: 220px" />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="公告正文" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.block {
  padding: 14px 16px 16px;
  margin-bottom: 16px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.block-head h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.config-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--ssoj-border);
}

.config-row:last-child {
  border-bottom: none;
}

.config-label {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.config-label .mono {
  font-size: 13px;
}

.config-remark {
  font-size: 12px;
}
</style>
