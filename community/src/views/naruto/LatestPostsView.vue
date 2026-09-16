<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { type Post } from '../../api/naruto/data'
import { usePostList } from '../../composables/usePostList'
import { useRouter } from 'vue-router'

const router = useRouter()
const { posts, isLoading, sort, totalPages, page, hasMore, fetch, changeSort, loadMore } = usePostList()
sort.value = 'latest'

onMounted(() => {
  fetch()
})

const latestPosts = computed(() => {
  return [...posts.value].sort((a, b) => {
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
})

const goToPost = (id: number) => {
  router.push(`/forum/post/${id}`)
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <!-- 页面标题 -->
    <div class="naruto-card text-center">
      <div class="flex items-center justify-center mb-4">
        <div class="w-4 h-8 bg-gradient-to-b from-[#228b22] to-[#32cd32] rounded-full mr-4"></div>
        <h1 class="text-3xl font-bold text-[#ff4d00] tracking-wider">✨ 最新帖子</h1>
        <div class="w-4 h-8 bg-gradient-to-b from-[#ffd700] to-[#ffa500] rounded-full ml-4"></div>
      </div>
      <p class="text-gray-600 dark:text-gray-400">按发布时间排序，最新发布的帖子优先展示</p>
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

    <!-- 最新帖子列表 -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div 
        v-for="post in latestPosts" 
        :key="post.id"
        @click="goToPost(post.id)"
        class="naruto-card cursor-pointer group relative overflow-hidden"
      >
        <!-- 顶部装饰条 -->
        <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#228b22] via-[#ffd700] to-[#ff4d00]"></div>
        
        <!-- 新帖标识 -->
        <div class="absolute top-3 right-3">
          <span class="px-2 py-1 bg-gradient-to-r from-[#228b22] to-[#32cd32] text-white text-xs font-bold rounded-full shadow-lg">
            NEW
          </span>
        </div>
        
        <div class="p-5">
          <!-- 帖子标题 -->
          <h3 class="font-bold text-lg text-gray-800 dark:text-white mb-3 group-hover:text-[#ff4d00] transition-colors duration-300">
            {{ post.title }}
          </h3>
          
          <!-- 帖子内容 -->
          <p class="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">{{ post.content }}</p>
          
          <!-- 作者信息 -->
          <div class="flex items-center space-x-3 mb-4">
            <AuthorLink :author-id="post.authorId" :name="post.authorName" :avatar="post.authorAvatar" />
            <span class="text-xs text-gray-400">{{ post.createdAt }}</span>
          </div>
          
          <!-- 标签和统计 -->
          <div class="flex items-center justify-between pt-4 border-t border-gray-200 dark:border-gray-700">
            <span class="px-3 py-1 bg-[#228b22]/10 text-[#228b22] text-xs font-medium rounded-full">
              {{ post.boardName }}
            </span>
            <div class="flex items-center space-x-3 text-sm text-gray-500">
              <span class="flex items-center space-x-1">
                <span class="text-[#ff4d00]">👁️</span>
                <span>{{ post.views }}</span>
              </span>
              <span class="flex items-center space-x-1">
                <span class="text-[#228b22]">💬</span>
                <span>{{ post.replies }}</span>
              </span>
            </div>
          </div>
        </div>
        
        <!-- 悬浮效果 -->
        <div class="absolute inset-0 bg-gradient-to-br from-[#228b22]/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="!isLoading && latestPosts.length > 0" class="flex justify-center mt-2">
      <button
        v-if="hasMore()"
        @click="loadMore"
        :disabled="isLoading"
        class="px-6 py-2.5 bg-[#ff4d00]/10 hover:bg-[#ff4d00]/20 text-[#ff4d00] rounded-xl font-medium transition-all duration-300"
      >
        {{ isLoading ? '加载中...' : '加载更多' }}
      </button>
      <span v-else class="text-sm text-gray-400">已经到底啦 ~</span>
    </div>

    <!-- 底部提示 -->
    <div class="text-center py-6">
      <p class="text-gray-500 dark:text-gray-400">
        <span class="text-2xl mr-2">✨</span>
        最新发布的帖子都在这里
        <span class="text-2xl ml-2">🌟</span>
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
