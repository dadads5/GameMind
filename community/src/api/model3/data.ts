/**
 * 原神社区 API
 *
 * <p>具体实现已收敛到 api/community.ts（原神与火影共用同一份），
 * 本文件只负责绑定原神的社区 ID 与默认板块 ID，
 * 页面侧的 import 路径与导出名保持不变。
 *
 * <p>注意：model2（杀戮尖塔）与 model4（王者荣耀）历史上也从本文件导入帖子接口，
 * 它们调用时会显式传入自己的 communityId，默认行为与原实现一致。
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

/** 原神社区 ID（community_id=1，子板块 1~6）；历史总入口板块 id=1（蒙德） */
const GENSHIN_COMMUNITY_ID = COMMUNITY_ID.genshin
const GENSHIN_BOARD_ID = 1

export const getPostsFromBackend = async (params: PostPageParams = {}): Promise<PostPage> =>
  fetchPosts(params, GENSHIN_COMMUNITY_ID)

export const getPostById = fetchPostById

export const getRepliesFromBackend = fetchComments

export const getHotPosts = async (limit = 20): Promise<Post[]> =>
  fetchHotPosts(GENSHIN_COMMUNITY_ID, limit)

export const getGlobalHotPosts = fetchGlobalHotPosts

export const getOnlineCount = fetchOnlineCount

export const uploadImage = uploadImageFile

export const createPost = async (
  title: string,
  content: string,
  boardId: number = GENSHIN_BOARD_ID,
  images: string[] = [],
): Promise<boolean> => createPostRequest(title, content, boardId, images)

export const updatePost = updatePostRequest

export const createComment = createCommentRequest

export { getAvatarUrl, onAvatarError }

export type { BackendComment, BackendPost, Post, PostPage, PostPageParams, Reply }
