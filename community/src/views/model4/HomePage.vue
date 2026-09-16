<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendedHeroes, getHeroStatistics, getHeroAvatar, onHeroAvatarError, type WzHero } from '../../api/model4/data'
import { getPostsFromBackend } from '../../api/model3/data'

const router = useRouter()
const recommended = ref<WzHero[]>([])
const heroTotal = ref(0)
const postTotal = ref(0)

const goHeroes = () => router.push('/module4/heroes')
const goForum = () => router.push('/module4/forum')
const goHero = (id: number) => router.push(`/module4/heroes/${id}`)

onMounted(async () => {
  recommended.value = await getRecommendedHeroes(6)
  const stats = await getHeroStatistics()
  if (stats) heroTotal.value = stats.total
  const page = await getPostsFromBackend({ communityId: 4, size: 1 })
  postTotal.value = page.total
})
</script>

<template>
  <div class="min-h-screen bg-[#0b1020] text-slate-100">
    <!-- Hero -->
    <header class="relative py-24 overflow-hidden">
      <div class="absolute inset-0 bg-[radial-gradient(circle_at_25%_15%,rgba(212,175,55,0.22),transparent),radial-gradient(circle_at_75%_60%,rgba(220,38,38,0.22),transparent)]"></div>
      <div class="relative z-10 max-w-6xl mx-auto px-4 text-center">
        <h1 class="text-5xl md:text-6xl font-extrabold mb-5 bg-gradient-to-r from-amber-300 via-yellow-400 to-red-500 bg-clip-text text-transparent">
          王者荣耀
        </h1>
        <p class="text-slate-300 text-lg max-w-2xl mx-auto mb-9">
          王者峡谷，荣耀之战。查阅英雄图鉴、交流玩法心得，与召唤师们一起上分。
        </p>
        <div class="flex justify-center gap-4">
          <button
            @click="goHeroes"
            class="px-8 py-3 rounded-full font-bold bg-gradient-to-r from-amber-500 to-red-600 text-white hover:shadow-lg hover:shadow-amber-500/30 transition"
          >
            英雄图鉴
          </button>
          <button
            @click="goForum"
            class="px-8 py-3 rounded-full font-bold bg-slate-800 border border-amber-500/40 text-amber-200 hover:bg-slate-700 transition"
          >
            王者论坛
          </button>
        </div>
      </div>
    </header>

    <!-- 统计 -->
    <section class="max-w-6xl mx-auto px-4 -mt-8">
      <div class="grid grid-cols-3 gap-4">
        <div class="bg-slate-900/70 border border-amber-500/20 rounded-2xl p-6 text-center">
          <div class="text-3xl font-bold text-amber-300">{{ heroTotal }}</div>
          <div class="text-slate-400 text-sm mt-1">英雄总数</div>
        </div>
        <div class="bg-slate-900/70 border border-amber-500/20 rounded-2xl p-6 text-center">
          <div class="text-3xl font-bold text-red-300">{{ postTotal }}</div>
          <div class="text-slate-400 text-sm mt-1">社区帖子</div>
        </div>
        <div class="bg-slate-900/70 border border-amber-500/20 rounded-2xl p-6 text-center">
          <div class="text-3xl font-bold text-yellow-300">4</div>
          <div class="text-slate-400 text-sm mt-1">板块数量</div>
        </div>
      </div>
    </section>

    <!-- 热门英雄推荐 -->
    <section class="max-w-6xl mx-auto px-4 py-14">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-amber-200">热门英雄</h2>
        <button @click="goHeroes" class="text-sm text-amber-300 hover:text-amber-200">查看全部 ›</button>
      </div>
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
        <div
          v-for="hero in recommended"
          :key="hero.id"
          @click="goHero(hero.id)"
          class="bg-slate-900/60 border border-slate-800 rounded-2xl p-4 text-center cursor-pointer hover:border-amber-500/50 hover:-translate-y-1 transition"
        >
          <img
            :src="getHeroAvatar(hero)"
            :alt="hero.name"
            @error="onHeroAvatarError($event, hero.name)"
            class="w-16 h-16 rounded-2xl mx-auto mb-3 object-cover"
          />
          <div class="font-semibold text-slate-100">{{ hero.name }}</div>
          <div class="text-xs text-slate-500 mt-0.5">{{ hero.role }}</div>
        </div>
        <div v-if="recommended.length === 0" class="col-span-full text-center text-slate-500 py-10">
          暂无英雄数据
        </div>
      </div>
    </section>
  </div>
</template>
