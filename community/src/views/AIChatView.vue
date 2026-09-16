<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { streamAI } from '../api/ai'
import type { ChatMessage } from '../api/ai'
import { ElMessage } from 'element-plus'

const router = useRouter()

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: string
}

const messages = ref<Message[]>([
  {
    id: 1,
    role: 'assistant',
    content: '你好！我是Gamemind AI助手。我可以帮你解答关于游戏、社区使用等方面的问题。有什么需要帮助的吗？',
    timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
])

const inputValue = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLDivElement>()

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const buildHistory = (): ChatMessage[] => {
  return messages.value.slice(1).map((msg) => ({
    role: msg.role,
    content: msg.content,
  }))
}

const sendMessage = async () => {
  const content = inputValue.value.trim()
  if (!content || isLoading.value) return

  // 添加用户消息
  const userMessage: Message = {
    id: Date.now(),
    role: 'user',
    content: content,
    timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  messages.value.push(userMessage)
  inputValue.value = ''
  scrollToBottom()

  // 构建对话历史（不包括第一条欢迎消息和刚添加的用户消息）
  const history = buildHistory()

  // 调用真实AI接口（流式 SSE，逐字渲染）
  isLoading.value = true
  const aiId = Date.now() + 1
  messages.value.push({
    id: aiId,
    role: 'assistant',
    content: '',
    timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })

  try {
    await streamAI(content, history, {
      onToken: (token) => {
        const aiMessage = messages.value.find((m) => m.id === aiId)
        if (aiMessage) {
          aiMessage.content += token
          scrollToBottom()
        }
      },
      onError: (message) => {
        const aiMessage = messages.value.find((m) => m.id === aiId)
        if (aiMessage && !aiMessage.content) {
          aiMessage.content = message
        } else {
          ElMessage.error(message)
        }
      },
      onDone: () => {},
    })
    const aiMessage = messages.value.find((m) => m.id === aiId)
    if (aiMessage && !aiMessage.content) {
      aiMessage.content = '抱歉，我没有理解您的问题。'
    }
  } catch (error) {
    console.error('AI请求失败:', error)
    ElMessage.error('AI请求失败，请稍后重试')
    const aiMessage = messages.value.find((m) => m.id === aiId)
    if (aiMessage && !aiMessage.content) {
      aiMessage.content = '抱歉，我遇到了一些问题，请稍后再试。'
    }
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const clearMessages = () => {
  messages.value = [
    {
      id: 1,
      role: 'assistant',
      content: '你好！我是Gamemind AI助手。我可以帮你解答关于游戏、社区使用等方面的问题。有什么需要帮助的吗？',
      timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
  ]
}

const quickQuestions = [
  '鸣人最厉害的忍术是什么？',
  '如何在论坛发帖？',
  '佐助的写轮眼有几种形态？',
  '怎么修改个人资料？'
]

const askQuickQuestion = (question: string) => {
  inputValue.value = question
  sendMessage()
}

onMounted(() => {
  scrollToBottom()
})
</script>

<template>
  <div class="max-w-5xl mx-auto py-8 px-4">
    <!-- 页面标题 -->
    <div class="text-center mb-8">
      <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg mb-4 overflow-hidden">
        <img src="/AI.jpg" alt="AI" class="w-full h-full object-cover" />
      </div>
      <h1 class="text-3xl md:text-4xl font-bold text-gray-800 dark:text-white mb-2">
        AI智能问答
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        与AI进行对话交流，获取帮助和信息
      </p>
    </div>

    <!-- 聊天容器 -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden border border-indigo-100 dark:border-indigo-900">
      <!-- 顶部栏 -->
      <div class="bg-gradient-to-r from-indigo-500 to-purple-600 px-6 py-4 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center overflow-hidden">
            <img src="/AI.jpg" alt="AI" class="w-full h-full object-cover" />
          </div>
          <div>
            <h2 class="text-white font-bold">Gamemind AI助手</h2>
            <p class="text-white/80 text-sm">在线</p>
          </div>
        </div>
        <button 
          @click="clearMessages"
          class="px-4 py-2 bg-white/20 hover:bg-white/30 text-white rounded-lg text-sm transition-colors"
        >
          清空对话
        </button>
      </div>

      <!-- 消息列表 -->
      <div 
        ref="messagesContainer"
        class="h-96 overflow-y-auto p-6 space-y-4 bg-gradient-to-br from-gray-50 to-indigo-50 dark:from-gray-900 dark:to-gray-800"
      >
        <div v-for="message in messages" :key="message.id" class="flex" :class="message.role === 'user' ? 'justify-end' : 'justify-start'">
          <div class="flex items-end space-x-3 max-w-[80%]" :class="message.role === 'user' ? 'flex-row-reverse space-x-reverse' : ''">
            <!-- 头像 -->
            <div 
              class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden"
              :class="message.role === 'user' ? 'bg-gradient-to-br from-pink-500 to-purple-600' : 'bg-gradient-to-br from-indigo-500 to-purple-600'"
            >
              <img v-if="message.role === 'assistant'" src="/AI.jpg" alt="AI" class="w-full h-full object-cover" />
              <span v-else class="text-lg">👤</span>
            </div>
            
            <!-- 消息气泡 -->
            <div class="space-y-1">
              <div 
                class="px-4 py-3 rounded-2xl shadow-sm"
                :class="message.role === 'user' 
                  ? 'bg-gradient-to-r from-pink-500 to-purple-600 text-white rounded-tr-sm' 
                  : 'bg-white dark:bg-gray-700 text-gray-800 dark:text-white rounded-tl-sm border border-indigo-100 dark:border-indigo-800'"
              >
                <p class="text-sm leading-relaxed whitespace-pre-wrap">{{ message.content || (isLoading ? '正在思考…' : '') }}</p>
              </div>
              <p class="text-xs text-gray-400 dark:text-gray-500" :class="message.role === 'user' ? 'text-right' : ''">
                {{ message.timestamp }}
              </p>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="isLoading" class="flex justify-start">
          <div class="flex items-end space-x-3">
            <div class="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center overflow-hidden">
              <img src="/AI.jpg" alt="AI" class="w-full h-full object-cover" />
            </div>
            <div class="px-4 py-3 rounded-2xl bg-white dark:bg-gray-700 rounded-tl-sm border border-indigo-100 dark:border-indigo-800 shadow-sm">
              <div class="flex space-x-1">
                <div class="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style="animation-delay: 0ms"></div>
                <div class="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style="animation-delay: 150ms"></div>
                <div class="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style="animation-delay: 300ms"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷问题 -->
      <div class="px-6 py-4 bg-gray-50 dark:bg-gray-900/50 border-t border-gray-100 dark:border-gray-700">
        <p class="text-sm text-gray-500 dark:text-gray-400 mb-3">快捷问题：</p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="(question, index) in quickQuestions"
            :key="index"
            @click="askQuickQuestion(question)"
            class="px-4 py-2 bg-white dark:bg-gray-800 text-sm text-gray-700 dark:text-gray-300 rounded-lg border border-indigo-100 dark:border-indigo-800 hover:border-indigo-300 dark:hover:border-indigo-600 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
          >
            {{ question }}
          </button>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="p-6 bg-white dark:bg-gray-800 border-t border-gray-100 dark:border-gray-700">
        <div class="flex space-x-4">
          <textarea
            v-model="inputValue"
            @keydown.enter.prevent="sendMessage"
            placeholder="输入你的问题..."
            class="flex-1 px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-600 bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
            rows="1"
          ></textarea>
          <button
            @click="sendMessage"
            :disabled="!inputValue.trim() || isLoading"
            class="px-6 py-3 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-xl font-medium hover:from-indigo-600 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-md hover:shadow-lg"
          >
            发送
          </button>
        </div>
      </div>
    </div>

    <!-- 返回首页按钮 -->
    <div class="text-center mt-8">
      <button
        @click="router.push('/')"
        class="px-6 py-3 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
      >
        ← 返回首页
      </button>
    </div>
  </div>
</template>
