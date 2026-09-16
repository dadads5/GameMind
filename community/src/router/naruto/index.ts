import type { RouteRecordRaw } from 'vue-router'

const narutoRoutes: RouteRecordRaw[] = [
  {
    path: '/forum',
    name: 'naruto-home',
    component: () => import('../../views/naruto/HomeView.vue')
  },
  {
    path: '/forum/sections',
    name: 'naruto-sections',
    component: () => import('../../views/naruto/SectionsView.vue')
  },
  {
    path: '/forum/section/:id',
    name: 'naruto-section-detail',
    component: () => import('../../views/naruto/SectionDetailView.vue')
  },
  {
    path: '/forum/hot',
    name: 'naruto-hot',
    component: () => import('../../views/naruto/HotPostsView.vue')
  },
  {
    path: '/forum/latest',
    name: 'naruto-latest',
    component: () => import('../../views/naruto/LatestPostsView.vue')
  },
  {
    path: '/forum/post/:id',
    name: 'naruto-post-detail',
    component: () => import('../../views/naruto/PostDetailView.vue')
  },
  {
    path: '/forum/create',
    name: 'naruto-create',
    component: () => import('../../views/naruto/CreatePostView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/forum/profile',
    name: 'naruto-profile',
    component: () => import('../../views/ProfileView.vue')
  }
]

export default narutoRoutes
