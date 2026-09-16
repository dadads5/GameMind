import { createRouter, createWebHistory } from 'vue-router'
import CommunityHomeView from '../views/CommunityHomeView.vue'
import narutoRoutes from './naruto'
import model3Routes from './model3'
import model4Routes from './naruto/model4'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  /**
   * 路由切换时的滚动行为：
   * - 浏览器前进/后退：恢复该页面原来的滚动位置
   * - 带 hash 锚点：滚动到对应元素
   * - 其余（进入新页面）：一律回到顶部，避免沿用上一个页面的滚动位置
   */
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, top: 0 }
    return { top: 0, left: 0 }
  },
  routes: [
    // 社区问答主页面（首页）
    {
      path: '/',
      name: 'community',
      component: CommunityHomeView
    },
    // 火影论坛模块 - 使用独立路由配置
    ...narutoRoutes,
    // 全站热门榜单（复用火影热榜组件，但展示全局热榜）
    {
      path: '/hot',
      name: 'global-hot',
      component: () => import('../views/naruto/HotPostsView.vue')
    },
    // 模块二 - 杀戮尖塔2攻略（已转 Vue 组件；原生站保留于 public/model2 作 fallback）
    {
      path: '/module2',
      name: 'module2',
      component: () => import('../views/model2/HomeView.vue')
    },
    {
      path: '/module2/forum',
      name: 'module2-forum',
      component: () => import('../views/model2/ForumView.vue')
    },
    {
      path: '/module2/post/:id',
      name: 'module2-post-detail',
      component: () => import('../views/model2/PostDetailView.vue')
    },
    {
      path: '/module2/create',
      name: 'module2-create',
      component: () => import('../views/model2/CreatePostView.vue'),
      meta: { requiresAuth: true }
    },
    // 模块三 - 原神攻略（已转 Vue 组件；原生站保留于 public/model3 作 fallback）
    ...model3Routes,
    // 模块四 - 王者荣耀论坛
    ...model4Routes,
    // 问答中心
    {
      path: '/qa',
      name: 'qa',
      component: () => import('../views/QACenterView.vue')
    },
    // 全局个人中心（需登录）
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    // 管理后台（需登录；是否管理员由后端 /api/admin 接口二次校验）
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: { requiresAuth: true }
    },
    // 游戏社区（管理员在后台新增，统一论坛模板，纯论坛形态）
    {
      path: '/community/:communityId',
      name: 'community-forum',
      component: () => import('../views/CommunityForumView.vue')
    },
    {
      path: '/community/:communityId/post/:id',
      name: 'community-post-detail',
      component: () => import('../views/CommunityPostDetailView.vue')
    },
    {
      path: '/community/:communityId/create',
      name: 'community-create',
      component: () => import('../views/CommunityCreatePostView.vue'),
      meta: { requiresAuth: true }
    },
    // 用户主页（公开，可给主页每天点赞）
    {
      path: '/user/:id',
      name: 'user-profile',
      component: () => import('../views/UserProfileView.vue')
    },
    // 登录页面
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    // 注册页面
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    // AI问答页面（需登录）
    {
      path: '/ai-chat',
      name: 'ai-chat',
      component: () => import('../views/AIChatView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫：未登录访问需鉴权页面时跳转登录页
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
