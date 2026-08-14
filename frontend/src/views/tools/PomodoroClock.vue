<template>
  <div class="pomodoro-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <div class="eyebrow">FOCUS STUDIO</div>
        <h2 class="title">番茄钟</h2>
        <p class="desc">
          轻游戏化的本地专注工具。支持专注、短休、长休切换，自动续算恢复，并用轻任务清单记录今天要完成的事。
        </p>

        <div class="hero-tags">
          <el-tag effect="dark" type="primary">本地保存</el-tag>
          <el-tag effect="plain">自动续算</el-tag>
          <el-tag effect="plain">轻任务清单</el-tag>
          <el-tag effect="plain">今日统计</el-tag>
        </div>
      </div>

      <div class="hero-stats">
        <div class="hero-card">
          <span class="hero-card__label">今日番茄</span>
          <strong class="hero-card__value">{{ todayStats.completedFocusCount }}</strong>
          <span class="hero-card__hint">累计专注次数</span>
        </div>
        <div class="hero-card">
          <span class="hero-card__label">今日专注</span>
          <strong class="hero-card__value">{{ todayStats.totalFocusMinutes }} 分</strong>
          <span class="hero-card__hint">累计专注时长</span>
        </div>
        <div class="hero-card hero-card--accent">
          <span class="hero-card__label">当前状态</span>
          <strong class="hero-card__value">{{ modeMeta.title }}</strong>
          <span class="hero-card__hint">{{ statusText }}</span>
        </div>
      </div>
    </section>

    <section class="workspace-grid">
      <div class="focus-column">
        <section class="timer-panel" :class="`timer-panel--${runtime.mode}`">
          <div class="timer-panel__top">
            <div>
              <div class="timer-kicker">{{ modeMeta.kicker }}</div>
              <h3 class="timer-title">{{ modeMeta.title }}</h3>
              <p class="timer-note">{{ modeMeta.description }}</p>
            </div>
            <div class="timer-chip">{{ cycleLabel }}</div>
          </div>

          <div class="timer-ring" :style="ringStyle">
            <div class="timer-ring__outer">
              <div class="timer-ring__inner">
                <div class="timer-number">{{ formattedRemaining }}</div>
                <div class="timer-sub">{{ remainingText }}</div>
              </div>
            </div>
          </div>

          <div class="timer-actions">
            <el-button
              type="primary"
              :icon="runtime.status === 'running' ? VideoPause : VideoPlay"
              @click="toggleTimer"
            >
              {{ runtime.status === 'running' ? '暂停' : runtime.status === 'paused' ? '继续' : '开始' }}
            </el-button>
            <el-button plain :icon="RefreshRight" @click="resetCurrentPhase">重置</el-button>
            <el-button plain :icon="ArrowRight" @click="skipPhase">跳过</el-button>
          </div>

          <transition name="fade-up">
            <div v-if="flashMessage" class="flash-badge">{{ flashMessage }}</div>
          </transition>
        </section>

        <section class="settings-panel">
          <div class="panel-head">
            <div>
              <div class="panel-eyebrow">配置</div>
              <h3>计时设置</h3>
            </div>
            <el-tag type="info" effect="plain">自动保存</el-tag>
          </div>

          <div class="settings-grid">
            <label class="setting-item">
              <span class="setting-item__label">专注时长</span>
              <el-input-number
                v-model="settings.focusMinutes"
                :min="1"
                :max="120"
                controls-position="right"
                @change="applySettingsUpdate"
              />
            </label>

            <label class="setting-item">
              <span class="setting-item__label">短休时长</span>
              <el-input-number
                v-model="settings.shortBreakMinutes"
                :min="1"
                :max="60"
                controls-position="right"
                @change="applySettingsUpdate"
              />
            </label>

            <label class="setting-item">
              <span class="setting-item__label">长休时长</span>
              <el-input-number
                v-model="settings.longBreakMinutes"
                :min="1"
                :max="90"
                controls-position="right"
                @change="applySettingsUpdate"
              />
            </label>

            <label class="setting-item">
              <span class="setting-item__label">几轮后长休</span>
              <el-input-number
                v-model="settings.cyclesBeforeLongBreak"
                :min="2"
                :max="8"
                controls-position="right"
                @change="applySettingsUpdate"
              />
            </label>
          </div>

          <div class="preset-grid">
            <button
              v-for="preset in presets"
              :key="preset.label"
              class="preset-card"
              type="button"
              @click="applyPreset(preset)"
            >
              <span class="preset-card__title">{{ preset.label }}</span>
              <span class="preset-card__meta">{{ preset.meta }}</span>
            </button>
          </div>
        </section>
      </div>

      <div class="side-column">
        <section class="task-panel">
          <div class="panel-head">
            <div>
              <div class="panel-eyebrow">任务</div>
              <h3>轻任务清单</h3>
            </div>
            <el-tag effect="plain">{{ tasks.length }} 条</el-tag>
          </div>

          <div class="task-editor">
            <el-input
              v-model="taskDraft.title"
              placeholder="输入一个今天要推进的任务"
              clearable
              @keyup.enter="addTask"
            />
            <el-input
              v-model="taskDraft.note"
              type="textarea"
              resize="none"
              :rows="2"
              placeholder="可选备注，比如要写的章节或要完成的步骤"
            />
            <el-button type="primary" :disabled="!taskDraft.title.trim()" @click="addTask">添加任务</el-button>
          </div>

          <div v-if="tasks.length" class="task-list">
            <article
              v-for="task in tasks"
              :key="task.id"
              class="task-card"
              :class="{ 'is-active': task.id === selectedTaskId, 'is-done': task.completed }"
            >
              <div class="task-card__main">
                <el-checkbox v-model="task.completed" @change="persistRuntime">
                  <span class="task-card__title">{{ task.title }}</span>
                </el-checkbox>
                <p v-if="task.note" class="task-card__note">{{ task.note }}</p>
              </div>

              <div class="task-card__actions">
                <el-button size="small" plain @click="selectTask(task.id)">
                  {{ task.id === selectedTaskId ? '当前任务' : '设为当前' }}
                </el-button>
                <el-button size="small" plain type="danger" @click="removeTask(task.id)">删除</el-button>
              </div>
            </article>
          </div>

          <el-empty
            v-else
            description="还没有任务，先写下今天最值得推进的一件事。"
            :image-size="96"
          />
        </section>

        <section class="stats-panel">
          <div class="panel-head">
            <div>
              <div class="panel-eyebrow">统计</div>
              <h3>今日成果</h3>
            </div>
          </div>

          <div class="stats-grid">
            <article class="stat-card">
              <span class="stat-card__label">今日完成</span>
              <strong class="stat-card__value">{{ todayStats.completedFocusCount }}</strong>
              <span class="stat-card__desc">个番茄</span>
            </article>
            <article class="stat-card">
              <span class="stat-card__label">累计专注</span>
              <strong class="stat-card__value">{{ todayStats.totalFocusMinutes }}</strong>
              <span class="stat-card__desc">分钟</span>
            </article>
            <article class="stat-card">
              <span class="stat-card__label">当前轮次</span>
              <strong class="stat-card__value">{{ currentCycleNumber }}</strong>
              <span class="stat-card__desc">{{ cycleLabel }}</span>
            </article>
            <article class="stat-card">
              <span class="stat-card__label">当前任务</span>
              <strong class="stat-card__value stat-card__value--task">{{ selectedTaskTitle }}</strong>
              <span class="stat-card__desc">当前专注目标</span>
            </article>
          </div>

          <div class="growth-panel">
            <div class="growth-panel__head">
              <span>成长进度</span>
              <span>{{ growthProgressLabel }}</span>
            </div>
            <el-progress
              :percentage="growthProgressPercent"
              :stroke-width="10"
              :color="progressBarColor"
            />
            <p class="growth-panel__hint">{{ growthHint }}</p>
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, RefreshRight, VideoPause, VideoPlay } from '@element-plus/icons-vue'

const SETTINGS_KEY = 'pomodoro-settings'
const RUNTIME_KEY = 'pomodoro-runtime-state'
const TICK_INTERVAL = 1000

const DEFAULT_SETTINGS = Object.freeze({
  focusMinutes: 25,
  shortBreakMinutes: 5,
  longBreakMinutes: 15,
  cyclesBeforeLongBreak: 4
})

const DEFAULT_MODE = 'focus'

const presets = [
  {
    label: '标准 25/5',
    meta: '经典节奏',
    focusMinutes: 25,
    shortBreakMinutes: 5,
    longBreakMinutes: 15,
    cyclesBeforeLongBreak: 4
  },
  {
    label: '深度 50/10',
    meta: '长时段冲刺',
    focusMinutes: 50,
    shortBreakMinutes: 10,
    longBreakMinutes: 20,
    cyclesBeforeLongBreak: 3
  },
  {
    label: '轻量 15/3',
    meta: '快速起步',
    focusMinutes: 15,
    shortBreakMinutes: 3,
    longBreakMinutes: 10,
    cyclesBeforeLongBreak: 4
  }
]

const settings = reactive(loadSettings())
const runtime = reactive(createDefaultRuntime(settings))
const tasks = ref([])
const selectedTaskId = ref('')
const taskDraft = reactive({
  title: '',
  note: ''
})
const now = ref(Date.now())
const flashMessage = ref('')
let tickHandle = null
let flashHandle = null

function getTodayKey() {
  const date = new Date()
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function safeJsonParse(raw, fallback) {
  try {
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function loadSettings() {
  const persisted = safeJsonParse(localStorage.getItem(SETTINGS_KEY), {})
  return {
    focusMinutes: normalizeInt(persisted.focusMinutes, DEFAULT_SETTINGS.focusMinutes, 1, 120),
    shortBreakMinutes: normalizeInt(persisted.shortBreakMinutes, DEFAULT_SETTINGS.shortBreakMinutes, 1, 60),
    longBreakMinutes: normalizeInt(persisted.longBreakMinutes, DEFAULT_SETTINGS.longBreakMinutes, 1, 90),
    cyclesBeforeLongBreak: normalizeInt(persisted.cyclesBeforeLongBreak, DEFAULT_SETTINGS.cyclesBeforeLongBreak, 2, 8)
  }
}

function createDefaultRuntime(currentSettings) {
  return {
    mode: DEFAULT_MODE,
    status: 'idle',
    remainingMs: getDurationMs(DEFAULT_MODE, currentSettings),
    startedAt: null,
    endsAt: null,
    cycleIndex: 0,
    todayStats: {
      date: getTodayKey(),
      completedFocusCount: 0,
      totalFocusMinutes: 0
    },
    lastActiveDate: getTodayKey()
  }
}

function normalizeInt(value, fallback, min, max) {
  const parsed = Number.parseInt(value, 10)
  if (Number.isNaN(parsed)) return fallback
  return Math.max(min, Math.min(max, parsed))
}

function normalizeTasks(items) {
  if (!Array.isArray(items)) return []
  return items
    .map(item => ({
      id: String(item?.id || createId()),
      title: String(item?.title || '').trim(),
      note: String(item?.note || '').trim(),
      completed: Boolean(item?.completed)
    }))
    .filter(item => item.title)
}

function createId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

function getDurationMs(mode, currentSettings = settings) {
  if (mode === 'shortBreak') return currentSettings.shortBreakMinutes * 60 * 1000
  if (mode === 'longBreak') return currentSettings.longBreakMinutes * 60 * 1000
  return currentSettings.focusMinutes * 60 * 1000
}

function currentRemainingMs() {
  if (runtime.status === 'running' && runtime.endsAt) {
    return Math.max(0, runtime.endsAt - now.value)
  }
  return Math.max(0, runtime.remainingMs)
}

function modeLabel(mode) {
  if (mode === 'shortBreak') return '短休'
  if (mode === 'longBreak') return '长休'
  return '专注'
}

function statusLabel(status) {
  if (status === 'running') return '计时进行中'
  if (status === 'paused') return '已暂停，可继续'
  return '等待开始'
}

function nextModeAfterFocus(nextCycleIndex) {
  return nextCycleIndex % settings.cyclesBeforeLongBreak === 0 ? 'longBreak' : 'shortBreak'
}

function persistSettings() {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify({
    focusMinutes: settings.focusMinutes,
    shortBreakMinutes: settings.shortBreakMinutes,
    longBreakMinutes: settings.longBreakMinutes,
    cyclesBeforeLongBreak: settings.cyclesBeforeLongBreak
  }))
}

function persistRuntime() {
  localStorage.setItem(RUNTIME_KEY, JSON.stringify({
    mode: runtime.mode,
    status: runtime.status,
    remainingMs: runtime.remainingMs,
    startedAt: runtime.startedAt,
    endsAt: runtime.endsAt,
    cycleIndex: runtime.cycleIndex,
    todayStats: runtime.todayStats,
    lastActiveDate: runtime.lastActiveDate,
    tasks: tasks.value,
    selectedTaskId: selectedTaskId.value
  }))
}

function hydrateRuntime() {
  const persisted = safeJsonParse(localStorage.getItem(RUNTIME_KEY), null)
  if (!persisted) {
    persistRuntime()
    return
  }

  const fallbackRuntime = createDefaultRuntime(settings)
  runtime.mode = ['focus', 'shortBreak', 'longBreak'].includes(persisted.mode) ? persisted.mode : fallbackRuntime.mode
  runtime.status = ['idle', 'running', 'paused'].includes(persisted.status) ? persisted.status : fallbackRuntime.status
  runtime.remainingMs = Number.isFinite(persisted.remainingMs)
    ? Math.max(0, persisted.remainingMs)
    : getDurationMs(runtime.mode)
  runtime.startedAt = typeof persisted.startedAt === 'number' ? persisted.startedAt : null
  runtime.endsAt = typeof persisted.endsAt === 'number' ? persisted.endsAt : null
  runtime.cycleIndex = Number.isFinite(persisted.cycleIndex) ? Math.max(0, persisted.cycleIndex) : 0
  runtime.todayStats = normalizeTodayStats(persisted.todayStats)
  runtime.lastActiveDate = typeof persisted.lastActiveDate === 'string' ? persisted.lastActiveDate : getTodayKey()

  tasks.value = normalizeTasks(persisted.tasks)
  selectedTaskId.value = tasks.value.some(task => task.id === persisted.selectedTaskId)
    ? persisted.selectedTaskId
    : tasks.value[0]?.id || ''

  rolloverTodayStatsIfNeeded()
  recoverRuntime()
}

function normalizeTodayStats(stats) {
  const today = getTodayKey()
  if (!stats || stats.date !== today) {
    return {
      date: today,
      completedFocusCount: 0,
      totalFocusMinutes: 0
    }
  }

  return {
    date: today,
    completedFocusCount: Number.isFinite(stats.completedFocusCount) ? Math.max(0, stats.completedFocusCount) : 0,
    totalFocusMinutes: Number.isFinite(stats.totalFocusMinutes) ? Math.max(0, stats.totalFocusMinutes) : 0
  }
}

function rolloverTodayStatsIfNeeded() {
  const today = getTodayKey()
  if (runtime.todayStats.date !== today) {
    runtime.todayStats = {
      date: today,
      completedFocusCount: 0,
      totalFocusMinutes: 0
    }
  }
  runtime.lastActiveDate = today
}

function startTicker() {
  stopTicker()
  tickHandle = window.setInterval(() => {
    now.value = Date.now()
    if (runtime.status === 'running' && runtime.endsAt && now.value >= runtime.endsAt) {
      completeCurrentPhase(false)
    }
  }, TICK_INTERVAL)
}

function stopTicker() {
  if (tickHandle) {
    clearInterval(tickHandle)
    tickHandle = null
  }
}

function showFlash(message) {
  flashMessage.value = message
  if (flashHandle) clearTimeout(flashHandle)
  flashHandle = window.setTimeout(() => {
    flashMessage.value = ''
  }, 2400)
}

function startTimer() {
  rolloverTodayStatsIfNeeded()
  const remaining = currentRemainingMs() || getDurationMs(runtime.mode)
  const currentTime = Date.now()

  runtime.status = 'running'
  runtime.remainingMs = remaining
  runtime.startedAt = currentTime
  runtime.endsAt = currentTime + remaining
  now.value = currentTime
  persistRuntime()
  startTicker()
}

function pauseTimer() {
  if (runtime.status !== 'running') return

  runtime.remainingMs = currentRemainingMs()
  runtime.status = 'paused'
  runtime.startedAt = null
  runtime.endsAt = null
  stopTicker()
  persistRuntime()
}

function toggleTimer() {
  if (runtime.status === 'running') {
    pauseTimer()
    return
  }
  startTimer()
}

function setPhase(mode, status = 'idle') {
  runtime.mode = mode
  runtime.status = status
  runtime.startedAt = null
  runtime.endsAt = null
  runtime.remainingMs = getDurationMs(mode)
}

function completeCurrentPhase(fromRecovery) {
  const finishedMode = runtime.mode
  stopTicker()

  if (finishedMode === 'focus') {
    runtime.cycleIndex += 1
    runtime.todayStats.completedFocusCount += 1
    runtime.todayStats.totalFocusMinutes += settings.focusMinutes
    setPhase(nextModeAfterFocus(runtime.cycleIndex))
    showFlash(
      fromRecovery
        ? '已为你续算到下一阶段。'
        : `太棒了，今天已经完成 ${runtime.todayStats.completedFocusCount} 个番茄。`
    )
  } else {
    setPhase('focus')
    showFlash(fromRecovery ? '已为你恢复到当前阶段。' : '休息结束，准备开始下一轮专注。')
  }

  runtime.lastActiveDate = getTodayKey()
  persistRuntime()
}

function skipPhase() {
  completeCurrentPhase(true)
  ElMessage.info('已切换到下一阶段')
}

function resetCurrentPhase() {
  stopTicker()
  setPhase(runtime.mode)
  persistRuntime()
  ElMessage.success('当前阶段已重置')
}

function applySettingsUpdate() {
  settings.focusMinutes = normalizeInt(settings.focusMinutes, DEFAULT_SETTINGS.focusMinutes, 1, 120)
  settings.shortBreakMinutes = normalizeInt(settings.shortBreakMinutes, DEFAULT_SETTINGS.shortBreakMinutes, 1, 60)
  settings.longBreakMinutes = normalizeInt(settings.longBreakMinutes, DEFAULT_SETTINGS.longBreakMinutes, 1, 90)
  settings.cyclesBeforeLongBreak = normalizeInt(settings.cyclesBeforeLongBreak, DEFAULT_SETTINGS.cyclesBeforeLongBreak, 2, 8)
  persistSettings()

  if (runtime.status !== 'running') {
    runtime.remainingMs = getDurationMs(runtime.mode)
    persistRuntime()
  }
}

function applyPreset(preset) {
  settings.focusMinutes = preset.focusMinutes
  settings.shortBreakMinutes = preset.shortBreakMinutes
  settings.longBreakMinutes = preset.longBreakMinutes
  settings.cyclesBeforeLongBreak = preset.cyclesBeforeLongBreak
  applySettingsUpdate()
  resetCurrentPhase()
  ElMessage.success(`已切换为 ${preset.label}`)
}

function addTask() {
  const title = taskDraft.title.trim()
  if (!title) return

  const task = {
    id: createId(),
    title,
    note: taskDraft.note.trim(),
    completed: false
  }

  tasks.value.unshift(task)
  selectedTaskId.value = task.id
  taskDraft.title = ''
  taskDraft.note = ''
  persistRuntime()
  ElMessage.success('任务已添加')
}

function selectTask(taskId) {
  selectedTaskId.value = taskId
  persistRuntime()
}

function removeTask(taskId) {
  tasks.value = tasks.value.filter(task => task.id !== taskId)
  if (selectedTaskId.value === taskId) {
    selectedTaskId.value = tasks.value[0]?.id || ''
  }
  persistRuntime()
  ElMessage.success('任务已删除')
}

function recoverRuntime() {
  rolloverTodayStatsIfNeeded()

  if (runtime.status !== 'running' || !runtime.endsAt) {
    runtime.remainingMs = runtime.remainingMs || getDurationMs(runtime.mode)
    persistRuntime()
    return
  }

  const remaining = runtime.endsAt - Date.now()
  if (remaining > 0) {
    runtime.remainingMs = remaining
    now.value = Date.now()
    startTicker()
    persistRuntime()
    return
  }

  completeCurrentPhase(true)
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    now.value = Date.now()
    recoverRuntime()
  } else {
    persistRuntime()
  }
}

function formatDuration(ms) {
  const totalSeconds = Math.max(0, Math.ceil(ms / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

watch(
  tasks,
  () => {
    persistRuntime()
  },
  { deep: true }
)

const todayStats = computed(() => runtime.todayStats)
const modeMeta = computed(() => {
  if (runtime.mode === 'shortBreak') {
    return {
      kicker: 'RECOVERY WINDOW',
      title: '短休时间',
      description: '站起来活动一下，呼吸放慢一点，然后回到下一轮。'
    }
  }
  if (runtime.mode === 'longBreak') {
    return {
      kicker: 'DEEP RESET',
      title: '长休时间',
      description: '你完成了一组完整循环，给大脑一次更完整的恢复。'
    }
  }
  return {
    kicker: 'FOCUS SESSION',
    title: '专注时间',
    description: '把注意力留给当前任务，先推进最重要的这一段。'
  }
})
const statusText = computed(() => statusLabel(runtime.status))
const currentCycleNumber = computed(() => runtime.cycleIndex + 1)
const cycleLabel = computed(() => {
  if (runtime.mode === 'focus') return `第 ${currentCycleNumber.value} 轮`
  return `${modeLabel(runtime.mode)}阶段`
})
const selectedTask = computed(() => tasks.value.find(task => task.id === selectedTaskId.value) || null)
const selectedTaskTitle = computed(() => selectedTask.value?.title || '未选择任务')
const formattedRemaining = computed(() => formatDuration(currentRemainingMs()))
const remainingText = computed(() => {
  if (runtime.status === 'running') return '倒计时进行中'
  if (runtime.status === 'paused') return '已暂停，剩余时间已保留'
  return '等待开始'
})
const progressRatio = computed(() => {
  const duration = getDurationMs(runtime.mode)
  if (!duration) return 0
  return Math.max(0, Math.min(1, 1 - currentRemainingMs() / duration))
})
const progressBarColor = computed(() => {
  if (runtime.mode === 'shortBreak') return '#0f9f6e'
  if (runtime.mode === 'longBreak') return '#d97706'
  return '#2f5bea'
})
const ringStyle = computed(() => ({
  '--progress': `${progressRatio.value * 360}deg`,
  '--ring-color': progressBarColor.value
}))
const growthProgressPercent = computed(() => {
  return Math.min(100, Math.round((todayStats.value.completedFocusCount % settings.cyclesBeforeLongBreak) / settings.cyclesBeforeLongBreak * 100))
})
const growthProgressLabel = computed(() => {
  return `${todayStats.value.completedFocusCount % settings.cyclesBeforeLongBreak}/${settings.cyclesBeforeLongBreak}`
})
const growthHint = computed(() => {
  if (todayStats.value.completedFocusCount === 0) {
    return '今天还没有开始专注，先按下开始。'
  }
  if (todayStats.value.completedFocusCount < settings.cyclesBeforeLongBreak) {
    return '再完成几轮，就能进入一次更完整的长休息。'
  }
  return '你已经完成一个完整循环，可以继续下一组。'
})

onMounted(() => {
  hydrateRuntime()
  now.value = Date.now()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  stopTicker()
  if (flashHandle) clearTimeout(flashHandle)
  persistRuntime()
})
</script>

<style lang="scss" scoped>
.pomodoro-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
  min-height: 100%;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  padding: 28px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(148, 163, 184, 0.16);
  background:
    radial-gradient(circle at 18% 0%, rgba(47, 91, 234, 0.18), transparent 22%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(243, 247, 255, 0.96));
  box-shadow: 0 22px 52px rgba(15, 23, 42, 0.08);
}

.eyebrow,
.panel-eyebrow,
.timer-kicker {
  color: #64748b;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.title {
  margin: 10px 0 0;
  color: #0f172a;
  font-size: clamp(2rem, 3.6vw, 3.2rem);
  font-weight: 700;
  font-family: var(--font-display);
  letter-spacing: -0.05em;
}

.desc {
  max-width: 760px;
  margin: 12px 0 0;
  color: #475569;
  line-height: 1.8;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 22px;
}

.hero-card {
  padding: 18px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.05);
}

.hero-card--accent {
  background: linear-gradient(135deg, rgba(47, 91, 234, 0.08), rgba(15, 159, 110, 0.08));
}

.hero-card__label,
.stat-card__label {
  display: block;
  color: #64748b;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-card__value {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  font-size: 1.7rem;
  font-weight: 900;
  font-family: var(--font-mono);
  line-height: 1;
}

.hero-card__hint,
.growth-panel__hint {
  display: block;
  margin-top: 8px;
  color: #64748b;
  font-size: 0.84rem;
  line-height: 1.5;
}

.workspace-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(0, 0.92fr);
  gap: 20px;
  min-height: 0;
}

.focus-column,
.side-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.timer-panel,
.settings-panel,
.task-panel,
.stats-panel {
  padding: 22px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.06);
}

.timer-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
}

.timer-panel--focus {
  background:
    radial-gradient(circle at top, rgba(47, 91, 234, 0.08), transparent 32%),
    rgba(255, 255, 255, 0.95);
}

.timer-panel--shortBreak {
  background:
    radial-gradient(circle at top, rgba(15, 159, 110, 0.08), transparent 32%),
    rgba(255, 255, 255, 0.95);
}

.timer-panel--longBreak {
  background:
    radial-gradient(circle at top, rgba(217, 119, 6, 0.08), transparent 32%),
    rgba(255, 255, 255, 0.95);
}

.timer-panel__top,
.panel-head,
.growth-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.timer-title,
.panel-head h3 {
  margin: 8px 0 0;
  color: #0f172a;
  font-size: 1.35rem;
  font-weight: 800;
}

.timer-note {
  margin: 10px 0 0;
  color: #64748b;
  line-height: 1.65;
}

.timer-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(47, 91, 234, 0.08);
  color: #2f5bea;
  font-size: 0.84rem;
  font-weight: 700;
  white-space: nowrap;
}

.timer-ring {
  --progress: 0deg;
  --ring-color: #2f5bea;
  width: min(100%, 360px);
  aspect-ratio: 1 / 1;
}

.timer-ring__outer {
  width: 100%;
  height: 100%;
  padding: 16px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0) 58%, rgba(255, 255, 255, 0.2) 58%),
    conic-gradient(var(--ring-color) 0deg, var(--ring-color) var(--progress), rgba(148, 163, 184, 0.12) var(--progress), rgba(148, 163, 184, 0.12) 360deg);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.timer-ring__inner {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.97);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.16);
}

.timer-number {
  color: #0f172a;
  font-family: var(--font-mono);
  font-size: clamp(2.8rem, 8vw, 4.8rem);
  font-weight: 900;
  letter-spacing: -0.06em;
  line-height: 1;
}

.timer-sub {
  color: #64748b;
  font-size: 0.88rem;
}

.timer-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.flash-badge {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(15, 159, 110, 0.1);
  color: #0f9f6e;
  font-weight: 700;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-item__label {
  color: #334155;
  font-size: 0.88rem;
  font-weight: 700;
}

.preset-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.preset-card {
  padding: 14px 16px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, #fff, #f8fbff);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.preset-card:hover {
  transform: translateY(-2px);
  border-color: rgba(47, 91, 234, 0.24);
  box-shadow: 0 14px 26px rgba(15, 23, 42, 0.06);
}

.preset-card__title {
  display: block;
  color: #0f172a;
  font-weight: 800;
}

.preset-card__meta {
  display: block;
  margin-top: 6px;
  color: #64748b;
  font-size: 0.82rem;
  line-height: 1.45;
}

.task-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.task-card {
  padding: 14px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: #f8fafc;
}

.task-card.is-active {
  border-color: rgba(47, 91, 234, 0.24);
  background: rgba(47, 91, 234, 0.05);
}

.task-card.is-done {
  opacity: 0.82;
}

.task-card__title {
  color: #0f172a;
  font-weight: 700;
}

.task-card__note {
  margin: 8px 0 0 28px;
  color: #64748b;
  font-size: 0.84rem;
  line-height: 1.55;
}

.task-card__actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  padding-left: 28px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.stat-card {
  padding: 16px;
  border-radius: var(--radius-md);
  background: #f8fafc;
}

.stat-card__value {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  font-size: 1.6rem;
  font-weight: 900;
  font-family: var(--font-mono);
  line-height: 1;
}

.stat-card__value--task {
  font-size: 1rem;
  line-height: 1.55;
  word-break: break-word;
}

.stat-card__desc {
  display: block;
  margin-top: 6px;
  color: #64748b;
  font-size: 0.82rem;
}

.growth-panel {
  margin-top: 18px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, #f8fafc, #fff);
}

.growth-panel__head {
  margin-bottom: 10px;
  color: #334155;
  font-size: 0.9rem;
  font-weight: 700;
}

.fade-up-enter-active,
.fade-up-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 1180px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-panel,
  .timer-panel,
  .settings-panel,
  .task-panel,
  .stats-panel {
    padding: 18px;
  }

  .settings-grid,
  .preset-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .timer-panel__top,
  .panel-head,
  .growth-panel__head,
  .task-card__actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .timer-actions {
    width: 100%;
    flex-direction: column;
  }

  .timer-actions :deep(.el-button) {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
</style>
