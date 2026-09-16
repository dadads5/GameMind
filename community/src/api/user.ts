import request from './request'
import type { PageResult, PostVO } from '../types/api'

export interface UserProfile {
  id: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  bio?: string
  role: number
  /** VIP 标识：0 否，1 是 */
  vip?: number
  likeCount?: number
  todayLiked?: boolean | null
  createdAt?: string
}

export interface UserStats {
  totalPosts: number
  totalReplies: number
  /** 帖子获赞总数 */
  totalLikes: number
  /** 主页被点赞总数 */
  homeLikeCount: number
}

export interface UserPostItem {
  id: number
  title: string
  content: string
  authorId: number
  boardId: number
  boardName?: string
  communityId?: number
  createdAt: string
  views: number
  replies: number
  likes: number
  isHot?: boolean
  authorName?: string
}

export const userApi = {
  /** 获取用户公开主页（含点赞数与今日是否已赞） */
  getProfile: async (id: number): Promise<UserProfile | null> => {
    const res = await request.get<UserProfile>(`/users/${id}`)
    return res?.success ? res.data : null
  },

  /** 给某用户主页点赞（每天一次，需登录） */
  likeUser: (id: number) =>
    request.post<{ likeCount: number; todayLiked: boolean }>(`/users/${id}/like`),

  /** 用户活跃度统计（发帖/回复/获赞/主页获赞） */
  getStats: async (id: number): Promise<UserStats | null> => {
    const res = await request.get<UserStats>(`/users/${id}/stats`)
    return res?.success ? res.data : null
  },

  /** 某用户发布的帖子 */
  getPosts: async (id: number, page = 1, size = 20) => {
    const res = await request.get<PageResult<PostVO>>(`/users/${id}/posts`, { params: { page, size } })
    if (res?.success && res.data) {
      const posts = (res.data.content ?? []).map((post): UserPostItem => ({
        id: post.id ?? 0,
        title: post.title ?? '',
        content: post.content ?? '',
        authorId: post.authorId ?? 0,
        boardId: post.boardId ?? 0,
        boardName: post.boardName,
        communityId: post.communityId,
        createdAt: post.createdAt ?? '',
        views: post.viewCount ?? 0,
        replies: post.commentCount ?? 0,
        likes: post.likeCount ?? 0,
        isHot: (post.likeCount ?? 0) > 100,
        authorName: post.authorName,
      }))
      return { posts, total: res.data.total ?? 0, totalPages: res.data.totalPages ?? 1 }
    }
    return { posts: [], total: 0, totalPages: 1 }
  },
}
