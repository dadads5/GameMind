<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostsFromBackend, type Post } from '../../api/model3/data'
import { listBoards } from '../../api/board'

const router = useRouter()
const posts = ref<Post[]>([])
const isLoading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const sort = ref<'latest' | 'hot'>('latest')
const keyword = ref('')
const activeBoardId = ref<number | undefined>(undefined)
const boardOptions = ref<{ id: number; name: string }[]>([])

const hasMore = () => page.value < totalPages.value

async function fetchPosts(reset = true) {
  if (reset) {
    page.value = 1
    posts.value = []
  }
  isLoading.value = true
  const res = await getPostsFromBackend({
    page: page.value,
    size: 10,
    sort: sort.value,
    keyword: keyword.value.trim() || undefined,
    boardId: activeBoardId.value,
  })
  posts.value = reset ? res.posts : [...posts.value, ...res.posts]
  totalPages.value = res.totalPages
  isLoading.value = false
}

function changeSort(s: 'latest' | 'hot') {
  sort.value = s
  fetchPosts()
}
function changeBoard(id: number | undefined) {
  activeBoardId.value = id
  fetchPosts()
}
function onSearch() {
  fetchPosts()
}
function loadMore() {
  if (hasMore()) {
    page.value++
    fetchPosts(false)
  }
}
function goPost(id: number) {
  router.push(`/module3/post/${id}`)
}
function goCreate() {
  if (!localStorage.getItem('token')) {
    router.push({ name: 'login', query: { redirect: '/module3/create' } })
    return
  }
  router.push('/module3/create')
}

onMounted(async () => {
  const list = await listBoards(1)
  boardOptions.value = list
  fetchPosts()
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-indigo-950/30 to-slate-950 text-slate-100">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-slate-400 hover:text-white mb-4">← 返回</button>

      <!-- 头部 -->
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
        <div>
          <h1 class="text-3xl font-bold bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">
            原神论坛
          </h1>
          <p class="text-slate-400 text-sm mt-1">提瓦特大陆 · 七国玩家交流</p>
        </div>
        <button
          @click="goCreate"
          class="px-4 py-2 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white font-medium hover:shadow-lg hover:shadow-indigo-500/30 transition"
        >
          发表帖子
        </button>
      </div>

      <!-- 搜索 + 排序 -->
      <div class="flex flex-col sm:flex-row gap-3 mb-4">
        <input
          v-model="keyword"
          @keyup.enter="onSearch"
          placeholder="搜索帖子..."
          class="flex-1 px-4 py-2 rounded-full bg-slate-800/80 border border-slate-700 focus:outline-none focus:border-indigo-500"
        />
        <div class="flex gap-2">
          <button
            @click="changeSort('latest')"
            :class="sort === 'latest' ? 'bg-indigo-500 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-2 rounded-full text-sm transition"
          >
            最新
          </button>
          <button
            @click="changeSort('hot')"
            :class="sort === 'hot' ? 'bg-purple-500 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-2 rounded-full text-sm transition"
          >
            热门
          </button>
        </div>
      </div>

      <!-- 板块筛选 -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          @click="changeBoard(undefined)"
          :class="activeBoardId === undefined ? 'bg-indigo-500 text-white' : 'bg-slate-800 text-slate-300'"
          class="px-3 py-1.5 rounded-full text-sm transition"
        >
          全部
        </button>
        <button
          v-for="b in boardOptions"
          :key="b.id"
          @click="changeBoard(b.id)"
          :class="activeBoardId === b.id ? 'bg-indigo-500 text-white' : 'bg-slate-800 text-slate-300'"
          class="px-3 py-1.5 rounded-full text-sm transition"
        >
          {{ b.name }}
        </button>
      </div>

      <!-- 列表 -->
      <div v-if="isLoading && posts.length === 0" class="text-center text-slate-400 py-20">加载中...</div>
      <div v-else-if="posts.length === 0" class="text-center text-slate-400 py-20">暂无帖子，快来发表第一篇吧</div>
      <div v-else class="space-y-4">
        <div
          v-for="post in posts"
          :key="post.id"
          @click="goPost(post.id)"
          class="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 hover:border-indigo-500/50 transition cursor-pointer"
        >
          <div class="flex items-center gap-2 mb-2">
            <span class="text-xs px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300">{{ post.boardName }}</span>
            <span v-if="post.isHot" class="text-xs px-2 py-0.5 rounded-full bg-orange-500/20 text-orange-300">热门</span>
          </div>
          <h3 class="text-lg font-semibold text-slate-100 mb-1">{{ post.title }}</h3>
          <p class="text-slate-400 text-sm line-clamp-2 mb-3">{{ post.content }}</p>
          <div class="flex items-center justify-between text-xs text-slate-500">
            <div class="flex items-center gap-2">
              <AuthorLink :author-id="post.authorId" :name="post.authorName" :avatar="post.authorAvatar" />
              <span>{{ post.createdAt }}</span>
            </div>
            <div class="flex gap-3">
              <span>👁 {{ post.views }}</span>
              <span>💬 {{ post.replies }}</span>
              <span>❤ {{ post.likes }}</span>
            </div>
          </div>
        </div>
        <button
          v-if="hasMore()"
          @click="loadMore"
          :disabled="isLoading"
          class="w-full py-3 rounded-full bg-slate-800 text-slate-300 hover:bg-slate-700 disabled:opacity-50 transition"
        >
          加载更多
        </button>
      </div>
    </div>
  </div>
</template>
