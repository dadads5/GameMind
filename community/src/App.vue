<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useThemeStore, useUserStore } from './stores'
import { useNotificationStore } from './stores/notification'
import { authApi } from './api/auth'

const themeStore = useThemeStore()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const route = useRoute()
const router = useRouter()
const isMobileMenuOpen = ref(false)
const preloadRef = ref<HTMLIFrameElement | null>(null)

const hideGlobalNavbar = computed(() => {
  return route.path === '/module2'
})

const checkLoginStatus = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    userStore.clearUser()
    return
  }

  try {
    const user = await authApi.getUserInfo()
    if (user) {
      userStore.setUser(user)
    } else {
      userStore.clearUser()
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
    userStore.clearUser()
  }
}

const handleLogout = async () => {
  await authApi.logout()
  userStore.clearUser()
  router.push('/')
}

// Token 失效（后端返回 401）时跳回登录页
const onUnauthorized = () => {
  userStore.clearUser()
  router.push('/login')
}

onMounted(() => {
  if (themeStore.darkMode) {
    document.documentElement.classList.add('dark')
  }

  checkLoginStatus()

  window.addEventListener('auth:unauthorized', onUnauthorized)

  if (!sessionStorage.getItem('module2Preloaded')) {
    setTimeout(() => {
      if (preloadRef.value) {
        preloadRef.value.src = '/model2/index.html'
        sessionStorage.setItem('module2Preloaded', 'true')
      }
    }, 50)
  }
})

// 登录状态变化时启动 / 停止未读消息轮询（顶栏红点）
watch(
  () => userStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      notificationStore.startPolling()
    } else {
      notificationStore.reset()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  notificationStore.stopPolling()
})
</script>

<template>
  <div class="min-h-screen bg-sky-200 dark:bg-dark text-gray-800 dark:text-gray-100">
    <header v-if="!hideGlobalNavbar" class="sticky top-0 z-50">
      <div class="absolute inset-0 bg-gradient-to-r from-[#4f46e5] via-[#7c3aed] to-[#4f46e5] opacity-90"></div>
      <div class="absolute inset-0 bg-gradient-to-r from-[#1e1b4b] via-[#312e81] to-[#1e1b4b] opacity-90" v-if="themeStore.darkMode"></div>
      
      <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#818cf8] via-[#a78bfa] to-[#818cf8]"></div>
      
      <div class="relative w-full px-4 py-4 flex justify-between items-center">
        <!-- 左侧：社区问答 -->
        <div class="flex items-center">
          <router-link to="/" class="flex items-center space-x-3 group">
            <div class="relative w-12 h-12">
              <div class="absolute inset-0 rounded-full bg-gradient-to-br from-[#4f46e5] to-[#7c3aed] shadow-lg shadow-[#4f46e5]/50 transition-transform duration-300 group-hover:rotate-12 group-hover:scale-110"></div>
              <div class="absolute inset-1 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center overflow-hidden">
                <img src="/mainlogo.png" alt="Gamemind" class="w-full h-full object-cover" />
              </div>
            </div>
            <div class="flex flex-col">
              <span class="text-xl md:text-2xl font-bold text-white drop-shadow-lg tracking-wider group-hover:scale-105 transition-transform duration-300">
                Gamemind-游戏社区服务平台
              </span>
              <span class="text-xs text-white/70 hidden sm:block">GAMEMIND</span>
            </div>
          </router-link>
        </div>

        <!-- 中间：导航菜单 -->
        <nav class="hidden md:flex items-center space-x-1">
          <RouterLink 
            to="/"
            class="nav-link px-4 py-2 rounded-lg hover:bg-white/20 transition-all duration-300 text-white relative group"
          >
            <span class="flex items-center">
              <span class="mr-1">🏠</span>
              首页
            </span>
            <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1 bg-[#818cf8] rounded-full transition-all duration-300 group-hover:w-full"></div>
          </RouterLink>
          <RouterLink 
            to="/forum"
            class="nav-link px-4 py-2 rounded-lg hover:bg-white/20 transition-all duration-300 text-white relative group"
          >
            <span class="flex items-center">
              <img src="/huo/logo.jpg" alt="火影" class="w-5 h-5 mr-1 rounded-full object-cover" />
              火影
            </span>
            <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1 bg-[#818cf8] rounded-full transition-all duration-300 group-hover:w-full"></div>
          </RouterLink>
          <RouterLink 
            to="/module2"
            class="nav-link px-4 py-2 rounded-lg hover:bg-white/20 transition-all duration-300 text-white relative group"
          >
            <span class="flex items-center">
              <img src="/shalu-logo.jpg" alt="杀戮尖塔" class="w-5 h-5 mr-1 rounded-full object-cover" />
              杀戮2
            </span>
            <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1 bg-[#818cf8] rounded-full transition-all duration-300 group-hover:w-full"></div>
          </RouterLink>
          <RouterLink 
            to="/module3"
            class="nav-link px-4 py-2 rounded-lg hover:bg-white/20 transition-all duration-300 text-white relative group"
          >
            <span class="flex items-center">
              <img src="/yslogo.png" alt="原神" class="w-5 h-5 mr-1 rounded-full object-cover" />
              原神
            </span>
            <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1 bg-[#818cf8] rounded-full transition-all duration-300 group-hover:w-full"></div>
          </RouterLink>
          <RouterLink 
            to="/module4"
            class="nav-link px-4 py-2 rounded-lg hover:bg-white/20 transition-all duration-300 text-white relative group"
          >
            <span class="flex items-center">
              <img src="/wzry-logo.jpg" alt="王者荣耀" class="w-5 h-5 mr-1 rounded-full object-cover" />
              王者荣耀
            </span>
            <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-1 bg-[#818cf8] rounded-full transition-all duration-300 group-hover:w-full"></div>
          </RouterLink>
        </nav>

        <!-- 右侧：主题切换、个人中心/登录注册 -->
        <div class="flex items-center space-x-3">
          <!-- 主题切换 -->
          <button 
            @click="themeStore.toggleDarkMode()"
            class="p-2 rounded-full bg-white/20 hover:bg-white/30 backdrop-blur-sm transition-all duration-300 transform hover:scale-110 shadow-lg"
            aria-label="切换主题"
          >
            <span v-if="!themeStore.darkMode" class="text-xl">🌙</span>
            <span v-else class="text-xl">☀️</span>
          </button>

          <!-- 管理员：管理后台入口 -->
          <RouterLink
            v-if="userStore.isLoggedIn && userStore.currentUser?.role === 1"
            to="/admin"
            class="inline-flex px-3 py-2 items-center space-x-1.5 bg-white/10 hover:bg-white/20 backdrop-blur-sm rounded-lg text-white font-medium transition-all duration-300 border border-white/30"
            title="管理后台"
          >
            <span>🛠️</span>
            <span class="hidden lg:inline">管理后台</span>
          </RouterLink>

          <!-- 已登录：显示个人中心 -->
          <RouterLink
            v-if="userStore.isLoggedIn"
            to="/profile"
            class="relative inline-flex px-4 py-2 flex items-center space-x-2 bg-gradient-to-r from-[#818cf8] to-[#a78bfa] hover:from-[#6366f1] hover:to-[#8b5cf6] text-white font-medium rounded-lg transition-all duration-300 transform hover:scale-105 shadow-lg"
          >
            <span>👤</span>
            <span>{{ userStore.currentUser?.nickname || userStore.currentUser?.username || '个人中心' }}</span>
            <!-- 未读消息红点 -->
            <span
              v-if="notificationStore.unreadCount > 0"
              class="absolute -top-1.5 -right-1.5 min-w-[18px] h-[18px] px-1 flex items-center justify-center text-[10px] font-bold text-white bg-red-500 rounded-full border-2 border-white shadow"
            >{{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}</span>
          </RouterLink>

          <!-- 未登录：显示登录和注册按钮 -->
          <template v-else>
            <RouterLink
              to="/login"
              class="inline-flex px-4 py-2 flex items-center space-x-2 bg-gradient-to-r from-[#818cf8] to-[#a78bfa] hover:from-[#6366f1] hover:to-[#8b5cf6] text-white font-medium rounded-lg transition-all duration-300 transform hover:scale-105 shadow-lg"
            >
              <span>👤</span>
              <span>登录</span>
            </RouterLink>

            <RouterLink
              to="/register"
              class="hidden sm:flex items-center space-x-2 px-4 py-2 bg-white/10 hover:bg-white/20 backdrop-blur-sm rounded-lg text-white font-medium transition-all duration-300 border border-white/30"
            >
              <span>📝</span>
              <span>注册</span>
            </RouterLink>

            <!-- 个人中心入口 -->
            <RouterLink
              to="/profile"
              class="p-2 rounded-full bg-white/20 hover:bg-white/30 backdrop-blur-sm transition-all duration-300 transform hover:scale-110 shadow-lg"
              title="个人中心"
            >
              <span class="text-xl">⭕</span>
            </RouterLink>
          </template>


          <!-- 移动端菜单按钮 -->
          <button 
            @click="isMobileMenuOpen = !isMobileMenuOpen"
            class="md:hidden p-2 rounded-lg bg-white/20 hover:bg-white/30 backdrop-blur-sm transition-all duration-300"
          >
            <span v-if="!isMobileMenuOpen" class="text-xl text-white">☰</span>
            <span v-else class="text-xl text-white">✕</span>
          </button>
        </div>
      </div>

      <!-- 移动端菜单 -->
      <div v-if="isMobileMenuOpen" class="md:hidden bg-gradient-to-r from-[#4f46e5] to-[#7c3aed] border-t border-white/20">
        <div class="w-full px-4 py-4">
          <!-- 菜单列表 -->
          <div class="flex flex-col space-y-2">
            <RouterLink 
              to="/" 
              class="py-3 px-4 rounded-lg hover:bg-white/20 transition-all duration-300 text-white font-medium flex items-center"
            >
              <span class="mr-2">🏠</span> 首页
            </RouterLink>
            <RouterLink 
              to="/forum" 
              class="py-3 px-4 rounded-lg hover:bg-white/20 transition-all duration-300 text-white font-medium flex items-center"
            >
              <img src="/huo/logo.jpg" alt="火影" class="w-5 h-5 mr-2 rounded-full object-cover" /> 火影忍者
            </RouterLink>
            <RouterLink 
              to="/module2" 
              class="py-3 px-4 rounded-lg hover:bg-white/20 transition-all duration-300 text-white font-medium flex items-center"
            >
              <img src="/shalu-logo.jpg" alt="杀戮尖塔" class="w-5 h-5 mr-2 rounded-full object-cover" /> 杀戮尖塔2
            </RouterLink>
            <RouterLink 
              to="/module3" 
              class="py-3 px-4 rounded-lg hover:bg-white/20 transition-all duration-300 text-white font-medium flex items-center"
            >
              <img src="/yslogo.png" alt="原神" class="w-5 h-5 mr-2 rounded-full object-cover" /> 原神
            </RouterLink>
            <RouterLink 
              to="/module4" 
              class="py-3 px-4 rounded-lg hover:bg-white/20 transition-all duration-300 text-white font-medium flex items-center"
            >
              <img src="/wzry-logo.jpg" alt="王者荣耀" class="w-5 h-5 mr-2 rounded-full object-cover" /> 王者荣耀
            </RouterLink>
            <!-- 已登录：显示个人中心和退出按钮 -->
            <template v-if="userStore.isLoggedIn">
              <RouterLink
                to="/profile"
                class="py-3 px-4 rounded-lg bg-white/20 hover:bg-white/30 transition-all duration-300 text-white font-medium flex items-center"
              >
                <span class="mr-2">👤</span> {{ userStore.currentUser?.nickname || userStore.currentUser?.username || '个人中心' }}
              </RouterLink>
              <button
                @click="handleLogout(); isMobileMenuOpen = false"
                class="py-3 px-4 rounded-lg border border-white/30 hover:bg-white/10 transition-all duration-300 text-white font-medium text-center"
              >
                <span class="mr-2">🚪</span> 退出登录
              </button>
            </template>
            
            <!-- 未登录：显示登录和注册按钮 -->
            <template v-else>
              <RouterLink
                to="/login"
                class="flex-1 py-3 px-4 rounded-lg bg-white/20 hover:bg-white/30 transition-all duration-300 text-white font-medium text-center"
              >
                <span class="mr-2">👤</span> 登录
              </RouterLink>
              <RouterLink
                to="/register"
                class="flex-1 py-3 px-4 rounded-lg border border-white/30 hover:bg-white/10 transition-all duration-300 text-white font-medium text-center"
              >
                <span class="mr-2">📝</span> 注册
              </RouterLink>
              <RouterLink
                to="/profile"
                class="py-3 px-4 rounded-lg bg-white/20 hover:bg-white/30 transition-all duration-300 text-white font-medium flex items-center"
              >
                <span class="mr-2">⭕</span> 个人中心
              </RouterLink>
            </template>
          </div>
        </div>
      </div>
    </header>

    <main class="container mx-auto px-0 py-0">
      <router-view v-slot="{ Component, route }">
        <transition name="page" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>

    <footer v-if="!hideGlobalNavbar" class="relative mt-12">
      <div class="absolute inset-0 bg-gradient-to-r from-[#4f46e5]/90 to-[#7c3aed]/90"></div>
      <div class="absolute inset-0 bg-gradient-to-r from-[#1e1b4b]/90 to-[#312e81]/90" v-if="themeStore.darkMode"></div>
      <div class="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-[#818cf8] via-[#a78bfa] to-[#818cf8]"></div>
      
      <div class="relative w-full px-4 py-8">
        <div class="flex flex-col md:flex-row justify-between items-center">
          <div class="mb-4 md:mb-0 text-center md:text-left">
            <div class="flex items-center space-x-2 justify-center md:justify-start">
              <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center overflow-hidden">
                <img src="/mainlogo.png" alt="Gamemind" class="w-full h-full object-cover" />
              </div>
              <span class="text-lg font-bold text-white">Gamemind-游戏社区服务平台</span>
            </div>
            <p class="mt-2 text-sm text-white/70">© 2026 Gamemind</p>
          </div>
          <div class="flex items-center space-x-6">
            <a href="#" class="p-2 rounded-full bg-white/20 hover:bg-white/30 transition-all duration-300 transform hover:scale-110">
              <span class="text-xl text-white">📱</span>
            </a>
            <a href="#" class="p-2 rounded-full bg-white/20 hover:bg-white/30 transition-all duration-300 transform hover:scale-110">
              <span class="text-xl text-white">📧</span>
            </a>
            <a href="#" class="p-2 rounded-full bg-white/20 hover:bg-white/30 transition-all duration-300 transform hover:scale-110">
              <span class="text-xl text-white">🐱</span>
            </a>
          </div>
        </div>
        <div class="mt-6 pt-4 border-t border-white/20 flex flex-wrap justify-center gap-4 text-sm text-white/60">
          <span>🍃 共建美好社区 🍃</span>
          <span>|</span>
          <span>✨ 知识共享，共同进步 ✨</span>
        </div>
      </div>
    </footer>

    <!-- 预加载杀戮尖塔2 -->
    <iframe 
      ref="preloadRef"
      class="hidden"
      title="preload-module2"
    ></iframe>
  </div>
</template>

<style scoped>
.nav-link {
  font-weight: 600;
  transition: all 0.3s ease;
}
</style>
