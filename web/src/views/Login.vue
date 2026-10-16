<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api'
import { useLoginState } from '@/store/user'

const route = useRoute()
const router = useRouter()
const loginState = useLoginState()

const activeTab = ref('login')
const submitting = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', nickname: '', password: '', confirm: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 16, message: '用户名长度为 4 到 16 位', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 位', trigger: 'blur' },
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleLogin() {
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await login(loginForm)
      ElMessage.success('登录成功')
      router.push(route.query.redirect || '/problems')
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      submitting.value = false
    }
  })
}

async function handleRegister() {
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await register(registerForm)
      ElMessage.success('注册成功，请使用新账号登录')
      loginForm.username = registerForm.username
      loginForm.password = ''
      activeTab.value = 'login'
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  if (loginState.user) {
    router.replace('/problems')
  }
})
</script>

<template>
  <div class="login-page">
    <div class="login-box">
      <div class="brand">
        <span class="brand-mark">蚀</span>
        <span class="brand-name">蚀山在线判题系统</span>
      </div>

      <div class="panel card">
        <el-tabs v-model="activeTab" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              label-position="top"
              @submit.prevent="handleLogin"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-button
                class="submit-btn"
                type="primary"
                :loading="submitting"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              label-position="top"
              @submit.prevent="handleRegister"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="registerForm.username" placeholder="4 到 16 位，用于登录" />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="registerForm.nickname" placeholder="显示在榜单与提交记录中" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="6 到 20 位"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirm">
                <el-input
                  v-model="registerForm.confirm"
                  type="password"
                  placeholder="请再次输入密码"
                  show-password
                />
              </el-form-item>
              <el-button
                class="submit-btn"
                type="primary"
                :loading="submitting"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>

      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: var(--ssoj-bg);
}

.login-box {
  width: 400px;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 7px;
  background: var(--ssoj-primary);
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
}

.card {
  padding: 8px 24px 20px;
}

.submit-btn {
  width: 100%;
}

</style>
