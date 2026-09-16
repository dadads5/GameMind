<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getHotPosts, getGlobalHotPosts, type Post } from '../../api/naruto/data'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const posts = ref<Post[]>([])
const isLoading = ref(true)
const sort = ref<'hot' | 'latest'>('hot')

const isGlobal = computed(() => route.name === 'global-hot' || route.path === '/hot')

const loadHot = async () => {
  isLoading.value = true
  try {
    posts.value = isGlobal.value ? await getGlobalHotPosts(50) : await getHotPosts(50)
  } finally {
    isLoading.value = false
  }
}

onMounted(loadHot)

const hotPosts = computed(() => {
  const list = [...posts.value]
  if (sort.value === 'hot') {
    return list.sort((a, b) => (b.views + b.likes * 10 + b.replies * 5) - (a.views + a.likes * 10 + a.replies * 5))
  }
  return list.sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
})

const goToPost = (id: number) => {
  router.push(`/forum/post/${id}`)
}

const changeSort = (s: 'hot' | 'latest') => {
  sort.value = s
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <!-- 页面标题 -->
    <div class="naruto-card text-center">
      <div class="flex items-center justify-center mb-4">
        <div class="w-4 h-8 bg-gradient-to-b from-[#ff6347] to-[#ff4500] rounded-full mr-4"></div>
        <h1 class="text-3xl font-bold text-[#ff4d00] tracking-wider">{{ isGlobal ? '🔥 全站热门' : '🔥 热门帖子' }}</h1>
        <div class="w-4 h-8 bg-gradient-to-b from-[#ff4d00] to-[#ff6b35] rounded-full ml-4"></div>
      </div>
      <p class="text-gray-600 dark:text-gray-400">根据浏览量、点赞数和回复数综合排序</p>
      <div class="flex items-center justify-center mt-4">
        <div class="flex items-center rounded-xl overflow-hidden border border-[#ff4d00]/20 text-sm">
          <button
            @click="changeSort('latest')"
            :class="sort === 'latest' ? 'bg-[#ff4d00] text-white' : 'text-[#ff4d00] hover:bg-[#ff4d00]/10'"
            class="px-3 py-2 font-medium transition-colors"
          >最新</button>
          <button
            @click="changeSort('hot')"
            :class="sort === 'hot' ? 'bg-[#ff4d00] text-white' : 'text-[#ff4d00] hover:bg-[#ff4d00]/10'"
            class="px-3 py-2 font-medium transition-colors"
          >最热</button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[#ff4d00]"></div>
    </div>

    <!-- 热门榜单 -->
    <div v-else class="space-y-4">
      <div 
        v-for="(post, index) in hotPosts" 
        :key="post.id"
        @click="goToPost(post.id)"
        class="naruto-card cursor-pointer group"
      >
        <div class="flex items-start">
          <!-- 排名徽章 -->
          <div 
            class="flex-shrink-0 w-14 h-14 rounded-xl flex items-center justify-center mr-5 shadow-lg transition-all duration-300 group-hover:scale-110"
            :class="{
              'bg-gradient-to-br from-yellow-400 to-yellow-600 text-white': index === 0,
              'bg-gradient-to-br from-gray-300 to-gray-400 text-white': index === 1,
              'bg-gradient-to-br from-amber-600 to-amber-800 text-white': index === 2,
              'bg-gradient-to-br from-[#ff4d00]/20 to-[#ff4d00]/10 text-[#ff4d00]': index > 2
            }"
          >
            <span class="text-2xl font-bold">{{ index + 1 }}</span>
          </div>
          
          <!-- 帖子内容 -->
          <div class="flex-1">
            <div class="flex items-start justify-between mb-2">
              <h3 class="font-bold text-xl text-gray-800 dark:text-white group-hover:text-[#ff4d00] transition-colors duration-300">
                {{ post.title }}
              </h3>
              <span v-if="post.isHot" class="px-2 py-1 bg-gradient-to-r from-[#ff6347] to-[#ff4500] text-white text-xs font-bold rounded animate-pulse">
                HOT
              </span>
            </div>
            
            <p class="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">{{ post.content }}</p>
            
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div class="flex items-center space-x-3">
                <AuthorLink :author-id="post.authorId" :name="post.authorName" :avatar="post.authorAvatar" />
                <span class="px-2 py-0.5 bg-[#ff4d00]/10 text-[#ff4d00] text-xs font-medium rounded">
                  {{ post.boardName }}
                </span>
                <span class="text-xs text-gray-400">{{ post.createdAt }}</span>
              </div>
              
              <div class="flex items-center space-x-4 text-sm">
                <span class="flex items-center space-x-1 text-gray-500">
                  <span class="text-[#ff4d00]">👁️</span>
                  <span>{{ post.views }}</span>
                </span>
                <span class="flex items-center space-x-1 text-gray-500">
                  <span class="text-[#228b22]">💬</span>
                  <span>{{ post.replies }}</span>
                </span>
                <span class="flex items-center space-x-1 text-gray-500">
                  <span class="text-[#ff69b4]">❤️</span>
                  <span>{{ post.likes }}</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>


    <!-- 底部提示 -->
    <div class="text-center py-6">
      <p class="text-gray-500 dark:text-gray-400">
        <span class="text-2xl mr-2">🔥</span>
        {{ isGlobal ? '以上是当前全站最热门的帖子' : '以上是当前最热门的帖子' }}
        <span class="text-2xl ml-2">🔥</span>
      </p>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
