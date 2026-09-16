<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHeroById, getHeroAvatar, onHeroAvatarError, roleColor, type WzHero } from '../../api/model4/data'

const route = useRoute()
const router = useRouter()
const heroId = computed(() => Number(route.params.id))
const hero = ref<WzHero | null>(null)
const isLoading = ref(true)

// 技能按换行拆分，出装按顿号拆分
const skillLines = computed(() => (hero.value?.skills || '').split('\n').filter(Boolean))
const buildList = computed(() => (hero.value?.builds || '').split('、').filter(Boolean))

onMounted(async () => {
  isLoading.value = true
  hero.value = await getHeroById(heroId.value)
  isLoading.value = false
})
</script>

<template>
  <div class="min-h-screen bg-[#0b1020] text-slate-100">
    <div class="max-w-3xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-slate-400 hover:text-amber-300 mb-4">← 返回</button>

      <div v-if="isLoading" class="text-center text-slate-400 py-20">加载中...</div>
      <div v-else-if="!hero" class="text-center text-slate-400 py-20">英雄不存在</div>

      <template v-else>
        <!-- 头部 -->
        <div class="bg-slate-900/60 border border-amber-500/20 rounded-2xl p-6 mb-5">
          <div class="flex items-center gap-5">
            <img
              :src="getHeroAvatar(hero)"
              :alt="hero.name"
              @error="onHeroAvatarError($event, hero.name)"
              class="w-24 h-24 rounded-2xl object-cover"
            />
            <div>
              <h1 class="text-3xl font-bold bg-gradient-to-r from-amber-300 to-red-500 bg-clip-text text-transparent">
                {{ hero.name }}
              </h1>
              <p class="text-slate-400">{{ hero.title }}</p>
              <div class="flex flex-wrap gap-2 mt-2">
                <span class="text-xs px-2 py-0.5 rounded-full" :class="roleColor[hero.role] || 'text-slate-300 bg-slate-700'">
                  {{ hero.role }}
                </span>
                <span class="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-300">{{ hero.lane }}</span>
                <span class="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-amber-300">难度 {{ hero.difficulty }}/10</span>
              </div>
            </div>
          </div>
          <div class="flex gap-6 mt-5 text-sm text-slate-400">
            <span>💰 {{ hero.priceGold }} 金币</span>
            <span>💎 {{ hero.priceCoupon }} 点券</span>
          </div>
        </div>

        <!-- 技能 -->
        <section class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 mb-5">
          <h2 class="text-lg font-bold text-amber-200 mb-3">技能</h2>
          <p v-for="(line, i) in skillLines" :key="i" class="text-slate-300 text-sm leading-relaxed mb-2">{{ line }}</p>
        </section>

        <!-- 推荐出装 -->
        <section class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 mb-5">
          <h2 class="text-lg font-bold text-amber-200 mb-3">推荐出装</h2>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="(item, i) in buildList"
              :key="i"
              class="px-3 py-1 rounded-lg bg-slate-800 border border-slate-700 text-sm text-slate-200"
            >{{ item }}</span>
          </div>
        </section>

        <!-- 推荐铭文 -->
        <section class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 mb-5">
          <h2 class="text-lg font-bold text-amber-200 mb-3">推荐铭文</h2>
          <p class="text-slate-300 text-sm whitespace-pre-wrap">{{ hero.runes }}</p>
        </section>

        <!-- 玩法技巧 -->
        <section class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
          <h2 class="text-lg font-bold text-amber-200 mb-3">玩法技巧</h2>
          <p class="text-slate-300 text-sm leading-relaxed whitespace-pre-wrap">{{ hero.tips }}</p>
        </section>
      </template>
    </div>
  </div>
</template>
