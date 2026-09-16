import request from './request'
import type { PageResult } from '../types/api'

/**
 * 社区内容 API 的统一实现
 *
 * <p>原神（model3）与火影（naruto）曾经各自维护一份几乎完全相同的 data.ts，
 * 差异只有一个社区 ID，约 600 行重复代码。现收敛到本文件，
 * 由 api/naruto/data.ts 与 api/model3/data.ts 做薄封装（绑定各自社区 ID），
 * 页面侧的 import 路径保持不变。
 */

/** 社区 ID 常量：1=原神，2=火影忍者 */
export const COMMUNITY_ID = {
  genshin: 1,
  naruto: 2,
} as const

/** 评论列表单页条数（与后端 CommentService 的单页上限保持一致） */
const COMMENT_PAGE_SIZE = 100

export interface Post {
  id: number
  title: string
  content: string
  authorId: number
  boardId: number
  boardName: string
  createdAt: string
  views: number
  replies: number
  likes: number
  isHot: boolean
  /** 当前用户是否已点赞（由后端 liked 字段映射） */
  liked?: boolean
  /** 配图 URL 列表 */
  images?: string[]
  tag: string
  authorName?: string
  authorAvatar?: string
}

export interface Reply {
  id: number
  postId: number
  authorId: number
  content: string
  createdAt: string
  authorName?: string
  authorAvatar?: string
  /** 评论点赞数（由后端 likeCount 映射） */
  likeCount?: number
}

/** 后端返回的帖子类型（对应后端 PostVO） */
export interface BackendPost {
  id: number
  authorId: number
  boardId: number
  boardName: string
  title: string
  content: string
  viewCount: number
  likeCount: number
  commentCount: number
  createdAt: string
  authorName: string
  authorAvatar?: string
  images?: string[]
  /** 当前登录用户是否已点赞该帖，由后端按 Token 计算 */
  liked?: boolean
}

/** 后端返回的评论类型（对应后端 CommentVO） */
export interface BackendComment {
  id: number
  postId: number
  authorId: number
  content: string
  createdAt: string
  authorName: string
  authorAvatar?: string
  likeCount?: number
}

/** 帖子分页查询参数 */
export interface PostPageParams {
  page?: number
  size?: number
  sort?: 'latest' | 'hot'
  keyword?: string
  /** 指定板块时走 /posts?boardId=，否则按社区聚合 */
  boardId?: number
  /** 社区 ID；不传 boardId 时优先按社区聚合其下所有板块 */
  communityId?: number
}

/** 帖子分页结果 */
export interface PostPage {
  posts: Post[]
  total: number
  totalPages: number
  page: number
  size: number
}

// 后端 PostVO -> 前端 Post
const mapPost = (post: BackendPost): Post => ({
  id: post.id,
  title: post.title,
  content: post.content,
  authorId: post.authorId,
  boardId: post.boardId,
  boardName: post.boardName,
  createdAt: formatDateTime(post.createdAt),
  views: post.viewCount,
  replies: post.commentCount,
  likes: post.likeCount,
  isHot: post.likeCount > 100,
  // 列表页同样需要点赞态，否则列表与详情页的点赞按钮状态不一致
  liked: post.liked ?? false,
  tag: '讨论',
  authorName: post.authorName,
  authorAvatar: post.authorAvatar,
  images: post.images ?? [],
})

/** 从后端分页获取帖子（支持板块筛选 / 关键词 / 排序 / 分页） */
export const fetchPosts = async (
  params: PostPageParams = {},
  defaultCommunityId: number,
): Promise<PostPage> => {
  const empty: PostPage = {
    posts: [],
    total: 0,
    totalPages: 0,
    page: params.page ?? 1,
    size: params.size ?? 10,
  }
  try {
    const query: Record<string, unknown> = {
      page: params.page ?? 1,
      size: params.size ?? 10,
      sort: params.sort ?? 'latest',
    }
    if (params.keyword) query.keyword = params.keyword

    // 指定板块 -> 按板块；否则按社区聚合（含其下所有子板块）
    if (params.boardId) {
      query.boardId = params.boardId
    } else {
      query.communityId = params.communityId ?? defaultCommunityId
    }

    const res = await request.get<PageResult<BackendPost>>('/posts', { params: query })
    if (!res.success || !res.data || !res.data.content) return empty
    return {
      posts: res.data.content.map(mapPost),
      total: res.data.total,
      totalPages: res.data.totalPages,
      page: res.data.page,
      size: res.data.size,
    }
  } catch (error) {
    console.error('获取帖子数据失败:', error)
    return empty
  }
}

/** 从后端获取单个帖子详情 */
export const fetchPostById = async (postId: number): Promise<Post | null> => {
  try {
    const res = await request.get<BackendPost>(`/posts/${postId}`)
    if (!res.success || !res.data) return null
    const post = res.data
    return {
      id: post.id,
      title: post.title,
      content: post.content,
      authorId: post.authorId,
      boardId: post.boardId,
      boardName: post.boardName,
      createdAt: formatDateTime(post.createdAt),
      views: post.viewCount,
      replies: post.commentCount,
      likes: post.likeCount,
      isHot: post.likeCount > 100,
      liked: post.liked ?? false,
      tag: '讨论',
      authorName: post.authorName,
      authorAvatar: post.authorAvatar,
      images: post.images ?? [],
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    return null
  }
}

/**
 * 从后端获取帖子评论列表
 *
 * <p>后端已分页（默认 20 条、单页上限 100）。这里默认取第一页最多 100 条，
 * 既覆盖绝大多数帖子，又避免热帖一次性拉取全量评论。
 */
export const fetchComments = async (postId: number): Promise<Reply[]> => {
  try {
    const res = await request.get<PageResult<BackendComment>>(`/comments/post/${postId}`, {
      params: { page: 1, size: COMMENT_PAGE_SIZE },
    })
    if (!res.success || !res.data) return []
    return (res.data.content ?? []).map((comment) => ({
      id: comment.id,
      postId: comment.postId,
      authorId: comment.authorId,
      content: comment.content,
      createdAt: formatDateTime(comment.createdAt),
      authorName: comment.authorName,
      authorAvatar: comment.authorAvatar,
      // 带上服务端点赞数，否则未点赞过的评论恒显示 0
      likeCount: comment.likeCount ?? 0,
    }))
  } catch (error) {
    console.error('获取评论数据失败:', error)
    return []
  }
}

/** 从后端获取热门帖子（服务端按浏览/点赞/评论综合排序） */
export const fetchHotPosts = async (communityId: number, limit = 20): Promise<Post[]> => {
  try {
    const res = await request.get<BackendPost[]>('/posts/hot', { params: { limit, communityId } })
    if (!res.success || !res.data) return []
    return res.data.map(mapPost)
  } catch (error) {
    console.error('获取热门帖子失败:', error)
    return []
  }
}

/** 获取全站热门帖子（不带社区筛选，用于社区总首页） */
export const fetchGlobalHotPosts = async (limit = 10): Promise<Post[]> => {
  try {
    const res = await request.get<BackendPost[]>('/posts/hot', { params: { limit } })
    if (!res.success || !res.data) return []
    return res.data.map(mapPost)
  } catch (error) {
    console.error('获取全站热门帖子失败:', error)
    return []
  }
}

/** 获取当前在线用户数（后端按最近 5 分钟活跃统计） */
export const fetchOnlineCount = async (): Promise<number> => {
  try {
    // 仅取在线人数这一项，不拉取整份统计总览
    const res = await request.get<{ onlineUsers: number }>('/statistics/online')
    if (!res.success || !res.data) return 0
    return res.data.onlineUsers ?? 0
  } catch (error) {
    console.error('获取在线人数失败:', error)
    return 0
  }
}

/** 上传帖子配图，返回可访问的 URL（null 表示失败） */
export const uploadImageFile = async (file: File): Promise<string | null> => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post<{ url: string }>('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (res.success && res.data?.url) return res.data.url
    return null
  } catch (error) {
    console.error('上传图片失败:', error)
    return null
  }
}

/** 创建帖子（用户身份由后端 Token 解析，无需传 userId） */
export const createPostRequest = async (
  title: string,
  content: string,
  boardId: number,
  images: string[] = [],
): Promise<boolean> => {
  try {
    const res = await request.post('/posts', { title, content, boardId, images })
    return res.success
  } catch (error) {
    console.error('创建帖子失败:', error)
    return false
  }
}

/** 编辑帖子（作者身份由后端 Token 解析） */
export const updatePostRequest = async (
  postId: number,
  title: string,
  content: string,
  boardId: number,
  images: string[] = [],
): Promise<boolean> => {
  try {
    const res = await request.put(`/posts/${postId}`, { title, content, boardId, images })
    return res.success
  } catch (error) {
    console.error('编辑帖子失败:', error)
    return false
  }
}

/** 创建评论 */
export const createCommentRequest = async (postId: number, content: string): Promise<boolean> => {
  try {
    const res = await request.post(`/comments/post/${postId}`, { content })
    return res.success
  } catch (error) {
    console.error('创建评论失败:', error)
    return false
  }
}

/** 格式化日期时间 */
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return '未知时间'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/** 头像地址：优先用后端返回的真实头像，缺失时用昵称首字母生成默认头像（避免破图） */
export const getAvatarUrl = (avatar?: string, name?: string): string => {
  if (avatar && avatar.trim()) return avatar
  return getDefaultAvatarUrl(name)
}

/** 昵称首字母默认头像（SVG data URL） */
const getDefaultAvatarUrl = (name?: string): string => {
  const ch = (name && name.trim()[0]) || '?'
  const colors = ['#ff4d00', '#228b22', '#ffd700', '#ff6347', '#32cd32']
  const color = colors[(ch.charCodeAt(0) || 0) % colors.length]
  return `data:image/svg+xml;utf8,${encodeURIComponent(
    `<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40'><rect width='40' height='40' rx='20' fill='${color}'/><text x='50%' y='50%' dy='.35em' text-anchor='middle' fill='white' font-size='20' font-family='sans-serif'>${ch}</text></svg>`,
  )}`
}

/** 头像加载失败时回退到默认首字母头像 */
export const onAvatarError = (e: Event, name?: string): void => {
  const img = e.target as HTMLImageElement
  img.src = getDefaultAvatarUrl(name)
}
