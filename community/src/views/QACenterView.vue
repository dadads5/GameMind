<script setup lang="ts">
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-8">
    <!-- 问答中心页面 -->
    <div class="naruto-card text-center">
      <div class="text-6xl mb-6">❓</div>
      <h1 class="text-3xl font-bold text-gray-800 dark:text-white mb-4">问答中心</h1>
      <p class="text-gray-600 dark:text-gray-400 mb-8">
        在这里提问、回答，与社区成员交流知识
      </p>
    </div>

    <!-- 问答区域 -->
    <div class="grid md:grid-cols-3 gap-6">
      <!-- 热门问题 -->
      <div class="md:col-span-2 naruto-card">
        <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-6 flex items-center">
          <span class="mr-2">🔥</span> 热门问题
        </h3>
        
        <div class="space-y-4">
          <div class="p-4 bg-gray-100 dark:bg-gray-800 rounded-xl hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer">
            <h4 class="font-medium text-gray-800 dark:text-white mb-2">如何学习Vue3？</h4>
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">浏览 1234</span>
              <span class="text-gray-500">回答 45</span>
              <span class="text-[#ff4d00]">热门</span>
            </div>
          </div>
          
          <div class="p-4 bg-gray-100 dark:bg-gray-800 rounded-xl hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer">
            <h4 class="font-medium text-gray-800 dark:text-white mb-2">Spring Boot最佳实践有哪些？</h4>
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">浏览 987</span>
              <span class="text-gray-500">回答 32</span>
              <span class="text-[#ff4d00]">热门</span>
            </div>
          </div>
          
          <div class="p-4 bg-gray-100 dark:bg-gray-800 rounded-xl hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer">
            <h4 class="font-medium text-gray-800 dark:text-white mb-2">前端性能优化技巧</h4>
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">浏览 756</span>
              <span class="text-gray-500">回答 28</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 侧边栏 -->
      <div class="space-y-6">
        <div class="naruto-card">
          <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-4 flex items-center">
            <span class="mr-2">📊</span> 问答统计
          </h3>
          <div class="grid grid-cols-2 gap-4">
            <div class="text-center p-3 bg-[#ff4d00]/10 rounded-lg">
              <p class="text-2xl font-bold text-[#ff4d00]">1234</p>
              <p class="text-xs text-gray-500">问题总数</p>
            </div>
            <div class="text-center p-3 bg-[#228b22]/10 rounded-lg">
              <p class="text-2xl font-bold text-[#228b22]">5678</p>
              <p class="text-xs text-gray-500">回答总数</p>
            </div>
            <div class="text-center p-3 bg-[#1e90ff]/10 rounded-lg">
              <p class="text-2xl font-bold text-[#1e90ff]">89</p>
              <p class="text-xs text-gray-500">活跃用户</p>
            </div>
            <div class="text-center p-3 bg-[#ff69b4]/10 rounded-lg">
              <p class="text-2xl font-bold text-[#ff69b4]">98%</p>
              <p class="text-xs text-gray-500">解决率</p>
            </div>
          </div>
        </div>
        
        <div class="naruto-card">
          <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-4 flex items-center">
            <span class="mr-2">🏷️</span> 热门标签
          </h3>
          <div class="flex flex-wrap gap-2">
            <span class="px-3 py-1 bg-[#ff4d00]/10 text-[#ff4d00] text-sm rounded-full">Vue</span>
            <span class="px-3 py-1 bg-[#228b22]/10 text-[#228b22] text-sm rounded-full">Java</span>
            <span class="px-3 py-1 bg-[#1e90ff]/10 text-[#1e90ff] text-sm rounded-full">React</span>
            <span class="px-3 py-1 bg-[#ff69b4]/10 text-[#ff69b4] text-sm rounded-full">Node.js</span>
            <span class="px-3 py-1 bg-[#ffd700]/10 text-[#ffd700] text-sm rounded-full">Python</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
