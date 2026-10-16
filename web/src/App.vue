<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, SwitchButton, User } from '@element-plus/icons-vue'
import { clearLogin, useLoginState } from '@/store/user'

const route = useRoute()
const router = useRouter()
const loginState = useLoginState()

const navItems = computed(() => {
  const items = [
    { label: '题库', path: '/problems' },
    { label: '竞赛', path: '/contests' },
    { label: '榜单', path: '/rank' },
    { label: '提交记录', path: '/submissions' },
  ]
  if (loginState.user?.role === 1) {
    items.push({ label: '管理后台', path: '/admin' })
  }
  return items
})

const activePath = computed(() => {
  const path = route.path
  if (path.startsWith('/problems')) return '/problems'
  if (path.startsWith('/contests')) return '/contests'
  if (path.startsWith('/admin')) return '/admin'
  return path
})

const blankLayout = computed(() => route.meta.blank === true)

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  }
  if (command === 'logout') {
    clearLogin()
    router.push('/login')
  }
}
</script>

<template>
  <RouterView v-if="blankLayout" />

  <div v-else class="app">
    <header class="header">
      <div class="header-inner">
        <RouterLink to="/problems" class="brand">
          <span class="brand-mark">蚀</span>
          <span class="brand-name">蚀山在线判题系统</span>
        </RouterLink>

        <nav class="nav">
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ 'is-active': activePath === item.path }"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="header-right">
          <el-dropdown v-if="loginState.user" trigger="click" @command="handleCommand">
            <span class="user">
              <el-avatar :size="26" class="user-avatar">
                {{ loginState.user.nickname.slice(0, 1) }}
              </el-avatar>
              <span class="user-name">{{ loginState.user.nickname }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton" divided>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button v-else type="primary" size="small" @click="router.push('/login')">
            登录
          </el-button>
        </div>
      </div>
    </header>

    <main class="main">
      <div class="container">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
.app {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  background: var(--ssoj-panel);
  border-bottom: 1px solid var(--ssoj-border);
}

.header-inner {
  max-width: 1280px;
  height: 56px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
}

.brand-mark {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--ssoj-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-name {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1 1 auto;
  min-width: 0;
}

.nav-item {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 14px;
  color: var(--ssoj-text-2);
  white-space: nowrap;
}

.nav-item:hover {
  background: #f2f4f7;
  color: var(--ssoj-text);
}

.nav-item.is-active {
  background: #eef3fe;
  color: var(--ssoj-primary);
  font-weight: 500;
}

.header-right {
  flex: 0 0 auto;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  color: var(--ssoj-text);
}

.user-avatar {
  background: #eef3fe;
  color: var(--ssoj-primary);
  font-size: 13px;
}

.user-name {
  font-size: 14px;
}

.user-arrow {
  font-size: 12px;
  color: var(--ssoj-text-3);
}

.main {
  flex: 1 1 auto;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 24px 48px;
}
</style>
