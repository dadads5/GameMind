<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostsFromBackend, type Post } from '../../api/model3/data'
import { GAME_DATA } from './data'

const router = useRouter()
const COMMUNITY_ID = 3 // 杀戮尖塔2 社区 ID

// 展示站数据
const characters = GAME_DATA.characters
const cards = GAME_DATA.cards
const relics = GAME_DATA.relics
const potions = GAME_DATA.potions
const staticPosts = GAME_DATA.posts

const selectedCharId = ref(characters[0].id)
const cardFilter = ref('all')
const backendPosts = ref<Post[]>([])

const selectedChar = computed(() => characters.find((c) => c.id === selectedCharId.value)!)
const filteredCards = computed(() =>
  cardFilter.value === 'all' ? cards : cards.filter((c) => c.charId === cardFilter.value),
)

// 优先展示后端真实帖子；后端无数据时回退到静态攻略
const displayPosts = computed(() => {
  if (backendPosts.value.length) {
    return backendPosts.value.map((p) => ({
      id: p.id,
      title: p.title,
      author: p.authorName || '匿名',
      date: (p.createdAt || '').slice(0, 10),
      category: p.boardName || '社区',
      summary: (p.content || '').slice(0, 60),
      backend: true,
    }))
  }
  return staticPosts.map((p: any) => ({ ...p, backend: false }))
})

const charName = (id: string) => characters.find((c) => c.id === id)?.name || '通用'
const rarityClass = (r: string) =>
  r === 'rare' ? 'text-yellow-400' : r === 'uncommon' ? 'text-sky-400' : 'text-slate-300'

const selectChar = (id: string) => (selectedCharId.value = id)
const selectFilter = (f: string) => (cardFilter.value = f)

// 后端帖 -> 独立详情页；静态帖 -> 进入论坛列表
const goPost = (p: any) => {
  if (p.backend) router.push(`/module2/post/${p.id}`)
  else router.push('/module2/forum')
}
const goForum = () => router.push('/module2/forum')

onMounted(async () => {
  try {
    const page = await getPostsFromBackend({ communityId: COMMUNITY_ID, size: 50 })
    if (page.posts?.length) backendPosts.value = page.posts
  } catch {
    /* 静默回退静态 */
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-red-950/20 to-slate-950 text-slate-100">
    <!-- 顶部导航栏（与原生站一致） -->
    <nav class="sticky top-0 z-50 bg-slate-900/80 backdrop-blur-md border-b border-slate-800">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <span class="text-2xl font-bold bg-gradient-to-r from-red-500 to-purple-600 bg-clip-text text-transparent">
            Slay the Spire 2
          </span>
          <div class="hidden md:flex items-baseline space-x-1">
            <button
              @click="router.push('/')"
              class="px-4 py-2 rounded-full text-sm font-medium bg-gradient-to-r from-purple-600 to-red-600 text-white hover:shadow-lg hover:scale-105 transition-all mr-2"
            >
              gamemind
            </button>
            <a href="#characters" class="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-800 transition-colors">角色攻略</a>
            <a href="#cards" class="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-800 transition-colors">卡牌推荐</a>
            <a href="#relics" class="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-800 transition-colors">遗物药水</a>
            <a href="#potions" class="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-800 transition-colors">药水图鉴</a>
            <a href="#community" class="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-800 transition-colors">社区帖子</a>
            <button @click="goForum" class="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-800 transition-colors">社区论坛</button>
          </div>
        </div>
      </div>
    </nav>

    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- Hero -->
      <header class="text-center py-16">
        <h1 class="text-5xl md:text-6xl font-black mb-4 bg-gradient-to-r from-red-500 to-orange-400 bg-clip-text text-transparent">
          杀戮尖塔2
        </h1>
        <p class="text-slate-300 text-lg">构筑你的卡组，征服无尽塔楼</p>
      </header>

      <!-- 角色 -->
      <section id="characters" class="mb-16">
        <h2 class="text-2xl font-bold mb-6">角色攻略</h2>
        <div class="flex flex-wrap gap-2 mb-6">
          <button
            v-for="c in characters"
            :key="c.id"
            @click="selectChar(c.id)"
            :class="selectedCharId === c.id ? 'bg-red-600 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-2 rounded-full text-sm transition"
          >
            {{ c.name }}
          </button>
        </div>
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-8">
          <div class="flex items-center gap-3 mb-4">
            <span class="px-3 py-1 bg-slate-800 rounded-full text-xs font-bold uppercase tracking-wider text-slate-400">{{ selectedChar.title }}</span>
            <span class="text-sm font-medium" :class="selectedChar.color">难度: {{ selectedChar.difficulty }}</span>
          </div>
          <h3 class="text-3xl font-black mb-4" :class="selectedChar.color">{{ selectedChar.name }}</h3>
          <p class="text-slate-300 leading-relaxed mb-6 text-lg">{{ selectedChar.description }}</p>
          <div class="grid md:grid-cols-2 gap-6">
            <div>
              <h4 class="text-sm font-bold text-slate-500 uppercase mb-3">推荐流派</h4>
              <div class="flex flex-wrap gap-2 mb-6">
                <span v-for="d in selectedChar.recommendedDecks" :key="d" class="px-3 py-1 rounded-md bg-slate-800 border border-slate-700 text-sm text-slate-300">{{ d }}</span>
              </div>
              <h4 class="text-sm font-bold text-slate-500 uppercase mb-3">核心机制</h4>
              <div class="flex flex-wrap gap-2">
                <span v-for="m in selectedChar.mechanics" :key="m" class="px-3 py-1 rounded-md bg-slate-800 border border-slate-700 text-sm">{{ m }}</span>
              </div>
            </div>
            <div>
              <h4 class="text-sm font-bold text-slate-500 uppercase mb-3">进阶攻略</h4>
              <p class="text-slate-400 text-sm leading-relaxed bg-slate-950 p-4 rounded-lg border-l-4" :class="selectedChar.borderColor">{{ selectedChar.strategy }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 卡牌 -->
      <section id="cards" class="mb-16">
        <h2 class="text-2xl font-bold mb-6">卡牌推荐</h2>
        <div class="flex flex-wrap gap-2 mb-6">
          <button
            @click="selectFilter('all')"
            :class="cardFilter === 'all' ? 'bg-red-600 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-2 rounded-full text-sm transition"
          >
            全部卡牌
          </button>
          <button
            v-for="c in characters"
            :key="c.id"
            @click="selectFilter(c.id)"
            :class="cardFilter === c.id ? 'bg-red-600 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-2 rounded-full text-sm transition"
          >
            {{ c.name }}
          </button>
        </div>
        <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="card in filteredCards" :key="card.id" class="bg-slate-900 rounded-xl p-6 border border-slate-800 flex flex-col">
            <div class="flex justify-between items-start mb-4">
              <span class="text-xs font-bold px-2 py-0.5 rounded bg-slate-800" :class="selectedChar.color || 'text-slate-300'">{{ charName(card.charId) }}</span>
              <div class="w-8 h-8 rounded-full bg-red-600 flex items-center justify-center text-sm font-bold">⚡{{ card.cost }}</div>
            </div>
            <h3 class="text-xl font-bold mb-2" :class="rarityClass(card.rarity)">{{ card.name }}</h3>
            <div class="text-xs text-slate-500 uppercase mb-3">{{ card.type }}</div>
            <p class="text-slate-300 text-sm flex-grow mb-4 leading-relaxed">{{ card.description }}</p>
            <div class="mt-auto pt-4 border-t border-slate-800">
              <div class="text-[10px] text-slate-500 uppercase font-bold mb-1">推荐 Combo</div>
              <p class="text-xs text-slate-400 italic">{{ card.combo }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 遗物 -->
      <section id="relics" class="mb-16">
        <h2 class="text-2xl font-bold mb-6">遗物图鉴</h2>
        <div class="grid sm:grid-cols-2 gap-4">
          <div v-for="relic in relics" :key="relic.id" class="bg-slate-900/60 p-4 rounded-xl flex items-center gap-4 border border-slate-800">
            <div class="w-12 h-12 bg-slate-950 rounded-lg flex items-center justify-center text-2xl">🏺</div>
            <div class="flex-grow">
              <div class="flex items-center gap-2 mb-1">
                <h4 class="font-bold text-slate-100">{{ relic.name }}</h4>
                <span class="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-500 uppercase">{{ relic.rarity }}</span>
                <span v-if="relic.charId !== 'all'" class="text-[10px] text-red-400">{{ charName(relic.charId) }}</span>
              </div>
              <p class="text-xs text-slate-400">{{ relic.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 药水 -->
      <section id="potions" class="mb-16">
        <h2 class="text-2xl font-bold mb-6">药水图鉴</h2>
        <div class="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div v-for="potion in potions" :key="potion.id" class="p-4 rounded-xl flex items-center gap-4 bg-slate-900/50 border border-slate-800">
            <div class="text-2xl">🧪</div>
            <div>
              <h4 class="font-bold text-slate-100 text-sm">{{ potion.name }}</h4>
              <p class="text-xs text-slate-400">{{ potion.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 社区帖子（对接后端 communityId=3；点击进详情页，查看全部进论坛） -->
      <section id="community" class="mb-16">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold">社区攻略</h2>
          <button
            @click="goForum"
            class="px-4 py-2 rounded-full bg-red-600 hover:bg-red-500 text-white text-sm font-medium transition"
          >
            进入社区 ›
          </button>
        </div>
        <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="post in displayPosts"
            :key="post.id"
            @click="goPost(post)"
            class="rounded-2xl overflow-hidden flex flex-col h-full cursor-pointer bg-slate-900/60 border border-slate-800 hover:border-red-500/50 transition"
          >
            <div class="h-32 bg-slate-800 relative overflow-hidden flex items-center justify-center text-6xl opacity-20">📖</div>
            <div class="p-6 flex-grow flex flex-col">
              <span class="self-start px-2 py-1 bg-red-600 text-white text-[10px] font-bold rounded uppercase mb-3">{{ post.category }}</span>
              <h3 class="text-lg font-bold mb-3 line-clamp-2">{{ post.title }}</h3>
              <p class="text-slate-400 text-sm mb-4 line-clamp-3 flex-grow">{{ post.summary }}</p>
              <div class="flex items-center justify-between mt-auto pt-4 border-t border-slate-800">
                <span class="text-xs text-slate-500">{{ post.author }}</span>
                <span class="text-[10px] text-slate-600">{{ post.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
