<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  listHeroes,
  getHeroAvatar,
  onHeroAvatarError,
  HERO_ROLES,
  HERO_LANES,
  roleColor,
  type WzHero,
} from '../../api/model4/data'

const router = useRouter()
const allHeroes = ref<WzHero[]>([])
const isLoading = ref(false)
const keyword = ref('')
const activeRole = ref<string>('')
const activeLane = ref<string>('')

// 后端已支持 role/lane/keyword 参数，这里在前端做二次过滤，切换筛选无需重新请求
const filtered = computed(() =>
  allHeroes.value.filter((h) => {
    const matchRole = !activeRole.value || h.role === activeRole.value
    const matchLane = !activeLane.value || h.lane === activeLane.value
    const kw = keyword.value.trim()
    const matchKw = !kw || h.name.includes(kw) || (h.title || '').includes(kw)
    return matchRole && matchLane && matchKw
  }),
)

const setRole = (role: string) => (activeRole.value = activeRole.value === role ? '' : role)
const setLane = (lane: string) => (activeLane.value = activeLane.value === lane ? '' : lane)
const goDetail = (id: number) => router.push(`/module4/heroes/${id}`)
const goBack = () => router.back()

onMounted(async () => {
  isLoading.value = true
  allHeroes.value = await listHeroes()
  isLoading.value = false
})
</script>

<template>
  <div class="min-h-screen bg-[#0b1020] text-slate-100">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <button @click="goBack" class="text-slate-400 hover:text-amber-300 mb-4">← 返回</button>

      <h1 class="text-3xl font-bold mb-6 bg-gradient-to-r from-amber-300 via-yellow-400 to-red-500 bg-clip-text text-transparent">
        英雄图鉴
      </h1>

      <!-- 搜索 -->
      <input
        v-model="keyword"
        placeholder="搜索英雄或称号..."
        class="w-full mb-4 px-4 py-2.5 rounded-full bg-slate-800/80 border border-slate-700 focus:outline-none focus:border-amber-500"
      />

      <!-- 职业筛选 -->
      <div class="flex flex-wrap gap-2 mb-3">
        <span class="text-xs text-slate-500 self-center mr-1">职业</span>
        <button
          v-for="role in HERO_ROLES"
          :key="role"
          @click="setRole(role)"
          :class="activeRole === role ? 'bg-gradient-to-r from-amber-500 to-red-600 text-white' : 'bg-slate-800 text-slate-300'"
          class="px-3 py-1.5 rounded-full text-sm transition"
        >
          {{ role }}
        </button>
      </div>

      <!-- 分路筛选 -->
      <div class="flex flex-wrap gap-2 mb-7">
        <span class="text-xs text-slate-500 self-center mr-1">分路</span>
        <button
          v-for="lane in HERO_LANES"
          :key="lane"
          @click="setLane(lane)"
          :class="activeLane === lane ? 'bg-gradient-to-r from-amber-500 to-red-600 text-white' : 'bg-slate-800 text-slate-300'"
          class="px-3 py-1.5 rounded-full text-sm transition"
        >
          {{ lane }}
        </button>
      </div>

      <!-- 列表 -->
      <div v-if="isLoading" class="text-center text-slate-400 py-20">加载中...</div>
      <div v-else-if="filtered.length === 0" class="text-center text-slate-400 py-20">暂无匹配英雄</div>
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        <div
          v-for="hero in filtered"
          :key="hero.id"
          @click="goDetail(hero.id)"
          class="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 text-center cursor-pointer hover:border-amber-500/50 hover:-translate-y-1 transition"
        >
          <img
            :src="getHeroAvatar(hero)"
            :alt="hero.name"
            @error="onHeroAvatarError($event, hero.name)"
            class="w-20 h-20 rounded-2xl mx-auto mb-3 object-cover"
          />
          <h3 class="font-bold text-slate-100">{{ hero.name }}</h3>
          <p class="text-xs text-slate-500 mb-2">{{ hero.title }}</p>
          <div class="flex justify-center gap-1.5 mb-2">
            <span class="text-[10px] px-2 py-0.5 rounded-full" :class="roleColor[hero.role] || 'text-slate-300 bg-slate-700'">
              {{ hero.role }}
            </span>
            <span class="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-400">{{ hero.lane }}</span>
          </div>
          <div class="text-[10px] text-amber-400">难度 {{ hero.difficulty }}/10</div>
        </div>
      </div>
    </div>
  </div>
</template>
