import request from './request'

export interface BoardItem {
  id: number
  name: string
  description?: string
  icon?: string
  communityId?: number
}

export interface CommunityItem {
  id: number
  name: string
  description?: string
  icon?: string
}

// 获取后端真实板块列表（用于发帖选板块、板块列表等）
// - communityId 指定时拉该社区下板块（如 2=火影忍者），否则拉全部板块
// - 失败返回空数组，由调用方回退到本地 mock，保证不阻塞交互
export const listBoards = async (communityId?: number): Promise<BoardItem[]> => {
  try {
    const url = communityId ? `/boards/community/${communityId}` : '/boards'
    const res = await request.get<BoardItem[]>(url)
    return res.success && res.data ? res.data : []
  } catch {
    return []
  }
}

// 社区列表（原神 / 火影忍者 ...）
export const listCommunities = async (): Promise<CommunityItem[]> => {
  try {
    const res = await request.get<CommunityItem[]>('/communities')
    return res.success && res.data ? res.data : []
  } catch {
    return []
  }
}
