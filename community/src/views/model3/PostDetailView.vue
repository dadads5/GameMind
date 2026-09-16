<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getPostById,
  getRepliesFromBackend,
  getAvatarUrl,
  updatePost,
  uploadImage,
  type Post,
  type Reply,
} from '../../api/model3/data'
import { listBoards } from '../../api/board'
import request from '../../api/request'
import { authApi } from '../../api/auth'
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
const boardOptions = ref<{ id: number; name: string }[]>([])

const isLiked = ref(false)
const isEditing = ref(false)
const editForm = ref({ title: '', content: '', boardId: 1, images: [] as string[] })
const replyContent = ref('')
const likeCounts = ref<Record<number, number>>({})

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
  loadBoardOptions()
})

const loadBoardOptions = async () => {
  try {
    const list = await listBoards(1)
    if (list.length) boardOptions.value = list
  } catch {
    /* 忽略 */
  }
}
const loadUserInfo = async () => {
  try {
    const user = await authApi.getUserInfo()
    if (user) currentUser.value = user
  } catch {
    /* 忽略 */
  }
}
const loadPost = async () => {
  isLoading.value = true
  try {
    post.value = await getPostById(postId.value)
    if (post.value) {
      isLiked.value = post.value.liked ?? false
      editForm.value.boardId = post.value.boardId
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
    const response = await request.post<{ liked: boolean; likeCount: number }>(`/posts/${post.value.id}/like`)
    if (response.success && response.data) {
      post.value.likes = response.data.likeCount
      isLiked.value = response.data.liked
    }
  } catch {
    /* 忽略 */
  }
}
const likeReply = async (replyId: number) => {
  try {
    const response = await request.post<{ likeCount: number }>(`/comments/${replyId}/like`)
    if (response.success) likeCounts.value[replyId] = response.data?.likeCount ?? 0
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
  try {
    const res = await request.post('/comments', { postId: postId.value, content: replyContent.value.trim() })
    if (res.success) {
      replyContent.value = ''
      await loadReplies()
      notifySuccess('评论成功')
    } else {
      notifyError('评论失败')
    }
  } catch {
    notifyError('评论失败')
  }
}
const deletePost = async () => {
  if (!post.value) return
  if (!await notifyConfirm('确定删除这篇帖子吗？')) return
  try {
    await request.delete(`/posts/${post.value.id}`)
    notifySuccess('已删除')
    router.push('/module3/forum')
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
const openEdit = () => {
  if (!post.value) return
  editForm.value = {
    title: post.value.title,
    content: post.value.content,
    boardId: post.value.boardId,
    images: post.value.images ?? [],
  }
  isEditing.value = true
}
const submitEdit = async () => {
  if (!post.value) return
  if (!editForm.value.title.trim() || !editForm.value.content.trim()) {
    notifyWarning('标题和内容不能为空')
    return
  }
  const ok = await updatePost(
    post.value.id,
    editForm.value.title.trim(),
    editForm.value.content.trim(),
    editForm.value.boardId,
    editForm.value.images,
  )
  if (ok) {
    isEditing.value = false
    notifySuccess('已更新')
    await loadPost()
  } else {
    notifyError('更新失败')
  }
}
const handleEditImageUpload = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const url = await uploadImage(file)
  if (url) editForm.value.images.push(url)
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-indigo-950/30 to-slate-950 text-slate-100">
    <div class="max-w-3xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-slate-400 hover:text-white mb-4">← 返回</button>

      <div v-if="isLoading" class="text-center text-slate-400 py-20">加载中...</div>
      <div v-else-if="!post" class="text-center text-slate-400 py-20">帖子不存在或已删除</div>
      <template v-else>
        <!-- 帖子主体 -->
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 mb-6">
          <div class="flex items-center gap-2 mb-3">
            <span class="text-xs px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300">{{ post.boardName }}</span>
          </div>
          <h1 class="text-2xl font-bold mb-4">{{ post.title }}</h1>
          <div class="flex items-center gap-3 text-sm text-slate-400 mb-4">
            <AuthorLink :author-id="post.authorId" :name="author.name" :avatar="author.avatar" size="md" />
            <span>{{ post.createdAt }}</span>
            <span>👁 {{ post.views }}</span>
          </div>
          <div v-if="post.images?.length" class="flex flex-wrap gap-2 mb-4">
            <img v-for="(img, i) in post.images" :key="i" :src="img" class="max-h-60 rounded-xl" />
          </div>
          <p class="text-slate-200 leading-relaxed whitespace-pre-wrap">{{ post.content }}</p>
          <div class="flex items-center gap-3 mt-5">
            <button
              @click="likePost"
              :class="isLiked ? 'text-pink-400' : 'text-slate-400'"
              class="flex items-center gap-1 hover:text-pink-400 transition"
            >
              ❤ {{ post.likes }}
            </button>
            <button v-if="isPostAuthor" @click="openEdit" class="text-slate-400 hover:text-white text-sm">编辑</button>
            <button v-if="isPostAuthor" @click="deletePost" class="text-red-400 hover:text-red-300 text-sm">删除</button>
            <PostAdminActions :post-id="post.id" @changed="loadPost" @deleted="router.push('/module3/forum')" />
          </div>
          <AiReply :title="post.title" :content="post.content" theme="dark" />
        </div>

        <!-- 评论 -->
        <h2 class="text-lg font-semibold mb-3">评论 ({{ postReplies.length }})</h2>
        <div class="space-y-3 mb-6">
          <div
            v-for="reply in postReplies"
            :key="reply.id"
            class="bg-slate-900/40 border border-slate-800 rounded-xl p-4"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2 text-sm">
                <AuthorLink :author-id="reply.authorId" :name="reply.authorName" :avatar="reply.authorAvatar" />
                <span class="text-slate-500">{{ reply.createdAt }}</span>
              </div>
              <div class="flex items-center gap-3 text-sm">
                <button @click="likeReply(reply.id)" class="text-slate-400 hover:text-pink-400">❤ {{ likeCounts[reply.id] ?? 0 }}</button>
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
            class="w-full bg-slate-800/60 border border-slate-700 rounded-xl p-3 focus:outline-none focus:border-indigo-500 resize-none"
          ></textarea>
          <div class="flex justify-end mt-2">
            <button
              @click="submitReply"
              class="px-4 py-2 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white text-sm font-medium hover:shadow-lg hover:shadow-indigo-500/30 transition"
            >
              发表评论
            </button>
          </div>
        </div>
      </template>

      <!-- 编辑弹窗 -->
      <div
        v-if="isEditing"
        class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 p-4"
        @click.self="isEditing = false"
      >
        <div class="bg-slate-900 border border-slate-700 rounded-2xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
          <h3 class="text-lg font-bold mb-4">编辑帖子</h3>
          <input
            v-model="editForm.title"
            class="w-full mb-3 px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-indigo-500"
            placeholder="标题"
          />
          <select v-model="editForm.boardId" class="w-full mb-3 px-4 py-2 rounded-xl bg-slate-800 border border-slate-700">
            <option v-for="b in boardOptions" :key="b.id" :value="b.id">{{ b.name }}</option>
          </select>
          <textarea
            v-model="editForm.content"
            rows="8"
            class="w-full mb-3 px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 focus:outline-none focus:border-indigo-500 resize-none"
          ></textarea>
          <div class="flex justify-end gap-2">
            <button @click="isEditing = false" class="px-4 py-2 rounded-full bg-slate-800 text-slate-300">取消</button>
            <button
              @click="submitEdit"
              class="px-4 py-2 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white"
            >
              保存
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
