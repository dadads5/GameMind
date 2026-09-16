import request from './request'
import type { StatisticsVO } from '../types/api'

/**
 * 平台统计接口
 */
export const statisticsApi = {
  /** 首页一次请求拿全平台数据 */
  overview: () => request.get<StatisticsVO>('/statistics'),
}

export default statisticsApi
