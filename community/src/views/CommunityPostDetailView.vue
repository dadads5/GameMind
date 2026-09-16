<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getPostById,
  getRepliesFromBackend,
  getAvatarUrl,
  onAvatarError,
  createComment,
  type Post,
  type Reply,
} from '../api/model3/data'
import { authApi } from '../api/auth'
import request from '../api/request'
import { notifyError, notifySuccess, notifyWarning, notifyConfirm } from '../utils/notify'
import PostAdminActions from '../components/PostAdminActions.vue'
import AiReply from '../components/AiReply.vue'

const route = useRoute()
const router = useRouter()
const communityId = computed(() => Number(route.params.communityId))
const postId = computed(() => Number(route.params.id))
const post = ref<Post | null>(null)
const isLoading = ref(true)
const postReplies = ref<Reply[]>([])
const currentUser = ref<any>(null)
const isLiked = ref(false)
const likeCounts = ref<Record<number, number>>({})
const replyContent = ref('')

const author = computed(() => {
  const p = post.value
  return { avatar: getAvatarUrl(p?.authorAvatar, p?.authorName), name: p?.authorName || '未知用户' }
})
const isPostAuthor = computed(
  () => post.value && currentUser.value && currentUser.value.id === post.value.authorId,
)
const isReplyAuthor = (reply: Reply) => currentUser.value && currentUser.value.id === reply.authorId

function back() {
  // 有站内上一页则真正后退（不新增历史记录），
  // 否则（如直接粘贴链接打开）兜底跳回社区列表页
  if (window.history.state?.back) {
    router.back()
  } else {
    router.push(`/community/${communityId.value}`)
  }
}

onMounted(() => {
  loadUserInfo()
  loadPost()
})

async function loadUserInfo() {
  // 未登录时不要请求 /auth/me：它会返回 401，游客将被全局拦截器弹到登录页，无法浏览公开内容
  if (!localStorage.getItem('token')) {
    return
  }
  try {
    const user = await authApi.getUserInfo()
    if (user) currentUser.value = user
  } catch {
    /* 登录态失效也可浏览 */
  }
}
async function loadPost() {
  isLoading.value = true
  try {
    post.value = await getPostById(postId.value)
    if (post.value) {
      isLiked.value = post.value.liked ?? false
      await loadReplies()
    }
  } catch {
    post.value = null
  } finally {
    isLoading.value = false
  }
}
async function loadReplies() {
  try {
    postReplies.value = await getRepliesFromBackend(postId.value)
    // 用后端返回的点赞数初始化，否则未点赞过的评论会一直显示 0
    postReplies.value.forEach((reply) => {
      likeCounts.value[reply.id] = reply.likeCount ?? 0
    })
  } catch {
    /* 忽略 */
  }
}
async function likePost() {
  if (!post.value) return
  try {
    const res = await request.post<{ liked: boolean; likeCount: number }>(`/posts/${post.value.id}/like`)
    if (res.success && res.data) {
      post.value.likes = res.data.likeCount
      isLiked.value = res.data.liked
    }
  } catch {
    /* 忽略 */
  }
}
async function likeReply(id: number) {
  try {
    const res = await request.post<{ likeCount: number }>(`/comments/${id}/like`)
    if (res.success) likeCounts.value[id] = res.data?.likeCount ?? 0
  } catch {
    /* 忽略 */
  }
}
async function submitReply() {
  if (!currentUser.value) {
    notifyWarning('请先登录')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (!replyContent.value.trim()) {
    notifyWarning('请输入评论内容')
    return
  }
  const ok = await createComment(postId.value, replyContent.value.trim())
  if (ok) {
    replyContent.value = ''
    await loadReplies()
    notifySuccess('评论成功')
  } else {
    notifyError('评论失败')
  }
}
async function deletePost() {
  if (!post.value) return
  if (!await notifyConfirm('确定删除这篇帖子吗？')) return
  try {
    await request.delete(`/posts/${post.value.id}`)
    notifySuccess('已删除')
    back()
  } catch {
    notifyError('删除失败')
  }
}
async function deleteComment(commentId: number) {
  if (!await notifyConfirm('确定删除这条评论吗？')) return
  try {
    await request.delete(`/comments/${commentId}`)
    await loadReplies()
    notifySuccess('已删除')
  } catch {
    notifyError('删除失败')
  }
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-indigo-50/60 to-white dark:from-gray-900 dark:to-gray-900 text-gray-800 dark:text-gray-100">
    <div class="max-w-3xl mx-auto px-4 py-8">
      <button @click="back" class="text-gray-500 hover:text-indigo-600 mb-4">← 返回</button>

      <div v-if="isLoading" class="text-center text-gray-500 py-20">加载中...</div>
      <div v-else-if="!post" class="text-center text-gray-500 py-20">帖子不存在或已删除</div>

      <template v-else>
        <div class="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl p-6 mb-6">
          <span class="text-xs px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-300">{{ post.boardName }}</span>
          <h1 class="text-2xl font-bold mt-3 mb-4">{{ post.title }}</h1>
          <div class="flex items-center gap-3 text-sm text-gray-400 mb-4">
            <router-link :to="`/user/${post.authorId}`" class="flex items-center gap-3 group">
              <img :src="author.avatar" @error="onAvatarError($event, author.name)" class="w-8 h-8 rounded-full" />
              <span class="group-hover:text-indigo-600">{{ author.name }}</span>
            </router-link>
            <span>{{ post.createdAt }}</span>
            <span>👁 {{ post.views }}</span>
          </div>
          <p class="text-gray-700 dark:text-gray-200 leading-relaxed whitespace-pre-wrap">{{ post.content }}</p>
          <div class="flex items-center gap-3 mt-5">
            <button
              @click="likePost"
              :class="isLiked ? 'text-red-500' : 'text-gray-400'"
              class="flex items-center gap-1 hover:text-red-500 transition"
            >❤ {{ post.likes }}</button>
            <button v-if="isPostAuthor" @click="deletePost" class="text-red-500 hover:text-red-400 text-sm">删除</button>
            <PostAdminActions :post-id="post.id" @changed="loadPost" @deleted="back" />
          </div>
          <AiReply :title="post.title" :content="post.content" theme="light" />
        </div>

        <h2 class="text-lg font-semibold mb-3 text-indigo-600 dark:text-indigo-300">评论 ({{ postReplies.length }})</h2>
        <div class="space-y-3 mb-6">
          <div v-for="reply in postReplies" :key="reply.id" class="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-xl p-4">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2 text-sm">
                <AuthorLink :author-id="reply.authorId" :name="reply.authorName" :avatar="reply.authorAvatar" />
                <span class="text-gray-500">{{ reply.createdAt }}</span>
              </div>
              <div class="flex items-center gap-3 text-sm">
                <button @click="likeReply(reply.id)" class="text-gray-400 hover:text-red-500">❤ {{ likeCounts[reply.id] ?? 0 }}</button>
                <button v-if="isReplyAuthor(reply)" @click="deleteComment(reply.id)" class="text-red-500 hover:text-red-400">删除</button>
              </div>
            </div>
            <p class="text-gray-600 dark:text-gray-300 whitespace-pre-wrap">{{ reply.content }}</p>
          </div>
          <div v-if="postReplies.length === 0" class="text-gray-500 text-sm">暂无评论，快来抢沙发</div>
        </div>

        <div class="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl p-4">
          <textarea
            v-model="replyContent"
            rows="3"
            placeholder="写下你的评论..."
            class="w-full bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl p-3 focus:outline-none focus:border-indigo-500 resize-none"
          ></textarea>
          <div class="flex justify-end mt-2">
            <button
              @click="submitReply"
              class="px-5 py-2 rounded-full bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-sm font-medium transition"
            >发表评论</button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
