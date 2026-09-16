<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../stores'
import { adminApi } from '../api/admin'
import { notifyError, notifySuccess, notifyConfirm } from '../utils/notify'

/**
 * 帖子详情页的「管理员就地操作」：置顶 / 删除
 *
 * <p>仅当当前用户 role = 1（管理员）时渲染；真正的权限校验在后端 /api/admin 接口，
 * 这里隐藏按钮只是交互层面的优化。
 */
const props = defineProps<{
  postId: number
  isTop?: boolean
}>()

const emit = defineEmits<{
  /** 操作成功，通知父级重新加载帖子 */
  (e: 'changed'): void
  /** 已删除，通知父级跳回列表 */
  (e: 'deleted'): void
}>()

const userStore = useUserStore()
const isAdmin = computed(() => userStore.currentUser?.role === 1)

const toggleTop = async () => {
  try {
    await adminApi.toggleTop(props.postId)
    notifySuccess('已切换置顶状态')
    emit('changed')
  } catch (e: any) {
    notifyError(e?.response?.data?.message || '操作失败')
  }
}

const removePost = async () => {
  if (!await notifyConfirm('确定删除这篇帖子？该帖下的评论会一并删除，且不可恢复。')) return
  try {
    await adminApi.deletePost(props.postId)
    notifySuccess('已删除')
    emit('deleted')
  } catch (e: any) {
    notifyError(e?.response?.data?.message || '删除失败')
  }
}
</script>

<template>
  <template v-if="isAdmin">
    <button
      @click="toggleTop"
      class="px-2 py-0.5 rounded-md text-xs font-medium bg-amber-500/15 text-amber-500 hover:bg-amber-500/25 transition"
    >
      {{ isTop ? '取消置顶' : '置顶' }}
    </button>
    <button
      @click="removePost"
      class="px-2 py-0.5 rounded-md text-xs font-medium bg-red-500/15 text-red-500 hover:bg-red-500/25 transition"
    >
      删除（管理）
    </button>
  </template>
</template>
