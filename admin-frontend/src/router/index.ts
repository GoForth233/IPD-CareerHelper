import { createRouter, createWebHashHistory } from 'vue-router';

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'students', name: 'students', component: () => import('@/views/Students.vue') },
      { path: 'skill-map', name: 'skill-map', component: () => import('@/views/SkillMap.vue') },
      { path: 'questions', name: 'questions', component: () => import('@/views/QuestionBank.vue') },
    ],
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  if (to.meta?.public) return next();
  const token = localStorage.getItem('admin_token');
  if (!token) return next({ name: 'login' });
  return next();
});

export default router;
