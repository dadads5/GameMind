<script setup lang="ts"></script>

<template>
  <div class="card space-y-8">
    <h1 class="text-3xl font-bold text-primary text-center mb-8">关于火影忍者论坛</h1>
    
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row items-center space-y-6 md:space-y-0 md:space-x-8">
        <div class="w-32 h-32 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
          <span class="text-primary text-4xl font-bold">火</span>
        </div>
        <div class="flex-1">
          <p class="text-gray-800 dark:text-gray-200 leading-relaxed">
            火影忍者论坛是一个专门为火影忍者粉丝打造的交流平台，这里汇聚了来自全国各地的火影迷，大家可以在这里讨论剧情、分享资源、交流心得。
          </p>
          <p class="text-gray-800 dark:text-gray-200 leading-relaxed mt-4">
            论坛成立于2024年，致力于为火影迷提供一个干净、友好的交流环境。我们希望通过这个平台，让更多的人了解和喜爱火影忍者，传承火影的精神。
          </p>
        </div>
      </div>
      
      <div class="divider"></div>
      
      <div>
        <h2 class="section-title">论坛特色</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
          <div class="bg-gray-50 dark:bg-gray-800/50 p-4 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700/50 transition-colors duration-300">
            <div class="text-2xl mb-3">📚</div>
            <h3 class="text-lg font-bold mb-2">丰富的板块分类</h3>
            <p class="text-gray-600 dark:text-gray-400">涵盖火影忍者的各个方面，满足不同粉丝的需求</p>
          </div>
          <div class="bg-gray-50 dark:bg-gray-800/50 p-4 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700/50 transition-colors duration-300">
            <div class="text-2xl mb-3">👥</div>
            <h3 class="text-lg font-bold mb-2">活跃的用户社区</h3>
            <p class="text-gray-600 dark:text-gray-400">与其他火影迷交流互动，分享你的观点和想法</p>
          </div>
          <div class="bg-gray-50 dark:bg-gray-800/50 p-4 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700/50 transition-colors duration-300">
            <div class="text-2xl mb-3">🎉</div>
            <h3 class="text-lg font-bold mb-2">定期举办活动</h3>
            <p class="text-gray-600 dark:text-gray-400">增加论坛的趣味性，让你在交流中获得更多乐趣</p>
          </div>
          <div class="bg-gray-50 dark:bg-gray-800/50 p-4 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700/50 transition-colors duration-300">
            <div class="text-2xl mb-3">🌟</div>
            <h3 class="text-lg font-bold mb-2">高质量的内容分享</h3>
            <p class="text-gray-600 dark:text-gray-400">包括同人创作、资源分享等，满足你的各种需求</p>
          </div>
        </div>
      </div>
      
      <div class="divider"></div>
      
      <div>
        <h2 class="section-title">联系我们</h2>
        <div class="bg-gray-50 dark:bg-gray-800/50 p-6 rounded-lg mt-4">
          <p class="text-gray-800 dark:text-gray-200 mb-4">
            如果您有任何问题或建议，欢迎联系我们的管理员团队。
          </p>
          <div class="flex items-center space-x-4">
            <div class="flex items-center">
              <span class="text-xl mr-2">📧</span>
              <span>admin@narutoforum.com</span>
            </div>
            <div class="flex items-center">
              <span class="text-xl mr-2">📱</span>
              <span>123-4567-8910</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>