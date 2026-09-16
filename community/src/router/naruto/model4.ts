import type { RouteRecordRaw } from 'vue-router'

const model4Routes: RouteRecordRaw[] = [
  {
    path: '/module4',
    name: 'module4',
    component: () => import('../../views/model4/HomePage.vue')
  },
  {
    path: '/module4/heroes',
    name: 'module4-heroes',
    component: () => import('../../views/model4/HeroesPage.vue')
  },
  {
    path: '/module4/heroes/:id',
    name: 'module4-hero-detail',
    component: () => import('../../views/model4/HeroDetailPage.vue')
  },
  {
    path: '/module4/forum',
    name: 'module4-forum',
    component: () => import('../../views/model4/ForumPage.vue')
  },
  {
    path: '/module4/create',
    name: 'module4-create',
    component: () => import('../../views/model4/CreatePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/module4/posts/:id',
    name: 'module4-post-detail',
    component: () => import('../../views/model4/PostDetailPage.vue')
  }
]

export default model4Routes
