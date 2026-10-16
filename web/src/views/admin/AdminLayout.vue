<script setup>
import { useRoute } from 'vue-router'
import { DataLine, Document, Setting, Trophy, User } from '@element-plus/icons-vue'

const route = useRoute()

const menus = [
  { path: '/admin', label: '管理首页', icon: DataLine },
  { path: '/admin/problems', label: '题目管理', icon: Document },
  { path: '/admin/contests', label: '竞赛管理', icon: Trophy },
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/system', label: '公告与配置', icon: Setting },
]
</script>

<template>
  <div class="admin">
    <aside class="panel side">
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
      <div class="side-foot muted">蚀山制造 · 课程设计</div>
    </aside>

    <section class="content">
      <RouterView />
    </section>
  </div>
</template>

<style scoped>
.admin {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.side {
  padding: 6px 0 10px;
  position: sticky;
  top: 16px;
}

.menu {
  border-right: none;
}

.menu :deep(.el-menu-item) {
  height: 42px;
  line-height: 42px;
  font-size: 14px;
  margin: 2px 8px;
  border-radius: 6px;
}

.menu :deep(.el-menu-item:hover) {
  background: #f2f4f7;
}

.menu :deep(.el-menu-item.is-active) {
  background: #eef3fe;
  color: var(--ssoj-primary);
  font-weight: 500;
}

.side-foot {
  margin: 12px 16px 0;
  padding-top: 10px;
  border-top: 1px solid var(--ssoj-border);
  font-size: 12px;
}

.content {
  min-width: 0;
}

@media (max-width: 1120px) {
  .admin {
    grid-template-columns: minmax(0, 1fr);
  }

  .side {
    position: static;
  }

  .menu {
    display: flex;
    flex-wrap: wrap;
  }
}
</style>
