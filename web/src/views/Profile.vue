<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import JudgeStatus from '@/components/JudgeStatus.vue'
import { changePassword, getProfile, getStatistics, updateProfile } from '@/api'
import { statusText } from '@/utils/dict'
import { formatShortDateTime } from '@/utils/format'
import { useLoginState } from '@/store/user'

const loginState = useLoginState()
const profileFormRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const stats = ref(null)

const profileForm = reactive({
  nickname: loginState.user?.nickname || '',
  email: '',
})

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 位', trigger: 'blur' },
  ],
  confirm: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

const statusRows = computed(() => {
  if (!stats.value) return []
  const counts = stats.value.statusCounts
  const max = Math.max(...Object.values(counts), 1)
  return Object.keys(counts).map((status) => ({
    status,
    text: statusText(status),
    count: counts[status],
    width: Math.round((counts[status] / max) * 100),
  }))
})

async function load() {
  loading.value = true
  try {
    const [profile, statistics] = await Promise.all([getProfile(), getStatistics()])
    profileForm.nickname = profile.nickname
    profileForm.email = profile.email || ''
    stats.value = statistics
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const profile = await updateProfile({
        nickname: profileForm.nickname,
        email: profileForm.email,
      })
      if (loginState.user) {
        loginState.user.nickname = profile.nickname
        localStorage.setItem('user', JSON.stringify(loginState.user))
      }
      ElMessage.success('资料已保存')
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      saving.value = false
    }
  })
}

async function savePassword() {
  if (!passwordForm.oldPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码已修改')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirm = ''
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="page-head">
      <h1 class="page-title">个人中心</h1>
      <span class="muted">
        {{ loginState.user?.nickname }}（{{ loginState.user?.role === 1 ? '管理员' : '学生' }}）
      </span>
    </div>

    <div class="layout">
      <div class="left">
        <section class="panel block">
          <header class="block-head"><h2>资料维护</h2></header>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="88px"
          >
            <el-form-item label="用户名">
              <el-input :model-value="loginState.user?.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="显示在榜单与提交记录中" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="选填，便于账号联系" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </section>

        <section class="panel block">
          <header class="block-head"><h2>修改密码</h2></header>
          <el-form :model="passwordForm" :rules="passwordRules" label-width="88px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="6 到 20 位"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirm">
              <el-input v-model="passwordForm.confirm" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </section>
      </div>

      <div class="right">
        <section class="panel block">
          <header class="block-head"><h2>我的统计</h2></header>
          <div v-if="stats" class="stat-grid">
            <div class="stat">
              <span class="stat-value mono">{{ stats.submitCount }}</span>
              <span class="stat-label muted">提交总数</span>
            </div>
            <div class="stat">
              <span class="stat-value mono">{{ stats.acCount }}</span>
              <span class="stat-label muted">通过题数</span>
            </div>
            <div class="stat">
              <span class="stat-value mono">{{ stats.acceptRate }}%</span>
              <span class="stat-label muted">通过率</span>
            </div>
          </div>
        </section>

        <section class="panel block">
          <header class="block-head"><h2>判题结果分布</h2></header>
          <div v-if="!statusRows.length" class="empty muted">暂无提交记录</div>
          <div v-for="item in statusRows" :key="item.status" class="dist-row">
            <JudgeStatus :status="item.status" class="dist-name" />
            <div class="dist-bar">
              <span
                class="dist-fill"
                :class="`dist-fill--${item.status}`"
                :style="{ width: `${item.width}%` }"
              ></span>
            </div>
            <span class="mono muted dist-count">{{ item.count }}</span>
          </div>
        </section>

        <section class="panel block">
          <header class="block-head"><h2>最近提交</h2></header>
          <div v-if="!stats?.recent?.length" class="empty muted">暂无提交记录</div>
          <div v-for="item in stats?.recent || []" :key="item.id" class="recent-row">
            <span class="recent-main">
              <span class="recent-title">{{ item.problemTitle }}</span>
              <span class="muted recent-time">{{ formatShortDateTime(item.submitTime) }}</span>
            </span>
            <JudgeStatus :status="item.status" />
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 400px;
  gap: 16px;
  align-items: start;
}

.left,
.right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.block {
  padding: 16px 20px 18px;
}

.block-head {
  margin-bottom: 14px;
}

.block-head h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.3;
}

.stat-label {
  font-size: 12px;
}

.dist-row {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr) 32px;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.dist-name {
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dist-bar {
  height: 6px;
  border-radius: 3px;
  background: #f0f2f5;
  overflow: hidden;
}

.dist-fill {
  display: block;
  height: 100%;
  border-radius: 3px;
  background: var(--ssoj-text-3);
}

.dist-fill--ac {
  background: var(--ssoj-ac);
}

.dist-fill--wa {
  background: var(--ssoj-wa);
}

.dist-fill--tle {
  background: var(--ssoj-tle);
}

.dist-fill--re {
  background: var(--ssoj-re);
}

.dist-fill--ce {
  background: var(--ssoj-ce);
}

.dist-count {
  text-align: right;
  font-size: 12px;
}

.recent-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  font-size: 13px;
  border-bottom: 1px dashed var(--ssoj-border);
}

.recent-row:last-child {
  border-bottom: none;
}

.recent-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.recent-time {
  font-size: 12px;
}

.empty {
  padding: 8px 0;
  font-size: 13px;
}

@media (max-width: 1120px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
