<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createUser, deleteUser, getUserList, resetUserPassword, updateUserStatus } from '@/api'
import { formatDateTime } from '@/utils/format'
import { useLoginState } from '@/store/user'

const loginState = useLoginState()
const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', nickname: '', email: '', password: '', role: 0 })

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 16, message: '用户名长度为 4 到 16 位', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 位', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

/** 当前登录账号不允许停用自己，界面上直接用文字代替开关 */
function isSelf(row) {
  return row.id === loginState.user?.id
}

async function load() {
  loading.value = true
  try {
    const data = await getUserList(query)
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

async function changeStatus(row) {
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '账号已启用' : '账号已禁用')
  } catch {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('操作失败，请稍后重试')
  }
}

async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确认将《${row.nickname}》的密码重置为初始密码 123456？`,
      '重置密码',
      { type: 'warning', confirmButtonText: '确认重置', cancelButtonText: '取消' },
    )
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch {
    // 取消操作时不做处理
  }
}

function openCreate() {
  Object.assign(form, { username: '', nickname: '', email: '', password: '', role: 0 })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await createUser({ ...form })
      ElMessage.success('账号已创建')
      dialogVisible.value = false
      load()
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      saving.value = false
    }
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除账号《${row.nickname}》（${row.username}）？删除后该账号无法登录，历史提交记录保留。`,
      '删除账号',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await deleteUser(row.id)
    ElMessage.success('账号已删除')
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
      <h1 class="page-title">用户管理</h1>
      <span class="muted">共 {{ total }} 个账号</span>
    </div>

    <div class="panel filter">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="用户名或昵称"
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
      <el-button type="primary" :icon="Plus" @click="openCreate">新建用户</el-button>
    </div>

    <div class="panel table-panel">
      <el-table v-loading="loading" :data="records" row-key="id">
        <el-table-column label="用户名" width="140">
          <template #default="{ row }">
            <span class="mono">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="row.role === 1 ? 'warning' : 'info'">
              {{ row.role === 1 ? '管理员' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交数" width="90" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.submitCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="通过数" width="90" align="right">
          <template #default="{ row }">
            <span class="mono muted">{{ row.acceptCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            <span class="mono muted">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="isSelf(row)" size="small" type="info" effect="plain">当前账号</el-tag>
            <el-switch
              v-else
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="changeStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button v-if="!isSelf(row)" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新建用户" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="4 到 16 位，用于登录" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="显示在榜单与提交记录中" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="form.password" placeholder="6 到 20 位" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role">
            <el-radio :value="0">学生</el-radio>
            <el-radio :value="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">创建</el-button>
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
</style>
