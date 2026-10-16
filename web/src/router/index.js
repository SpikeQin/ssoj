import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/problems' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', blank: true },
  },
  {
    path: '/problems',
    name: 'ProblemList',
    component: () => import('@/views/ProblemList.vue'),
    meta: { title: '题库' },
  },
  {
    path: '/problems/:id',
    name: 'ProblemDetail',
    component: () => import('@/views/ProblemDetail.vue'),
    meta: { title: '题目详情' },
  },
  {
    path: '/contests',
    name: 'ContestList',
    component: () => import('@/views/ContestList.vue'),
    meta: { title: '竞赛' },
    },
  {
    path: '/contests/:id',
    name: 'ContestDetail',
    component: () => import('@/views/ContestDetail.vue'),
    meta: { title: '竞赛详情' },
  },
  {
    path: '/rank',
    name: 'RankList',
    component: () => import('@/views/RankList.vue'),
    meta: { title: '榜单' },
  },
  {
    path: '/submissions',
    name: 'SubmissionList',
    component: () => import('@/views/SubmissionList.vue'),
    meta: { title: '提交记录', requiresAuth: true },
  },
  {
    path: '/submissions/:id',
    name: 'SubmissionDetail',
    component: () => import('@/views/SubmissionDetail.vue'),
    meta: { title: '提交详情', requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true },
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '管理首页', requiresAuth: true, requiresAdmin: true } },
      { path: 'problems', name: 'AdminProblems', component: () => import('@/views/admin/ProblemManage.vue'), meta: { title: '题目管理', requiresAuth: true, requiresAdmin: true } },
      { path: 'contests', name: 'AdminContests', component: () => import('@/views/admin/ContestManage.vue'), meta: { title: '竞赛管理', requiresAuth: true, requiresAdmin: true } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true } },
      { path: 'system', name: 'AdminSystem', component: () => import('@/views/admin/SystemSetting.vue'), meta: { title: '公告与配置', requiresAuth: true, requiresAdmin: true } },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/problems' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.meta.requiresAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && user?.role !== 1) {
    return { path: '/problems' }
  }
  return true
})

// 站点名称后续从系统配置接口读取，这里先用站点默认值
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 蚀山在线判题系统` : '蚀山在线判题系统'
})

export default router
