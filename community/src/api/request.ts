import axios, {
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'

const TOKEN_KEY = 'token'

/**
 * 全局唯一 HTTP 客户端
 *
 * - baseURL 使用相对路径 `/api`，由 Vite 代理转发到后端，避免硬编码地址与跨域问题。
 * - 请求拦截器自动携带 Bearer Token。
 * - 响应拦截器直接返回后端统一响应体 `{ code, success, data, message }`，
 *   业务失败时（HTTP 非 2xx）由全局异常处理器转为 200 + success:false。
 * - 401 视为登录失效：清除本地 Token 并广播 `auth:unauthorized` 事件，由 App 跳转登录页。
 */
const axiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  },
})

export interface ApiEnvelope<T = unknown> {
  code: number
  success: boolean
  data: T
  message: string | null
}

axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 注意：拦截器把响应体改写为了后端信封，这里用 any 绕过 axios 默认的 AxiosResponse 类型约束
axiosInstance.interceptors.response.use(
  (response): any => response.data,
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      // 只有「请求时确实带了 Token」才算登录失效。
      // 游客（无 Token）访问需登录接口返回 401 属于正常现象，若此时清 Token 并跳登录页，
      // 会导致公开页面（帖子详情、社区详情等）被强制弹到登录页。
      const sentToken = error.config?.headers?.Authorization ?? error.config?.headers?.authorization
      if (sentToken) {
        localStorage.removeItem(TOKEN_KEY)
        if (window.location.pathname !== '/login') {
          window.dispatchEvent(new CustomEvent('auth:unauthorized'))
        }
      }
    }
    return Promise.reject(error)
  },
)

const request = {
  get: <T = unknown>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<ApiEnvelope<T>> =>
    axiosInstance.get(url, config) as Promise<ApiEnvelope<T>>,

  post: <T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig,
  ): Promise<ApiEnvelope<T>> =>
    axiosInstance.post(url, data, config) as Promise<ApiEnvelope<T>>,

  put: <T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig,
  ): Promise<ApiEnvelope<T>> =>
    axiosInstance.put(url, data, config) as Promise<ApiEnvelope<T>>,

  delete: <T = unknown>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<ApiEnvelope<T>> =>
    axiosInstance.delete(url, config) as Promise<ApiEnvelope<T>>,
}

export default request
