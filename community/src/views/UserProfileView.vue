<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi, type UserProfile, type UserPostItem, type UserStats } from '../api/user'
import { getAvatarUrl, onAvatarError } from '../api/model3/data'
import { notifySuccess, notifyError, notifyWarning } from '../utils/notify'
import { computeUserRating } from '../utils/userRating'

const route = useRoute()
const router = useRouter()
const userId = computed(() => Number(route.params.id))
const profile = ref<UserProfile | null>(null)
const posts = ref<UserPostItem[]>([])
const isLoading = ref(true)
const postsLoading = ref(false)
const likeCount = ref(0)
const todayLiked = ref(false)
const isLiking = ref(false)
const userLevel = ref(1)
const userBadges = ref<{ name: string; icon: string; color: string }[]>([])
const userStats = ref({
  totalPosts: 0,
  totalReplies: 0,
  totalLikes: 0,
})

const isLoggedIn = () => !!localStorage.getItem('token')

async function loadProfile() {
  isLoading.value = true
  try {
    const p = await userApi.getProfile(userId.value)
    profile.value = p
    likeCount.value = p?.likeCount ?? 0
    todayLiked.value = !!p?.todayLiked
  } catch {
    profile.value = null
  } finally {
    isLoading.value = false
  }
}

async function loadPosts() {
  postsLoading.value = true
  try {
    const res = await userApi.getPosts(userId.value, 1, 20)
    posts.value = res.posts
  } catch {
    posts.value = []
  } finally {
    postsLoading.value = false
  }
}

async function loadRating() {
  if (!profile.value) return
  const stats: UserStats | null = await userApi.getStats(userId.value)
  if (!stats) return
  const rating = computeUserRating({
    role: profile.value.role,
    vip: profile.value.vip,
    createdAt: profile.value.createdAt,
    homeLikeCount: stats.homeLikeCount,
    totalPosts: stats.totalPosts,
    totalReplies: stats.totalReplies,
    totalLikes: stats.totalLikes,
  })
  userLevel.value = rating.level
  userBadges.value = rating.badges
  userStats.value = {
    totalPosts: stats.totalPosts ?? 0,
    totalReplies: stats.totalReplies ?? 0,
    totalLikes: stats.totalLikes ?? 0,
  }
}

async function like() {
  if (!isLoggedIn()) {
    notifyWarning('请先登录')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (todayLiked.value) return
  isLiking.value = true
  try {
    const res = await userApi.likeUser(userId.value)
    if (res.success && res.data) {
      likeCount.value = res.data.likeCount
      todayLiked.value = res.data.todayLiked
      notifySuccess('点赞成功')
    }
  } catch (e: any) {
    notifyError(e?.response?.data?.message || '点赞失败')
  } finally {
    isLiking.value = false
  }
}

function goPost(p: UserPostItem) {
  if (p.communityId) {
    router.push(`/community/${p.communityId}/post/${p.id}`)
  } else {
    notifyWarning('该帖子无法跳转')
  }
}

function formatDate(s?: string) {
  return s ? s.slice(0, 10) : ''
}

onMounted(async () => {
  await loadProfile()
  loadPosts()
  loadRating()
})

watch(
  () => route.params.id,
  async () => {
    await loadProfile()
    loadPosts()
    loadRating()
  },
)
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-indigo-50/60 to-white dark:from-gray-900 dark:to-gray-900 text-gray-800 dark:text-gray-100">
    <div class="max-w-3xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-gray-500 hover:text-indigo-600 mb-4">← 返回</button>

      <div v-if="isLoading" class="text-center text-gray-500 py-20">加载中...</div>
      <div v-else-if="!profile" class="text-center text-gray-500 py-20">用户不存在</div>

      <template v-else>
        <!-- 资料卡 -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 mb-6 flex items-center gap-5">
          <img
            :src="getAvatarUrl(profile.avatar, profile.nickname || profile.username)"
            @error="onAvatarError($event, profile.nickname || profile.username)"
            class="w-20 h-20 rounded-full object-cover"
          />
          <div class="flex-1 min-w-0">
            <h1 class="text-2xl font-bold truncate">{{ profile.nickname || profile.username }}</h1>
            <p class="text-gray-500 text-sm">@{{ profile.username }}</p>
            <p v-if="profile.bio" class="text-gray-600 dark:text-gray-300 mt-1">{{ profile.bio }}</p>
            <p class="text-gray-400 text-xs mt-1">注册于 {{ formatDate(profile.createdAt) }}</p>
          </div>
          <div class="text-center flex-shrink-0">
            <div class="text-2xl font-bold text-indigo-600 dark:text-indigo-300">❤ {{ likeCount }}</div>
            <div class="text-xs text-gray-500 mb-2">主页点赞</div>
            <button
              @click="like"
              :disabled="todayLiked || isLiking"
              :class="todayLiked ? 'bg-gray-200 text-gray-500 dark:bg-gray-700 dark:text-gray-400' : 'bg-indigo-500 text-white hover:bg-indigo-600'"
              class="px-4 py-2 rounded-full text-sm font-medium transition disabled:cursor-not-allowed"
            >{{ todayLiked ? '今日已点赞' : '点赞' }}</button>
          </div>
        </div>

        <!-- 用户徽章 -->
        <div v-if="userBadges.length > 0" class="flex flex-wrap gap-2 mb-6">
          <span
            v-for="badge in userBadges"
            :key="badge.name"
            class="px-4 py-2 rounded-full text-sm font-medium flex items-center space-x-1"
            :class="{
              'bg-yellow-100 text-yellow-800': badge.color === 'yellow',
              'bg-purple-100 text-purple-800': badge.color === 'purple',
              'bg-red-100 text-red-800': badge.color === 'red',
              'bg-blue-100 text-blue-800': badge.color === 'blue',
              'bg-green-100 text-green-800': badge.color === 'green',
              'bg-indigo-100 text-indigo-800': badge.color === 'indigo',
              'bg-pink-100 text-pink-800': badge.color === 'pink',
              'bg-amber-100 text-amber-800': badge.color === 'gold'
            }"
          >
            <span>{{ badge.icon }}</span>
            <span>{{ badge.name }}</span>
          </span>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <div class="bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-xl p-4 text-center">
            <div class="text-3xl font-bold text-blue-600 mb-1">{{ userStats.totalPosts }}</div>
            <div class="text-sm text-blue-600/80">帖子数</div>
          </div>
          <div class="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-xl p-4 text-center">
            <div class="text-3xl font-bold text-green-600 mb-1">{{ userStats.totalReplies }}</div>
            <div class="text-sm text-green-600/80">回复数</div>
          </div>
          <div class="bg-gradient-to-br from-red-50 to-red-100 dark:from-red-900/20 dark:to-red-800/20 rounded-xl p-4 text-center">
            <div class="text-3xl font-bold text-red-600 mb-1">{{ userStats.totalLikes }}</div>
            <div class="text-sm text-red-600/80">获赞数</div>
          </div>
          <div class="bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-xl p-4 text-center">
            <div class="text-3xl font-bold text-purple-600 mb-1">Lv{{ userLevel }}</div>
            <div class="text-sm text-purple-600/80">用户等级</div>
          </div>
        </div>

        <!-- TA的帖子 -->
        <h2 class="text-lg font-semibold mb-3 text-indigo-600 dark:text-indigo-300">
          {{ profile.nickname || profile.username }} 的帖子
        </h2>
        <div v-if="postsLoading" class="text-center text-gray-500 py-10">加载中...</div>
        <div v-else-if="posts.length === 0" class="text-center text-gray-500 py-10">还没有发布帖子</div>
        <div v-else class="space-y-3">
          <div
            v-for="p in posts"
            :key="p.id"
            @click="goPost(p)"
            class="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl p-5 hover:border-indigo-400 hover:shadow-md transition cursor-pointer"
          >
            <div class="flex items-center gap-2 mb-1">
              <span class="text-xs px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-300">{{ p.boardName }}</span>
            </div>
            <h3 class="text-lg font-semibold mb-1">{{ p.title }}</h3>
            <p class="text-gray-500 dark:text-gray-400 text-sm line-clamp-2">{{ p.content }}</p>
            <div class="flex items-center gap-3 text-xs text-gray-400 mt-2">
              <span>{{ p.createdAt }}</span>
              <span>👁 {{ p.views }}</span>
              <span>💬 {{ p.replies }}</span>
              <span>❤ {{ p.likes }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
