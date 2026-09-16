import { defineStore } from 'pinia'

// 主题模式状态管理
export const useThemeStore = defineStore('theme', {
  state: () => ({
    darkMode: false
  }),
  actions: {
    toggleDarkMode() {
      this.darkMode = !this.darkMode
      // 应用主题到 DOM
      if (this.darkMode) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  }
})

// 用户状态管理
interface UserState {
  id?: number
  username?: string
  nickname?: string
  avatar?: string
  level?: string
  /** 角色：0 普通用户，1 管理员 */
  role?: number
}

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null as UserState | null,
    isLoggedIn: false
  }),
  actions: {
    setUser(user: UserState) {
      this.currentUser = user
      this.isLoggedIn = true
    },
    clearUser() {
      this.currentUser = null
      this.isLoggedIn = false
    },
    updateNickname(nickname: string) {
      if (this.currentUser) {
        this.currentUser.nickname = nickname
      }
    },
    updateUsername(username: string) {
      if (this.currentUser) {
        this.currentUser.username = username
      }
    },
    updateAvatar(avatar: string) {
      if (this.currentUser) {
        this.currentUser.avatar = avatar
      }
    }
  }
})