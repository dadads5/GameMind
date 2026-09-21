<script setup lang="ts">
import { ref } from 'vue'
import { askAI } from '../api/ai'
import { notifyWarning } from '../utils/notify'

const props = defineProps<{
  /** 正文内容（v-model:content 双向绑定） */
  content: string
}>()
const emit = defineEmits<{ 'update:content': [value: string] }>()

const modes = [
  { key: 'smart', label: '智能润色' },
  { key: 'concise', label: '更简洁' },
  { key: 'professional', label: '更专业' },
  { key: 'vivid', label: '更生动' },
  { key: 'fix', label: '纠错' },
]
const mode = ref('smart')
const open = ref(false)
const loading = ref(false)
const polished = ref('')
const raw = ref('')

const promptMap: Record<string, string> = {
  smart: '请智能润色下面的帖子正文：修正错别字和语病，让表达更流畅生动，保留原意与风格。直接返回润色后的全文，不要任何解释或前缀。',
  concise: '请把下面的帖子正文改写得更简洁明了，去掉冗余、保留关键信息与原意。直接返回改写后的全文，不要解释。',
  professional: '请把下面的帖子正文改写得更专业严谨，用词规范、逻辑清晰，保留原意。直接返回改写后的全文，不要解释。',
  vivid: '请把下面的帖子正文改写得更生动有趣、有感染力，保留原意。直接返回改写后的全文，不要解释。',
  fix: '请检查并修正下面帖子正文中的错别字、语病和标点问题，不要大幅改写、保留原意。直接返回修正后的全文，不要解释。',
}

function openPolish() {
  if (!props.content.trim()) {
    notifyWarning('请先输入正文内容')
    return
  }
  raw.value = props.content
  polished.value = ''
  open.value = true
  runPolish()
}

async function runPolish() {
  if (!raw.value.trim()) return
  loading.value = true
  polished.value = ''
  try {
    // 润写是一次性辅助，不写入会话，避免污染会话列表
    const result = await askAI(`${promptMap[mode.value]}\n\n正文：\n${raw.value}`, [], {
      persist: false,
    })
    polished.value = (result ?? '').trim()
    if (!polished.value) notifyWarning('AI 未返回内容，请重试')
  } catch (e) {
    notifyWarning('AI 润写失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function applyPolish() {
  if (!polished.value) return
  emit('update:content', polished.value)
  open.value = false
}
</script>

<template>
  <div>
    <!-- 正文右下方的润写按钮 -->
    <div class="flex justify-end mt-2">
      <button
        type="button"
        :disabled="!content.trim()"
        @click="openPolish"
        class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full text-white text-sm disabled:opacity-40 disabled:cursor-not-allowed bg-gradient-to-r from-indigo-500 to-purple-600"
      >
        <span>✨</span>
        <span v-if="!loading">AI 润写</span>
        <span v-else>润写中…</span>
      </button>
    </div>

    <!-- 润色结果弹窗 -->
    <Teleport to="body">
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
        @click.self="open = false"
      >
        <div class="w-full max-w-2xl max-h-[85vh] overflow-y-auto bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
          <h3 class="text-lg font-bold mb-4 flex items-center gap-2 text-gray-800 dark:text-gray-100">
            <span>✨</span> AI 润写
          </h3>

          <div class="flex flex-wrap gap-2 mb-4">
            <button
              v-for="m in modes"
              :key="m.key"
              type="button"
              @click="mode = m.key; runPolish()"
              :class="mode === m.key ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
              class="px-3 py-1 rounded-full text-sm transition"
            >
              {{ m.label }}
            </button>
          </div>

          <div class="grid md:grid-cols-2 gap-4">
            <div>
              <p class="text-xs text-gray-500 mb-1">原文</p>
              <div class="h-48 overflow-y-auto whitespace-pre-wrap rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 p-3 text-sm text-gray-600 dark:text-gray-300">{{ raw }}</div>
            </div>
            <div>
              <p class="text-xs text-gray-500 mb-1">润色后（可手动微调）</p>
              <textarea
                v-model="polished"
                rows="8"
                class="w-full h-48 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 p-3 text-sm focus:outline-none focus:border-indigo-500 resize-none"
              ></textarea>
            </div>
          </div>

          <p v-if="loading" class="text-sm text-gray-500 mt-3 animate-pulse">AI 正在润写，请稍候…</p>

          <div class="flex justify-end gap-2 mt-5">
            <button
              type="button"
              @click="open = false"
              class="px-4 py-2 rounded-full border border-gray-300 dark:border-gray-600 text-gray-600 dark:text-gray-300"
            >取消</button>
            <button
              type="button"
              :disabled="!polished || loading"
              @click="applyPolish"
              class="px-5 py-2 rounded-full text-white disabled:opacity-40 bg-gradient-to-r from-indigo-500 to-purple-600"
            >替换原文</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
