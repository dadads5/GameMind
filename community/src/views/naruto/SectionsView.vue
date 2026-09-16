<script setup lang="ts">
import { listBoards, type BoardItem } from '../../api/board'
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'

const router = useRouter()
const boardList = ref<BoardItem[]>([])

onMounted(async () => {
  try {
    const list = await listBoards(2)
    if (list.length) boardList.value = list
  } catch (e) {
    console.error('拉取板块列表失败:', e)
  }
})

const goToSection = (id: number) => {
  router.push(`/forum/section/${id}`)
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <!-- 页面标题 -->
    <div class="naruto-card text-center">
      <div class="flex items-center justify-center mb-4">
        <div class="w-4 h-8 bg-gradient-to-b from-[#228b22] to-[#32cd32] rounded-full mr-4"></div>
        <h1 class="text-3xl font-bold text-[#ff4d00] tracking-wider">🏯 论坛板块</h1>
        <div class="w-4 h-8 bg-gradient-to-b from-[#ff4d00] to-[#ff6b35] rounded-full ml-4"></div>
      </div>
      <p class="text-gray-600 dark:text-gray-400">选择你感兴趣的板块，加入忍者们的讨论</p>
    </div>

    <!-- 板块网格 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="board in boardList" 
        :key="board.id"
        @click="goToSection(board.id)"
        class="board-card cursor-pointer group relative overflow-hidden"
      >
        <!-- 顶部渐变条 -->
        <div class="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-[#ff4d00] via-[#ff9900] to-[#228b22]"></div>
        
        <!-- 板块图标 -->
        <div class="flex items-start justify-between mb-4 mt-2">
          <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-[#ff4d00]/20 to-[#228b22]/20 flex items-center justify-center text-3xl shadow-lg group-hover:scale-110 transition-transform duration-300">
            {{ board.icon }}
          </div>
          <div class="flex flex-col items-end">
            <span class="badge badge-hot text-xs">{{ board.todayPostCount ?? 0 }} 今日</span>
          </div>
        </div>
        
        <!-- 板块名称 -->
        <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-2 group-hover:text-[#ff4d00] transition-colors duration-300">
          {{ board.name }}
        </h3>
        
        <!-- 板块描述 -->
        <p class="text-gray-600 dark:text-gray-400 mb-4 text-sm leading-relaxed">
          {{ board.description }}
        </p>
        
        <!-- 统计信息 -->
        <div class="flex items-center justify-between pt-4 border-t border-gray-200 dark:border-gray-700">
          <div class="flex items-center space-x-4 text-sm text-gray-500">
            <span class="flex items-center">
              <span class="mr-1 text-[#ff4d00]">📝</span>
              火影板块
            </span>
          </div>
          <div class="flex items-center text-[#ff4d00] group-hover:translate-x-1 transition-transform duration-300">
            <span class="text-sm font-medium">进入板块</span>
            <span class="ml-1">→</span>
          </div>
        </div>
        
        <!-- 悬浮发光效果 -->
        <div class="absolute inset-0 bg-gradient-to-br from-[#ff4d00]/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
      </div>
    </div>

    <!-- 底部提示 -->
    <div class="text-center py-6">
      <p class="text-gray-500 dark:text-gray-400">
        <span class="text-2xl mr-2">🍃</span>
        选择一个板块开始你的忍道之旅
        <span class="text-2xl ml-2">🔥</span>
      </p>
    </div>
  </div>
</template>
