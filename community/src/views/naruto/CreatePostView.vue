<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { uploadImage } from '../../api/naruto/data'
import { useRouter } from 'vue-router'
import request from '../../api/request'
import { authApi } from '../../api/auth'
import { listBoards } from '../../api/board'
import { notifyError, notifyWarning } from '../../utils/notify'
import AiPolish from '../../components/AiPolish.vue'

const router = useRouter()
const boardId = ref<number>(1)
const boardOptions = ref<{ id: number; name: string }[]>([])
const title = ref('')
const content = ref('')
const uploadedImages = ref<string[]>([])
const isLoading = ref(false)
const currentUser = ref<any>(null)

onMounted(() => {
  loadUserInfo()
  loadBoards()
})

// 拉取后端真实板块列表（火影社区）；失败则板块下拉为空，提示用户稍后重试，不再回退到原神 mock
const loadBoards = async () => {
  try {
    const list = await listBoards(2)
    if (list.length) {
      boardOptions.value = list
      if (!list.some((b) => b.id === boardId.value)) {
        boardId.value = list[0].id
      }
    }
  } catch (e) {
    console.error('拉取板块列表失败:', e)
  }
}

const loadUserInfo = async () => {
  try {
    const user = await authApi.getUserInfo()
    if (user) {
      currentUser.value = user
      localStorage.setItem('user', JSON.stringify(user))
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 上传配图（真实上传到后端，返回可访问 URL）
const handleImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  if (!target.files || target.files.length === 0) return
  for (const file of Array.from(target.files)) {
    const url = await uploadImage(file)
    if (url) {
      uploadedImages.value.push(url)
    } else {
      notifyError('图片上传失败，请重试')
    }
  }
  // 清空 input，允许重复选择同一文件
  target.value = ''
}

const removeImage = (index: number) => {
  uploadedImages.value.splice(index, 1)
}

// 提交帖子到后端
const submitPost = async () => {
  if (!title.value.trim() || !content.value.trim()) {
    return
  }
  
  if (!currentUser.value) {
    notifyWarning('请先登录后再发表帖子')
    router.push('/login')
    return
  }
  
  isLoading.value = true
  
  try {
    const response = await request.post('/posts', {
      title: title.value,
      content: content.value,
      boardId: boardId.value,
      images: uploadedImages.value,
    })

    if (response.success) {
      router.push('/forum')
    } else {
      notifyError(response.message || '发布失败，请稍后重试')
    }
  } catch (error) {
    console.error('创建帖子失败:', error)
    notifyError('创建帖子失败，请检查网络或稍后重试')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="max-w-6xl mx-auto">
    <div class="naruto-card">
      <h1 class="text-3xl font-bold mb-6 text-[#ff4d00] text-center flex items-center justify-center">
        <span class="mr-3">✏️</span>
        发表新帖
        <span class="ml-3">🔥</span>
      </h1>
      <form @submit.prevent="submitPost" class="space-y-6">
        <!-- 选择板块 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">选择板块</label>
          <select 
            v-model="boardId"
            class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-[#ff4d00]/50"
          >
            <option 
              v-for="board in boardOptions" 
              :key="board.id"
              :value="board.id"
            >
              {{ board.name }}
            </option>
          </select>
        </div>

        <!-- 输入标题 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">标题</label>
          <input 
            v-model="title"
            type="text"
            class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-[#ff4d00]/50"
            placeholder="请输入帖子标题"
            maxlength="100"
          />
          <div class="text-right text-xs text-gray-500 dark:text-gray-400 mt-1">{{ title.length }}/100</div>
        </div>

        <!-- 输入正文 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">正文</label>
          <textarea 
            v-model="content"
            class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-[#ff4d00]/50 resize-none"
            rows="10"
            placeholder="请输入帖子内容"
            maxlength="5000"
          ></textarea>
          <AiPolish v-model:content="content" />
          <div class="text-right text-xs text-gray-500 dark:text-gray-400 mt-1">{{ content.length }}/5000</div>
        </div>

        <!-- 上传图片 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">上传图片</label>
          <input 
            type="file"
            multiple
            class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200"
            @change="handleImageUpload"
            accept="image/*"
          />
          <div v-if="uploadedImages.length" class="mt-4 grid grid-cols-3 gap-3">
            <div 
              v-for="(img, index) in uploadedImages" 
              :key="index"
              class="relative group"
            >
              <img :src="img" alt="预览" class="w-full h-28 object-cover rounded-md border border-gray-200 dark:border-gray-700" />
              <button 
                type="button"
                @click="removeImage(index)"
                class="absolute top-1 right-1 w-6 h-6 flex items-center justify-center rounded-full bg-black/60 text-white text-sm opacity-0 group-hover:opacity-100 transition-opacity"
              >
                ×
              </button>
            </div>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="flex justify-center space-x-4">
          <button 
            type="button"
            class="px-6 py-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-600 transition-all duration-300"
            @click="router.push('/forum')"
          >
            取消
          </button>
          <button 
            type="submit"
            class="px-8 py-3 bg-gradient-to-r from-[#ff4d00] to-[#ff6b35] text-white rounded-lg font-bold shadow-lg shadow-[#ff4d00]/30 hover:shadow-xl hover:shadow-[#ff4d00]/50 transition-all duration-300 transform hover:scale-105"
            :disabled="!title.trim() || !content.trim() || isLoading"
            :class="{ 'opacity-50 cursor-not-allowed': !title.trim() || !content.trim() || isLoading }"
          >
            <span v-if="isLoading">发布中...</span>
            <span v-else>发表帖子</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
