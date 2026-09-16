<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '../api/auth'
import { useUserStore } from '../stores'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: ''
})

const isLoading = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    const response = await authApi.login({
      username: form.value.username,
      password: form.value.password
    })
    
    if (response.success) {
      // 保存token到localStorage
      localStorage.setItem('token', response.data.token)
      
      // 更新Pinia store中的用户状态
      if (response.data.user) {
        userStore.setUser(response.data.user)
        localStorage.setItem('user', JSON.stringify(response.data.user))
      }
      
      // 跳转到首页或重定向页面
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } else {
      errorMessage.value = response.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
    console.error('登录错误:', error)
  } finally {
    isLoading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-50 to-blue-50 dark:from-gray-900 dark:to-gray-800 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Logo区域 -->
      <div class="text-center">
        <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gradient-to-br from-[#4f46e5] to-[#7c3aed] shadow-lg mb-4 overflow-hidden">
          <img src="/mainlogo.png" alt="Gamemind" class="w-full h-full object-cover" />
        </div>
        <h2 class="text-3xl font-extrabold text-gray-900 dark:text-white">
          欢迎回来
        </h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
          登录 Gamemind-游戏社区服务平台
        </p>
      </div>

      <!-- 登录表单 -->
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <!-- 错误提示 -->
        <div v-if="errorMessage" class="bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-xl p-4 text-red-600 dark:text-red-400">
          {{ errorMessage }}
        </div>
        
        <div class="space-y-4">
          <!-- 用户名 -->
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              用户名
            </label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              required
              class="appearance-none rounded-xl relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-[#818cf8] focus:border-transparent transition-all duration-200"
              placeholder="请输入用户名"
            />
          </div>

          <!-- 密码 -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              密码
            </label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              required
              class="appearance-none rounded-xl relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-[#818cf8] focus:border-transparent transition-all duration-200"
              placeholder="请输入密码"
            />
          </div>
        </div>

        <!-- 登录按钮 -->
        <div>
          <button
            type="submit"
            :disabled="isLoading"
            class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-xl text-white bg-gradient-to-r from-[#4f46e5] to-[#7c3aed] hover:from-[#4338ca] hover:to-[#6d28d9] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#818cf8] transition-all duration-200 transform hover:scale-[1.02] shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="isLoading">登录中...</span>
            <span v-else>登录</span>
          </button>
        </div>

        <!-- 跳转注册 -->
        <div class="text-center">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            还没有账号？
            <button
              type="button"
              @click="goToRegister"
              class="font-medium text-[#4f46e5] hover:text-[#7c3aed] transition-colors duration-200"
            >
              立即注册
            </button>
          </p>
        </div>

        <!-- 返回首页 -->
        <div class="text-center">
          <a
            href="/"
            class="text-sm text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 transition-colors duration-200"
          >
            ← 返回首页
          </a>
        </div>
      </form>
    </div>
  </div>
</template>
