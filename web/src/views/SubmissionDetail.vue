<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import JudgeStatus from '@/components/JudgeStatus.vue'
import { getSubmissionDetail } from '@/api'
import { statusKey } from '@/utils/dict'
import { formatDateTime } from '@/utils/format'
import { languageOptions } from '@/utils/language'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const record = ref(null)

/** 后端返回的状态码是数字，需要先转换成短名再统计通过数 */
const passedCases = computed(
  () => (record.value?.cases || []).filter((item) => statusKey(item.status) === 'ac').length,
)

function languageLabel(value) {
  return languageOptions.find((item) => item.value === value)?.label || value
}

function formatTime(value) {
  return value === null || value === undefined ? '—' : `${value} ms`
}

async function load(id) {
  loading.value = true
  try {
    record.value = await getSubmissionDetail(id)
  } catch (error) {
    ElMessage.error(error.message)
    router.replace('/submissions')
  } finally {
    loading.value = false
  }
}

onMounted(() => load(route.params.id))
</script>

<template>
  <div v-loading="loading">
    <div class="back">
      <el-button link :icon="ArrowLeft" @click="router.push('/submissions')">返回提交记录</el-button>
    </div>

    <template v-if="record">
      <div class="detail-head">
        <h1 class="page-title">
          <span class="mono muted">#{{ record.id }}</span>
          <RouterLink class="problem-link" :to="`/problems/${record.problemId}`">
            {{ record.problemTitle }}
          </RouterLink>
          <JudgeStatus :status="record.status" />
          <el-tag v-if="record.contestId" size="small" type="warning" effect="light">
            竞赛提交
          </el-tag>
        </h1>

        <div class="meta">
          <span>语言 {{ languageLabel(record.language) }}</span>
          <span class="sep">·</span>
          <span class="mono">{{ formatDateTime(record.submitTime) }}</span>
          <span class="sep">·</span>
          <span class="mono">耗时 {{ formatTime(record.timeUsed) }}</span>
          <span class="sep">·</span>
          <span class="mono">代码 {{ record.codeLength }} B</span>
        </div>
      </div>

      <div class="layout">
        <section class="panel block">
          <header class="block-head">
            <h2>提交代码</h2>
            <span class="muted code-lang mono">{{ languageLabel(record.language) }}</span>
          </header>
          <pre class="code mono">{{ record.code }}</pre>
        </section>

        <aside class="side">
          <section class="panel block">
            <header class="block-head">
              <h2>用例结果</h2>
              <span class="mono muted">
                {{ passedCases }} / {{ record.cases.length }} 个用例通过
              </span>
            </header>
            <div v-for="(item, index) in record.cases" :key="index" class="case-row">
              <span class="muted">用例 {{ index + 1 }}</span>
              <JudgeStatus :status="item.status" />
              <span class="mono muted case-time">{{ formatTime(item.timeUsed) }}</span>
            </div>
          </section>

          <section v-if="record.errorMessage" class="panel block">
            <header class="block-head"><h2>编译信息</h2></header>
            <pre class="code code--error mono">{{ record.errorMessage }}</pre>
          </section>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.back {
  margin-bottom: 4px;
}

.back :deep(.el-button) {
  padding-left: 0;
  font-size: 13px;
}

.detail-head {
  margin-bottom: 16px;
}

.detail-head .page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.problem-link {
  color: var(--ssoj-text);
}

.problem-link:hover {
  color: var(--ssoj-primary);
}

.meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--ssoj-text-2);
}

.sep {
  color: var(--ssoj-text-3);
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 16px;
  align-items: start;
}

.side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.block {
  padding: 14px 16px 16px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.block-head h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.code-lang {
  font-size: 12px;
}

.code {
  margin: 0;
  padding: 12px 14px;
  max-height: 520px;
  background: #fafbfc;
  border: 1px solid var(--ssoj-border);
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.7;
  overflow: auto;
  white-space: pre;
}

.code--error {
  color: #b0392f;
  white-space: pre-wrap;
}

.case-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  font-size: 13px;
  border-bottom: 1px dashed var(--ssoj-border);
}

.case-row:last-child {
  border-bottom: none;
}

.case-time {
  flex: 0 0 auto;
  font-size: 12px;
}

@media (max-width: 1120px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
