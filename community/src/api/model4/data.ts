import request from '../request'

/** 王者荣耀社区 ID（community_id = 4） */
export const WZRY_COMMUNITY_ID = 4

export interface WzHero {
  id: number
  name: string
  title: string
  role: string
  lane: string
  difficulty: number
  priceGold: number
  priceCoupon: number
  icon: string
  avatar?: string
  skills: string
  builds: string
  runes: string
  tips: string
  createdAt?: string
}

/** 英雄职业与分路常量 */
export const HERO_ROLES = ['坦克', '战士', '刺客', '法师', '射手', '辅助'] as const
export const HERO_LANES = ['对抗路', '打野', '中路', '发育路', '游走'] as const

/** 职业配色（金红主题下的柔和区分色） */
export const roleColor: Record<string, string> = {
  坦克: 'text-sky-300 bg-sky-500/15',
  战士: 'text-amber-300 bg-amber-500/15',
  刺客: 'text-fuchsia-300 bg-fuchsia-500/15',
  法师: 'text-violet-300 bg-violet-500/15',
  射手: 'text-emerald-300 bg-emerald-500/15',
  辅助: 'text-rose-300 bg-rose-500/15',
}

/** 英雄列表，支持职业 / 分路 / 关键词 */
export const listHeroes = async (
  params: { role?: string; lane?: string; keyword?: string } = {},
): Promise<WzHero[]> => {
  try {
    const query: Record<string, unknown> = {}
    if (params.role) query.role = params.role
    if (params.lane) query.lane = params.lane
    if (params.keyword) query.keyword = params.keyword
    const res = await request.get<WzHero[]>('/wzry/heroes', { params: query })
    return res.success && res.data ? res.data : []
  } catch (error) {
    console.error('获取英雄列表失败:', error)
    return []
  }
}

/** 英雄详情 */
export const getHeroById = async (id: number): Promise<WzHero | null> => {
  try {
    const res = await request.get<WzHero>(`/wzry/heroes/${id}`)
    return res.success && res.data ? res.data : null
  } catch (error) {
    console.error('获取英雄详情失败:', error)
    return null
  }
}

/** 随机推荐英雄 */
export const getRecommendedHeroes = async (limit = 6): Promise<WzHero[]> => {
  try {
    const res = await request.get<WzHero[]>('/wzry/heroes/recommend', { params: { limit } })
    return res.success && res.data ? res.data : []
  } catch (error) {
    console.error('获取推荐英雄失败:', error)
    return []
  }
}

/** 英雄统计：{ total, byRole, byLane } */
export const getHeroStatistics = async (): Promise<{ total: number } | null> => {
  try {
    const res = await request.get<{ total: number }>('/wzry/heroes/statistics')
    return res.success && res.data ? res.data : null
  } catch (error) {
    console.error('获取英雄统计失败:', error)
    return null
  }
}

/** 昵称首字母默认头像（SVG data URL），避免破图 */
const defaultHeroAvatar = (name?: string): string => {
  const ch = (name && name.trim()[0]) || '?'
  const colors = ['#d4af37', '#e8b923', '#c8952a', '#f0c75e', '#b8860b']
  const color = colors[(ch.charCodeAt(0) || 0) % colors.length]
  return `data:image/svg+xml;utf8,${encodeURIComponent(
    `<svg xmlns='http://www.w3.org/2000/svg' width='96' height='96'><rect width='96' height='96' rx='24' fill='${color}'/><text x='50%' y='50%' dy='.35em' text-anchor='middle' fill='white' font-size='44' font-family='sans-serif'>${ch}</text></svg>`,
  )}`
}

/** 英雄头像：优先真实头像，缺失时回退首字母头像 */
export const getHeroAvatar = (hero?: { avatar?: string; name?: string }): string => {
  if (hero?.avatar && hero.avatar.trim()) return hero.avatar
  return defaultHeroAvatar(hero?.name)
}

/** 头像加载失败时回退 */
export const onHeroAvatarError = (e: Event, name?: string): void => {
  const img = e.target as HTMLImageElement
  img.src = defaultHeroAvatar(name)
}
