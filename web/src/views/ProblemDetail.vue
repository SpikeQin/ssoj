<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CodeEditor from '@/components/CodeEditor.vue'
import JudgeStatus from '@/components/JudgeStatus.vue'
import MarkdownView from '@/components/MarkdownView.vue'
import { getProblemDetail, getSubmissionList, submitCode } from '@/api'
import { difficultyText, statusText } from '@/utils/dict'
import { formatShortDateTime } from '@/utils/format'
import { languageOptions } from '@/utils/language'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const problem = ref(null)
const language = ref('cpp')
const code = ref('')
const submitting = ref(false)
const judgeStatus = ref(null)
const caseResults = ref([])
const judgeResult = ref(null)
const mySubmissions = ref([])
/**
 * 从竞赛页进入时携带的竞赛编号，直接访问题库时回退到题目当前所属的进行中竞赛。
 * 竞赛归属最终由后端按时间判定，这里只用于界面提示。
 */
const contestId = computed(() => route.query.contestId || problem.value?.contestId || null)

const statusView = computed(() => {
  if (!judgeStatus.value) return null
  const classMap = { pending: 'idle', judging: 'running' }
  const cls = classMap[judgeStatus.value] || judgeStatus.value
  return { cls: `status--${cls}`, text: statusText(judgeStatus.value) }
})

function difficultyType(difficulty) {
  return difficulty === 1 ? 'success' : difficulty === 2 ? 'warning' : 'danger'
}

function languageLabel(value) {
  return languageOptions.find((item) => item.value === value)?.label || value
}

function templateOf(value) {
  return languageOptions.find((item) => item.value === value)?.template || ''
}

async function loadProblem(id) {
  loading.value = true
  judgeStatus.value = null
  caseResults.value = []
  judgeResult.value = null
  try {
    problem.value = await getProblemDetail(id)
    code.value = templateOf(language.value)
    const data = await getSubmissionList({ problemId: id, size: 3 })
    mySubmissions.value = data.records
  } catch (error) {
    ElMessage.error(error.message)
    router.replace('/problems')
  } finally {
    loading.value = false
  }
}

function handleLanguageChange(value) {
  // 代码未被改动时切换到新语言的模板，已编辑则保留用户内容
  const templates = languageOptions.map((item) => item.template)
  if (!code.value.trim() || templates.includes(code.value)) {
    code.value = templateOf(value)
  }
}

async function handleSubmit() {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码再提交')
    return
  }
  submitting.value = true
  judgeStatus.value = 'pending'
  caseResults.value = []
  judgeResult.value = null

  try {
    await submitCode(
      {
        problemId: problem.value.id,
        language: language.value,
        code: code.value,
        contestId: contestId.value,
      },
      (event) => {
        if (event.type === 'status') {
          judgeStatus.value = event.status
        }
        if (event.type === 'case') {
          caseResults.value = [...caseResults.value, event.result]
        }
        if (event.type === 'done') {
          judgeStatus.value = event.result.status
          judgeResult.value = event.result
        }
      },
    )
    const data = await getSubmissionList({ problemId: problem.value.id, size: 3 })
    mySubmissions.value = data.records
  } catch (error) {
    // 提交失败（间隔限制、题目下线、队列已满、超时等）时要清掉 pending，否则页面会一直显示“Pending”
    judgeStatus.value = null
    ElMessage.error(error.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadProblem(route.params.id))
watch(
  () => route.params.id,
  (id) => {
    if (id) loadProblem(id)
  },
)
</script>

<template>
  <div v-loading="loading">
    <div class="back">
      <el-button link :icon="ArrowLeft" @click="router.push('/problems')">返回题库</el-button>
    </div>

    <template v-if="problem">
      <div class="detail-head">
        <h1 class="page-title">
          <span class="mono muted">#{{ problem.id }}</span>
          {{ problem.title }}
        </h1>
        <div class="meta">
          <span>时间限制 {{ problem.timeLimit }} ms</span>
          <span class="sep">·</span>
          <span>内存限制 {{ problem.memoryLimit }} MB</span>
          <span class="sep">·</span>
          <el-tag size="small" effect="light" :type="difficultyType(problem.difficulty)">
            {{ difficultyText[problem.difficulty] }}
          </el-tag>
          <span v-for="tag in problem.tags" :key="tag" class="tag">{{ tag }}</span>
        </div>
      </div>

      <div v-if="problem.contestTitle" class="contest-bar">
        <el-tag size="small" type="warning" effect="dark">竞赛中</el-tag>
        <span>
          本题正在竞赛《{{ problem.contestTitle }}》中进行，比赛期间提交会计入该竞赛榜单
        </span>
        <el-button link type="primary" @click="router.push(`/contests/${problem.contestId}`)">
          进入竞赛
        </el-button>
      </div>

      <div class="layout">
        <section class="panel desc">
          <MarkdownView :content="problem.description" />
        </section>

        <aside class="side">
          <section class="panel block">
            <header class="block-head">
              <h2>提交代码</h2>
              <div class="block-head-right">
                <el-tag v-if="contestId" size="small" type="warning" effect="light">竞赛提交</el-tag>
                <el-select
                  v-model="language"
                  size="small"
                  style="width: 120px"
                  @change="handleLanguageChange"
                >
                  <el-option
                    v-for="item in languageOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </div>
            </header>
            <CodeEditor v-model="code" />
            <el-button
              class="submit-btn"
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
            >
              提交评测
            </el-button>
          </section>

          <section class="panel block">
            <header class="block-head">
              <h2>判题结果</h2>
              <span v-if="judgeResult" class="mono muted">
                {{ judgeResult.passedCases }} / {{ judgeResult.totalCases }} 个用例通过
              </span>
            </header>

            <div v-if="!statusView" class="empty muted">尚未提交</div>
            <template v-else>
              <div class="result-top">
                <span class="status" :class="statusView.cls">
                  <i class="dot"></i>{{ statusView.text }}
                </span>
                <span v-if="judgeResult" class="mono muted result-metrics">
                  <template v-if="judgeResult.timeUsed !== null && judgeResult.timeUsed !== undefined">
                    耗时 {{ judgeResult.timeUsed }} ms
                  </template>
                </span>
              </div>

              <div v-if="caseResults.length" class="cases">
                <div v-for="(item, index) in caseResults" :key="index" class="case-row">
                  <span class="muted">用例 {{ index + 1 }}</span>
                  <JudgeStatus :status="item.status" />
                  <span class="mono muted case-time">
                    {{ item.timeUsed === null || item.timeUsed === undefined ? '—' : `${item.timeUsed} ms` }}
                  </span>
                </div>
              </div>

              <pre v-if="judgeResult && judgeResult.errorMessage" class="code-block">{{
                judgeResult.errorMessage
              }}</pre>
            </template>
          </section>

          <section class="panel block">
            <header class="block-head">
              <h2>我的提交</h2>
              <el-button link @click="router.push('/submissions')">全部记录</el-button>
            </header>

            <div v-if="!mySubmissions.length" class="empty muted">暂无提交记录</div>
            <div v-for="item in mySubmissions" :key="item.id" class="mine-row">
              <JudgeStatus :status="item.status" />
              <span class="mono muted">{{ languageLabel(item.language) }}</span>
              <span class="mono muted mine-time">{{ formatShortDateTime(item.submitTime) }}</span>
            </div>
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
  align-items: baseline;
  gap: 8px;
}

.contest-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding: 10px 16px;
  border: 1px solid #f0d9a8;
  border-radius: 6px;
  background: #fdf6e8;
  font-size: 13px;
  color: var(--ssoj-text-2);
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

.tag {
  padding: 1px 8px;
  border-radius: 4px;
  background: #f2f4f7;
  color: var(--ssoj-text-2);
  font-size: 12px;
  line-height: 18px;
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 460px;
  gap: 16px;
  align-items: start;
}

.desc {
  padding: 20px 24px;
  min-height: 420px;
}

.side {
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.block-head-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.submit-btn {
  width: 100%;
  margin-top: 12px;
}

.empty {
  padding: 12px 0;
  font-size: 13px;
}

.result-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--ssoj-border);
}

.result-metrics {
  font-size: 12px;
}

.cases {
  padding-top: 6px;
}

.case-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 6px 0;
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

.code-block {
  margin: 12px 0 0;
  padding: 10px 12px;
  background: #f7f8fa;
  border: 1px solid var(--ssoj-border);
  border-radius: 6px;
  color: #b0392f;
  font-family: 'Cascadia Mono', Consolas, 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.7;
  overflow: auto;
  white-space: pre-wrap;
}

.mine-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  font-size: 13px;
  border-bottom: 1px dashed var(--ssoj-border);
}

.mine-row:last-child {
  border-bottom: none;
}

.mine-time {
  flex: 0 0 auto;
  font-size: 12px;
}

@media (max-width: 1120px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
