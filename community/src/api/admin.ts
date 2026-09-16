import request from './request'

export interface AdminPostItem {
  id: number
  title: string
  content: string
  boardId: number
  boardName?: string
  userId?: number
  authorName?: string
  authorAvatar?: string
  viewCount: number
  likeCount: number
  commentCount: number
  isTop: boolean
  isEssence: boolean
  createdAt: string
}

export interface AdminCommentItem {
  id: number
  postId: number
  postTitle?: string
  content: string
  authorName?: string
  createdAt: string
}

export interface AdminUserItem {
  id: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  /** 1 正常，0 禁用 */
  status: number
  /** 0 普通用户，1 管理员 */
  role: number
  /** 0 否，1 是 */
  vip?: number
  createdAt: string
}

export interface BoardItem {
  id?: number
  communityId: number
  name: string
  icon?: string
  description?: string
  sort: number
}

/** 游戏社区（论坛型社区，由管理员在后台新增） */
export interface AdminCommunityItem {
  id?: number
  name: string
  icon?: string
  description?: string
  sort: number
}

export interface AdminPage<T> {
  list: T[]
  total: number
}

/**
 * 管理后台接口
 *
 * <p>注意：这里不做 try/catch 吞错，403 需要抛到页面层用于展示「无权限」提示。
 */
export const adminApi = {
  async dashboard(): Promise<Record<string, number>> {
    const res = await request.get<Record<string, number>>('/admin/dashboard')
    return res.success && res.data ? res.data : {}
  },

  async listPosts(keyword = '', page = 1, size = 20): Promise<AdminPage<AdminPostItem>> {
    const res = await request.get<AdminPage<AdminPostItem>>('/admin/posts', { params: { keyword, page, size } })
    return res.success && res.data ? res.data : { list: [], total: 0 }
  },

  toggleTop: (id: number) => request.put(`/admin/posts/${id}/top`),
  toggleEssence: (id: number) => request.put(`/admin/posts/${id}/essence`),
  deletePost: (id: number) => request.delete(`/admin/posts/${id}`),

  async listComments(keyword = '', page = 1, size = 20): Promise<AdminPage<AdminCommentItem>> {
    const res = await request.get<AdminPage<AdminCommentItem>>('/admin/comments', { params: { keyword, page, size } })
    return res.success && res.data ? res.data : { list: [], total: 0 }
  },

  deleteComment: (id: number) => request.delete(`/admin/comments/${id}`),

  async listUsers(keyword = '', page = 1, size = 20): Promise<AdminPage<AdminUserItem>> {
    const res = await request.get<AdminPage<AdminUserItem>>('/admin/users', { params: { keyword, page, size } })
    return res.success && res.data ? res.data : { list: [], total: 0 }
  },

  updateUserStatus: (id: number, status: number) =>
    request.put(`/admin/users/${id}/status`, null, { params: { status } }),

  updateUserRole: (id: number, role: number) =>
    request.put(`/admin/users/${id}/role`, null, { params: { role } }),

  updateUserVip: (id: number, vip: number) =>
    request.put(`/admin/users/${id}/vip`, null, { params: { vip } }),

  async listBoards(): Promise<BoardItem[]> {
    const res = await request.get<BoardItem[]>('/admin/boards')
    return res.success && res.data ? res.data : []
  },

  createBoard: (board: Partial<BoardItem>) => request.post('/admin/boards', board),
  updateBoard: (id: number, board: Partial<BoardItem>) => request.put(`/admin/boards/${id}`, board),
  deleteBoard: (id: number) => request.delete(`/admin/boards/${id}`),

  // ---------------- 游戏社区管理 ----------------

  /** 社区列表（公开接口 /communities，管理员页面复用） */
  async listAdminCommunities(): Promise<AdminCommunityItem[]> {
    const res = await request.get<AdminCommunityItem[]>('/communities')
    return res.success && res.data ? res.data : []
  },

  createCommunity: (community: Partial<AdminCommunityItem>) =>
    request.post('/admin/communities', community),
  updateCommunity: (id: number, community: Partial<AdminCommunityItem>) =>
    request.put(`/admin/communities/${id}`, community),
  deleteCommunity: (id: number) => request.delete(`/admin/communities/${id}`),
}
