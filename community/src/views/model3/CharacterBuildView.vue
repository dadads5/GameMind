<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '../../api/request'
import { useRouter } from 'vue-router'

const router = useRouter()

interface Character {
  name: string
  icon: string
  subtitle: string
  weapons: string
  artifacts: string
  stats: string
  talents: string
}

const characters = ref<Character[]>([])
const searchText = ref('')
const selected = ref<Character | null>(null)
const loading = ref(true)

const filtered = computed(() => {
  const t = searchText.value.trim()
  if (!t) return characters.value
  return characters.value.filter((c) => c.name.includes(t))
})

const load = async () => {
  loading.value = true
  try {
    const res = await request.get<Character[]>('/characters')
    if (res.success && res.data) characters.value = res.data
  } catch {
    characters.value = []
  } finally {
    loading.value = false
  }
}

const weaponsOf = (c: Character) => (c.weapons || '').split('、').filter(Boolean)
const artifactsOf = (c: Character) => (c.artifacts || '').split('<br>').filter(Boolean)
const statsOf = (c: Character) =>
  (c.stats || '')
    .split(' / ')
    .map((s) => {
      const [k, v] = s.split('+')
      return { k: k?.trim(), v: v?.trim() }
    })
    .filter((x) => x.k)

onMounted(load)
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-indigo-950/30 to-slate-950 text-slate-100">
    <div class="max-w-5xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-slate-400 hover:text-white mb-4">← 返回</button>
      <h1 class="text-2xl font-bold mb-6 bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">
        角色配装
      </h1>

      <!-- 列表态 -->
      <template v-if="!selected">
        <div class="mb-4">
          <input
            v-model="searchText"
            placeholder="搜索角色..."
            class="w-full px-4 py-2 rounded-full bg-slate-800/80 border border-slate-700 focus:outline-none focus:border-indigo-500"
          />
        </div>
        <div v-if="loading" class="text-center text-slate-400 py-20">加载中...</div>
        <div v-else-if="filtered.length === 0" class="text-center text-slate-400 py-20">暂无角色数据</div>
        <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div
            v-for="c in filtered"
            :key="c.name"
            @click="selected = c"
            class="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 text-center hover:border-indigo-500/50 cursor-pointer transition"
          >
            <div class="w-14 h-14 mx-auto rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 flex items-center justify-center text-2xl mb-3">
              {{ c.icon || c.name[0] }}
            </div>
            <h3 class="font-semibold">{{ c.name }}</h3>
            <p class="text-slate-400 text-xs mt-1">{{ c.subtitle }}</p>
          </div>
        </div>
      </template>

      <!-- 详情态 -->
      <template v-else>
        <button @click="selected = null" class="text-slate-400 hover:text-white mb-4">← 返回列表</button>
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
          <div class="flex items-center gap-4 mb-6">
            <div class="w-16 h-16 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 flex items-center justify-center text-3xl">
              {{ selected.icon || selected.name[0] }}
            </div>
            <div>
              <h2 class="text-xl font-bold">{{ selected.name }}</h2>
              <p class="text-slate-400 text-sm">{{ selected.subtitle }}</p>
            </div>
          </div>
          <div class="grid md:grid-cols-2 gap-4">
            <div class="bg-slate-800/40 rounded-xl p-4">
              <h4 class="text-indigo-300 font-medium mb-2">⚔️ 武器推荐</h4>
              <ul class="text-slate-300 text-sm space-y-1">
                <li v-for="(w, i) in weaponsOf(selected)" :key="i">{{ w }}</li>
              </ul>
            </div>
            <div class="bg-slate-800/40 rounded-xl p-4">
              <h4 class="text-purple-300 font-medium mb-2">🏺 圣遗物推荐</h4>
              <ul class="text-slate-300 text-sm space-y-1">
                <li v-for="(a, i) in artifactsOf(selected)" :key="i">{{ a }}</li>
              </ul>
            </div>
            <div class="bg-slate-800/40 rounded-xl p-4">
              <h4 class="text-sky-300 font-medium mb-2">📊 面板推荐</h4>
              <ul class="text-slate-300 text-sm space-y-1">
                <li v-for="(s, i) in statsOf(selected)" :key="i">{{ s.k }} +{{ s.v }}</li>
              </ul>
            </div>
            <div class="bg-slate-800/40 rounded-xl p-4">
              <h4 class="text-amber-300 font-medium mb-2">🌟 天赋加点</h4>
              <p class="text-slate-300 text-sm whitespace-pre-wrap">{{ selected.talents }}</p>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
