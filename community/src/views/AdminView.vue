<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  adminApi,
  type AdminPostItem,
  type AdminCommentItem,
  type AdminUserItem,
  type AdminCommunityItem,
} from '../api/admin'
import { notifyError, notifySuccess, notifyConfirm } from '../utils/notify'

const router = useRouter()
const activeTab = ref('dashboard')
const isLoading = ref(false)
const forbidden = ref(false)
const keyword = ref('')

const dashboard = ref<Record<string, number>>({})
const posts = ref<AdminPostItem[]>([])
const comments = ref<AdminCommentItem[]>([])
const users = ref<AdminUserItem[]>([])
const communities = ref<AdminCommunityItem[]>([])

// 社区表单（新增 / 编辑共用）
const communityForm = ref<Partial<AdminCommunityItem>>({ name: '', icon: '', description: '', sort: 0 })
const editingCommunityId = ref<number | null>(null)

const tabs = [
  { key: 'dashboard', label: '📊 数据看板' },
  { key: 'posts', label: '📝 内容管理' },
  { key: 'comments', label: '💬 评论管理' },
  { key: 'users', label: '👥 用户管理' },
  { key: 'communities', label: '🎮 游戏社区' },
]

const handleError = (e: any) => {
  if (e?.response?.status === 403) {
    forbidden.value = true
  } else {
    notifyError(e?.response?.data?.message || '操作失败')
  }
}

const loadDashboard = async () => {
  dashboard.value = await adminApi.dashboard()
}

const loadCurrent = async () => {
  isLoading.value = true
  try {
    if (activeTab.value === 'posts') {
      const data = await adminApi.listPosts(keyword.value.trim())
      posts.value = data.list
    } else if (activeTab.value === 'comments') {
      const data = await adminApi.listComments(keyword.value.trim())
      comments.value = data.list
    } else if (activeTab.value === 'users') {
      const data = await adminApi.listUsers(keyword.value.trim())
      users.value = data.list
    } else if (activeTab.value === 'communities') {
      const allCommunities = await adminApi.listAdminCommunities()
    communities.value = allCommunities.filter((c) => (c.id ?? 0) > 4)
    }
  } catch (e) {
    handleError(e)
  } finally {
    isLoading.value = false
  }
}

const switchTab = (key: string) => {
  activeTab.value = key
  keyword.value = ''
  loadCurrent()
}

const onSearch = () => loadCurrent()

onMounted(async () => {
  try {
    await loadDashboard()
    await loadCurrent()
  } catch (e) {
    handleError(e)
  }
})

// ---------- 帖子操作 ----------
const toggleTop = async (p: AdminPostItem) => {
  try {
    await adminApi.toggleTop(p.id)
    p.isTop = !p.isTop
    notifySuccess(p.isTop ? '已置顶' : '已取消置顶')
  } catch (e) {
    handleError(e)
  }
}
const toggleEssence = async (p: AdminPostItem) => {
  try {
    await adminApi.toggleEssence(p.id)
    p.isEssence = !p.isEssence
    notifySuccess(p.isEssence ? '已设为精华' : '已取消精华')
  } catch (e) {
    handleError(e)
  }
}
const deletePost = async (p: AdminPostItem) => {
  if (!await notifyConfirm(`确定删除帖子「${p.title}」？该帖下的评论也会一并删除，且不可恢复。`)) return
  try {
    await adminApi.deletePost(p.id)
    posts.value = posts.value.filter((x) => x.id !== p.id)
    notifySuccess('已删除')
  } catch (e) {
    handleError(e)
  }
}
const goPost = (p: AdminPostItem) => router.push(`/forum/post/${p.id}`)

// ---------- 评论操作 ----------
const deleteComment = async (c: AdminCommentItem) => {
  if (!await notifyConfirm('确定删除这条评论？')) return
  try {
    await adminApi.deleteComment(c.id)
    comments.value = comments.value.filter((x) => x.id !== c.id)
    notifySuccess('已删除')
  } catch (e) {
    handleError(e)
  }
}

// ---------- 用户操作 ----------
const toggleUserStatus = async (u: AdminUserItem) => {
  const next = u.status === 1 ? 0 : 1
  try {
    await adminApi.updateUserStatus(u.id, next)
    u.status = next
    notifySuccess(next === 1 ? '已启用' : '已禁用')
  } catch (e) {
    handleError(e)
  }
}
const toggleUserRole = async (u: AdminUserItem) => {
  const next = u.role === 1 ? 0 : 1
  if (next === 0 && !(await notifyConfirm('确定取消该用户的管理员权限？'))) return
  try {
    await adminApi.updateUserRole(u.id, next)
    u.role = next
    notifySuccess(next === 1 ? '已设为管理员' : '已取消管理员')
  } catch (e) {
    handleError(e)
  }
}

const toggleUserVip = async (u: AdminUserItem) => {
  const next = u.vip === 1 ? 0 : 1
  try {
    await adminApi.updateUserVip(u.id, next)
    u.vip = next
    notifySuccess(next === 1 ? '已设为 VIP' : '已取消 VIP')
  } catch (e) {
    handleError(e)
  }
}

// ---------- 游戏社区操作 ----------
const resetCommunityForm = () => {
  editingCommunityId.value = null
  communityForm.value = { name: '', icon: '', description: '', sort: 0 }
}
const editCommunity = (c: AdminCommunityItem) => {
  editingCommunityId.value = c.id ?? null
  communityForm.value = { ...c }
}
const submitCommunity = async () => {
  if (!communityForm.value.name?.trim()) {
    notifyError('请输入社区名称')
    return
  }
  try {
    if (editingCommunityId.value) {
      await adminApi.updateCommunity(editingCommunityId.value, communityForm.value)
      notifySuccess('已保存')
    } else {
      await adminApi.createCommunity(communityForm.value)
      notifySuccess('游戏社区已创建（已自动生成「综合讨论」板块）')
    }
    resetCommunityForm()
    const allCommunities = await adminApi.listAdminCommunities()
    communities.value = allCommunities.filter((c) => (c.id ?? 0) > 4)
  } catch (e) {
    handleError(e)
  }
}
const deleteCommunity = async (c: AdminCommunityItem) => {
  if (!c.id) return
  if (!await notifyConfirm(`确定删除游戏社区「${c.name}」？仅当该社区下没有帖子时才能删除。`)) return
  try {
    await adminApi.deleteCommunity(c.id)
    communities.value = communities.value.filter((x) => x.id !== c.id)
    notifySuccess('已删除')
  } catch (e: any) {
    notifyError(e?.response?.data?.message || '删除失败')
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-100">
    <div class="max-w-7xl mx-auto px-4 py-8">
      <button @click="router.back()" class="text-gray-500 hover:text-[#4f46e5] mb-4">← 返回</button>
      <h1 class="text-3xl font-bold mb-6">🛠️ 管理后台</h1>

      <!-- 无权限 -->
      <div v-if="forbidden" class="bg-white dark:bg-gray-800 rounded-2xl shadow p-10 text-center">
        <div class="text-5xl mb-4">🔒</div>
        <h2 class="text-xl font-bold mb-2">没有访问权限</h2>
        <p class="text-gray-500">该页面仅管理员可用。如需权限，请联系管理员为你的账号分配管理员角色。</p>
      </div>

      <template v-else>
        <!-- Tab -->
        <div class="flex flex-wrap gap-2 mb-6">
          <button
            v-for="t in tabs"
            :key="t.key"
            @click="switchTab(t.key)"
            class="px-4 py-2 rounded-full text-sm font-medium transition"
            :class="activeTab === t.key ? 'bg-[#4f46e5] text-white' : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
          >{{ t.label }}</button>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow p-6">
          <!-- 数据看板 -->
          <div v-if="activeTab === 'dashboard'" class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div v-for="item in [
              { label: '用户总数', value: dashboard.userTotal, icon: '👥' },
              { label: '今日新增用户', value: dashboard.userToday, icon: '✨' },
              { label: '帖子总数', value: dashboard.postTotal, icon: '📝' },
              { label: '今日新帖', value: dashboard.postToday, icon: '🆕' },
              { label: '评论总数', value: dashboard.commentTotal, icon: '💬' },
              { label: '今日评论', value: dashboard.commentToday, icon: '🗨️' },
              { label: '板块数量', value: dashboard.boardTotal, icon: '📋' },
              { label: '总浏览量', value: dashboard.viewTotal, icon: '👁️' },
            ]" :key="item.label" class="bg-gray-50 dark:bg-gray-700/40 rounded-xl p-5 text-center">
              <div class="text-2xl mb-1">{{ item.icon }}</div>
              <div class="text-2xl font-bold text-[#4f46e5]">{{ item.value ?? 0 }}</div>
              <div class="text-xs text-gray-500 mt-1">{{ item.label }}</div>
            </div>
          </div>

          <!-- 内容 / 评论 / 用户：搜索框 -->
          <div v-if="['posts', 'comments', 'users'].includes(activeTab)" class="flex gap-2 mb-4">
            <input
              v-model="keyword"
              @keyup.enter="onSearch"
              :placeholder="activeTab === 'users' ? '搜索用户名 / 昵称 / 邮箱' : '搜索内容'"
              class="flex-1 px-4 py-2 rounded-xl border dark:border-gray-600 bg-white dark:bg-gray-700 focus:outline-none focus:border-[#4f46e5]"
            />
            <button @click="onSearch" class="px-5 py-2 rounded-xl bg-[#4f46e5] text-white">搜索</button>
          </div>

          <div v-if="isLoading" class="text-center py-12 text-gray-500">加载中...</div>

          <!-- 帖子列表 -->
          <div v-else-if="activeTab === 'posts'" class="space-y-3">
            <div v-if="posts.length === 0" class="text-center py-12 text-gray-500">暂无帖子</div>
            <div v-for="p in posts" :key="p.id" class="border dark:border-gray-700 rounded-xl p-4">
              <div class="flex items-start justify-between gap-4">
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-2 mb-1">
                    <span v-if="p.isTop" class="px-2 py-0.5 rounded text-[10px] bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-300">置顶</span>
                    <span v-if="p.isEssence" class="px-2 py-0.5 rounded text-[10px] bg-amber-100 text-amber-600 dark:bg-amber-900/40 dark:text-amber-300">精华</span>
                    <span class="px-2 py-0.5 rounded text-[10px] bg-gray-100 dark:bg-gray-700">{{ p.boardName || '未知板块' }}</span>
                    <span class="text-xs text-gray-500">{{ p.authorName || '匿名' }} · {{ p.createdAt }}</span>
                  </div>
                  <h3 class="font-semibold truncate cursor-pointer hover:text-[#4f46e5]" @click="goPost(p)">{{ p.title }}</h3>
                  <p class="text-sm text-gray-500 line-clamp-1 mt-0.5">{{ p.content }}</p>
                  <div class="text-xs text-gray-400 mt-1">👁 {{ p.viewCount }} · 💬 {{ p.commentCount }} · ❤ {{ p.likeCount }}</div>
                </div>
                <div class="flex flex-wrap gap-2 flex-shrink-0">
                  <button @click="toggleTop(p)" class="px-3 py-1 rounded-lg text-xs bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600">
                    {{ p.isTop ? '取消置顶' : '置顶' }}
                  </button>
                  <button @click="toggleEssence(p)" class="px-3 py-1 rounded-lg text-xs bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600">
                    {{ p.isEssence ? '取消精华' : '加精' }}
                  </button>
                  <button @click="deletePost(p)" class="px-3 py-1 rounded-lg text-xs bg-red-500 text-white hover:bg-red-600">删除</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <div v-else-if="activeTab === 'comments'" class="space-y-3">
            <div v-if="comments.length === 0" class="text-center py-12 text-gray-500">暂无评论</div>
            <div v-for="c in comments" :key="c.id" class="border dark:border-gray-700 rounded-xl p-4 flex items-start justify-between gap-4">
              <div class="min-w-0 flex-1">
                <p class="text-sm">{{ c.content }}</p>
                <div class="text-xs text-gray-500 mt-1">
                  {{ c.authorName || '匿名' }} · 评论于「{{ c.postTitle || '已删除帖子' }}」 · {{ c.createdAt }}
                </div>
              </div>
              <button @click="deleteComment(c)" class="px-3 py-1 rounded-lg text-xs bg-red-500 text-white hover:bg-red-600 flex-shrink-0">删除</button>
            </div>
          </div>

          <!-- 用户列表 -->
          <div v-else-if="activeTab === 'users'" class="space-y-3">
            <div v-if="users.length === 0" class="text-center py-12 text-gray-500">暂无用户</div>
            <div v-for="u in users" :key="u.id" class="border dark:border-gray-700 rounded-xl p-4 flex items-center justify-between gap-4">
              <div class="min-w-0">
                <div class="flex items-center gap-2 flex-wrap">
                  <span class="font-semibold">{{ u.nickname || u.username }}</span>
                  <span class="text-xs text-gray-500">@{{ u.username }}</span>
                  <span class="px-2 py-0.5 rounded text-[10px]" :class="u.role === 1 ? 'bg-purple-100 text-purple-600 dark:bg-purple-900/40 dark:text-purple-300' : 'bg-gray-100 dark:bg-gray-700'">
                    {{ u.role === 1 ? '管理员' : '普通用户' }}
                  </span>
                  <span class="px-2 py-0.5 rounded text-[10px]" :class="u.status === 1 ? 'bg-green-100 text-green-600 dark:bg-green-900/40 dark:text-green-300' : 'bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-300'">
                    {{ u.status === 1 ? '正常' : '已禁用' }}
                  </span>
                  <span v-if="u.vip === 1" class="px-2 py-0.5 rounded text-[10px] bg-pink-100 text-pink-600 dark:bg-pink-900/40 dark:text-pink-300">
                    VIP
                  </span>
                </div>
                <div class="text-xs text-gray-500 mt-1">{{ u.email }} · 注册于 {{ u.createdAt }}</div>
              </div>
              <div class="flex gap-2 flex-shrink-0">
                <RouterLink :to="`/user/${u.id}`" class="px-3 py-1 rounded-lg text-xs bg-indigo-50 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-300 hover:bg-indigo-100 dark:hover:bg-indigo-900/60">
                  主页
                </RouterLink>
                <button @click="toggleUserStatus(u)" class="px-3 py-1 rounded-lg text-xs bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600">
                  {{ u.status === 1 ? '禁用' : '启用' }}
                </button>
                <button @click="toggleUserRole(u)" class="px-3 py-1 rounded-lg text-xs" :class="u.role === 1 ? 'bg-gray-100 dark:bg-gray-700' : 'bg-[#4f46e5] text-white'">
                  {{ u.role === 1 ? '取消管理员' : '设为管理员' }}
                </button>
                <button @click="toggleUserVip(u)" class="px-3 py-1 rounded-lg text-xs" :class="u.vip === 1 ? 'bg-gray-100 dark:bg-gray-700' : 'bg-pink-500 text-white hover:bg-pink-600'">
                  {{ u.vip === 1 ? '取消 VIP' : '设为 VIP' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 游戏社区管理 -->
          <div v-else-if="activeTab === 'communities'" class="space-y-6">
            <!-- 表单 -->
            <div class="border dark:border-gray-700 rounded-xl p-4 grid md:grid-cols-4 gap-3 items-end">
              <div>
                <label class="block text-xs text-gray-500 mb-1">社区名称</label>
                <input v-model="communityForm.name" class="w-full px-3 py-2 rounded-lg border dark:border-gray-600 bg-white dark:bg-gray-700" placeholder="如：蛋仔派对" />
              </div>
              <div>
                <label class="block text-xs text-gray-500 mb-1">图标（emoji）</label>
                <input v-model="communityForm.icon" class="w-full px-3 py-2 rounded-lg border dark:border-gray-600 bg-white dark:bg-gray-700" placeholder="🎮" />
              </div>
              <div>
                <label class="block text-xs text-gray-500 mb-1">描述</label>
                <input v-model="communityForm.description" class="w-full px-3 py-2 rounded-lg border dark:border-gray-600 bg-white dark:bg-gray-700" placeholder="一句话简介" />
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-xs text-gray-500">排序（数字越小越靠前）</span>
                <div class="flex gap-2">
                  <input v-model.number="communityForm.sort" type="number" placeholder="如 1" class="w-20 px-3 py-2 rounded-lg border dark:border-gray-600 bg-white dark:bg-gray-700" />
                  <button @click="submitCommunity" class="flex-1 px-3 py-2 rounded-lg bg-[#4f46e5] text-white text-sm">
                    {{ editingCommunityId ? '保存' : '新增社区' }}
                  </button>
                  <button v-if="editingCommunityId" @click="resetCommunityForm" class="px-3 py-2 rounded-lg bg-gray-100 dark:bg-gray-700 text-sm">取消</button>
                </div>
              </div>
            </div>

            <div class="text-xs text-gray-500">新增的社区为「纯论坛」形态，自动生成「综合讨论」板块，首页「游戏社区」区可直达，点击进入统一论坛模板。</div>

            <!-- 列表 -->
            <div class="space-y-2">
              <div v-if="communities.length === 0" class="text-center py-8 text-gray-500">暂无游戏社区</div>
              <div v-for="c in communities" :key="c.id" class="border dark:border-gray-700 rounded-xl p-3 flex items-center justify-between gap-3">
                <div class="min-w-0">
                  <span class="font-medium">{{ c.icon }} {{ c.name }}</span>
                  <span class="ml-2 text-xs text-gray-500">排序 {{ c.sort }}</span>
                  <div class="text-xs text-gray-400 truncate">{{ c.description }}</div>
                </div>
                <div class="flex gap-2 flex-shrink-0">
                  <button @click="editCommunity(c)" class="px-3 py-1 rounded-lg text-xs bg-gray-100 dark:bg-gray-700">编辑</button>
                  <button @click="deleteCommunity(c)" class="px-3 py-1 rounded-lg text-xs bg-red-500 text-white">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
