<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostById, getRepliesFromBackend, getAvatarUrl, updatePost, uploadImage, type Post, type Reply } from '../../api/naruto/data'
import { listBoards } from '../../api/board'
import request from '../../api/request'
import { authApi } from '../../api/auth'
import PostAdminActions from '../../components/PostAdminActions.vue'
import AiReply from '../../components/AiReply.vue'
import { notifyError, notifySuccess, notifyWarning, notifyConfirm } from '../../utils/notify'

const route = useRoute()
const router = useRouter()
const postId = computed(() => Number(route.params.id))
const post = ref<Post | null>(null)
const isLoading = ref(true)
const postReplies = ref<Reply[]>([])
const currentUser = ref<any>(null)
const boardOptions = ref<{ id: number; name: string }[]>([])

// 点赞状态（由后端 liked 字段驱动）
const isLiked = ref(false)
// 编辑帖子
const isEditing = ref(false)
const editForm = ref({ title: '', content: '', boardId: 1, images: [] as string[] })

const author = computed(() => {
  const p = post.value
  // 作者信息直接使用后端返回的字段
  return {
    avatar: getAvatarUrl(p?.authorAvatar, p?.authorName),
    name: p?.authorName || '未知用户',
  }
})
const replyContent = ref('')

const likeCounts = ref<Record<number, number>>({})

onMounted(() => {
  loadUserInfo()
  loadPost()
  loadBoardOptions()
})

const loadBoardOptions = async () => {
  try {
    const list = await listBoards(2)
    if (list.length) boardOptions.value = list
  } catch (e) {
    console.error('拉取板块列表失败:', e)
  }
}

const loadUserInfo = async () => {
  // 未登录时不要请求 /auth/me：它会返回 401，游客将被全局拦截器弹到登录页，无法浏览公开内容
  if (!localStorage.getItem('token')) {
    return
  }
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

const loadPost = async () => {
  isLoading.value = true
  try {
    post.value = await getPostById(postId.value)
    if (post.value) {
      isLiked.value = post.value.liked ?? false
      editForm.value.boardId = post.value.boardId
      await loadReplies()
    }
  } catch (error) {
    console.error('加载帖子详情失败:', error)
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
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 点赞帖子
const likePost = async () => {
  if (!post.value) return

  try {
    const response = await request.post<{ liked: boolean; likeCount: number }>(`/posts/${post.value.id}/like`)
    if (response.success && response.data) {
      post.value.likes = response.data.likeCount
      isLiked.value = response.data.liked
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 点赞评论
const likeReply = async (replyId: number) => {
  try {
    const response = await request.post<{ likeCount: number }>(`/comments/${replyId}/like`)
    if (response.success) {
      likeCounts.value[replyId] = response.data?.likeCount ?? 0
    }
  } catch (error) {
    console.error('评论点赞失败:', error)
  }
}

// 提交回复到后端
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    return
  }

  if (!currentUser.value) {
    notifyWarning('请先登录后再发表评论')
    router.push('/login')
    return
  }

  try {
    await request.post(`/comments/post/${postId.value}`, {
      content: replyContent.value,
    })

    replyContent.value = ''
    // 重新加载回复列表
    await loadReplies()
  } catch (error) {
    console.error('提交回复失败:', error)
    notifyError('提交回复失败，请检查网络或稍后重试')
  }
}

const goBack = () => {
  router.back()
}

// 判断是否是帖子作者
const isPostAuthor = computed(() => {
  if (!currentUser.value || !post.value) return false
  return currentUser.value.id === post.value.authorId || currentUser.value.userId === post.value.authorId
})

// 判断是否是评论作者
const isReplyAuthor = (reply: Reply) => {
  if (!currentUser.value) return false
  return currentUser.value.id === reply.authorId || currentUser.value.userId === reply.authorId
}

// 删除帖子
const deletePost = async () => {
  if (!await notifyConfirm('确定要删除这篇帖子吗？删除后将无法恢复！')) return
  
  try {
    const token = localStorage.getItem('token')
    const response = await request.delete(`/posts/${postId.value}`, {
      headers: { Authorization: `Bearer ${token}` } as any
    }) as { success: boolean; message: string }
    
    if (response.success) {
      notifySuccess('帖子删除成功')
      router.push('/forum')
    } else {
      notifyError(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除帖子失败:', error)
    notifyError('删除帖子失败，请检查网络或稍后重试')
  }
}

// 删除评论
const deleteComment = async (commentId: number) => {
  if (!await notifyConfirm('确定要删除这条评论吗？')) return
  
  try {
    const response = await request.delete(`/comments/${commentId}`)

    if (response.success) {
      // 重新加载评论列表
      await loadReplies()
      notifySuccess('评论删除成功')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    notifyError('删除评论失败，请检查网络或稍后重试')
  }
}

// 打开编辑弹窗，预填当前帖子内容
const openEdit = () => {
  if (!post.value) return
  editForm.value = {
    title: post.value.title,
    content: post.value.content,
    boardId: post.value.boardId,
    images: [...(post.value.images ?? [])],
  }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const handleEditImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  if (!target.files || target.files.length === 0) return
  for (const file of Array.from(target.files)) {
    const url = await uploadImage(file)
    if (url) editForm.value.images.push(url)
  }
  target.value = ''
}

const removeEditImage = (index: number) => {
  editForm.value.images.splice(index, 1)
}

// 提交编辑（作者身份由后端 Token 解析）
const submitEdit = async () => {
  if (!post.value) return
  if (!editForm.value.title.trim() || !editForm.value.content.trim()) return
  try {
    const ok = await updatePost(
      post.value.id,
      editForm.value.title.trim(),
      editForm.value.content.trim(),
      editForm.value.boardId,
      editForm.value.images,
    )
    if (ok) {
      await loadPost()
      isEditing.value = false
    } else {
      notifyError('保存失败，请稍后重试')
    }
  } catch (error) {
    console.error('编辑帖子失败:', error)
    notifyError('编辑帖子失败，请检查网络或稍后重试')
  }
}
</script>

<template>
  <div>
    <div class="max-w-6xl mx-auto space-y-8">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[#ff4d00]"></div>
    </div>

    <template v-else>
      <!-- 返回按钮 -->
      <button 
        @click="goBack"
        class="flex items-center text-[#ff4d00] hover:text-[#ff6b35] transition-colors duration-300"
      >
        <span class="mr-2">←</span>
        <span>返回</span>
      </button>

      <!-- 帖子内容 -->
      <div v-if="post" class="naruto-card">
        <div class="flex flex-wrap items-center justify-center mb-6">
          <h1 class="text-2xl md:text-3xl font-bold text-[#ff4d00] text-center flex-1">{{ post.title }}</h1>
        </div>
        <div class="flex flex-wrap items-center justify-center text-sm text-gray-500 dark:text-gray-400 mb-6 pb-4 border-b dark:border-gray-700">
          <div class="flex items-center mr-6 mb-2">
            <AuthorLink :author-id="post.authorId" :name="author?.name" :avatar="author?.avatar" size="lg" />
          </div>
          <div class="flex items-center mr-6 mb-2">
            <span class="mr-1">📅</span>
            <span>{{ post.createdAt }}</span>
          </div>
          <div class="flex items-center mr-6 mb-2">
            <span class="mr-1">👁️</span>
            <span>{{ post.views }} 浏览</span>
          </div>
          <button 
            @click="likePost"
            class="flex items-center mb-2 p-2 rounded-full transition-colors duration-300 transform hover:scale-110"
            :class="isLiked ? 'bg-[#ff4d00]/10 text-[#ff4d00]' : 'text-gray-500 dark:text-gray-400 hover:bg-[#ff4d00]/10'"
          >
            <span class="mr-1">{{ isLiked ? '❤️' : '🤍' }}</span>
            <span>{{ post.likes }} 点赞</span>
          </button>
          <!-- 删除帖子按钮 -->
          <button 
            v-if="isPostAuthor"
            @click="deletePost"
            class="flex items-center mb-2 p-2 rounded-full hover:bg-red-100 text-red-500 transition-colors duration-300"
          >
            <span class="mr-1">🗑️</span>
            <span>删除帖子</span>
          </button>
          <!-- 编辑帖子按钮 -->
          <button 
            v-if="isPostAuthor"
            @click="openEdit"
            class="flex items-center mb-2 p-2 rounded-full hover:bg-blue-100 text-blue-500 transition-colors duration-300"
          >
            <span class="mr-1">✏️</span>
            <span>编辑帖子</span>
          </button>
          <!-- 管理员操作：置顶 / 删除 -->
          <PostAdminActions :post-id="post.id" @changed="loadPost" @deleted="router.push('/forum')" />
        </div>
        <div class="text-gray-800 dark:text-gray-200 leading-relaxed text-lg">
          {{ post.content }}
        </div>

        <!-- 帖子配图 -->
        <div v-if="post.images && post.images.length" class="mt-4 grid grid-cols-2 md:grid-cols-3 gap-3">
          <img 
            v-for="(img, index) in post.images" 
            :key="index"
            :src="img" 
            alt="配图"
            class="w-full h-48 object-cover rounded-lg border border-[#ff4d00]/20"
          />
        </div>
        
        <AiReply :title="post.title" :content="post.content" theme="light" />
      </div>

      <!-- 回复列表 -->
      <div v-if="post" class="naruto-card">
        <h2 class="text-xl font-bold text-[#ff4d00] mb-6 flex items-center">
          <span class="mr-2">💬</span>
          回复 ({{ postReplies.length }})
        </h2>
        <div class="space-y-6">
          <div 
            v-for="reply in postReplies" 
            :key="reply.id"
            class="p-4 rounded-xl bg-gradient-to-br from-white to-[#fff5ee] border border-[#ff4d00]/20"
          >
            <div class="flex flex-wrap items-center text-sm text-gray-500 dark:text-gray-400 mb-3">
              <div class="flex items-center mr-6 mb-2">
                <AuthorLink :author-id="reply.authorId" :name="reply.authorName" :avatar="reply.authorAvatar" />
              </div>
              <div class="flex items-center mr-6 mb-2">
                <span class="mr-1">📅</span>
                <span>{{ reply.createdAt }}</span>
              </div>
              <button 
                @click="likeReply(reply.id)"
                class="flex items-center mb-2 p-1 rounded-full hover:bg-[#ff4d00]/10 transition-colors duration-300"
              >
                <span class="mr-1 text-[#ff4d00]">❤️</span>
                <span>{{ likeCounts[reply.id] || 0 }}</span>
              </button>
              <!-- 删除评论按钮 -->
              <button 
                v-if="isReplyAuthor(reply)"
                @click="deleteComment(reply.id)"
                class="flex items-center mb-2 p-1 rounded-full hover:bg-red-100 text-red-500 transition-colors duration-300"
              >
                <span class="mr-1">🗑️</span>
                <span>删除</span>
              </button>
            </div>
            <div class="text-gray-800 dark:text-gray-200 pl-10">
              {{ reply.content }}
            </div>
          </div>
        </div>
      </div>

      <!-- 回复输入框 -->
      <div v-if="post" class="naruto-card">
        <h2 class="text-xl font-bold text-[#ff4d00] mb-6 flex items-center">
          <span class="mr-2">✏️</span>
          发表回复
        </h2>
        <textarea 
          v-model="replyContent"
          class="w-full p-4 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 mb-4 resize-none focus:outline-none focus:ring-2 focus:ring-[#ff4d00]/50"
          rows="4"
          placeholder="请输入回复内容..."
        ></textarea>
        <div class="flex justify-center">
          <button 
            @click="submitReply"
            class="px-8 py-3 bg-gradient-to-r from-[#ff4d00] to-[#ff6b35] text-white rounded-lg font-bold shadow-lg shadow-[#ff4d00]/30 hover:shadow-xl hover:shadow-[#ff4d00]/50 transition-all duration-300 transform hover:scale-105"
          >
            提交回复
          </button>
        </div>
      </div>

      <!-- 帖子不存在 -->
      <div v-else class="naruto-card text-center py-12">
        <h2 class="text-2xl font-bold mb-4">帖子不存在</h2>
        <p class="text-gray-600 dark:text-gray-400">该帖子可能已被删除或不存在</p>
      </div>
    </template>
  </div>

  <!-- 编辑帖子弹窗 -->
  <Teleport to="body">
    <Transition name="modal">
      <div 
        v-if="isEditing" 
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md p-4"
        @click.self="cancelEdit"
      >
        <div class="relative w-full max-w-2xl bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-2xl">
          <h3 class="text-xl font-bold text-[#ff4d00] mb-4">编辑帖子</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">板块</label>
              <select v-model="editForm.boardId" class="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200">
                <option v-for="b in boardOptions" :key="b.id" :value="b.id">{{ b.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">标题</label>
              <input v-model="editForm.title" maxlength="100" class="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">正文</label>
              <textarea v-model="editForm.content" rows="8" class="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 resize-none"></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">配图</label>
              <input type="file" multiple accept="image/*" @change="handleEditImageUpload" class="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200" />
              <div v-if="editForm.images.length" class="mt-3 grid grid-cols-3 gap-3">
                <div v-for="(img, index) in editForm.images" :key="index" class="relative group">
                  <img :src="img" alt="配图" class="w-full h-24 object-cover rounded-md border border-gray-200 dark:border-gray-700" />
                  <button type="button" @click="removeEditImage(index)" class="absolute top-1 right-1 w-6 h-6 flex items-center justify-center rounded-full bg-black/60 text-white text-sm opacity-0 group-hover:opacity-100">×</button>
                </div>
              </div>
            </div>
          </div>
          <div class="flex justify-end space-x-3 mt-6">
            <button @click="cancelEdit" class="px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300">取消</button>
            <button @click="submitEdit" class="px-6 py-2 rounded-lg bg-gradient-to-r from-[#ff4d00] to-[#ff6b35] text-white font-bold">保存</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
  </div>
</template>
