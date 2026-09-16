import { defineStore } from 'pinia'
import { notificationApi } from '../api/notification'

/**
 * 未读消息状态管理
 *
 * <p>全站共用一份未读数与一个轮询定时器：顶栏红点、个人中心 Tab 红点都读这里，
 * 避免多处各自轮询造成重复请求。
 */
export const useNotificationStore = defineStore('notification', {
  state: () => ({
    unreadCount: 0,
    timer: null as number | null,
  }),
  actions: {
    /** 拉取一次未读数 */
    async fetchUnread() {
      try {
        this.unreadCount = await notificationApi.unreadCount()
      } catch {
        // 静默失败：未读红点不影响主流程
      }
    },

    /** 启动轮询（默认 30 秒一次），重复调用不会创建多个定时器 */
    startPolling(intervalMs = 30000) {
      this.fetchUnread()
      if (this.timer) return
      this.timer = window.setInterval(() => this.fetchUnread(), intervalMs)
    },

    stopPolling() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },

    setUnread(count: number) {
      this.unreadCount = count
    },

    /** 读了一条消息时本地递减，避免等待下一次轮询 */
    decrement() {
      if (this.unreadCount > 0) this.unreadCount -= 1
    },

    /** 登出时清空 */
    reset() {
      this.stopPolling()
      this.unreadCount = 0
    },
  },
})
