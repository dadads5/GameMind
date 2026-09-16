<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api/request'
import { getPostsFromBackend } from '../../api/model3/data'

const router = useRouter()

const stats = ref({ totalPosts: 0, todayPosts: 0 })
const characterCount = ref(0)

const navItems = [
  { label: '论坛', desc: '七国玩家交流', to: '/module3/forum', icon: '📖' },
  { label: '角色配装', desc: '阵容与武器推荐', to: '/module3/build', icon: '⚔️' },
  { label: '伤害计算器', desc: '估算你的输出', to: '/module3/calculator', icon: '🧮' },
  { label: '抽卡模拟', desc: '试试手气', to: '/module3/gacha', icon: '🎲' },
  { label: '新闻资讯', desc: '版本前瞻', to: '/module3/news', icon: '📰' },
]

onMounted(async () => {
  // 帖子统计：按原神社区聚合（后端无社区维度统计端点，前端计算）
  try {
    const page = await getPostsFromBackend({ communityId: 1, size: 500 })
    const now = new Date()
    const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
    stats.value = {
      totalPosts: page.total,
      todayPosts: page.posts.filter((p) => (p.createdAt || '').slice(0, 10) === todayStr).length,
    }
  } catch {
    /* 统计失败不阻塞页面 */
  }
  // 角色总数：/characters/statistics 返回 { total, byElement }
  try {
    const res = await request.get<{ total: number }>('/characters/statistics')
    if (res.success && res.data) characterCount.value = res.data.total ?? 0
  } catch {
    /* 忽略 */
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-indigo-950/40 to-slate-950 text-slate-100">
    <!-- Hero -->
    <header class="relative py-28 overflow-hidden">
      <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(102,126,234,0.25),transparent),radial-gradient(circle_at_70%_60%,rgba(118,75,162,0.25),transparent)]"></div>
      <div class="relative z-10 max-w-6xl mx-auto px-4 text-center">
        <h1 class="text-5xl md:text-7xl font-extrabold mb-6 bg-gradient-to-r from-indigo-300 via-purple-300 to-sky-300 bg-clip-text text-transparent">
          原神 · 提瓦特
        </h1>
        <p class="text-xl text-slate-300 max-w-2xl mx-auto mb-10">
          在七国之间旅行，与伙伴并肩，分享你的攻略、配装与故事。
        </p>
        <div class="flex justify-center gap-4">
          <button
            @click="router.push('/module3/forum')"
            class="bg-gradient-to-r from-indigo-500 to-purple-500 hover:shadow-lg hover:shadow-indigo-500/30 text-white px-8 py-3 rounded-full font-bold transition"
          >
            进入论坛
          </button>
          <button
            @click="router.push('/module3/build')"
            class="bg-slate-800 hover:bg-slate-700 text-white px-8 py-3 rounded-full font-bold transition border border-slate-700"
          >
            角色配装
          </button>
        </div>
      </div>
    </header>

    <!-- 统计 -->
    <section class="max-w-6xl mx-auto px-4 -mt-10">
      <div class="grid grid-cols-3 gap-4">
        <div class="bg-slate-900/70 border border-slate-800 rounded-2xl p-6 text-center">
          <div class="text-3xl font-bold text-indigo-300">{{ stats.totalPosts }}</div>
          <div class="text-slate-400 text-sm mt-1">总帖子</div>
        </div>
        <div class="bg-slate-900/70 border border-slate-800 rounded-2xl p-6 text-center">
          <div class="text-3xl font-bold text-purple-300">{{ stats.todayPosts }}</div>
          <div class="text-slate-400 text-sm mt-1">今日新帖</div>
        </div>
        <div class="bg-slate-900/70 border border-slate-800 rounded-2xl p-6 text-center">
          <div class="text-3xl font-bold text-sky-300">{{ characterCount }}</div>
          <div class="text-slate-400 text-sm mt-1">角色总数</div>
        </div>
      </div>
    </section>

    <!-- 导航卡片 -->
    <section class="max-w-6xl mx-auto px-4 py-16">
      <div class="grid md:grid-cols-3 gap-6">
        <div
          v-for="item in navItems"
          :key="item.label"
          @click="router.push(item.to)"
          class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 hover:border-indigo-500/50 transition cursor-pointer"
        >
          <div class="text-3xl mb-3">{{ item.icon }}</div>
          <h3 class="text-lg font-semibold text-slate-100">{{ item.label }}</h3>
          <p class="text-slate-400 text-sm mt-1">{{ item.desc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>
