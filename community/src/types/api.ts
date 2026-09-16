/**
 * 全局共享类型定义
 *
 * 与后端 VO / DTO 一一对应，避免各页面重复声明 `any`。
 */

export interface ApiEnvelope<T = unknown> {
  code: number
  success: boolean
  data: T
  message: string | null
}

export interface PageResult<T> {
  content: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface AuthUser {
  id?: number
  username?: string
  nickname?: string
  email?: string
  avatar?: string
  bio?: string
  createdAt?: string
  [key: string]: unknown
}

export interface LoginData {
  username: string
  password: string
  rememberMe?: boolean
}

/** 登录 / 注册返回，对应后端 AuthVO */
export interface AuthVO {
  token: string
  tokenType?: string
  expiresIn?: number
  user?: AuthUser
}

export interface RegisterData {
  username: string
  email: string
  password: string
}

export interface ChangePasswordData {
  oldPassword: string
  newPassword: string
}

export interface UpdateProfileData {
  username?: string
  nickname?: string
  email?: string
  bio?: string
  avatar?: string
}

export interface PostVO {
  id?: number
  authorId?: number
  authorName?: string
  authorAvatar?: string
  boardId?: number
  boardName?: string
  communityId?: number
  title?: string
  content?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  liked?: boolean
  createdAt?: string
  [key: string]: unknown
}

export interface CommentVO {
  id?: number
  postId?: number
  userId?: number
  authorName?: string
  authorAvatar?: string
  content?: string
  likeCount?: number
  liked?: boolean
  postTitle?: string
  createdAt?: string
  [key: string]: unknown
}

export interface BoardVO {
  id?: number
  name?: string
  description?: string
  [key: string]: unknown
}

export interface CharacterVO {
  id?: number
  name?: string
  icon?: string
  subtitle?: string
  weapons?: string
  artifacts?: string
  stats?: string
  talents?: string
  element?: string
  createdAt?: string
  [key: string]: unknown
}

export interface StatisticsVO {
  totalUsers?: number
  todayNewUsers?: number
  totalPosts?: number
  todayPosts?: number
  totalViews?: number
  totalComments?: number
  todayComments?: number
}
