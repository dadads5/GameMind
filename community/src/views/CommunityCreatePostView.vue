<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listBoards, type BoardItem } from '../api/board'
import { createPost } from '../api/model3/data'
import AiPolish from '../components/AiPolish.vue'
import { notifyError, notifySuccess, notifyWarning } from '../utils/notify'

const route = useRoute()
const router = useRouter()
const communityId = Number(route.params.communityId)

const boards = ref<BoardItem[]>([])
const title = ref('')
const content = ref('')
const boardId = ref<number | undefined>(undefined)
const isSubmitting = ref(false)

onMounted(async () => {
  boards.value = await listBoards(communityId)
  if (boards.value.length) boardId.value = boards.value[0].id
})

function submit() {
  if (!title.value.trim()) {
    notifyWarning('请输入标题')
    return
  }
  if (!content.value.trim()) {
    notifyWarning('请输入内容')
    return
  }
  if (!boardId.value) {
    notifyWarning('请选择板块')
    return
  }
  isSubmitting.value = true
  createPost(title.value.trim(), content.value.trim(), boardId.value, [])
    .then((ok) => {
      if (ok) {
        notifySuccess('发布成功')
        router.push(`/community/${communityId}`)
      } else {
        notifyError('发布失败')
      }
    })
    .finally(() => {
      isSubmitting.value = false
    })
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-indigo-50/60 to-white dark:from-gray-900 dark:to-gray-900 text-gray-800 dark:text-gray-100">
    <div class="max-w-2xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-gray-500 hover:text-indigo-600 mb-4">← 返回</button>
      <h1 class="text-2xl font-bold mb-6">发表帖子</h1>

      <div class="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl p-6 space-y-4">
        <div>
          <label class="block text-sm text-gray-500 mb-1">板块</label>
          <select
            v-model="boardId"
            class="w-full px-3 py-2 rounded-lg border dark:border-gray-700 bg-white dark:bg-gray-900 focus:outline-none focus:border-indigo-500"
          >
            <option v-for="b in boards" :key="b.id" :value="b.id">{{ b.name }}</option>
            <option v-if="!boards.length" :value="undefined" disabled>暂无板块</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-gray-500 mb-1">标题</label>
          <input
            v-model="title"
            maxlength="100"
            placeholder="请输入标题"
            class="w-full px-3 py-2 rounded-lg border dark:border-gray-700 bg-white dark:bg-gray-900 focus:outline-none focus:border-indigo-500"
          />
        </div>
        <div>
          <label class="block text-sm text-gray-500 mb-1">内容</label>
          <textarea
            v-model="content"
            rows="10"
            placeholder="分享你的想法..."
            class="w-full px-3 py-2 rounded-lg border dark:border-gray-700 bg-white dark:bg-gray-900 focus:outline-none focus:border-indigo-500 resize-none"
          ></textarea>
          <AiPolish v-model:content="content" />
        </div>
        <div class="flex justify-end gap-2">
          <button @click="router.back()" class="px-4 py-2 rounded-full border dark:border-gray-700 text-gray-500">取消</button>
          <button
            @click="submit"
            :disabled="isSubmitting"
            class="px-5 py-2 rounded-full bg-gradient-to-r from-indigo-500 to-purple-600 text-white disabled:opacity-50"
          >发布</button>
        </div>
      </div>
    </div>
  </div>
</template>
