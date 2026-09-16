/**
 * 用户等级与徽章评级
 *
 * 计分规则（活跃度积分）：
 *   score = 发帖数 × 2 + 回复数 × 1 + 帖子获赞 × 0.5 + 主页获赞 × 2
 *
 * 等级阈值（从 Lv3 之后每次升级所需积分 ×10，即第 4 级起陡增）：
 *   Lv1=0  Lv2=10  Lv3=30  Lv4=330  Lv5=730  Lv6=1230
 *   Lv7=1830  Lv8=2530  Lv9=3330  Lv10=4230
 */

export interface UserRatingInput {
  /** 角色：0 普通用户，1 管理员 */
  role?: number
  /** VIP 标识：0 否，1 是 */
  vip?: number
  /** 注册时间（ISO 字符串） */
  createdAt?: string
  /** 主页被点赞总数 */
  homeLikeCount?: number
  totalPosts?: number
  totalReplies?: number
  /** 帖子获赞总数 */
  totalLikes?: number
}

export interface UserBadge {
  name: string
  icon: string
  /** 颜色主题：yellow / purple / red / blue / green / indigo / pink / gold */
  color: string
}

export interface UserRating {
  level: number
  badges: UserBadge[]
}

// 各等级所需累计积分（下标 0 对应 Lv1）
const LEVEL_THRESHOLDS = [0, 10, 30, 330, 730, 1230, 1830, 2530, 3330, 4230]

function computeLevel(score: number): number {
  let level = 1
  for (let i = 0; i < LEVEL_THRESHOLDS.length; i++) {
    if (score >= LEVEL_THRESHOLDS[i]) level = i + 1
    else break
  }
  return level
}

function daysSinceJoin(createdAt?: string): number | null {
  if (!createdAt) return null
  const t = new Date(createdAt).getTime()
  if (Number.isNaN(t)) return null
  return Math.floor((Date.now() - t) / (1000 * 60 * 60 * 24))
}

/**
 * 根据用户信息计算等级与徽章
 */
export function computeUserRating(input: UserRatingInput): UserRating {
  const totalPosts = input.totalPosts ?? 0
  const totalReplies = input.totalReplies ?? 0
  const totalLikes = input.totalLikes ?? 0
  const homeLikeCount = input.homeLikeCount ?? 0
  const days = daysSinceJoin(input.createdAt)

  const score =
    totalPosts * 2 + totalReplies * 1 + totalLikes * 0.5 + homeLikeCount * 2
  const level = computeLevel(score)

  const badges: UserBadge[] = []

  // 管理员（最高优先级，置顶展示）
  if (input.role === 1) {
    badges.push({ name: '管理员', icon: '👑', color: 'indigo' })
  }
  // VIP 用户
  if (input.vip === 1) {
    badges.push({ name: 'VIP用户', icon: '💎', color: 'pink' })
  }
  // 老用户
  if (days !== null && days >= 30) {
    badges.push({ name: '老用户', icon: '🏆', color: 'yellow' })
  }
  // 活跃用户：发帖 >= 10 或 发帖+回复 >= 30
  if (totalPosts >= 10 || totalPosts + totalReplies >= 30) {
    badges.push({ name: '活跃用户', icon: '⭐', color: 'purple' })
  }
  // 人气王：帖子获赞 >= 50 或 主页获赞 >= 20
  if (totalLikes >= 50 || homeLikeCount >= 20) {
    badges.push({ name: '人气王', icon: '🔥', color: 'red' })
  }
  // 评论达人
  if (totalReplies >= 20) {
    badges.push({ name: '评论达人', icon: '💬', color: 'blue' })
  }
  // 资深用户：等级 >= 4
  if (level >= 4) {
    badges.push({ name: '资深用户', icon: '🎖️', color: 'gold' })
  }
  // 新人王：注册 <= 7 天且发帖 >= 3
  if (days !== null && days <= 7 && totalPosts >= 3) {
    badges.push({ name: '新人王', icon: '🌱', color: 'green' })
  }

  return { level, badges }
}
