import request, { type ApiEnvelope } from './request'
import type {
  AuthUser,
  AuthVO,
  ChangePasswordData,
  LoginData,
  RegisterData,
  UpdateProfileData,
} from '../types/api'

/**
 * 认证相关接口
 *
 * 内部统一使用 `request`（单个 axios 实例），不再使用散落的 fetch。
 */
export const authApi = {
  login: (data: LoginData): Promise<ApiEnvelope<AuthVO>> =>
    request.post('/auth/login', data),

  register: (data: RegisterData): Promise<ApiEnvelope<AuthVO>> =>
    request.post('/auth/register', data),

  /** 获取当前登录用户资料，未登录返回 null */
  getUserInfo: async (): Promise<AuthUser | null> => {
    const res = await request.get<AuthUser>('/auth/me')
    return res?.success ? res.data : null
  },

  /** 更新资料（用户名 / 昵称 / 邮箱 / 简介 / 头像） */
  updateProfile: (data: UpdateProfileData): Promise<ApiEnvelope<AuthUser>> =>
    request.put('/auth/profile', data),

  /** 修改密码 */
  changePassword: (data: ChangePasswordData): Promise<ApiEnvelope<unknown>> =>
    request.post('/auth/change-password', data),

  /** 检查用户名是否可用（注册页使用） */
  checkUsername: (username: string): Promise<ApiEnvelope<{ available: boolean }>> =>
    request.get('/auth/check-username', { params: { username } }),

  logout: async (): Promise<void> => {
    try {
      await request.post('/auth/logout')
    } catch {
      // 忽略退出接口的网络错误，前端无论如何都要清除本地状态
    }
    localStorage.removeItem('token')
  },
}

export default authApi
