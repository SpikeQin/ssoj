<script setup>
import { computed, nextTick, ref } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  height: { type: String, default: '320px' },
})

const emit = defineEmits(['update:modelValue'])

const textareaRef = ref(null)
const gutterRef = ref(null)

const lineNumbers = computed(() => {
  const count = Math.max(props.modelValue.split('\n').length, 14)
  return Array.from({ length: count }, (_, index) => index + 1).join('\n')
})

function handleInput(event) {
  emit('update:modelValue', event.target.value)
}

function syncScroll() {
  if (gutterRef.value && textareaRef.value) {
    gutterRef.value.scrollTop = textareaRef.value.scrollTop
  }
}

// 编辑区内按 Tab 输入四个空格，避免焦点跳出编辑区
function handleKeydown(event) {
  if (event.key !== 'Tab') return
  event.preventDefault()
  const element = event.target
  const start = element.selectionStart
  const end = element.selectionEnd
  const next = `${props.modelValue.slice(0, start)}    ${props.modelValue.slice(end)}`
  emit('update:modelValue', next)
  nextTick(() => {
    element.selectionStart = start + 4
    element.selectionEnd = start + 4
  })
}
</script>

<template>
  <div class="editor" :style="{ height }">
    <pre ref="gutterRef" class="gutter">{{ lineNumbers }}</pre>
    <textarea
      ref="textareaRef"
      class="area"
      :value="modelValue"
      spellcheck="false"
      autocomplete="off"
      @input="handleInput"
      @scroll="syncScroll"
      @keydown="handleKeydown"
    ></textarea>
  </div>
</template>

<style scoped>
.editor {
  display: flex;
  border: 1px solid var(--ssoj-border-strong);
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.gutter {
  margin: 0;
  padding: 10px 8px 10px 0;
  width: 46px;
  flex: 0 0 46px;
  text-align: right;
  background: #fafbfc;
  border-right: 1px solid var(--ssoj-border);
  color: #a8b0ba;
  font-family: 'Cascadia Mono', Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 20px;
  overflow: hidden;
  user-select: none;
}

.area {
  flex: 1 1 auto;
  min-width: 0;
  padding: 10px 12px;
  border: none;
  outline: none;
  resize: none;
  color: var(--ssoj-text);
  font-family: 'Cascadia Mono', Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 20px;
  tab-size: 4;
  overflow: auto;
}
</style>
