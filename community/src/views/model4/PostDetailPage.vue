<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostById, getRepliesFromBackend, getAvatarUrl, createComment, type Post, type Reply } from '../../api/model3/data'
import { authApi } from '../../api/auth'
import request from '../../api/request'
import { notifyError, notifySuccess, notifyWarning, notifyConfirm } from '../../utils/notify'
import PostAdminActions from '../../components/PostAdminActions.vue'
import AiReply from '../../components/AiReply.vue'

const route = useRoute()
const router = useRouter()
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
  return {
    avatar: getAvatarUrl(p?.authorAvatar, p?.authorName),
    name: p?.authorName || '未知用户',
  }
})
const isPostAuthor = computed(
  () => post.value && currentUser.value && currentUser.value.id === post.value.authorId,
)
const isReplyAuthor = (reply: Reply) => currentUser.value && currentUser.value.id === reply.authorId

onMounted(() => {
  loadUserInfo()
  loadPost()
})

const loadUserInfo = async () => {
  try {
    const user = await authApi.getUserInfo()
    if (user) currentUser.value = user
  } catch {
    /* 未登录也可浏览 */
  }
}
const loadPost = async () => {
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
const loadReplies = async () => {
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
const likePost = async () => {
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
const likeReply = async (id: number) => {
  try {
    const res = await request.post<{ likeCount: number }>(`/comments/${id}/like`)
    if (res.success) likeCounts.value[id] = res.data?.likeCount ?? 0
  } catch {
    /* 忽略 */
  }
}
const submitReply = async () => {
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
const deletePost = async () => {
  if (!post.value) return
  if (!await notifyConfirm('确定删除这篇帖子吗？')) return
  try {
    await request.delete(`/posts/${post.value.id}`)
    notifySuccess('已删除')
    router.push('/module4/forum')
  } catch {
    notifyError('删除失败')
  }
}
const deleteComment = async (commentId: number) => {
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
  <div class="min-h-screen bg-[#0b1020] text-slate-100">
    <div class="max-w-3xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-slate-400 hover:text-amber-300 mb-4">← 返回</button>

      <div v-if="isLoading" class="text-center text-slate-400 py-20">加载中...</div>
      <div v-else-if="!post" class="text-center text-slate-400 py-20">帖子不存在或已删除</div>

      <template v-else>
        <!-- 帖子主体 -->
        <div class="bg-slate-900/60 border border-amber-500/20 rounded-2xl p-6 mb-6">
          <span class="text-xs px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300">{{ post.boardName }}</span>
          <h1 class="text-2xl font-bold mt-3 mb-4">{{ post.title }}</h1>
          <div class="flex items-center gap-3 text-sm text-slate-400 mb-4">
            <AuthorLink :author-id="post.authorId" :name="author.name" :avatar="author.avatar" size="md" />
            <span>{{ post.createdAt }}</span>
            <span>👁 {{ post.views }}</span>
          </div>
          <p class="text-slate-200 leading-relaxed whitespace-pre-wrap">{{ post.content }}</p>
          <div class="flex items-center gap-3 mt-5">
            <button
              @click="likePost"
              :class="isLiked ? 'text-red-400' : 'text-slate-400'"
              class="flex items-center gap-1 hover:text-red-400 transition"
            >❤ {{ post.likes }}</button>
            <button v-if="isPostAuthor" @click="deletePost" class="text-red-400 hover:text-red-300 text-sm">删除</button>
            <PostAdminActions :post-id="post.id" @changed="loadPost" @deleted="router.push('/module4/forum')" />
          </div>
          <AiReply :title="post.title" :content="post.content" theme="dark" />
        </div>

        <!-- 评论 -->
        <h2 class="text-lg font-semibold mb-3 text-amber-200">评论 ({{ postReplies.length }})</h2>
        <div class="space-y-3 mb-6">
          <div v-for="reply in postReplies" :key="reply.id" class="bg-slate-900/40 border border-slate-800 rounded-xl p-4">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2 text-sm">
                <AuthorLink :author-id="reply.authorId" :name="reply.authorName" :avatar="reply.authorAvatar" />
                <span class="text-slate-500">{{ reply.createdAt }}</span>
              </div>
              <div class="flex items-center gap-3 text-sm">
                <button @click="likeReply(reply.id)" class="text-slate-400 hover:text-red-400">❤ {{ likeCounts[reply.id] ?? 0 }}</button>
                <button v-if="isReplyAuthor(reply)" @click="deleteComment(reply.id)" class="text-red-400 hover:text-red-300">删除</button>
              </div>
            </div>
            <p class="text-slate-300 whitespace-pre-wrap">{{ reply.content }}</p>
          </div>
          <div v-if="postReplies.length === 0" class="text-slate-500 text-sm">暂无评论，快来抢沙发</div>
        </div>

        <!-- 回复框 -->
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-4">
          <textarea
            v-model="replyContent"
            rows="3"
            placeholder="写下你的评论..."
            class="w-full bg-slate-800/60 border border-slate-700 rounded-xl p-3 focus:outline-none focus:border-amber-500 resize-none"
          ></textarea>
          <div class="flex justify-end mt-2">
            <button
              @click="submitReply"
              class="px-5 py-2 rounded-full bg-gradient-to-r from-amber-500 to-red-600 text-white text-sm font-medium transition"
            >发表评论</button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
