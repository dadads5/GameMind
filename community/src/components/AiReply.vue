<script setup lang="ts">
import { computed, ref } from 'vue'
import { streamAI } from '../api/ai'

const props = defineProps<{
  /** 帖子标题（可选，用于拼入提示词） */
  title?: string
  /** 帖子正文 */
  content: string
  /** 配色：light 用于浅色页面，dark 用于深色页面 */
  theme?: 'light' | 'dark'
}>()

const aiReply = ref('')
const isAiLoading = ref(false)

/** 结果区配色按主题切换（静态类名，确保 Tailwind 能扫描到） */
const boxClass = computed(() =>
  props.theme === 'dark'
    ? 'bg-blue-950/40 border border-blue-800'
    : 'bg-blue-50 border border-blue-200',
)

async function getAiReply() {
  if (!props.content?.trim() || isAiLoading.value) return

  isAiLoading.value = true
  aiReply.value = ''

  try {
    await streamAI(
      `请对以下帖子内容进行智能回复：\n\n${props.title ? props.title + '\n' : ''}${props.content}`,
      [],
      {
        onToken: (text) => {
          aiReply.value += text
        },
        onError: (message) => {
          aiReply.value = message || '获取AI回复失败，请稍后重试'
        },
      },
      // 智能回复是一次性辅助，不写入会话，避免污染会话列表
      { persist: false },
    )
  } catch {
    aiReply.value = '获取AI回复失败，请稍后重试'
  } finally {
    isAiLoading.value = false
  }
}
</script>

<template>
  <div class="mt-5">
    <button
      type="button"
      @click="getAiReply"
      :disabled="isAiLoading || !content?.trim()"
      class="inline-flex items-center px-4 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white text-sm rounded-full hover:from-blue-600 hover:to-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed shadow"
    >
      <img src="/AI.jpg" alt="AI" class="w-5 h-5 rounded-full mr-2" />
      <span>{{ isAiLoading ? '思考中...' : '✨ AI 智能回复' }}</span>
    </button>

    <div v-if="aiReply" class="mt-4 p-5 rounded-xl" :class="boxClass">
      <div class="flex items-center mb-3">
        <img src="/AI.jpg" alt="AI" class="w-7 h-7 rounded-full mr-2" />
        <h3 class="text-base font-bold text-blue-600 dark:text-blue-300">AI 智能回复</h3>
      </div>
      <div class="text-gray-700 dark:text-gray-200 text-sm leading-relaxed whitespace-pre-wrap">{{ aiReply }}<span
        v-if="isAiLoading"
        class="inline-block w-2 h-4 ml-0.5 align-middle bg-blue-500 animate-pulse"
      ></span></div>
    </div>
  </div>
</template>
