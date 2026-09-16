<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { statisticsApi } from '../api/statistics'
import { listCommunities, type CommunityItem } from '../api/board'
import { getOnlineCount, getGlobalHotPosts, type Post } from '../api/naruto/data'

const router = useRouter()

const stats = ref({
  activeUsers: null as number | null,
  totalPosts: null as number | null,
  totalReplies: null as number | null,
  totalViews: null as number | null
})

const isLoading = ref(true)
const communities = ref<CommunityItem[]>([])

// 实时在线人数与全站热门榜单
const onlineUsers = ref(0)
const hotPosts = ref<Post[]>([])
let onlineTimer: number | null = null

const loadHot = async () => { hotPosts.value = await getGlobalHotPosts(5) }
const loadOnline = async () => { onlineUsers.value = await getOnlineCount() }

onMounted(async () => {
  await loadStatistics()
  try {
    const all = await listCommunities()
    // 仅展示管理员新增的游戏社区；内置社区 1~4 已在上方模块卡片展示，避免重复
    communities.value = all.filter((c) => (c.id ?? 0) > 4)
  } catch {
    /* 忽略：游戏社区加载失败不影响首页 */
  }
  await loadHot()
  await loadOnline()
  onlineTimer = window.setInterval(loadOnline, 30000)
})

onBeforeUnmount(() => {
  if (onlineTimer) clearInterval(onlineTimer)
})

async function loadStatistics() {
  isLoading.value = true
  try {
    const res = await statisticsApi.overview()
    if (res?.success && res.data) {
      stats.value.activeUsers = res.data.totalUsers ?? null
      stats.value.totalPosts = res.data.totalPosts ?? null
      stats.value.totalReplies = res.data.totalComments ?? null
      stats.value.totalViews = res.data.totalViews ?? null
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    isLoading.value = false
  }
}

const modules = [
  {
    id: 1,
    name: '火影忍者',
    icon: '/huo/logo.jpg',
    isImage: true,
    description: '火影忍者爱好者社区，讨论剧情、忍术、人物',
    color: 'from-orange-500 to-red-600',
    bgColor: 'bg-orange-50 dark:bg-orange-900/20',
    borderColor: 'border-orange-200 dark:border-orange-800',
    textColor: 'text-orange-600',
    path: '/forum'
  },
  {
    id: 2,
    name: '杀戮尖塔2',
    icon: '/shalu-logo.jpg',
    isImage: true,
    description: '杀戮尖塔2攻略，包含游戏攻略、人物介绍、装备推荐等',
    color: 'from-purple-500 to-fuchsia-600',
    bgColor: 'bg-purple-50 dark:bg-purple-900/20',
    borderColor: 'border-purple-200 dark:border-purple-800',
    textColor: 'text-purple-600',
    path: '/module2'
  },
  {
    id: 3,
    name: '原神',
    icon: '/yslogo.png',
    isImage: true,
    description: '原神攻略，包含游戏攻略、人物介绍、装备推荐等',
    color: 'from-blue-400 to-white',
    bgColor: 'bg-blue-50 dark:bg-blue-900/20',
    borderColor: 'border-blue-200 dark:border-blue-800',
    textColor: 'text-blue-600',
    path: '/module3'
  },
  {
    id: 4,
    name: '王者荣耀',
    icon: '/wzry-logo.jpg',
    isImage: true,
    description: '王者荣耀玩家社区，讨论英雄攻略、赛事资讯、组队开黑',
    color: 'from-yellow-500 to-orange-600',
    bgColor: 'bg-yellow-50 dark:bg-yellow-900/20',
    borderColor: 'border-yellow-200 dark:border-yellow-800',
    textColor: 'text-yellow-600',
    path: '/module4'
  }
]

const goToModule = (path: string) => {
  router.push(path)
}

const goToPost = (id: number) => {
  router.push(`/forum/post/${id}`)
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-10">
    <!-- 页面标题 -->
    <div class="text-center py-10">
      <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gradient-to-br from-pink-500 to-purple-600 shadow-lg mb-6 overflow-hidden">
        <img src="/mainlogo.png" alt="Gamemind" class="w-full h-full object-cover" />
      </div>
      <h1 class="text-4xl md:text-5xl font-bold text-gray-800 dark:text-white mb-4">
        Gamemind-游戏社区服务平台
      </h1>
      <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
        欢迎来到 Gamemind 游戏社区服务平台，选择您感兴趣的模块开始探索
      </p>
    </div>

    <!-- 实时在线人数 -->
    <div class="flex justify-center -mt-2">
      <div class="flex items-center space-x-2 px-4 py-2 rounded-full bg-green-50 border border-green-200 text-sm dark:bg-green-900/20 dark:border-green-800">
        <span class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
        <span class="text-green-600 dark:text-green-400 font-medium">当前在线 {{ onlineUsers }} 人</span>
      </div>
    </div>

    <!-- AI问答功能入口 -->
    <div
      @click="goToModule('/ai-chat')"
      class="relative overflow-hidden rounded-2xl border-2 p-8 cursor-pointer hover:-translate-y-1 transition-all duration-300 hover:shadow-xl bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 border-indigo-200 dark:border-indigo-800"
    >
      <!-- 顶部渐变条 -->
      <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-indigo-500 to-purple-600"></div>

      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-6">
          <!-- 图标区域 -->
          <div class="w-20 h-20 rounded-2xl flex items-center justify-center shadow-lg bg-gradient-to-br from-indigo-500 to-purple-600 overflow-hidden">
            <img src="/AI.jpg" alt="AI" class="w-full h-full object-cover" />
          </div>

          <!-- 文字区域 -->
          <div>
            <h3 class="text-2xl font-bold text-gray-800 dark:text-white mb-2">
              AI智能问答
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              与AI进行对话交流，获取游戏相关信息和帮助
            </p>
          </div>
        </div>

        <!-- 进入箭头 -->
        <div class="text-4xl text-indigo-500 hover:translate-x-2 transition-transform duration-300">
          →
        </div>
      </div>
    </div>

    <!-- 模块入口卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div
        v-for="module in modules"
        :key="module.id"
        @click="goToModule(module.path)"
        class="relative overflow-hidden rounded-2xl border-2 p-6 cursor-pointer hover:-translate-y-1 transition-all duration-300 hover:shadow-xl"
        :class="[module.bgColor, module.borderColor]"
      >
        <!-- 顶部渐变条 -->
        <div :class="['absolute top-0 left-0 right-0 h-1 bg-gradient-to-r', module.color]"></div>

        <!-- 图标区域 -->
        <div class="mb-4">
          <div :class="['w-16 h-16 mx-auto rounded-2xl flex items-center justify-center shadow-md transition-transform duration-300 group-hover:scale-110 overflow-hidden', module.bgColor]">
            <img v-if="module.isImage" :src="module.icon" :alt="module.name" class="w-full h-full object-cover" />
            <span v-else class="text-4xl">{{ module.icon }}</span>
          </div>
        </div>

        <!-- 模块名称 -->
        <h3 class="text-xl font-bold text-gray-800 dark:text-white text-center mb-2">
          {{ module.name }}
        </h3>

        <!-- 模块描述 -->
        <p class="text-gray-600 dark:text-gray-400 text-sm text-center px-2">
          {{ module.description }}
        </p>

        <!-- 进入提示 -->
        <div class="mt-6 flex justify-center">
          <span :class="['text-sm font-medium flex items-center hover:translate-x-1 transition-transform duration-300', module.textColor]">
            进入模块 →
          </span>
        </div>
      </div>
    </div>

    <!-- 游戏社区（管理员在后台新增，统一论坛模板，纯论坛形态） -->
    <div v-if="communities.length" class="mt-2">
      <h2 class="text-xl font-bold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
        🎮 游戏社区
        <span class="text-sm font-normal text-gray-400">由管理员创建 · 纯论坛交流</span>
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="c in communities"
          :key="c.id"
          @click="goToModule('/community/' + c.id)"
          class="relative overflow-hidden rounded-2xl border-2 p-5 cursor-pointer hover:-translate-y-1 transition-all duration-300 hover:shadow-xl border-indigo-200 dark:border-indigo-800 bg-indigo-50/60 dark:bg-indigo-900/20"
        >
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-indigo-500 to-purple-600"></div>
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-xl bg-white dark:bg-gray-800 flex items-center justify-center text-2xl shadow overflow-hidden">
              <span v-if="c.icon">{{ c.icon }}</span>
              <span v-else>🎮</span>
            </div>
            <div class="min-w-0">
              <h3 class="font-bold text-gray-800 dark:text-white">{{ c.name }}</h3>
              <p class="text-xs text-gray-500 dark:text-gray-400 line-clamp-1">{{ c.description || '游戏论坛社区' }}</p>
            </div>
          </div>
          <div class="mt-3 text-sm text-indigo-600 dark:text-indigo-300">进入社区 →</div>
        </div>
      </div>
    </div>

    <!-- 全站热门榜单 Top5 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow p-6 border border-pink-100 dark:border-pink-900">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-bold text-gray-800 dark:text-white flex items-center gap-2">🔥 热门榜单</h2>
        <RouterLink to="/hot" class="text-sm text-pink-500 hover:underline">查看全部</RouterLink>
      </div>
      <div v-if="hotPosts.length === 0" class="text-sm text-gray-400 py-2">暂无数据</div>
      <ul v-else class="space-y-2">
        <li
          v-for="(post, index) in hotPosts"
          :key="post.id"
          @click="goToPost(post.id)"
          class="flex items-center space-x-3 p-2 rounded-lg hover:bg-pink-50 dark:hover:bg-pink-900/20 cursor-pointer transition"
        >
          <span
            class="flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center text-sm font-bold"
            :class="index === 0 ? 'bg-pink-500 text-white' : index === 1 ? 'bg-purple-500 text-white' : index === 2 ? 'bg-fuchsia-500 text-white' : 'bg-pink-100 text-pink-600 dark:bg-pink-900/40 dark:text-pink-300'"
          >{{ index + 1 }}</span>
          <span class="flex-1 truncate text-gray-700 dark:text-gray-200">{{ post.title }}</span>
          <span class="text-xs text-gray-400">👁️ {{ post.views }}</span>
        </li>
      </ul>
    </div>

    <!-- 平台介绍 -->
    <div class="bg-gradient-to-r from-pink-500 to-purple-600 rounded-2xl shadow-lg p-8 text-white">
      <div class="grid md:grid-cols-3 gap-8 text-center">
        <div>
          <div class="text-4xl mb-3">🌐</div>
          <h3 class="font-bold text-lg mb-2">社区聚合</h3>
          <p class="text-pink-100 text-sm">热门游戏圈子、话题讨论一网打尽</p>
        </div>
        <div>
          <div class="text-4xl mb-3">💡</div>
          <h3 class="font-bold text-lg mb-2">AI 赋能</h3>
          <p class="text-pink-100 text-sm">大模型驱动，秒变你的专属游戏助手</p>
        </div>
        <div>
          <div class="text-4xl mb-3">🚀</div>
          <h3 class="font-bold text-lg mb-2">一站体验</h3>
          <p class="text-pink-100 text-sm">资讯、互动、AI 工具，打开即用</p>
        </div>
      </div>
    </div>

    <!-- 统计数据 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-6 text-center border border-pink-100 dark:border-pink-900">
        <p class="text-3xl font-bold text-pink-500 mb-1">
          <span v-if="isLoading" class="animate-pulse">--</span>
          <span v-else-if="stats.activeUsers !== null">{{ stats.activeUsers.toLocaleString() }}</span>
          <span v-else>N/A</span>
        </p>
        <p class="text-gray-600 dark:text-gray-400 text-sm">活跃用户</p>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-6 text-center border border-pink-100 dark:border-pink-900">
        <p class="text-3xl font-bold text-purple-500 mb-1">
          <span v-if="isLoading" class="animate-pulse">--</span>
          <span v-else-if="stats.totalPosts !== null">{{ stats.totalPosts.toLocaleString() }}</span>
          <span v-else>N/A</span>
        </p>
        <p class="text-gray-600 dark:text-gray-400 text-sm">帖子总数</p>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-6 text-center border border-pink-100 dark:border-pink-900">
        <p class="text-3xl font-bold text-fuchsia-500 mb-1">
          <span v-if="isLoading" class="animate-pulse">--</span>
          <span v-else-if="stats.totalReplies !== null">{{ stats.totalReplies.toLocaleString() }}</span>
          <span v-else>N/A</span>
        </p>
        <p class="text-gray-600 dark:text-gray-400 text-sm">回复总数</p>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-6 text-center border border-pink-100 dark:border-pink-900">
        <p class="text-3xl font-bold text-amber-500 mb-1">
          <span v-if="isLoading" class="animate-pulse">--</span>
          <span v-else-if="stats.totalViews !== null">{{ stats.totalViews.toLocaleString() }}</span>
          <span v-else>N/A</span>
        </p>
        <p class="text-gray-600 dark:text-gray-400 text-sm">累计浏览</p>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="text-center py-8">
      <p class="text-gray-500 dark:text-gray-400">
        <span class="text-xl mr-2">✨</span>
        欢迎加入Gamemind平台
        <span class="text-xl ml-2">🎉</span>
      </p>
    </div>
  </div>
</template>
