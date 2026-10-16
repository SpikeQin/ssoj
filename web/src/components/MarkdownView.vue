<script setup>
import { computed } from 'vue'
import DOMPurify from 'dompurify'
import { marked } from 'marked'

const props = defineProps({
  content: { type: String, default: '' },
})

/** 统一渲染 Markdown 文本：题面与公告共用同一套排版 */
const html = computed(() => (props.content ? DOMPurify.sanitize(marked.parse(props.content)) : ''))
</script>

<template>
  <div class="markdown-body" v-html="html"></div>
</template>

<style scoped>
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 22px 0 10px;
  font-size: 15px;
  font-weight: 600;
}

.markdown-body :deep(h2:first-child),
.markdown-body :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-body :deep(p) {
  margin: 10px 0;
  line-height: 1.8;
}

.markdown-body :deep(p:first-child) {
  margin-top: 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 10px 0;
  padding-left: 22px;
  line-height: 1.8;
}

.markdown-body :deep(pre) {
  margin: 10px 0;
  padding: 10px 12px;
  background: #f7f8fa;
  border: 1px solid var(--ssoj-border);
  border-radius: 6px;
  overflow: auto;
}

.markdown-body :deep(pre code) {
  font-family: 'Cascadia Mono', Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.7;
}

.markdown-body :deep(strong) {
  font-weight: 600;
}
</style>
