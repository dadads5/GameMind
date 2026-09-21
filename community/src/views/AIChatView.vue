<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  streamAI,
  getConversations,
  getConversationMessages,
  deleteConversationApi,
} from '../api/ai'
import type { AiConversation } from '../api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: string
}

const WELCOME =
  '你好！我是Gamemind AI助手。我可以帮你解答关于游戏、社区使用等方面的问题。有什么需要帮助的吗？'

const welcomeMessage = (): Message => ({
  id: 0,
  role: 'assistant',
  content: WELCOME,
  timestamp: now(),
})

function now() {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const messages = ref<Message[]>([welcomeMessage()])
const inputValue = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLDivElement>()
const textareaRef = ref<HTMLTextAreaElement>()

// 会话
const conversations = ref<AiConversation[]>([])
const conversationId = ref<number | null>(null)
/** 生成中的中断控制器 */
let controller: AbortController | null = null

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

/** 逐 token 追加时会高频触发，用 rAF 节流避免每个 token 都触发一次 DOM 更新 */
let scrollScheduled = false
const scheduleScroll = () => {
  if (scrollScheduled) return
  scrollScheduled = true
  requestAnimationFrame(() => {
    scrollScheduled = false
    scrollToBottom()
  })
}

/** 输入框高度自适应（最高 160px） */
const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

const loadConversations = async () => {
  conversations.value = await getConversations()
}

/** 新建会话（清空当前视图，下次提问时服务端自动创建） */
const startNew = () => {
  stopGenerate()
  conversationId.value = null
  messages.value = [welcomeMessage()]
  inputValue.value = ''
  autoResize()
  scrollToBottom()
}

const selectConversation = async (id: number) => {
  if (isLoading.value) stopGenerate()
  conversationId.value = id
  const list = await getConversationMessages(id)
  messages.value = [
    welcomeMessage(),
    ...list.map((m) => ({
      id: m.id,
      role: m.role as 'user' | 'assistant',
      content: m.content,
      timestamp: new Date(m.createdAt).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      }),
    })),
  ]
  scrollToBottom()
}

const removeConversation = async (conv: AiConversation) => {
  try {
    await ElMessageBox.confirm(`确定删除会话「${conv.title || '新会话'}」吗？`, '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await deleteConversationApi(conv.id)
  if (conversationId.value === conv.id) startNew()
  await loadConversations()
  ElMessage.success('已删除')
}

/** 中断生成：已生成的内容会保留，并由服务端保存进会话 */
const stopGenerate = () => {
  if (controller) {
    controller.abort()
    controller = null
  }
  isLoading.value = false
}

const sendMessage = async () => {
  const content = inputValue.value.trim()
  if (!content || isLoading.value) return

  messages.value.push({ id: Date.now(), role: 'user', content, timestamp: now() })
  inputValue.value = ''
  autoResize()
  scrollToBottom()

  // 占位消息：直接持有引用，避免每个 token 都 find 全量数组
  const aiMessage: Message = {
    id: Date.now() + 1,
    role: 'assistant',
    content: '',
    timestamp: now(),
  }
  messages.value.push(aiMessage)

  isLoading.value = true
  controller = new AbortController()

  try {
    await streamAI(
      content,
      [], // 历史交由服务端从会话读取，减小请求体
      {
        onToken: (token) => {
          aiMessage.content += token
          scheduleScroll()
        },
        onConversation: (id) => {
          // 首个事件：服务端分配的会话 id，后续多轮沿用
          if (conversationId.value !== id) {
            conversationId.value = id
            loadConversations()
          }
        },
        onError: (message) => {
          if (!aiMessage.content) {
            aiMessage.content = message
          } else {
            ElMessage.error(message)
          }
        },
        onDone: () => {},
      },
      {
        signal: controller.signal,
        conversationId: conversationId.value ?? undefined,
      },
    )

    if (!aiMessage.content) {
      aiMessage.content = '抱歉，我没有理解您的问题。'
    }
  } catch {
    ElMessage.error('AI请求失败，请稍后重试')
    if (!aiMessage.content) {
      aiMessage.content = '抱歉，我遇到了一些问题，请稍后再试。'
    }
  } finally {
    controller = null
    isLoading.value = false
    scrollToBottom()
  }
}

const quickQuestions = [
  '鸣人最厉害的忍术是什么？',
  '如何在论坛发帖？',
  '佐助的写轮眼有几种形态？',
  '怎么修改个人资料？',
]

const askQuickQuestion = (question: string) => {
  inputValue.value = question
  sendMessage()
}

onMounted(() => {
  loadConversations()
  scrollToBottom()
})
</script>

<template>
  <div class="max-w-6xl mx-auto py-8 px-4">
    <!-- 页面标题 -->
    <div class="text-center mb-8">
      <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg mb-4 overflow-hidden">
        <img src="/AI.jpg" alt="AI" class="w-full h-full object-cover" />
      </div>
      <h1 class="text-3xl md:text-4xl font-bold text-gray-800 dark:text-white mb-2">AI智能问答</h1>
      <p class="text-gray-600 dark:text-gray-400">与AI进行对话交流，获取帮助和信息</p>
    </div>

    <div class="grid md:grid-cols-[260px_1fr] gap-6 items-start">
      <!-- 会话列表 -->
      <aside class="hidden md:block bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-indigo-100 dark:border-indigo-900 overflow-hidden">
        <div class="p-4 border-b dark:border-gray-700">
          <button
            @click="startNew"
            class="w-full px-3 py-2 rounded-lg bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-sm font-medium hover:from-indigo-600 hover:to-purple-700 transition"
          >
            ＋ 新建会话
          </button>
        </div>
        <div class="max-h-[420px] overflow-y-auto">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            @click="selectConversation(conv.id)"
            class="group px-4 py-3 flex items-center justify-between gap-2 cursor-pointer border-b dark:border-gray-700 last:border-b-0 transition"
            :class="conversationId === conv.id ? 'bg-indigo-50 dark:bg-indigo-900/30' : 'hover:bg-gray-50 dark:hover:bg-gray-700/50'"
          >
            <span class="text-sm text-gray-700 dark:text-gray-200 truncate flex-1">
              {{ conv.title || '新会话' }}
            </span>
            <button
              @click.stop="removeConversation(conv)"
              class="opacity-0 group-hover:opacity-100 text-gray-400 hover:text-red-500 transition text-sm"
              title="删除会话"
            >
              ✕
            </button>
          </div>
          <p v-if="!conversations.length" class="px-4 py-6 text-center text-sm text-gray-400">
            暂无历史会话
          </p>
        </div>
      </aside>

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
          <div class="flex items-center gap-2">
            <button
              @click="startNew"
              class="md:hidden px-3 py-2 bg-white/20 hover:bg-white/30 text-white rounded-lg text-sm transition-colors"
            >
              ＋ 新会话
            </button>
            <button
              @click="startNew"
              class="px-4 py-2 bg-white/20 hover:bg-white/30 text-white rounded-lg text-sm transition-colors"
            >
              清空对话
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          ref="messagesContainer"
          class="h-96 overflow-y-auto p-6 space-y-4 bg-gradient-to-br from-gray-50 to-indigo-50 dark:from-gray-900 dark:to-gray-800"
        >
          <div v-for="message in messages" :key="message.id" class="flex" :class="message.role === 'user' ? 'justify-end' : 'justify-start'">
            <div class="flex items-end space-x-3 max-w-[80%]" :class="message.role === 'user' ? 'flex-row-reverse space-x-reverse' : ''">
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden"
                :class="message.role === 'user' ? 'bg-gradient-to-br from-pink-500 to-purple-600' : 'bg-gradient-to-br from-indigo-500 to-purple-600'"
              >
                <img v-if="message.role === 'assistant'" src="/AI.jpg" alt="AI" class="w-full h-full object-cover" />
                <span v-else class="text-lg">👤</span>
              </div>

              <div class="space-y-1">
                <div
                  class="px-4 py-3 rounded-2xl shadow-sm"
                  :class="message.role === 'user'
                    ? 'bg-gradient-to-r from-pink-500 to-purple-600 text-white rounded-tr-sm'
                    : 'bg-white dark:bg-gray-700 text-gray-800 dark:text-white rounded-tl-sm border border-indigo-100 dark:border-indigo-800'"
                >
                  <p class="text-sm leading-relaxed whitespace-pre-wrap">{{ message.content || (isLoading ? '正在思考…' : '') }}<span
                    v-if="isLoading && message.role === 'assistant' && message.content"
                    class="inline-block w-2 h-4 ml-0.5 align-middle bg-indigo-500 animate-pulse"
                  ></span></p>
                </div>
                <p class="text-xs text-gray-400 dark:text-gray-500" :class="message.role === 'user' ? 'text-right' : ''">
                  {{ message.timestamp }}
                </p>
              </div>
            </div>
          </div>

          <!-- 加载状态：仅在尚未收到首个 token 时显示 -->
          <div v-if="isLoading && !messages[messages.length - 1]?.content" class="flex justify-start">
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
              :disabled="isLoading"
              class="px-4 py-2 bg-white dark:bg-gray-800 text-sm text-gray-700 dark:text-gray-300 rounded-lg border border-indigo-100 dark:border-indigo-800 hover:border-indigo-300 dark:hover:border-indigo-600 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors disabled:opacity-50"
            >
              {{ question }}
            </button>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="p-6 bg-white dark:bg-gray-800 border-t border-gray-100 dark:border-gray-700">
          <div class="flex space-x-4 items-end">
            <textarea
              ref="textareaRef"
              v-model="inputValue"
              @input="autoResize"
              @keydown.enter.exact.prevent="sendMessage"
              placeholder="输入你的问题…（Enter 发送，Shift + Enter 换行）"
              class="flex-1 px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-600 bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
              rows="1"
            ></textarea>

            <!-- 生成中显示停止按钮 -->
            <button
              v-if="isLoading"
              @click="stopGenerate"
              class="px-5 py-3 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-xl font-medium transition-all duration-200 shadow"
            >
              ■ 停止
            </button>
            <button
              v-else
              @click="sendMessage"
              :disabled="!inputValue.trim()"
              class="px-6 py-3 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-xl font-medium hover:from-indigo-600 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-md hover:shadow-lg"
            >
              发送
            </button>
          </div>
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
