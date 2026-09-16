import { ref, type Ref } from 'vue'
import { getPostsFromBackend, type Post, type PostPageParams } from '../api/naruto/data'

export interface UsePostListOptions {
  /** 指定板块 ID（走 /posts?boardId=），不传则为整个游戏板块；可传 getter 以支持路由参数变化 */
  boardId?: number | (() => number | undefined)
  /** 每页条数，默认 10 */
  size?: number
}

/**
 * 帖子列表通用组合式函数：封装分页、排序、关键词搜索与「加载更多」。
 *
 * 用法：
 * <pre>
 *   const { posts, page, totalPages, sort, keyword, isLoading, fetch, changeSort, search, loadMore } = usePostList()
 * </pre>
 */
export function usePostList(options: UsePostListOptions = {}) {
  const posts: Ref<Post[]> = ref([])
  const page = ref(1)
  const totalPages = ref(0)
  const total = ref(0)
  const sort = ref<'latest' | 'hot'>('latest')
  const keyword = ref('')
  const isLoading = ref(false)
  const size = options.size ?? 10

  const fetch = async (reset = true) => {
    if (reset) page.value = 1
    isLoading.value = true
    try {
      const bid = typeof options.boardId === 'function' ? options.boardId() : options.boardId
      const params: PostPageParams = {
        page: page.value,
        size,
        sort: sort.value,
        keyword: keyword.value || undefined,
        boardId: bid,
      }
      const res = await getPostsFromBackend(params)
      // 加载更多则追加，否则替换
      posts.value = reset ? res.posts : [...posts.value, ...res.posts]
      totalPages.value = res.totalPages
      total.value = res.total
      page.value = res.page
    } catch (e) {
      console.error('加载帖子失败:', e)
    } finally {
      isLoading.value = false
    }
  }

  /** 切换排序方式（最新 / 最热），会重置列表 */
  const changeSort = (s: 'latest' | 'hot') => {
    if (sort.value === s) return
    sort.value = s
    fetch(true)
  }

  /** 关键词搜索，重置列表 */
  const search = (kw: string) => {
    keyword.value = kw
    fetch(true)
  }

  /** 加载下一页（追加） */
  const loadMore = () => {
    if (isLoading.value || page.value >= totalPages.value) return
    page.value += 1
    fetch(false)
  }

  /** 跳转到指定页（替换） */
  const goPage = (p: number) => {
    if (p < 1 || p > totalPages.value || p === page.value) return
    page.value = p
    fetch(true)
  }

  const hasMore = () => page.value < totalPages.value

  return {
    posts,
    page,
    totalPages,
    total,
    sort,
    keyword,
    isLoading,
    hasMore,
    fetch,
    changeSort,
    search,
    loadMore,
    goPage,
  }
}

export default usePostList
