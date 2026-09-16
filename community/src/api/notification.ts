import request from './request'

export interface NotificationItem {
  id: number
  userId: number
  actorId: number
  /** LIKE_POST / LIKE_COMMENT / COMMENT_POST */
  type: string
  /** POST / COMMENT */
  targetType: string
  /** 目标 ID（三类通知统一指向帖子，便于点击跳转） */
  targetId: number
  content: string
  isRead: boolean
  createdAt: string
  actorName?: string
  actorAvatar?: string
}

export interface NotificationListResult {
  list: NotificationItem[]
  total: number
  unread: number
}

/** 站内消息通知接口（需登录，轮询方式获取，无需 Redis） */
export const notificationApi = {
  /** 消息列表（同时返回总数与未读数） */
  async list(page = 1, size = 20): Promise<NotificationListResult> {
    const res = await request.get<NotificationListResult>('/notifications', { params: { page, size } })
    if (res.success && res.data) return res.data
    return { list: [], total: 0, unread: 0 }
  },

  /** 未读数量（红点用，轮询目标） */
  async unreadCount(): Promise<number> {
    const res = await request.get<number>('/notifications/unread-count')
    return res.success && typeof res.data === 'number' ? res.data : 0
  },

  async markRead(id: number): Promise<boolean> {
    const res = await request.put(`/notifications/${id}/read`)
    return !!res.success
  },

  async markAllRead(): Promise<boolean> {
    const res = await request.put('/notifications/read-all')
    return !!res.success
  },

  async remove(id: number): Promise<boolean> {
    const res = await request.delete(`/notifications/${id}`)
    return !!res.success
  },
}
