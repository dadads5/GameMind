<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { carouselImages, type Post } from '../../api/naruto/data'
import { usePostList } from '../../composables/usePostList'
import { useRouter, RouterLink } from 'vue-router'

const router = useRouter()
const currentSlide = ref(0)
const { posts, isLoading, sort, totalPages, page, hasMore, keyword, fetch, changeSort, search, loadMore } = usePostList()
const autoPlayTimer = ref<number | null>(null)
const isTransitioning = ref(false)

const showVideoModal = ref(false)
const currentVideoUrl = ref('')

const openVideoModal = () => {
  const video = carouselImages[currentSlide.value]?.video
  if (video) {
    currentVideoUrl.value = video
    showVideoModal.value = true
    pauseAutoPlay()
  }
}

const closeVideoModal = () => {
  showVideoModal.value = false
  currentVideoUrl.value = ''
  resumeAutoPlay()
}

// 防抖函数
const debounce = (fn: Function, delay: number) => {
  let timeoutId: number | null = null
  return (...args: any[]) => {
    if (timeoutId) clearTimeout(timeoutId)
    timeoutId = setTimeout(() => fn(...args), delay)
  }
}

// 节流函数
const throttle = (fn: Function, limit: number) => {
  let inThrottle: boolean = false
  return (...args: any[]) => {
    if (!inThrottle) {
      fn(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 切换到下一张
const nextSlide = () => {
  if (isTransitioning.value) return
  isTransitioning.value = true
  currentSlide.value = (currentSlide.value + 1) % carouselImages.length
  setTimeout(() => {
    isTransitioning.value = false
  }, 700)
}

// 切换到上一张
const prevSlide = () => {
  if (isTransitioning.value) return
  isTransitioning.value = true
  currentSlide.value = (currentSlide.value - 1 + carouselImages.length) % carouselImages.length
  setTimeout(() => {
    isTransitioning.value = false
  }, 700)
}

// 跳转到指定幻灯片
const goToSlide = (index: number) => {
  if (isTransitioning.value || index === currentSlide.value) return
  isTransitioning.value = true
  currentSlide.value = index
  setTimeout(() => {
    isTransitioning.value = false
  }, 700)
}

// 开始自动播放
const startAutoPlay = () => {
  autoPlayTimer.value = window.setInterval(() => {
    nextSlide()
  }, 8000)
}

// 暂停自动播放
const pauseAutoPlay = () => {
  if (autoPlayTimer.value) {
    clearInterval(autoPlayTimer.value)
    autoPlayTimer.value = null
  }
}

// 恢复自动播放
const resumeAutoPlay = () => {
  if (!autoPlayTimer.value) {
    startAutoPlay()
  }
}

// 防抖的跳转
const debouncedGoToSlide = debounce(goToSlide, 300)

// 节流的下一张
const throttledNextSlide = throttle(nextSlide, 700)

// 节流的上一张
const throttledPrevSlide = throttle(prevSlide, 700)

onMounted(() => {
  startAutoPlay()
  fetch()
  
  // 页面可见性变化时控制自动播放
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  pauseAutoPlay()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

const handleVisibilityChange = () => {
  if (document.hidden) {
    pauseAutoPlay()
  } else {
    resumeAutoPlay()
  }
}

// 搜索时展示全部结果（便于查看命中项），否则只取前 4 条作为精选
const featuredPosts = computed(() => (keyword.value ? posts.value : posts.value.slice(0, 4)))

const onSearch = () => search(keyword.value.trim())
const clearSearch = () => {
  keyword.value = ''
  search('')
}

const goToPost = (id: number) => {
  router.push(`/forum/post/${id}`)
}

const goToPage = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div>
    <div class="max-w-6xl mx-auto space-y-8">
    <!-- 下载游戏入口 -->
    <div class="flex justify-center">
      <a 
        href="https://www.ldmnq.com/ldy/ldymuban/?msclkid=2e85ed5bcb8114a4b25687454ccc4284#/landing/841" 
        target="_blank" 
        rel="noopener noreferrer"
        class="group relative block overflow-hidden rounded-2xl shadow-xl bg-gradient-to-r from-[#ff4d00] via-[#ff6b35] to-[#ff4d00] bg-size-200 p-4 md:p-5 max-w-md transition-all duration-500 hover:shadow-2xl hover:shadow-[#ff4d00]/30"
        style="background-size: 200% 100%;"
        @mouseenter="(e: any) => e.target.style.backgroundPosition = '100% 0'"
        @mouseleave="(e: any) => e.target.style.backgroundPosition = '0% 0'"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <div class="relative w-12 h-12 rounded-full bg-white/20 flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
              <span class="text-2xl">🎮</span>
              <div class="absolute inset-0 rounded-full bg-white/10 animate-ping"></div>
            </div>
            <div>
              <h3 class="text-lg md:text-xl font-bold text-white">下载游戏</h3>
              <p class="text-white/80 text-sm">体验火影忍者手游</p>
            </div>
          </div>
          <div class="flex items-center space-x-2 px-4 py-2 bg-white text-[#ff4d00] rounded-xl font-bold text-sm group-hover:scale-105 group-hover:shadow-lg transition-all duration-300">
            <span>下载</span>
            <span class="text-base animate-bounce">↓</span>
          </div>
        </div>
      </a>
    </div>

    <!-- 轮播图提示 -->
    <div class="flex items-center justify-center space-x-3 text-[#ff4d00] text-sm font-medium">
      <span class="text-xl animate-pulse">🎬</span>
      <span>精选热门角色动画，点击观看</span>
      <span class="text-xl animate-pulse">🎬</span>
    </div>

    <!-- 轮播图 -->
    <div 
      class="relative overflow-hidden rounded-3xl shadow-2xl h-64 md:h-96 lg:h-[500px] group cursor-pointer"
      @mouseenter="pauseAutoPlay"
      @mouseleave="resumeAutoPlay"
      @click="openVideoModal"
    >
      <!-- 背景装饰 -->
      <div class="absolute inset-0 bg-gradient-to-br from-[#ff4d00]/5 to-[#228b22]/5"></div>
      
      <div 
        class="relative flex transition-transform duration-700 ease-out" 
        :style="{ transform: `translateX(-${currentSlide * 100}%)` }"
      >
        <div 
          v-for="item in carouselImages" 
          :key="item.id"
          class="min-w-full h-full relative"
        >
          <img 
            :src="item.url" 
            :alt="item.title"
            class="w-full h-full object-cover will-change-transform"
            loading="lazy"
          />
          <div class="absolute inset-0 bg-gradient-to-t from-black/95 via-black/40 to-transparent"></div>
          <div class="absolute inset-0 bg-gradient-to-r from-black/30 via-transparent to-transparent"></div>
          
          <!-- 内容 -->
          <div class="absolute inset-0 flex flex-col justify-end p-6 md:p-10">
            <div class="absolute top-4 left-4 flex items-center space-x-2">
              <span class="px-4 py-2 bg-gradient-to-r from-[#ff4d00] to-[#ff6b35] text-white text-sm font-bold rounded-full shadow-lg shadow-[#ff4d00]/50">
                🔥 {{ item.title }}
              </span>
            </div>
            <h3 class="text-white text-2xl md:text-4xl lg:text-5xl font-bold mb-3 drop-shadow-2xl tracking-wide">
              {{ item.title }}
            </h3>
            <p class="text-white/90 text-lg md:text-xl max-w-2xl mb-6 leading-relaxed">
              {{ item.description }}
            </p>
            <div class="flex items-center space-x-4">
              <button 
                class="group/btn px-6 py-3 bg-gradient-to-r from-[#228b22] to-[#32cd32] text-white rounded-xl font-bold transition-all duration-300 transform hover:scale-105 hover:shadow-xl hover:shadow-[#228b22]/30 flex items-center space-x-2"
              >
                <span>立即查看</span>
                <span class="group-hover/btn:translate-x-1 transition-transform">→</span>
              </button>
            </div>
          </div>
          
          <!-- 右上角图标 -->
          <div class="absolute top-4 right-4 flex items-center space-x-2 px-4 py-2 bg-black/60 backdrop-blur-md rounded-full border border-white/20">
            <span class="w-2 h-2 rounded-full bg-[#228b22] animate-pulse"></span>
            <span class="text-white text-sm font-medium">木叶电视台</span>
          </div>
        </div>
      </div>
      
      <!-- 指示器 -->
      <div class="absolute bottom-8 left-1/2 transform -translate-x-1/2 flex space-x-3">
        <button 
          v-for="(item, index) in carouselImages" 
          :key="item.id"
          @click.stop="debouncedGoToSlide(index)"
          class="relative w-4 h-4 rounded-full transition-all duration-500" 
          :class="currentSlide === index ? 'bg-[#ff4d00]' : 'bg-white/50 hover:bg-white/80'"
        >
          <span 
            v-if="currentSlide === index"
            class="absolute inset-0 rounded-full bg-[#ff4d00] animate-ping opacity-75"
          ></span>
        </button>
      </div>
      
      <!-- 左右箭头 -->
      <button 
        @click.stop="throttledPrevSlide"
        class="absolute left-4 top-1/2 transform -translate-y-1/2 w-12 h-12 rounded-full bg-black/50 hover:bg-[#ff4d00] text-white flex items-center justify-center transition-all duration-300 shadow-lg backdrop-blur-md hover:shadow-xl hover:shadow-[#ff4d00]/30 hover:scale-110"
      >
        <span class="text-xl font-bold">‹</span>
      </button>
      <button 
        @click.stop="throttledNextSlide"
        class="absolute right-4 top-1/2 transform -translate-y-1/2 w-12 h-12 rounded-full bg-black/50 hover:bg-[#ff4d00] text-white flex items-center justify-center transition-all duration-300 shadow-lg backdrop-blur-md hover:shadow-xl hover:shadow-[#ff4d00]/30 hover:scale-110"
      >
        <span class="text-xl font-bold">›</span>
      </button>
    </div>

    <!-- 功能入口卡片 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
      <RouterLink 
        to="/forum/sections"
        class="group relative board-card cursor-pointer block p-6 overflow-hidden"
      >
        <div class="relative w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-[#ff4d00] to-[#ff6b35] flex items-center justify-center shadow-lg shadow-[#ff4d00]/40 group-hover:shadow-2xl group-hover:shadow-[#ff4d00]/60 transition-all duration-500 transform group-hover:scale-115">
          <span class="text-3xl">📋</span>
          <div class="absolute inset-0 rounded-2xl bg-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
        </div>
        <h3 class="font-bold text-[#ff4d00] text-lg mb-2 text-center">板块列表</h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 text-center">浏览所有板块</p>
        <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1.5 bg-gradient-to-r from-[#ff4d00] to-[#228b22] rounded-full transition-all duration-500 group-hover:w-3/4"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-black/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
      </RouterLink>
      
      <RouterLink 
        to="/forum/hot"
        class="group relative board-card cursor-pointer block p-6 overflow-hidden"
      >
        <div class="relative w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-[#ff6347] to-[#ff4500] flex items-center justify-center shadow-lg shadow-[#ff6347]/40 group-hover:shadow-2xl group-hover:shadow-[#ff6347]/60 transition-all duration-500 transform group-hover:scale-115">
          <span class="text-3xl">🔥</span>
          <div class="absolute inset-0 rounded-2xl bg-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
        </div>
        <h3 class="font-bold text-[#ff4d00] text-lg mb-2 text-center">热门帖子</h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 text-center">最受欢迎内容</p>
        <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1.5 bg-gradient-to-r from-[#ff6347] to-[#ff4500] rounded-full transition-all duration-500 group-hover:w-3/4"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-black/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
      </RouterLink>
      
      <RouterLink 
        to="/forum/latest"
        class="group relative board-card cursor-pointer block p-6 overflow-hidden"
      >
        <div class="relative w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-[#ffd700] to-[#ffa500] flex items-center justify-center shadow-lg shadow-[#ffd700]/40 group-hover:shadow-2xl group-hover:shadow-[#ffd700]/60 transition-all duration-500 transform group-hover:scale-115">
          <span class="text-3xl">✨</span>
          <div class="absolute inset-0 rounded-2xl bg-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
        </div>
        <h3 class="font-bold text-[#ff4d00] text-lg mb-2 text-center">最新动态</h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 text-center">最新发布内容</p>
        <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1.5 bg-gradient-to-r from-[#ffd700] to-[#ffa500] rounded-full transition-all duration-500 group-hover:w-3/4"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-black/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
      </RouterLink>
      
      <RouterLink 
        to="/forum/create"
        class="group relative board-card cursor-pointer block p-6 overflow-hidden"
      >
        <div class="relative w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-[#228b22] to-[#32cd32] flex items-center justify-center shadow-lg shadow-[#228b22]/40 group-hover:shadow-2xl group-hover:shadow-[#228b22]/60 transition-all duration-500 transform group-hover:scale-115">
          <span class="text-3xl">✏️</span>
          <div class="absolute inset-0 rounded-2xl bg-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
        </div>
        <h3 class="font-bold text-[#ff4d00] text-lg mb-2 text-center">发表帖子</h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 text-center">分享你的观点</p>
        <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1.5 bg-gradient-to-r from-[#228b22] to-[#32cd32] rounded-full transition-all duration-500 group-hover:w-3/4"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-black/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
      </RouterLink>
    </div>

    <!-- 帖子推荐 -->
    <div class="naruto-card relative overflow-hidden">
      <!-- 背景装饰 -->
      <div class="absolute top-0 left-0 w-32 h-32 bg-[#ff4d00]/5 rounded-full blur-3xl"></div>
      <div class="absolute bottom-0 right-0 w-40 h-40 bg-[#228b22]/5 rounded-full blur-3xl"></div>
      
      <div class="relative flex items-center justify-between mb-6">
        <div class="flex items-center">
          <div class="relative w-4 h-8 bg-gradient-to-b from-[#228b22] to-[#32cd32] rounded-full mr-3 overflow-hidden">
            <div class="absolute inset-0 bg-white/20 animate-pulse"></div>
          </div>
          <h2 class="text-2xl md:text-3xl font-bold text-[#ff4d00] tracking-wide">🔥 精选帖子</h2>
          <div class="relative w-4 h-8 bg-gradient-to-b from-[#ff4d00] to-[#ff6b35] rounded-full ml-3 overflow-hidden">
            <div class="absolute inset-0 bg-white/20 animate-pulse"></div>
          </div>
        </div>
        <div class="flex items-center space-x-3">
          <!-- 搜索框 -->
          <div class="relative">
            <input
              v-model="keyword"
              @keyup.enter="onSearch"
              placeholder="搜索帖子..."
              class="w-36 md:w-52 pl-9 pr-8 py-2 rounded-xl border border-[#ff4d00]/20 bg-white dark:bg-slate-800 text-sm text-gray-700 dark:text-gray-200 placeholder-gray-400 focus:outline-none focus:border-[#ff4d00] transition-colors"
            />
            <span class="absolute left-3 top-1/2 -translate-y-1/2 text-[#ff4d00]/70 text-sm">🔍</span>
            <button
              v-if="keyword"
              @click.stop="clearSearch"
              class="absolute right-2 top-1/2 -translate-y-1/2 w-5 h-5 flex items-center justify-center rounded-full text-gray-400 hover:text-[#ff4d00] hover:bg-[#ff4d00]/10 transition"
            >✕</button>
          </div>
          <div class="flex items-center rounded-xl overflow-hidden border border-[#ff4d00]/20 text-sm">
            <button
              @click="changeSort('latest')"
              :class="sort === 'latest' ? 'bg-[#ff4d00] text-white' : 'text-[#ff4d00] hover:bg-[#ff4d00]/10'"
              class="px-3 py-2 font-medium transition-colors"
            >最新</button>
            <button
              @click="changeSort('hot')"
              :class="sort === 'hot' ? 'bg-[#ff4d00] text-white' : 'text-[#ff4d00] hover:bg-[#ff4d00]/10'"
              class="px-3 py-2 font-medium transition-colors"
            >最热</button>
          </div>
          <button 
            @click.stop="goToPage('/forum/latest')" 
            class="group flex items-center space-x-2 px-5 py-2.5 bg-[#ff4d00]/10 hover:bg-[#ff4d00]/20 text-[#ff4d00] rounded-xl font-medium transition-all duration-300 transform hover:scale-105"
          >
            <span>查看更多</span>
            <span class="group-hover:translate-x-1 transition-transform">→</span>
          </button>
        </div>
      </div>
      
      <div v-if="isLoading" class="flex justify-center items-center py-16">
        <div class="relative">
          <div class="animate-spin rounded-full h-14 w-14 border-4 border-[#ff4d00]/20 border-t-[#ff4d00]"></div>
          <div class="absolute inset-0 animate-spin rounded-full h-14 w-14 border-4 border-[#228b22]/20 border-t-[#228b22] animation-delay-200"></div>
        </div>
      </div>
      
      <div v-else-if="posts.length === 0" class="text-center text-gray-500 py-16">
        没有找到相关帖子，换个关键词试试
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <button 
          v-for="post in featuredPosts" 
          :key="post.id"
          @click.stop="goToPost(post.id)"
          class="group relative p-6 rounded-2xl bg-gradient-to-br from-white to-[#fff5ee] hover:from-[#fff5ee] hover:to-white border-2 border-[#ff4d00]/10 hover:border-[#ff4d00]/40 cursor-pointer transition-all duration-500 transform hover:-translate-y-3 hover:shadow-2xl text-left w-full overflow-hidden"
        >
          <!-- 顶部渐变条 -->
          <div class="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-[#ff4d00] via-[#ff9900] to-[#ff4d00] opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
          
          <!-- 装饰角 -->
          <div class="absolute top-0 right-0 w-16 h-16 bg-[#ff4d00]/5 rounded-bl-full opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          
          <div class="relative flex items-start justify-between mb-4">
            <span class="px-3 py-1.5 bg-gradient-to-r from-[#ff4d00]/10 to-[#ff6b35]/10 text-[#ff4d00] text-xs font-bold rounded-lg border border-[#ff4d00]/20">
              {{ post.boardName }}
            </span>
            <span v-if="post.isHot" class="px-3 py-1.5 bg-gradient-to-r from-[#ff6347] to-[#ff4500] text-white text-xs font-bold rounded-lg animate-pulse shadow-lg shadow-[#ff6347]/30">
              🔥 HOT
            </span>
          </div>
          
          <h3 class="font-bold text-xl text-gray-800 dark:text-white mb-3 line-clamp-1 group-hover:text-[#ff4d00] transition-colors duration-300">
            {{ post.title }}
          </h3>
          
          <p class="text-gray-600 dark:text-gray-400 text-sm mb-5 line-clamp-2 leading-relaxed">{{ post.content }}</p>
          
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <AuthorLink :author-id="post.authorId" :name="post.authorName" :avatar="post.authorAvatar" size="lg" />
              <span class="text-xs text-gray-500">{{ post.createdAt }}</span>
            </div>
            
            <div class="flex items-center space-x-5 text-sm text-gray-500">
              <span class="group-hover:text-[#ff4d00] transition-colors flex items-center space-x-1">
                <span class="text-base">👁️</span>
                <span>{{ post.views.toLocaleString() }}</span>
              </span>
              <span class="group-hover:text-[#ff4d00] transition-colors flex items-center space-x-1">
                <span class="text-base">💬</span>
                <span>{{ post.replies }}</span>
              </span>
              <span class="group-hover:text-[#ff4d00] transition-colors flex items-center space-x-1">
                <span class="text-base">❤️</span>
                <span>{{ post.likes }}</span>
              </span>
            </div>
          </div>
        </button>
      </div>

      <!-- 加载更多 -->
      <div v-if="!isLoading && posts.length > 0" class="flex justify-center mt-6">
        <button
          v-if="hasMore()"
          @click="loadMore"
          :disabled="isLoading"
          class="px-6 py-2.5 bg-[#ff4d00]/10 hover:bg-[#ff4d00]/20 text-[#ff4d00] rounded-xl font-medium transition-all duration-300"
        >
          {{ isLoading ? '加载中...' : '加载更多' }}
        </button>
        <span v-else class="text-sm text-gray-400">已经到底啦 ~</span>
      </div>
    </div>

    <!-- 火影名言 -->
    <div class="relative rounded-3xl overflow-hidden">
      <!-- 背景 -->
      <div class="absolute inset-0 bg-gradient-to-r from-[#ff4d00]/15 via-[#228b22]/10 to-[#ff4d00]/15"></div>
      <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_50%,_rgba(255,77,0,0.1)_0%,_transparent_50%)]"></div>
      <div class="absolute inset-0 bg-[radial-gradient(circle_at_70%_50%,_rgba(34,139,34,0.1)_0%,_transparent_50%)]"></div>
      
      <!-- 装饰叶子 -->
      <div class="absolute top-4 left-8 text-4xl opacity-30 animate-float">🍃</div>
      <div class="absolute bottom-4 right-8 text-4xl opacity-30 animate-float animation-delay-300">🍃</div>
      <div class="absolute top-1/2 left-4 text-3xl opacity-20 animate-float animation-delay-500">🍃</div>
      <div class="absolute top-1/4 right-4 text-3xl opacity-20 animate-float animation-delay-700">🍃</div>
      
      <div class="relative p-8 md:p-12 text-center">
        <div class="text-6xl mb-6 animate-bounce">🍃</div>
        <blockquote class="text-xl md:text-2xl lg:text-3xl font-medium text-gray-700 dark:text-gray-300 italic leading-relaxed max-w-3xl mx-auto">
          "木叶飞舞之处，火亦生生不息。"
        </blockquote>
        <p class="mt-6 text-[#ff4d00] font-bold text-lg">— 第三代火影 · 猿飞日斩</p>
        
        <!-- 装饰线 -->
        <div class="mt-8 flex items-center justify-center space-x-4">
          <div class="w-20 h-0.5 bg-gradient-to-r from-transparent via-[#ff4d00] to-transparent"></div>
          <div class="w-3 h-3 rotate-45 bg-[#ff4d00]/30"></div>
          <div class="w-20 h-0.5 bg-gradient-to-r from-transparent via-[#228b22] to-transparent"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- 视频弹窗 -->
  <Teleport to="body">
    <Transition name="modal">
      <div 
        v-if="showVideoModal" 
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-md"
        @click.self="closeVideoModal"
      >
        <div class="relative w-full max-w-4xl mx-4 bg-gray-900 rounded-3xl overflow-hidden shadow-2xl transform transition-all duration-300">
          <button 
            @click="closeVideoModal"
            class="absolute top-4 right-4 z-10 w-12 h-12 flex items-center justify-center bg-black/60 hover:bg-black/80 text-white rounded-full transition-all duration-300 hover:scale-110 hover:rotate-90"
          >
            <span class="text-xl font-bold">✕</span>
          </button>
          <video 
            :src="currentVideoUrl" 
            controls 
            autoplay
            class="w-full h-auto max-h-[80vh]"
            @error="closeVideoModal"
          >
            您的浏览器不支持视频播放
          </video>
        </div>
      </div>
    </Transition>
  </Teleport>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from > div,
.modal-leave-to > div {
  transform: scale(0.9);
}

/* 浮动动画 */
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

/* 动画延迟 */
.animation-delay-200 {
  animation-delay: 0.2s;
}

.animation-delay-300 {
  animation-delay: 0.3s;
}

.animation-delay-500 {
  animation-delay: 0.5s;
}

.animation-delay-700 {
  animation-delay: 0.7s;
}

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}

/* 优化动画性能 */
.will-change-transform {
  will-change: transform;
}

/* 背景渐变动画 */
.bg-size-200 {
  background-size: 200% 100%;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 77, 0, 0.1);
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #ff4d00, #ff9900);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #cc3d00, #ff4d00);
}
</style>
