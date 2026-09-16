<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { authApi } from '../api/auth'
import { userApi } from '../api/user'
import { uploadApi } from '../api/upload'
import { notificationApi } from '../api/notification'
import { useNotificationStore } from '../stores/notification'
import type { PageResult, PostVO, CommentVO } from '../types/api'
import { useUserStore } from '../stores'
import { computeUserRating } from '../utils/userRating'
import type { UserStats } from '../api/user'
import { notifyError, notifySuccess, notifyWarning } from '../utils/notify'

const router = useRouter()
const userStore = useUserStore()

// 头像上传
const avatarInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const triggerAvatarUpload = () => avatarInput.value?.click()
const onAvatarChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const url = await uploadApi.uploadAvatar(file)
    if (currentUser.value) currentUser.value.avatar = url
    userStore.updateAvatar(url)
  } catch (err: any) {
    notifyError(err?.message || '头像上传失败')
  } finally {
    uploading.value = false
    ;(e.target as HTMLInputElement).value = ''
  }
}

const activeTab = ref('overview')
const isLoggedIn = ref(false)
const currentUser = ref<any>(null)
const isLoading = ref(true)

const userStats = ref({
  totalPosts: 0,
  totalReplies: 0,
  totalLikes: 0
})
// 主页获赞数（公开主页的点赞数）
const homeLikeCount = ref(0)

const userBadges = ref<any[]>([])
const userLevel = ref(1)
const userPosts = ref<any[]>([])
const userComments = ref<any[]>([])

// ---------- 站内消息 ----------
// 未读数统一由 notificationStore 管理（App.vue 负责全局轮询），这里只负责列表渲染
const notificationStore = useNotificationStore()
const notifications = ref<any[]>([])
const notificationLoading = ref(false)

const loadNotifications = async () => {
  notificationLoading.value = true
  try {
    const data = await notificationApi.list(1, 30)
    notifications.value = data.list || []
    notificationStore.setUnread(data.unread ?? 0)
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    notificationLoading.value = false
  }
}

const markAllRead = async () => {
  const ok = await notificationApi.markAllRead()
  if (ok) {
    notifications.value = notifications.value.map((n: any) => ({ ...n, isRead: true }))
    notificationStore.setUnread(0)
  }
}

/** 点击消息：标记已读并跳到对应帖子（三类通知的 targetId 均为帖子 ID） */
const goToNotification = async (n: any) => {
  if (!n.isRead) {
    await notificationApi.markRead(n.id)
    n.isRead = true
    notificationStore.decrement()
  }
  router.push(`/forum/post/${n.targetId}`)
}

const removeNotification = async (id: number) => {
  const ok = await notificationApi.remove(id)
  if (ok) notifications.value = notifications.value.filter((n: any) => n.id !== id)
}

// ---------- 消息分类：评论 / 点赞 分开 ----------
type NotifFilter = 'all' | 'comment' | 'like'
const notifFilter = ref<NotifFilter>('all')

const isCommentType = (type: string) => type === 'COMMENT_POST'
const isLikeType = (type: string) => type === 'LIKE_POST' || type === 'LIKE_COMMENT'

const filteredNotifications = computed(() => {
  if (notifFilter.value === 'comment') return notifications.value.filter((n: any) => isCommentType(n.type))
  if (notifFilter.value === 'like') return notifications.value.filter((n: any) => isLikeType(n.type))
  return notifications.value
})

/** 类型元数据：图标与标签 */
const notifTypeMeta = (type: string) => {
  if (isCommentType(type)) return { icon: '💬', label: '评论' }
  if (isLikeType(type)) return { icon: '❤️', label: '点赞' }
  return { icon: '🔔', label: '通知' }
}

const setNotifFilter = (f: NotifFilter) => (notifFilter.value = f)

const checkLoginStatus = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    isLoggedIn.value = false
    isLoading.value = false
    return
  }

  try {
    const response = await request.get('/auth/me') as any
    if (response.code === 200 || response.success) {
      isLoggedIn.value = true
      currentUser.value = response.data || response
      await loadUserData()
    } else {
      localStorage.removeItem('token')
      isLoggedIn.value = false
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
    isLoggedIn.value = false
  } finally {
    isLoading.value = false
  }
}

const loadUserData = async () => {
  if (!currentUser.value) return

  try {
    // 选项卡所需的帖子 / 评论列表
    const [postsRes, commentsRes, statsRes] = await Promise.all([
      request.get<PageResult<PostVO>>('/users/me/posts', { params: { page: 1, size: 20 } }),
      request.get<PageResult<CommentVO>>('/users/me/comments', { params: { page: 1, size: 20 } }),
      userApi.getStats(currentUser.value.id),
    ])

    const stats: UserStats = statsRes ?? {
      totalPosts: 0,
      totalReplies: 0,
      totalLikes: 0,
      homeLikeCount: 0,
    }

    if (postsRes.success) {
      userPosts.value = postsRes.data.content || []
    }
    if (commentsRes.success) {
      userComments.value = commentsRes.data.content || []
    }

    // 统计卡片数据
    userStats.value.totalPosts = stats.totalPosts
    userStats.value.totalReplies = stats.totalReplies
    userStats.value.totalLikes = stats.totalLikes
    // 主页获赞数（公开主页的点赞数）
    homeLikeCount.value = stats.homeLikeCount

    // 根据用户数据计算等级与徽章
    const rating = computeUserRating({
      role: currentUser.value.role,
      vip: currentUser.value.vip,
      createdAt: currentUser.value.createdAt,
      homeLikeCount: stats.homeLikeCount,
      totalPosts: stats.totalPosts,
      totalReplies: stats.totalReplies,
      totalLikes: stats.totalLikes,
    })
    userLevel.value = rating.level
    userBadges.value = rating.badges

    // 未读数与轮询由 App.vue 全局负责，此处不重复启动
    notificationStore.fetchUnread()
  } catch (error) {
    console.error('加载用户数据失败:', error)
  }
}

const handleLogout = async () => {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      await request.post('/auth/logout')
    } catch (error) {
      console.error('退出登录失败:', error)
    }
  }
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  userStore.clearUser()
  router.push('/')
}

const goToLogin = () => {
  router.push('/login')
}

const goToForum = () => {
  router.push('/forum')
}

const goToModule = (module: string) => {
  router.push(`/${module}`)
}

const goToPostDetail = (postId: number) => {
  router.push(`/forum/post/${postId}`)
}

// 设置相关功能
const showChangePasswordModal = ref(false)
const showEditProfileModal = ref(false)
const showEditNicknameModal = ref(false)
const showBindEmailModal = ref(false)

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const email = ref('')
const nickname = ref('')
const newNickname = ref('')

// 打开修改用户名弹窗时初始化
const openEditProfileModal = () => {
  nickname.value = currentUser.value?.username || ''
  showEditProfileModal.value = true
}

// 打开修改昵称弹窗时初始化
const openEditNicknameModal = () => {
  newNickname.value = currentUser.value?.nickname || ''
  showEditNicknameModal.value = true
}

const openChangePasswordModal = () => {
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  showChangePasswordModal.value = true
}

const changePassword = async () => {
  if (!oldPassword.value) {
    notifyWarning('请输入原密码')
    return
  }
  if (!newPassword.value || newPassword.value !== confirmPassword.value) {
    notifyWarning('两次输入的密码不一致')
    return
  }

  try {
    const response = await authApi.changePassword({
      oldPassword: oldPassword.value,
      newPassword: newPassword.value,
    })

    if (response.success) {
      notifySuccess('密码修改成功')
      showChangePasswordModal.value = false
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
    } else {
      notifyError('修改失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    notifyError('修改密码失败，请稍后重试')
  }
}

const editProfile = async () => {
  if (!nickname.value.trim()) {
    notifyWarning('请输入用户名')
    return
  }

  try {
    const response = await authApi.updateProfile({ username: nickname.value })

    if (response.success) {
      if (response.data) currentUser.value = response.data
      notifySuccess('用户名修改成功')
      showEditProfileModal.value = false
      nickname.value = ''
    } else {
      notifyError('修改失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('修改用户名失败:', error)
    notifyError('修改用户名失败，请稍后重试')
  }
}

const editNickname = async () => {
  if (!newNickname.value.trim()) {
    notifyWarning('请输入昵称')
    return
  }

  try {
    const response = await authApi.updateProfile({ nickname: newNickname.value })

    if (response.success) {
      if (response.data) currentUser.value = response.data
      notifySuccess('昵称修改成功')
      showEditNicknameModal.value = false
      newNickname.value = ''
    } else {
      notifyError('修改失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('修改昵称失败:', error)
    notifyError('修改昵称失败，请稍后重试')
  }
}

const bindEmail = async () => {
  if (!email.value || !email.value.includes('@')) {
    notifyWarning('请输入有效的邮箱地址')
    return
  }

  try {
    const response = await authApi.updateProfile({ email: email.value })

    if (response.success) {
      if (response.data) currentUser.value = response.data
      notifySuccess('邮箱绑定成功')
      showBindEmailModal.value = false
      email.value = ''
    } else {
      notifyError('绑定失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('绑定邮箱失败:', error)
    notifyError('绑定邮箱失败，请稍后重试')
  }
}

onMounted(() => {
  checkLoginStatus()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 未登录提示 -->
    <div v-if="!isLoading && !isLoggedIn" class="max-w-6xl mx-auto space-y-8 px-4">
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-12 text-center">
        <div class="mb-8">
          <div class="w-32 h-32 mx-auto bg-gradient-to-br from-[#4f46e5] to-[#7c3aed] rounded-full flex items-center justify-center mb-6 shadow-lg">
            <span class="text-6xl">👤</span>
          </div>
          <h2 class="text-3xl font-bold text-gray-800 dark:text-white mb-4">请先登录</h2>
          <p class="text-gray-600 dark:text-gray-400 mb-8">登录后即可查看和管理您的个人中心</p>
        </div>
        <a 
          href="/login"
          class="inline-block px-8 py-3 bg-gradient-to-r from-[#4f46e5] to-[#7c3aed] text-white rounded-xl font-bold shadow-lg hover:shadow-xl transition-all duration-300 transform hover:scale-105"
        >
          去登录
        </a>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-else-if="isLoading" class="max-w-6xl mx-auto space-y-8 px-4">
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-12 text-center">
        <div class="mb-8">
          <div class="w-16 h-16 mx-auto border-4 border-[#4f46e5] border-t-transparent rounded-full animate-spin mb-6"></div>
          <h2 class="text-2xl font-bold text-[#4f46e5] mb-4">加载中...</h2>
          <p class="text-gray-600 dark:text-gray-400">正在获取您的信息</p>
        </div>
      </div>
    </div>

    <!-- 已登录，显示用户信息 -->
    <div v-else-if="currentUser" class="max-w-6xl mx-auto space-y-8 px-4">
      <!-- 用户资料卡 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden">
        <div class="bg-gradient-to-r from-[#4f46e5] to-[#7c3aed] h-32"></div>
        <div class="px-8 pb-8">
          <div class="relative -mt-16 mb-6">
            <div
              class="w-32 h-32 rounded-full border-4 border-white dark:border-gray-700 overflow-hidden shadow-xl bg-gradient-to-br from-[#4f46e5] to-[#7c3aed] cursor-pointer group"
              @click="triggerAvatarUpload" title="点击更换头像"
            >
              <img
                v-if="currentUser.avatar"
                :src="currentUser.avatar"
                :alt="currentUser.username"
                class="w-full h-full object-cover"
                @error="currentUser.avatar = ''"
              />
              <div v-else class="w-full h-full flex items-center justify-center">
                <span class="text-5xl text-white">{{ currentUser.username?.charAt(0).toUpperCase() || currentUser.nickname?.charAt(0).toUpperCase() || 'U' }}</span>
              </div>
              <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
                <span class="text-white text-sm">{{ uploading ? '上传中...' : '更换头像' }}</span>
              </div>
            </div>
            <input ref="avatarInput" type="file" accept="image/*" class="hidden" @change="onAvatarChange" />
          </div>
          
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6">
            <div>
              <h1 class="text-3xl font-bold text-gray-800 dark:text-white mb-2">{{ currentUser.username || currentUser.nickname || '用户' }}</h1>
              <div class="flex items-center space-x-2 text-gray-600 dark:text-gray-400">
                <span>📅</span>
                <span>加入于 {{ currentUser.createdAt ? new Date(currentUser.createdAt).toLocaleDateString() : '未知' }}</span>
              </div>
            </div>
            <div class="flex items-center gap-3 mt-4 md:mt-0">
              <div
                class="flex items-center gap-1.5 px-4 py-2 bg-pink-50 dark:bg-pink-900/20 rounded-full"
                title="主页获赞"
              >
                <span class="text-pink-500 text-lg">❤️</span>
                <span class="font-bold text-pink-600 dark:text-pink-300">{{ homeLikeCount }}</span>
                <span class="text-sm text-pink-500/80">主页获赞</span>
              </div>
              <button
                @click="handleLogout"
                class="px-6 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-all duration-300"
              >
                退出登录
              </button>
            </div>
          </div>

          <!-- 用户徽章 -->
          <div v-if="userBadges.length > 0" class="flex flex-wrap gap-2 mb-6">
            <span 
              v-for="badge in userBadges" 
              :key="badge.name"
              class="px-4 py-2 rounded-full text-sm font-medium flex items-center space-x-1"
              :class="{
                'bg-yellow-100 text-yellow-800': badge.color === 'yellow',
                'bg-purple-100 text-purple-800': badge.color === 'purple',
                'bg-red-100 text-red-800': badge.color === 'red',
                'bg-blue-100 text-blue-800': badge.color === 'blue',
                'bg-green-100 text-green-800': badge.color === 'green',
                'bg-indigo-100 text-indigo-800': badge.color === 'indigo',
                'bg-pink-100 text-pink-800': badge.color === 'pink',
                'bg-amber-100 text-amber-800': badge.color === 'gold'
              }"
            >
              <span>{{ badge.icon }}</span>
              <span>{{ badge.name }}</span>
            </span>
          </div>

          <!-- 统计卡片 -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-xl p-4 text-center">
              <div class="text-3xl font-bold text-blue-600 mb-1">{{ userStats.totalPosts }}</div>
              <div class="text-sm text-blue-600/80">帖子数</div>
            </div>
            <div class="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-xl p-4 text-center">
              <div class="text-3xl font-bold text-green-600 mb-1">{{ userStats.totalReplies }}</div>
              <div class="text-sm text-green-600/80">回复数</div>
            </div>
            <div class="bg-gradient-to-br from-red-50 to-red-100 dark:from-red-900/20 dark:to-red-800/20 rounded-xl p-4 text-center">
              <div class="text-3xl font-bold text-red-600 mb-1">{{ userStats.totalLikes }}</div>
              <div class="text-sm text-red-600/80">获赞数</div>
            </div>
            <div class="bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-xl p-4 text-center">
              <div class="text-3xl font-bold text-purple-600 mb-1">Lv{{ userLevel }}</div>
              <div class="text-sm text-purple-600/80">用户等级</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 选项卡 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden">
        <div class="flex border-b dark:border-gray-700 overflow-x-auto">
          <button 
            @click="activeTab = 'overview'"
            class="px-6 py-4 font-medium transition-all duration-300 whitespace-nowrap"
            :class="activeTab === 'overview' ? 'text-[#4f46e5] border-b-2 border-[#4f46e5]' : 'text-gray-500 dark:text-gray-400 hover:text-[#4f46e5]'"
          >
            概览
          </button>
          <button 
            @click="activeTab = 'posts'"
            class="px-6 py-4 font-medium transition-all duration-300 whitespace-nowrap"
            :class="activeTab === 'posts' ? 'text-[#4f46e5] border-b-2 border-[#4f46e5]' : 'text-gray-500 dark:text-gray-400 hover:text-[#4f46e5]'"
          >
            我的帖子
          </button>
          <button 
            @click="activeTab = 'replies'"
            class="px-6 py-4 font-medium transition-all duration-300 whitespace-nowrap"
            :class="activeTab === 'replies' ? 'text-[#4f46e5] border-b-2 border-[#4f46e5]' : 'text-gray-500 dark:text-gray-400 hover:text-[#4f46e5]'"
          >
            我的评论
          </button>
          <button 
            @click="activeTab = 'messages'; loadNotifications()"
            class="px-6 py-4 font-medium transition-all duration-300 whitespace-nowrap"
            :class="activeTab === 'messages' ? 'text-[#4f46e5] border-b-2 border-[#4f46e5]' : 'text-gray-500 dark:text-gray-400 hover:text-[#4f46e5]'"
          >
            我的消息
            <span
              v-if="notificationStore.unreadCount > 0"
              class="ml-1 inline-flex items-center justify-center min-w-[18px] h-[18px] px-1 text-[10px] font-bold text-white bg-red-500 rounded-full align-middle"
            >{{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}</span>
          </button>
          <button 
            @click="activeTab = 'modules'"
            class="px-6 py-4 font-medium transition-all duration-300 whitespace-nowrap"
            :class="activeTab === 'modules' ? 'text-[#4f46e5] border-b-2 border-[#4f46e5]' : 'text-gray-500 dark:text-gray-400 hover:text-[#4f46e5]'"
          >
            我的模块
          </button>
          <button 
            @click="activeTab = 'settings'"
            class="px-6 py-4 font-medium transition-all duration-300 whitespace-nowrap"
            :class="activeTab === 'settings' ? 'text-[#4f46e5] border-b-2 border-[#4f46e5]' : 'text-gray-500 dark:text-gray-400 hover:text-[#4f46e5]'"
          >
            设置
          </button>
        </div>

        <!-- 选项卡内容 -->
        <div class="p-8">
          <!-- 概览 -->
          <div v-if="activeTab === 'overview'" class="space-y-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-4">活动概览</h3>
            
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <a 
                href="/forum"
                class="block bg-gradient-to-br from-orange-50 to-orange-100 dark:from-orange-900/20 dark:to-orange-800/20 rounded-xl p-6 cursor-pointer hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1"
              >
                <div class="flex items-center space-x-4 mb-4">
                  <div class="w-12 h-12 bg-gradient-to-br from-orange-500 to-red-600 rounded-xl flex items-center justify-center">
                    <span class="text-2xl">🔥</span>
                  </div>
                  <div>
                    <h4 class="font-bold text-gray-800 dark:text-white">火影忍者</h4>
                    <p class="text-sm text-gray-600 dark:text-gray-400">社区论坛</p>
                  </div>
                </div>
                <p class="text-gray-600 dark:text-gray-400 text-sm">参与讨论、交流心得</p>
              </a>

              <a 
                href="/module2"
                class="block bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-xl p-6 cursor-pointer hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1"
              >
                <div class="flex items-center space-x-4 mb-4">
                  <div class="w-12 h-12 bg-gradient-to-br from-purple-500 to-fuchsia-600 rounded-xl flex items-center justify-center">
                    <span class="text-2xl">⚡</span>
                  </div>
                  <div>
                    <h4 class="font-bold text-gray-800 dark:text-white">杀戮尖塔2</h4>
                    <p class="text-sm text-gray-600 dark:text-gray-400">游戏攻略</p>
                  </div>
                </div>
                <p class="text-gray-600 dark:text-gray-400 text-sm">查看攻略、角色介绍</p>
              </a>

              <a 
                href="/module3"
                class="block bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-xl p-6 cursor-pointer hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1"
              >
                <div class="flex items-center space-x-4 mb-4">
                  <div class="w-12 h-12 bg-gradient-to-br from-blue-400 to-white rounded-xl flex items-center justify-center">
                    <span class="text-2xl">💡</span>
                  </div>
                  <div>
                    <h4 class="font-bold text-gray-800 dark:text-white">原神</h4>
                    <p class="text-sm text-gray-600 dark:text-gray-400">游戏攻略</p>
                  </div>
                </div>
                <p class="text-gray-600 dark:text-gray-400 text-sm">角色配装、伤害计算</p>
              </a>

              <a 
                href="/module4"
                class="block bg-gradient-to-br from-yellow-50 to-orange-50 dark:from-yellow-900/20 dark:to-orange-900/20 rounded-xl p-6 cursor-pointer hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1"
              >
                <div class="flex items-center space-x-4 mb-4">
                  <div class="w-12 h-12 bg-gradient-to-br from-yellow-500 to-orange-500 rounded-xl flex items-center justify-center">
                    <span class="text-2xl">⚔️</span>
                  </div>
                  <div>
                    <h4 class="font-bold text-gray-800 dark:text-white">王者荣耀</h4>
                    <p class="text-sm text-gray-600 dark:text-gray-400">游戏攻略</p>
                  </div>
                </div>
                <p class="text-gray-600 dark:text-gray-400 text-sm">英雄攻略、出装推荐</p>
              </a>
            </div>

            <div class="bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 rounded-xl p-6">
              <h4 class="font-bold text-gray-800 dark:text-white mb-2">欢迎回来，{{ currentUser.username || currentUser.nickname }}！</h4>
              <p class="text-gray-600 dark:text-gray-400">这里是您的个人中心，可以管理您的账号和查看在各模块的活动情况。</p>
            </div>
          </div>

          <!-- 我的帖子 -->
          <div v-if="activeTab === 'posts'" class="space-y-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-4">我的帖子</h3>
            
            <div v-if="userPosts.length === 0" class="text-center py-12">
              <div class="text-6xl mb-4">📝</div>
              <p class="text-gray-500 dark:text-gray-400">您还没有发布过帖子</p>
              <a href="/forum/create" class="inline-block mt-4 px-6 py-2 bg-[#4f46e5] text-white rounded-lg hover:bg-[#4338ca] transition-colors">
                去发布帖子
              </a>
            </div>

            <div v-else class="space-y-4">
              <div 
                v-for="post in userPosts" 
                :key="post.id"
                @click="goToPostDetail(post.id)"
                class="bg-gray-50 dark:bg-gray-700 rounded-xl p-6 cursor-pointer hover:shadow-lg transition-all duration-300"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <h4 class="font-bold text-gray-800 dark:text-white mb-2 line-clamp-2">{{ post.title }}</h4>
                    <p class="text-gray-600 dark:text-gray-400 text-sm line-clamp-2">{{ post.content }}</p>
                  </div>
                  <div class="text-right ml-4">
                    <div class="text-sm text-gray-500 dark:text-gray-400">
                      <span class="mr-4">👁️ {{ post.viewCount || 0 }}</span>
                      <span class="mr-4">💬 {{ post.commentCount || 0 }}</span>
                      <span>❤️ {{ post.likeCount || 0 }}</span>
                    </div>
                    <div class="text-xs text-gray-400 mt-2">{{ post.createdAt ? new Date(post.createdAt).toLocaleString() : '' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 我的评论 -->
          <div v-if="activeTab === 'replies'" class="space-y-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-4">我的评论</h3>
            
            <div v-if="userComments.length === 0" class="text-center py-12">
              <div class="text-6xl mb-4">💬</div>
              <p class="text-gray-500 dark:text-gray-400">您还没有发表过评论</p>
            </div>

            <div v-else class="space-y-4">
              <div 
                v-for="comment in userComments" 
                :key="comment.id"
                class="bg-gray-50 dark:bg-gray-700 rounded-xl p-6"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <p class="text-gray-800 dark:text-white mb-2">{{ comment.content }}</p>
                    <p class="text-sm text-gray-500 dark:text-gray-400">
                      评论于：{{ comment.postTitle || '未知帖子' }}
                    </p>
                  </div>
                  <div class="text-right ml-4">
                    <div class="text-xs text-gray-400">{{ comment.createdAt ? new Date(comment.createdAt).toLocaleString() : '' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 我的消息 -->
          <div v-if="activeTab === 'messages'" class="space-y-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-xl font-bold text-gray-800 dark:text-white">我的消息</h3>
              <button
                v-if="notifications.length"
                @click="markAllRead"
                class="text-sm text-[#4f46e5] hover:underline"
              >全部标记已读</button>
            </div>

            <!-- 类型筛选：评论 / 点赞 分开 -->
            <div class="flex gap-2 mb-4">
              <button
                v-for="f in [
                  { key: 'all', label: '全部' },
                  { key: 'comment', label: '💬 评论' },
                  { key: 'like', label: '❤️ 点赞' },
                ]"
                :key="f.key"
                @click="setNotifFilter(f.key as any)"
                class="px-3 py-1.5 rounded-full text-sm transition"
                :class="notifFilter === f.key ? 'bg-[#4f46e5] text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'"
              >{{ f.label }}</button>
            </div>

            <div v-if="notificationLoading" class="text-center py-12 text-gray-500">加载中...</div>
            <div v-else-if="notifications.length === 0" class="text-center py-12 text-gray-500">
              暂无消息
            </div>
            <div v-else-if="filteredNotifications.length === 0" class="text-center py-12 text-gray-500">
              该分类下暂无消息
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="n in filteredNotifications"
                :key="n.id"
                @click="goToNotification(n)"
                class="flex items-start gap-4 p-4 rounded-xl border transition cursor-pointer"
                :class="n.isRead ? 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-[#4f46e5]/30' : 'border-[#4f46e5]/30 bg-[#4f46e5]/5'"
              >
                <div class="relative flex-shrink-0">
                  <div class="w-10 h-10 rounded-full bg-gradient-to-br from-[#4f46e5] to-purple-500 text-white flex items-center justify-center font-bold">
                    {{ (n.actorName || '?').charAt(0) }}
                  </div>
                  <span
                    v-if="!n.isRead"
                    class="absolute -top-0.5 -right-0.5 w-3 h-3 bg-red-500 rounded-full border-2 border-white dark:border-gray-800"
                  ></span>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm text-gray-800 dark:text-gray-100">
                    <span class="mr-1">{{ notifTypeMeta(n.type).icon }}</span>
                    <span class="font-semibold">{{ n.actorName || '某位用户' }}</span>
                    {{ n.content }}
                  </p>
                  <p class="text-xs text-gray-500 mt-1">
                    <span
                      class="mr-2 px-1.5 py-0.5 rounded text-[10px]"
                      :class="isCommentType(n.type)
                        ? 'bg-blue-100 text-blue-600 dark:bg-blue-900/40 dark:text-blue-300'
                        : 'bg-pink-100 text-pink-600 dark:bg-pink-900/40 dark:text-pink-300'"
                    >{{ notifTypeMeta(n.type).label }}</span>
                    {{ n.createdAt }}
                  </p>
                </div>
                <button
                  @click.stop="removeNotification(n.id)"
                  class="flex-shrink-0 text-gray-400 hover:text-red-500 text-sm px-2"
                >删除</button>
              </div>
            </div>
          </div>

          <!-- 我的模块 -->
          <div v-if="activeTab === 'modules'" class="space-y-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-4">我的模块</h3>
            
            <div class="space-y-4">
              <div class="bg-gradient-to-r from-orange-500 to-red-600 rounded-xl p-6 text-white">
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-4">
                    <span class="text-4xl">🔥</span>
                    <div>
                      <h4 class="font-bold text-xl mb-1">火影忍者社区</h4>
                      <p class="text-white/80">论坛帖子和讨论</p>
                    </div>
                  </div>
                  <a 
                    href="/forum"
                    class="inline-block px-6 py-2 bg-white text-orange-600 rounded-lg font-medium hover:bg-orange-50 transition-colors"
                  >
                    进入论坛
                  </a>
                </div>
              </div>

              <div class="bg-gradient-to-r from-purple-500 to-fuchsia-600 rounded-xl p-6 text-white">
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-4">
                    <span class="text-4xl">⚡</span>
                    <div>
                      <h4 class="font-bold text-xl mb-1">杀戮尖塔2攻略</h4>
                      <p class="text-white/80">游戏攻略与数据</p>
                    </div>
                  </div>
                  <a 
                    href="/module2"
                    class="inline-block px-6 py-2 bg-white text-purple-600 rounded-lg font-medium hover:bg-purple-50 transition-colors"
                  >
                    查看攻略
                  </a>
                </div>
              </div>

              <div class="bg-gradient-to-r from-blue-400 to-blue-600 rounded-xl p-6 text-white">
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-4">
                    <span class="text-4xl">💡</span>
                    <div>
                      <h4 class="font-bold text-xl mb-1">原神攻略</h4>
                      <p class="text-white/80">角色养成与配装</p>
                    </div>
                  </div>
                  <a 
                    href="/module3"
                    class="inline-block px-6 py-2 bg-white text-blue-600 rounded-lg font-medium hover:bg-blue-50 transition-colors"
                  >
                    查看攻略
                  </a>
                </div>
              </div>

              <div class="bg-gradient-to-r from-gray-500 to-gray-700 rounded-xl p-6 text-white">
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-4">
                    <span class="text-4xl">🎯</span>
                    <div>
                      <h4 class="font-bold text-xl mb-1">王者荣耀</h4>
                      <p class="text-white/80">王者荣耀玩家社区，讨论英雄攻略、赛事资讯、组队开黑</p>
                    </div>
                  </div>
                  <a 
                    href="/module4"
                    class="inline-block px-6 py-2 bg-white text-gray-600 rounded-lg font-medium hover:bg-gray-100 transition-colors"
                  >
                    探索更多
                  </a>
                </div>
              </div>
            </div>
          </div>

          <!-- 设置 -->
          <div v-if="activeTab === 'settings'" class="space-y-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-4">账号设置</h3>
            
            <div class="space-y-4">
              <div class="bg-gray-50 dark:bg-gray-700 rounded-xl p-6">
                <div class="flex items-center justify-between mb-4">
                  <h4 class="font-medium text-gray-800 dark:text-white">基本信息</h4>
                  <div class="flex space-x-2">
                    <button 
                      @click="openEditProfileModal"
                      class="px-4 py-2 bg-[#4f46e5] text-white text-sm rounded-lg hover:bg-[#4338ca] transition-colors"
                    >
                      修改用户名
                    </button>
                    <button 
                      @click="openEditNicknameModal"
                      class="px-4 py-2 bg-green-500 text-white text-sm rounded-lg hover:bg-green-600 transition-colors"
                    >
                      修改昵称
                    </button>
                  </div>
                </div>
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">用户名</label>
                    <input 
                      type="text"
                      :value="currentUser?.username || ''"
                      readonly
                      class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 cursor-not-allowed"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">昵称</label>
                    <input 
                      type="text"
                      :value="currentUser?.nickname || '未设置'"
                      readonly
                      class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 cursor-not-allowed"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">邮箱</label>
                    <input 
                      type="email"
                      :value="currentUser?.email || '未设置'"
                      readonly
                      class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 cursor-not-allowed"
                    />
                  </div>
                </div>
              </div>

              <div class="bg-gray-50 dark:bg-gray-700 rounded-xl p-6">
                <h4 class="font-medium text-gray-800 dark:text-white mb-4">安全设置</h4>
                <div class="space-y-3">
                  <button 
                    @click="openChangePasswordModal"
                    class="w-full text-left p-4 bg-white dark:bg-gray-800 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                  >
                    <div class="flex items-center justify-between">
                      <span class="text-gray-800 dark:text-white">修改密码</span>
                      <span class="text-gray-400">→</span>
                    </div>
                  </button>
                  <button 
                    @click="showBindEmailModal = true"
                    class="w-full text-left p-4 bg-white dark:bg-gray-800 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                  >
                    <div class="flex items-center justify-between">
                      <span class="text-gray-800 dark:text-white">绑定邮箱</span>
                      <span class="text-gray-400">→</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <Teleport to="body">
      <div v-if="showChangePasswordModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-6 w-full max-w-md">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white">修改密码</h3>
            <button @click="showChangePasswordModal = false" class="text-gray-400 hover:text-gray-600">
              ✕
            </button>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">原密码</label>
              <input 
                v-model="oldPassword"
                type="password"
                autocomplete="current-password"
                placeholder="请输入原密码"
                class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">新密码</label>
              <input 
                v-model="newPassword"
                type="password"
                autocomplete="new-password"
                placeholder="请输入新密码"
                class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">确认密码</label>
              <input 
                v-model="confirmPassword"
                type="password"
                autocomplete="new-password"
                placeholder="请再次输入新密码"
                class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200"
              />
            </div>
          </div>
          <div class="flex space-x-3 mt-6">
            <button 
              @click="showChangePasswordModal = false"
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            >
              取消
            </button>
            <button 
              @click="changePassword"
              class="flex-1 px-4 py-2 bg-[#4f46e5] text-white rounded-lg hover:bg-[#4338ca] transition-colors"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 编辑资料弹窗 -->
    <Teleport to="body">
      <div v-if="showEditProfileModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-6 w-full max-w-md">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white">编辑资料</h3>
            <button @click="showEditProfileModal = false" class="text-gray-400 hover:text-gray-600">
              ✕
            </button>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">用户名</label>
              <input 
                v-model="nickname"
                type="text"
                placeholder="请输入用户名"
                class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200"
              />
            </div>
          </div>
          <div class="flex space-x-3 mt-6">
            <button 
              @click="showEditProfileModal = false"
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            >
              取消
            </button>
            <button 
              @click="editProfile"
              class="flex-1 px-4 py-2 bg-[#4f46e5] text-white rounded-lg hover:bg-[#4338ca] transition-colors"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 修改昵称弹窗 -->
    <Teleport to="body">
      <div v-if="showEditNicknameModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-6 w-full max-w-md">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white">修改昵称</h3>
            <button @click="showEditNicknameModal = false" class="text-gray-400 hover:text-gray-600">
              ✕
            </button>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">昵称</label>
              <input 
                v-model="newNickname"
                type="text"
                placeholder="请输入昵称"
                class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200"
              />
            </div>
          </div>
          <div class="flex space-x-3 mt-6">
            <button 
              @click="showEditNicknameModal = false"
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            >
              取消
            </button>
            <button 
              @click="editNickname"
              class="flex-1 px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 绑定邮箱弹窗 -->
    <Teleport to="body">
      <div v-if="showBindEmailModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-6 w-full max-w-md">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-bold text-gray-800 dark:text-white">绑定邮箱</h3>
            <button @click="showBindEmailModal = false" class="text-gray-400 hover:text-gray-600">
              ✕
            </button>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">邮箱地址</label>
              <input 
                v-model="email"
                type="email"
                placeholder="请输入邮箱地址"
                class="w-full p-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200"
              />
            </div>
          </div>
          <div class="flex space-x-3 mt-6">
            <button 
              @click="showBindEmailModal = false"
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            >
              取消
            </button>
            <button 
              @click="bindEmail"
              class="flex-1 px-4 py-2 bg-[#4f46e5] text-white rounded-lg hover:bg-[#4338ca] transition-colors"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
