<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getPostsFromBackend,
  getAvatarUrl,
  onAvatarError,
  type Post,
} from '../api/model3/data'
import { listCommunities, listBoards, type CommunityItem } from '../api/board'

const route = useRoute()
const router = useRouter()
const communityId = computed(() => Number(route.params.communityId))

const community = ref<CommunityItem | null>(null)
const posts = ref<Post[]>([])
const isLoading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const sort = ref<'latest' | 'hot'>('latest')
const keyword = ref('')
const activeBoardId = ref<number | undefined>(undefined)
const boardOptions = ref<{ id: number; name: string }[]>([])

const hasMore = () => page.value < totalPages.value

async function fetchCommunity() {
  try {
    const list = await listCommunities()
    community.value = list.find((c) => c.id === communityId.value) ?? null
  } catch {
    /* 忽略 */
  }
}

async function fetchBoards() {
  boardOptions.value = await listBoards(communityId.value)
}

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
    communityId: communityId.value,
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
  router.push(`/community/${communityId.value}/post/${id}`)
}
function goCreate() {
  if (!localStorage.getItem('token')) {
    router.push({ name: 'login', query: { redirect: `/community/${communityId.value}/create` } })
    return
  }
  router.push(`/community/${communityId.value}/create`)
}

onMounted(() => {
  fetchCommunity()
  fetchBoards()
  fetchPosts()
})

watch(
  () => route.params.communityId,
  () => {
    activeBoardId.value = undefined
    fetchCommunity()
    fetchBoards()
    fetchPosts()
  },
)
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-indigo-50/60 to-white dark:from-gray-900 dark:to-gray-900 text-gray-800 dark:text-gray-100">
    <div class="max-w-5xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-gray-500 hover:text-indigo-600 mb-4">← 返回</button>

      <!-- 头部 banner -->
      <div class="bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl p-6 mb-6 text-white shadow-lg">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center text-3xl">
            <span v-if="community?.icon">{{ community.icon }}</span>
            <span v-else>🎮</span>
          </div>
          <div>
            <h1 class="text-2xl font-bold">{{ community?.name || '游戏社区' }}</h1>
            <p class="text-white/80 text-sm mt-1">{{ community?.description || '玩家交流论坛' }}</p>
          </div>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="flex flex-col sm:flex-row gap-3 mb-4">
        <input
          v-model="keyword"
          @keyup.enter="onSearch"
          placeholder="搜索帖子..."
          class="flex-1 px-4 py-2 rounded-full border dark:border-gray-700 bg-white dark:bg-gray-800 focus:outline-none focus:border-indigo-500"
        />
        <div class="flex gap-2">
          <button
            @click="changeSort('latest')"
            :class="sort === 'latest' ? 'bg-indigo-500 text-white' : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border dark:border-gray-700'"
            class="px-4 py-2 rounded-full text-sm transition"
          >最新</button>
          <button
            @click="changeSort('hot')"
            :class="sort === 'hot' ? 'bg-indigo-500 text-white' : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border dark:border-gray-700'"
            class="px-4 py-2 rounded-full text-sm transition"
          >热门</button>
        </div>
        <button
          @click="goCreate"
          class="px-5 py-2 rounded-full font-medium bg-gradient-to-r from-indigo-500 to-purple-600 text-white hover:shadow-lg transition"
        >发表帖子</button>
      </div>

      <!-- 板块筛选 -->
      <div v-if="boardOptions.length" class="flex flex-wrap gap-2 mb-6">
        <button
          @click="changeBoard(undefined)"
          :class="activeBoardId === undefined ? 'bg-indigo-500 text-white' : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border dark:border-gray-700'"
          class="px-3 py-1.5 rounded-full text-sm transition"
        >全部</button>
        <button
          v-for="b in boardOptions"
          :key="b.id"
          @click="changeBoard(b.id)"
          :class="activeBoardId === b.id ? 'bg-indigo-500 text-white' : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border dark:border-gray-700'"
          class="px-3 py-1.5 rounded-full text-sm transition"
        >{{ b.name }}</button>
      </div>

      <!-- 列表 -->
      <div v-if="isLoading && posts.length === 0" class="text-center text-gray-500 py-20">加载中...</div>
      <div v-else-if="posts.length === 0" class="text-center text-gray-500 py-20">暂无帖子，快来发表第一篇吧</div>
      <div v-else class="space-y-3">
        <div
          v-for="post in posts"
          :key="post.id"
          @click="goPost(post.id)"
          class="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl p-5 hover:border-indigo-400 hover:shadow-md transition cursor-pointer"
        >
          <div class="flex items-center gap-2 mb-2">
            <span class="text-xs px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-300">{{ post.boardName }}</span>
            <span v-if="post.isHot" class="text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-500 dark:bg-red-900/40 dark:text-red-300">热门</span>
          </div>
          <h3 class="text-lg font-semibold mb-1">{{ post.title }}</h3>
          <p class="text-gray-500 dark:text-gray-400 text-sm line-clamp-2 mb-3">{{ post.content }}</p>
          <div class="flex items-center justify-between text-xs text-gray-400">
            <div class="flex items-center gap-2">
              <router-link :to="`/user/${post.authorId}`" @click.stop class="flex items-center gap-2 group">
                <img :src="getAvatarUrl(post.authorAvatar, post.authorName)" @error="onAvatarError($event, post.authorName)" class="w-6 h-6 rounded-full" />
                <span class="group-hover:text-indigo-600">{{ post.authorName }}</span>
              </router-link>
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
          class="w-full py-3 rounded-full bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 transition"
        >加载更多</button>
      </div>
    </div>
  </div>
</template>
