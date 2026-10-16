<script setup>
import { computed } from 'vue'
import { statusKey, statusText } from '@/utils/dict'

const props = defineProps({
  status: { type: [Number, String], default: null },
  text: { type: String, default: '' },
})

/** pending 与 judging 使用无状态配色，其余按状态短名取色 */
const CLASS_MAP = { pending: 'idle', judging: 'running' }

const empty = computed(() =>
  props.status === null || props.status === undefined || props.status === '')

const key = computed(() => (empty.value ? 'idle' : statusKey(props.status)))

const className = computed(() => `status--${CLASS_MAP[key.value] || key.value}`)

const label = computed(() => {
  if (props.text) {
    return props.text
  }
  return empty.value ? '—' : statusText(props.status)
})
</script>

<template>
  <span class="status" :class="className">
    <i class="dot"></i>{{ label }}
  </span>
</template>
