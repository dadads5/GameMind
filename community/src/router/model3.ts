import type { RouteRecordRaw } from 'vue-router'

const model3Routes: RouteRecordRaw[] = [
  {
    path: '/module3',
    name: 'module3',
    component: () => import('../views/model3/HomeView.vue'),
  },
  {
    path: '/module3/forum',
    name: 'module3-forum',
    component: () => import('../views/model3/ForumView.vue'),
  },
  {
    path: '/module3/post/:id',
    name: 'module3-post-detail',
    component: () => import('../views/model3/PostDetailView.vue'),
  },
  {
    path: '/module3/create',
    name: 'module3-create',
    component: () => import('../views/model3/CreatePostView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/module3/calculator',
    name: 'module3-calculator',
    component: () => import('../views/model3/DamageCalculatorView.vue'),
  },
  {
    path: '/module3/gacha',
    name: 'module3-gacha',
    component: () => import('../views/model3/GachaSimulatorView.vue'),
  },
  {
    path: '/module3/build',
    name: 'module3-build',
    component: () => import('../views/model3/CharacterBuildView.vue'),
  },
  {
    path: '/module3/news',
    name: 'module3-news',
    component: () => import('../views/model3/NewsView.vue'),
  },
]

export default model3Routes
