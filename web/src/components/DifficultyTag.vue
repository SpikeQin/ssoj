<script setup>
import { computed } from 'vue'
import { difficultyText } from '@/utils/dict'

const props = defineProps({
  difficulty: { type: Number, default: 1 },
})

const type = computed(() =>
  props.difficulty === 1 ? 'success' : props.difficulty === 2 ? 'warning' : 'danger',
)
</script>

<template>
  <el-tag size="small" effect="light" :type="type">{{ difficultyText[difficulty] }}</el-tag>
</template>
