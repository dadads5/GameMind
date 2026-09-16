<script setup lang="ts">
import { getAvatarUrl, onAvatarError } from '../api/model3/data'

const props = withDefaults(
  defineProps<{
    authorId?: number | null
    name?: string
    avatar?: string
    size?: 'xs' | 'sm' | 'md' | 'lg'
    showAvatar?: boolean
  }>(),
  { size: 'sm', showAvatar: true },
)

const sizeMap = {
  xs: 'w-5 h-5',
  sm: 'w-6 h-6',
  md: 'w-8 h-8',
  lg: 'w-10 h-10',
}
</script>

<template>
  <router-link
    v-if="authorId"
    :to="`/user/${authorId}`"
    @click.stop
    class="inline-flex items-center gap-2 group max-w-full hover:opacity-90 transition"
  >
    <img
      v-if="showAvatar && (avatar || name)"
      :src="getAvatarUrl(avatar, name)"
      @error="onAvatarError($event, name)"
      :class="sizeMap[size]"
      class="rounded-full object-cover flex-shrink-0"
    />
    <span class="truncate group-hover:underline">{{ name }}</span>
  </router-link>
  <span v-else class="truncate">{{ name }}</span>
</template>
