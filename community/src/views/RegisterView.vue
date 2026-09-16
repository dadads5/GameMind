<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/auth'

const router = useRouter()

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isLoading = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    const response = await authApi.register({
      username: form.value.username,
      email: form.value.email,
      password: form.value.password
    })
    
    if (response.success) {
      // 注册成功后自动登录
      const loginResponse = await authApi.login({
        username: form.value.username,
        password: form.value.password
      })
      
      if (loginResponse.success) {
        // 保存token
        localStorage.setItem('token', loginResponse.data.token)
        
        // 保存用户信息
        if (loginResponse.data.user) {
          localStorage.setItem('user', JSON.stringify(loginResponse.data.user))
        }
        
        // 跳转到首页
        router.push('/')
      }
    } else {
      errorMessage.value = response.message || '注册失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
    console.error('注册错误:', error)
  } finally {
    isLoading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
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
          创建账号
        </h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
          加入 Gamemind-游戏社区服务平台
        </p>
      </div>

      <!-- 注册表单 -->
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

          <!-- 邮箱 -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              邮箱
            </label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              required
              class="appearance-none rounded-xl relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-[#818cf8] focus:border-transparent transition-all duration-200"
              placeholder="请输入邮箱"
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

          <!-- 确认密码 -->
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              确认密码
            </label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              type="password"
              required
              class="appearance-none rounded-xl relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-[#818cf8] focus:border-transparent transition-all duration-200"
              placeholder="请再次输入密码"
            />
          </div>
        </div>

        <!-- 注册按钮 -->
        <div>
          <button
            type="submit"
            :disabled="isLoading"
            class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-xl text-white bg-gradient-to-r from-[#4f46e5] to-[#7c3aed] hover:from-[#4338ca] hover:to-[#6d28d9] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#818cf8] transition-all duration-200 transform hover:scale-[1.02] shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="isLoading">注册中...</span>
            <span v-else>注册</span>
          </button>
        </div>

        <!-- 跳转登录 -->
        <div class="text-center">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            已有账号？
            <button
              type="button"
              @click="goToLogin"
              class="font-medium text-[#4f46e5] hover:text-[#7c3aed] transition-colors duration-200"
            >
              立即登录
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
