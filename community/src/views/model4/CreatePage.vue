<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../api/auth'
import { listBoards } from '../../api/board'
import { createPost } from '../../api/model3/data'
import { WZRY_COMMUNITY_ID } from '../../api/model4/data'
import { notifyError, notifySuccess, notifyWarning } from '../../utils/notify'

const router = useRouter()
const boardOptions = ref<{ id: number; name: string }[]>([])
const title = ref('')
const content = ref('')
const boardId = ref(0)
const submitting = ref(false)

onMounted(async () => {
  const user = await authApi.getUserInfo()
  if (!user) {
    notifyWarning('请先登录')
    router.push({ name: 'login', query: { redirect: '/module4/create' } })
    return
  }
  const list = await listBoards(WZRY_COMMUNITY_ID)
  boardOptions.value = list
  if (list.length) boardId.value = list[0].id
})

const submitPost = async () => {
  if (!title.value.trim() || !content.value.trim()) {
    notifyWarning('标题和内容不能为空')
    return
  }
  if (!boardId.value) {
    notifyWarning('请选择板块')
    return
  }
  submitting.value = true
  const ok = await createPost(title.value.trim(), content.value.trim(), boardId.value)
  submitting.value = false
  if (ok) {
    notifySuccess('发布成功')
    router.push('/module4/forum')
  } else {
    notifyError('发布失败')
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0b1020] text-slate-100">
    <div class="max-w-2xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-slate-400 hover:text-amber-300 mb-4">← 返回</button>
      <h1 class="text-2xl font-bold mb-6 bg-gradient-to-r from-amber-300 via-yellow-400 to-red-500 bg-clip-text text-transparent">
        发表帖子
      </h1>

      <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-4">
        <div>
          <label class="block text-sm text-slate-400 mb-1">板块</label>
          <select
            v-model="boardId"
            class="w-full px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-amber-500"
          >
            <option v-for="b in boardOptions" :key="b.id" :value="b.id">{{ b.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-slate-400 mb-1">标题</label>
          <input
            v-model="title"
            maxlength="100"
            placeholder="给你的帖子起个标题"
            class="w-full px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-amber-500"
          />
        </div>
        <div>
          <label class="block text-sm text-slate-400 mb-1">内容</label>
          <textarea
            v-model="content"
            rows="10"
            placeholder="分享你的英雄玩法、版本理解或组队信息..."
            class="w-full px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-amber-500 resize-none"
          ></textarea>
        </div>
        <div class="flex justify-end gap-2">
          <button @click="router.push('/module4/forum')" class="px-4 py-2 rounded-full bg-slate-800 text-slate-300">取消</button>
          <button
            @click="submitPost"
            :disabled="submitting"
            class="px-5 py-2 rounded-full bg-gradient-to-r from-amber-500 to-red-600 text-white font-medium disabled:opacity-50 transition"
          >
            {{ submitting ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
