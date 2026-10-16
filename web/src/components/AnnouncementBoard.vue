<script setup>
import { onMounted, ref } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import MarkdownView from '@/components/MarkdownView.vue'
import { getAnnouncementList } from '@/api'
import { formatDateTime } from '@/utils/format'

/** 题库页最多展示的公告条数，超出部分只提示条数 */
const MAX_ITEMS = 3

const announcements = ref([])
const total = ref(0)
const active = ref([])

onMounted(async () => {
  try {
    const list = await getAnnouncementList()
    total.value = list.length
    announcements.value = list.slice(0, MAX_ITEMS)
    active.value = announcements.value.length ? [announcements.value[0].id] : []
  } catch {
    // 公告加载失败不影响题库浏览
    announcements.value = []
    total.value = 0
  }
})
</script>

<template>
  <section v-if="announcements.length" class="panel announce">
    <header class="block-head">
      <h2>
        <el-icon><Bell /></el-icon>
        公告
      </h2>
      <span v-if="total > announcements.length" class="muted count">
        共 {{ total }} 条，展示最近 {{ announcements.length }} 条
      </span>
    </header>
    <el-collapse v-model="active">
      <el-collapse-item v-for="item in announcements" :key="item.id" :name="item.id">
        <template #title>
          <span class="title">{{ item.title }}</span>
          <span class="mono muted time">{{ formatDateTime(item.createTime) }}</span>
        </template>
        <MarkdownView :content="item.content" />
      </el-collapse-item>
    </el-collapse>
  </section>
</template>

<style scoped>
.announce {
  padding: 12px 16px 4px;
  margin-bottom: 12px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.block-head h2 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.count {
  font-size: 12px;
}

.title {
  font-size: 13px;
  font-weight: 500;
}

.time {
  margin-left: auto;
  padding-right: 8px;
  font-size: 12px;
}

.announce :deep(.el-collapse) {
  border-top: none;
}

.announce :deep(.el-collapse-item__header) {
  height: 40px;
  line-height: 40px;
  font-size: 13px;
}

/* 标题插槽默认按内容宽度收缩，铺满后时间才能靠右对齐 */
.announce :deep(.el-collapse-item__title) {
  display: flex;
  align-items: center;
  flex: 1 1 auto;
  min-width: 0;
}

.announce :deep(.el-collapse-item__content) {
  padding: 4px 0 12px;
  font-size: 13px;
  color: var(--ssoj-text-2);
}
</style>
