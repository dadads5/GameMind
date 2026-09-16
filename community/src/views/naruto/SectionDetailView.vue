<script setup lang="ts">
import { computed, onMounted, watch, ref } from 'vue'
import { type Post } from '../../api/naruto/data'
import { listBoards, type BoardItem } from '../../api/board'
import { usePostList } from '../../composables/usePostList'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const sectionId = computed(() => Number(route.params.id))
const boardList = ref<BoardItem[]>([])
const board = computed(() => boardList.value.find(b => b.id === sectionId.value))
const { posts, isLoading, sort, totalPages, page, hasMore, fetch, changeSort, loadMore } =
  usePostList({ boardId: () => sectionId.value })

onMounted(async () => {
  try {
    const list = await listBoards(2)
    if (list.length) boardList.value = list
  } catch {
    // 回退到本地 mock
  }
  fetch()
})

// 路由参数变化（切换到其他板块）时重新拉取
watch(() => route.params.id, () => {
  fetch(true)
})

const goToPost = (id: number) => {
  router.push(`/forum/post/${id}`)
}

const goToCreate = () => {
  router.push('/forum/create')
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <!-- 板块头部 -->
    <div v-if="board" class="naruto-card">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="flex items-center space-x-4">
          <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#ff4d00] to-[#228b22] flex items-center justify-center text-4xl shadow-lg">
            {{ board.icon }}
          </div>
          <div>
            <h1 class="text-3xl font-bold text-[#ff4d00]">{{ board.name }}</h1>
            <p class="text-gray-600 dark:text-gray-400 mt-1">{{ board.description }}</p>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <div class="text-center px-4 py-2 bg-[#228b22]/10 rounded-lg">
            <p class="text-2xl font-bold text-[#228b22]">{{ board?.todayPostCount ?? 0 }}</p>
            <p class="text-xs text-gray-500">今日发帖</p>
          </div>
        </div>
      </div>
      
      <button 
        @click="goToCreate" 
        class="mt-6 w-full md:w-auto btn-primary flex items-center justify-center space-x-2 py-3"
      >
        <span>✏️</span>
        <span>发表新帖</span>
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[#ff4d00]"></div>
    </div>

    <!-- 帖子列表 -->
    <div v-else class="naruto-card">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center">
          <span class="text-2xl mr-2">🔥</span>
          <h2 class="text-xl font-bold text-[#ff4d00]">板块帖子</h2>
        </div>
        <div class="flex items-center space-x-3">
          <div class="flex items-center rounded-xl overflow-hidden border border-[#ff4d00]/20 text-sm">
            <button
              @click="changeSort('latest')"
              :class="sort === 'latest' ? 'bg-[#ff4d00] text-white' : 'text-[#ff4d00] hover:bg-[#ff4d00]/10'"
              class="px-3 py-1.5 font-medium transition-colors"
            >最新</button>
            <button
              @click="changeSort('hot')"
              :class="sort === 'hot' ? 'bg-[#ff4d00] text-white' : 'text-[#ff4d00] hover:bg-[#ff4d00]/10'"
              class="px-3 py-1.5 font-medium transition-colors"
            >最热</button>
          </div>
          <span class="text-sm text-gray-500">共 {{ posts.length }} 篇帖子</span>
        </div>
      </div>
      
      <div v-if="posts.length > 0" class="space-y-4">
        <div 
          v-for="post in posts" 
          :key="post.id"
          @click="goToPost(post.id)"
          class="p-5 rounded-xl bg-gradient-to-br from-white to-[#fff5ee] hover:from-[#fff5ee] hover:to-white border border-[#ff4d00]/20 hover:border-[#ff4d00]/40 cursor-pointer transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg group"
        >
          <div class="flex items-start justify-between mb-3">
            <div class="flex items-center space-x-2">
              <span class="px-2 py-1 bg-[#ff4d00]/10 text-[#ff4d00] text-xs font-bold rounded">
                {{ board?.name }}
              </span>
              <span v-if="post.isHot" class="px-2 py-1 bg-gradient-to-r from-[#ff6347] to-[#ff4500] text-white text-xs font-bold rounded animate-pulse">
                HOT
              </span>
            </div>
            <span class="text-xs text-gray-400">{{ post.createdAt }}</span>
          </div>
          
          <h3 class="font-bold text-lg text-gray-800 dark:text-white mb-2 group-hover:text-[#ff4d00] transition-colors duration-300">
            {{ post.title }}
          </h3>
          
          <p class="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">{{ post.content }}</p>
          
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <AuthorLink :author-id="post.authorId" :name="post.authorName" :avatar="post.authorAvatar" />
            </div>
            
            <div class="flex items-center space-x-4 text-sm text-gray-500">
              <span class="flex items-center space-x-1">
                <span class="text-[#ff4d00]">👁️</span>
                <span>{{ post.views }}</span>
              </span>
              <span class="flex items-center space-x-1">
                <span class="text-[#228b22]">💬</span>
                <span>{{ post.replies }}</span>
              </span>
              <span class="flex items-center space-x-1">
                <span class="text-[#ff69b4]">❤️</span>
                <span>{{ post.likes }}</span>
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-16">
        <div class="text-6xl mb-4">📭</div>
        <p class="text-xl text-gray-500 dark:text-gray-400">暂无帖子</p>
        <p class="text-gray-400 mt-2">成为第一个发帖的忍者吧！</p>
        <button 
          @click="goToCreate" 
          class="mt-4 btn-primary"
        >
          发表第一篇帖子
        </button>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="!isLoading && posts.length > 0" class="flex justify-center mt-2">
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
