import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './module/auth/store'

/**
 * Маршрутизатор приложения с history-режимом.
 *
 * Доступные маршруты:
 * - `/` — редирект на `/login`
 * - `/login` — страница входа
 * - `/register` — страница регистрации
 * - `/projects` — список проектов
 * - `/projects/:id` — редирект на `/projects/:id/board`
 * - `/projects/:id/board` — доска проекта
 * - `/projects/:id/settings` — настройки проекта
 * - `/projects/:id/documents` — документы проекта
 * - `/projects/:id/reports` — отчёты проекта
 * - `/boards/:id` — доска с колонками
 * - `/tasks/:id` — детали задачи
 * - `/search` — поиск задач
 * - `/reports` — отчёты (глобальные)
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      meta: { guest: true },
      component: () => import('./module/auth/LoginPage.vue'),
    },
    {
      path: '/register',
      name: 'register',
      meta: { guest: true },
      component: () => import('./module/auth/RegisterPage.vue'),
    },
    {
      path: '/projects',
      name: 'projects',
      meta: { requiresAuth: true },
      component: () => import('./module/project/ProjectListPage.vue'),
    },
    {
      path: '/projects/:id',
      redirect: (to) => ({ path: `/projects/${to.params.id}/board` }),
    },
    {
      path: '/projects/:id/board',
      name: 'project-board',
      meta: { requiresAuth: true },
      component: () => import('./module/board/BoardPage.vue'),
    },
    {
      path: '/projects/:id/settings',
      name: 'project-settings',
      meta: { requiresAuth: true },
      component: () => import('./module/project/ProjectSettingsPage.vue'),
    },
    {
      path: '/projects/:id/documents',
      name: 'project-documents',
      meta: { requiresAuth: true },
      component: () => import('./module/document/DocumentListPage.vue'),
    },
    {
      path: '/projects/:id/documents/:docId',
      name: 'document-detail',
      meta: { requiresAuth: true },
      component: () => import('./module/document/DocumentDetailPage.vue'),
    },
    {
      path: '/projects/:id/reports',
      name: 'project-reports',
      meta: { requiresAuth: true },
      component: () => import('./module/report/ReportsPage.vue'),
    },
    {
      path: '/boards/:id',
      name: 'board',
      meta: { requiresAuth: true },
      component: () => import('./module/board/BoardPage.vue'),
    },
    {
      path: '/tasks/:id',
      name: 'task-detail',
      meta: { requiresAuth: true },
      component: () => import('./module/task/TaskDetailPage.vue'),
    },
    {
      path: '/access',
      name: 'access-control',
      meta: { requiresAuth: true },
      component: () => import('./module/access/AccessControlPage.vue'),
    },
    {
      path: '/search',
      name: 'search',
      meta: { requiresAuth: true },
      component: () => import('./module/search/SearchPage.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      meta: { requiresAuth: true },
      component: () => import('./module/auth/ProfilePage.vue'),
    },
    {
      path: '/reports',
      name: 'reports',
      meta: { requiresAuth: true },
      component: () => import('./module/report/ReportsPage.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return '/login'
  }
  if (to.meta.guest && authStore.isAuthenticated) {
    return '/projects'
  }
})

export default router
