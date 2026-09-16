/**
 * 火影忍者社区 API
 *
 * <p>具体实现已收敛到 api/community.ts（原神与火影共用同一份），
 * 本文件只负责绑定火影的社区 ID 与默认板块 ID，
 * 页面侧的 import 路径与导出名保持不变。
 */
import {
  COMMUNITY_ID,
  createCommentRequest,
  createPostRequest,
  fetchComments,
  fetchGlobalHotPosts,
  fetchHotPosts,
  fetchOnlineCount,
  fetchPostById,
  fetchPosts,
  getAvatarUrl,
  onAvatarError,
  updatePostRequest,
  uploadImageFile,
  type BackendComment,
  type BackendPost,
  type Post,
  type PostPage,
  type PostPageParams,
  type Reply,
} from '../community'

/** 火影忍者社区 ID（community_id=2，子板块 8~13）；历史总入口板块 id=7 */
const NARUTO_COMMUNITY_ID = COMMUNITY_ID.naruto
const NARUTO_BOARD_ID = 7

export const getPostsFromBackend = async (params: PostPageParams = {}): Promise<PostPage> =>
  fetchPosts(params, NARUTO_COMMUNITY_ID)

export const getPostById = fetchPostById

export const getRepliesFromBackend = fetchComments

export const getHotPosts = async (limit = 20): Promise<Post[]> =>
  fetchHotPosts(NARUTO_COMMUNITY_ID, limit)

export const getGlobalHotPosts = fetchGlobalHotPosts

export const getOnlineCount = fetchOnlineCount

export const uploadImage = uploadImageFile

export const createPost = async (
  title: string,
  content: string,
  boardId: number = NARUTO_BOARD_ID,
  images: string[] = [],
): Promise<boolean> => createPostRequest(title, content, boardId, images)

export const updatePost = updatePostRequest

export const createComment = createCommentRequest

export { getAvatarUrl, onAvatarError }

export type { BackendComment, BackendPost, Post, PostPage, PostPageParams, Reply }

export const carouselImages = [
  { id: 1, url: '/background/hengmada1.jpg', title: '孤高的传说', description: '主宇智波斑', video: '/huo/ban.mp4' },
  { id: 2, url: '/background/hengob1.jpg', title: '白面具', description: '宇智波带土', video: '/huo/baimian.mp4' },
  { id: 3, url: '/background/taoshi.webp', title: '桃式', description: '大筒木桃式', video: '/huo/taoshi.mp4' },
  { id: 4, url: '/background/zuozhuhaibao.webp', title: '永恒万花筒', description: '宇智波佐助', video: '/huo/zuozhu.mp4' },
  { id: 5, url: '/background/hengshui.jpg', title: '第七班', description: '青水', video: '/huo/qingshui.mp4' }
]
