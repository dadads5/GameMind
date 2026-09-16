<script setup lang="ts">
/**
 * 统一的加载 / 空 / 错误 状态展示组件
 */
withDefaults(
  defineProps<{
    status?: 'loading' | 'empty' | 'error'
    message?: string
    icon?: string
  }>(),
  {
    status: 'loading',
    message: '',
    icon: '',
  },
)

defineEmits<{
  (e: 'retry'): void
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 text-center text-gray-500 dark:text-gray-400">
    <template v-if="status === 'loading'">
      <div class="w-12 h-12 rounded-full border-4 border-indigo-500 border-t-transparent animate-spin mb-4"></div>
      <p>{{ message || '加载中...' }}</p>
    </template>

    <template v-else-if="status === 'error'">
      <div class="text-5xl mb-4">⚠️</div>
      <p class="mb-4">{{ message || '加载失败，请稍后重试' }}</p>
      <button
        class="px-5 py-2 bg-indigo-500 hover:bg-indigo-600 text-white rounded-lg transition-colors"
        @click="$emit('retry')"
      >
        重新加载
      </button>
    </template>

    <template v-else>
      <div class="text-5xl mb-4">{{ icon || '📭' }}</div>
      <p>{{ message || '暂无数据' }}</p>
    </template>
  </div>
</template>
