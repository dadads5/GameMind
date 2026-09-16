<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { authApi } from '../../api/auth'
import { useRouter } from 'vue-router'

interface NewsItem {
  id: number
  title: string
  excerpt: string
  category: string
  date: string
  views: number
  image: string
}

const router = useRouter()

// 与原生站 public/model3/news.html 同源的数据与封面图（图片复用原生站静态资源）
const newsData: NewsItem[] = [
  { id: 1, title: '「月之七」版本第一期话题活动开启', excerpt: '与尼可分享旅途趣事，或用留影机为杜林推荐蒙德探索地，参与赢取原石奖励！', category: 'event', date: '2026-05-20', views: 6151, image: '/model3/images/image.png' },
  { id: 2, title: '提瓦特委托板·杂谈篇', excerpt: '留言提问游历提瓦特时遇到的困惑，或为其他旅行者答疑互助，赢取原石和周边！', category: 'event', date: '2026-05-20', views: 5067, image: '/model3/images/image (1).png' },
  { id: 3, title: '【网页活动】魔女尼可的茶会', excerpt: '网页活动限时开启，完成页面任务必得原石及「方糖」，消耗方糖参与抽奖赢福利！', category: 'event', date: '2026-05-20', views: 16827, image: '/model3/images/image (2).png' },
  { id: 4, title: '「虚空劫灰往世书」剧情讨论开启', excerpt: '完成魔神任务空月之歌第九幕与第十幕后，分享你的感受，参与必得社区装扮！', category: 'official', date: '2026-05-20', views: 3952, image: '/model3/images/image (3).png' },
  { id: 5, title: '「喧寂于心」尼可绘画征集开启', excerpt: '为无言的「魔女」、弃声的「天使」尼可绘制作品，赢取周边礼包和原石奖励！', category: 'event', date: '2026-05-11', views: 328, image: '/model3/images/image (4).png' },
  { id: 6, title: '「稚梦归心」·布伦妮登场', excerpt: '小小「魔女猎人」布伦妮登场，分享童年趣味游戏，赢取角色立牌周边！', category: 'character', date: '2026-05-16', views: 2923, image: '/model3/images/image (5).png' },
  { id: 7, title: '5月生日会话题活动开启', excerpt: '5月伙伴生日庆典开启，在评论区为伙伴送上生日祝福，赢取周边奖品！', category: 'event', date: '2026-05-01', views: 10179, image: '/model3/images/image (6).png' },
  { id: 8, title: '「月之六」版本攻略征集开启', excerpt: '「逢归的谶羽」版本攻略征集，包含角色攻略、任务探索、活动详解等专题赛道！', category: 'guide', date: '2026-04-08', views: 290, image: '/model3/images/image (7).png' },
]

const categoryMap: Record<string, { name: string; color: string }> = {
  all: { name: '全部', color: '#667eea' },
  update: { name: '版本更新', color: '#667eea' },
  event: { name: '活动预告', color: '#ff6b6b' },
  character: { name: '角色情报', color: '#ffd700' },
  guide: { name: '攻略指南', color: '#00b09b' },
  official: { name: '官方公告', color: '#764ba2' },
}

const currentFilter = ref('all')
const currentPage = ref(1)
const pageSize = 6
const currentUser = ref<any>(null)
const detailNews = ref<NewsItem | null>(null)

const filtered = computed(() =>
  currentFilter.value === 'all' ? newsData : newsData.filter((n) => n.category === currentFilter.value),
)
const paged = computed(() => filtered.value.slice(0, currentPage.value * pageSize))
const hasMore = computed(() => paged.value.length < filtered.value.length)

const filterNews = (c: string) => {
  currentFilter.value = c
  currentPage.value = 1
}
const loadMore = () => currentPage.value++
const openDetail = (n: NewsItem) => (detailNews.value = n)
const closeDetail = () => (detailNews.value = null)
const hideImg = (e: Event) => ((e.target as HTMLImageElement).style.display = 'none')

onMounted(async () => {
  const u = await authApi.getUserInfo()
  if (u) currentUser.value = u
})
</script>

<template>
  <div class="min-h-screen text-white bg-[linear-gradient(135deg,#667eea_0%,#764ba2_100%)]">
    <div class="max-w-6xl mx-auto px-5">
      <!-- 顶部：返回 + 用户 -->
      <div class="flex items-center justify-between pt-5">
        <button
          @click="router.back()"
          class="px-4 py-2 rounded-full bg-white/10 border border-white/20 hover:bg-white/20 transition"
        >
          ← 返回
        </button>
        <div v-if="currentUser" class="text-sm text-white/80">{{ currentUser.nickname || currentUser.username }}</div>
      </div>

      <!-- 头部 + 筛选 -->
      <section class="text-center pt-14 pb-10">
        <h2 class="text-4xl md:text-5xl font-extrabold mb-5 [text-shadow:0_4px_8px_rgba(0,0,0,0.3)]">原神最新资讯</h2>
        <p class="text-lg text-white/90 max-w-xl mx-auto mb-8 leading-relaxed">
          获取《原神》最新版本更新、活动预告、角色情报、攻略指南等第一手资讯！
        </p>
        <div class="flex justify-center flex-wrap gap-4">
          <button
            v-for="(m, key) in categoryMap"
            :key="key"
            @click="filterNews(key)"
            class="px-5 py-2.5 rounded-full text-sm border transition cursor-pointer"
            :class="
              currentFilter === key
                ? 'bg-[linear-gradient(45deg,#6a11cb,#2575fc)] border-transparent'
                : 'bg-white/10 border-white/20 hover:bg-white/20'
            "
          >
            {{ m.name }}
          </button>
        </div>
      </section>

      <!-- 资讯网格 -->
      <section class="grid sm:grid-cols-2 lg:grid-cols-3 gap-7 pb-4">
        <div
          v-for="n in paged"
          :key="n.id"
          @click="openDetail(n)"
          class="group bg-white/10 backdrop-blur-md border border-white/10 rounded-3xl overflow-hidden cursor-pointer transition duration-300 hover:-translate-y-2 hover:bg-white/20 hover:shadow-[0_10px_30px_rgba(0,0,0,0.2)]"
        >
          <div class="h-48 bg-[linear-gradient(45deg,#667eea,#764ba2)] overflow-hidden">
            <img
              :src="n.image"
              :alt="n.title"
              class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
              @error="hideImg"
            />
          </div>
          <div class="p-6">
            <span
              class="inline-block px-3 py-1 rounded-full text-xs text-white mb-3"
              :style="{ background: categoryMap[n.category]?.color || '#667eea' }"
            >
              {{ categoryMap[n.category]?.name || '资讯' }}
            </span>
            <h3 class="text-xl font-bold mb-2 leading-snug">{{ n.title }}</h3>
            <p class="text-white/80 text-sm leading-relaxed mb-4">{{ n.excerpt }}</p>
            <div class="flex justify-between text-xs text-white/60">
              <span>📅 {{ n.date }}</span>
              <span>👁 {{ n.views.toLocaleString() }}</span>
            </div>
          </div>
        </div>
        <div v-if="paged.length === 0" class="col-span-full text-center py-16 text-white/70 text-xl">
          暂无相关资讯
        </div>
      </section>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="text-center pb-12">
        <button
          @click="loadMore"
          class="px-10 py-3.5 rounded-full bg-[linear-gradient(45deg,#6a11cb,#2575fc)] hover:-translate-y-0.5 hover:shadow-[0_5px_15px_rgba(0,0,0,0.3)] transition"
        >
          加载更多资讯
        </button>
      </div>

      <footer class="text-center py-10 border-t border-white/10 text-white/60 text-sm">
        © 2026 提瓦特大陆资讯站 | 资讯来源于官方公告和社区整理
      </footer>
    </div>

    <!-- 资讯详情弹窗 -->
    <div
      v-if="detailNews"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      @click.self="closeDetail"
    >
      <div class="bg-white/10 backdrop-blur-xl border border-white/20 rounded-3xl w-full max-w-2xl max-h-[88vh] overflow-y-auto relative">
        <button
          @click="closeDetail"
          class="absolute top-3 right-4 text-white/70 hover:text-white text-3xl leading-none"
        >
          ×
        </button>
        <div class="h-56 bg-[linear-gradient(45deg,#667eea,#764ba2)] overflow-hidden rounded-t-3xl">
          <img :src="detailNews.image" :alt="detailNews.title" class="w-full h-full object-cover" @error="hideImg" />
        </div>
        <div class="p-7">
          <span
            class="inline-block px-3 py-1 rounded-full text-xs text-white mb-3"
            :style="{ background: categoryMap[detailNews.category]?.color || '#667eea' }"
          >
            {{ categoryMap[detailNews.category]?.name || '资讯' }}
          </span>
          <h3 class="text-2xl font-bold mb-3">{{ detailNews.title }}</h3>
          <div class="flex gap-4 text-xs text-white/60 mb-4">
            <span>📅 {{ detailNews.date }}</span>
            <span>👁 {{ detailNews.views.toLocaleString() }}</span>
          </div>
          <p class="text-white/90 leading-relaxed">{{ detailNews.excerpt }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
