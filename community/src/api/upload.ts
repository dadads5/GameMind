import request from './request'

/**
 * 文件上传接口
 */
export const uploadApi = {
  /**
   * 上传头像，返回后端存储的可访问 URL（如 /uploads/avatars/xxx.png）
   */
  uploadAvatar: async (file: File): Promise<string> => {
    const form = new FormData()
    form.append('file', file)
    const res = await request.post<{ url: string }>('/upload/avatar', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (!res?.success || !res.data?.url) {
      throw new Error(res?.message || '头像上传失败')
    }
    return res.data.url
  },
}

export default uploadApi
