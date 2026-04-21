import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'subjects',
        name: 'Subjects',
        component: () => import('@/views/admin/Subjects.vue'),
        meta: { title: '科目管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'questions',
        name: 'Questions',
        component: () => import('@/views/admin/Questions.vue'),
        meta: { title: '题目管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'papers-manage',
        name: 'PapersManage',
        component: () => import('@/views/admin/PapersManage.vue'),
        meta: { title: '考卷管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'papers',
        name: 'Papers',
        component: () => import('@/views/user/Papers.vue'),
        meta: { title: '考卷列表', requiresAuth: true }
      },
      {
        path: 'exam/:recordId',
        name: 'Exam',
        component: () => import('@/views/user/Exam.vue'),
        meta: { title: '模拟考试', requiresAuth: true }
      },
      {
        path: 'exam-result/:recordId',
        name: 'ExamResult',
        component: () => import('@/views/user/ExamResult.vue'),
        meta: { title: '考试结果', requiresAuth: true }
      },
      {
        path: 'exam-records',
        name: 'ExamRecords',
        component: () => import('@/views/user/ExamRecords.vue'),
        meta: { title: '考试记录', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 模拟考试系统` : '模拟考试系统'
  
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (e) {
        next('/login')
        return
      }
    }
    
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next('/home')
      return
    }
  }
  
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/home')
    return
  }
  
  next()
})

export default router
