<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../api/auth'
import { listBoards } from '../../api/board'
import { uploadImage } from '../../api/model3/data'
import request from '../../api/request'
import { notifyError, notifySuccess, notifyWarning } from '../../utils/notify'
import AiPolish from '../../components/AiPolish.vue'

const router = useRouter()
const COMMUNITY_ID = 3
const boardOptions = ref<{ id: number; name: string }[]>([])
const title = ref('')
const content = ref('')
const boardId = ref(1)
const images = ref<string[]>([])
const submitting = ref(false)

onMounted(async () => {
  const user = await authApi.getUserInfo()
  if (!user) {
    notifyWarning('请先登录')
    router.push({ name: 'login', query: { redirect: '/module2/create' } })
    return
  }
  const list = await listBoards(COMMUNITY_ID)
  boardOptions.value = list
  if (list.length) boardId.value = list[0].id
})

const handleImageUpload = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const url = await uploadImage(file)
  if (url) images.value.push(url)
  else notifyError('图片上传失败')
}
const removeImage = (idx: number) => images.value.splice(idx, 1)

const submitPost = async () => {
  if (!title.value.trim()) {
    notifyWarning('请输入标题')
    return
  }
  if (!content.value.trim()) {
    notifyWarning('请输入内容')
    return
  }
  submitting.value = true
  const res = await request.post('/posts', {
    title: title.value.trim(),
    content: content.value.trim(),
    boardId: boardId.value,
    images: images.value,
  })
  submitting.value = false
  if (res.success) {
    notifySuccess('发布成功')
    router.push('/module2/forum')
  } else {
    notifyError('发布失败')
  }
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-red-950/20 to-slate-950 text-slate-100">
    <div class="max-w-2xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-slate-400 hover:text-white mb-4">← 返回</button>
      <h1 class="text-2xl font-bold mb-6 bg-gradient-to-r from-red-500 to-orange-400 bg-clip-text text-transparent">
        发表帖子
      </h1>

      <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-4">
        <div>
          <label class="block text-sm text-slate-400 mb-1">板块</label>
          <select v-model="boardId" class="w-full px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-red-500">
            <option v-for="b in boardOptions" :key="b.id" :value="b.id">{{ b.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-slate-400 mb-1">标题</label>
          <input
            v-model="title"
            maxlength="100"
            placeholder="给你的帖子起个标题"
            class="w-full px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-red-500"
          />
        </div>
        <div>
          <label class="block text-sm text-slate-400 mb-1">内容</label>
          <textarea
            v-model="content"
            rows="10"
            placeholder="分享你的攻略、配装或想法..."
            class="w-full px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-red-500 resize-none"
          ></textarea>
          <AiPolish v-model:content="content" />
        </div>
        <div>
          <label class="block text-sm text-slate-400 mb-1">配图</label>
          <div class="flex flex-wrap gap-2 mb-2">
            <div v-for="(img, i) in images" :key="i" class="relative">
              <img :src="img" class="h-20 rounded-lg" />
              <button
                @click="removeImage(i)"
                class="absolute -top-2 -right-2 w-5 h-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center"
              >×</button>
            </div>
          </div>
          <input type="file" accept="image/*" @change="handleImageUpload" class="text-sm text-slate-400" />
        </div>
        <div class="flex justify-end gap-2">
          <button @click="router.push('/module2/forum')" class="px-4 py-2 rounded-full bg-slate-800 text-slate-300">取消</button>
          <button
            @click="submitPost"
            :disabled="submitting"
            class="px-4 py-2 rounded-full bg-red-600 hover:bg-red-500 text-white font-medium transition disabled:opacity-50"
          >
            {{ submitting ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
