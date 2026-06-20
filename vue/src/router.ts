import { createRouter, createWebHistory } from 'vue-router'

/**
 * Маршрутизатор приложения с history-режимом.
 *
 * Доступные маршруты:
 * - `/` — редирект на `/login`
 * - `/login` — страница входа
 * - `/register` — страница регистрации
 * - `/projects` — список проектов
 * - `/projects/:id` — настройки проекта
 * - `/boards/:id` — доска с колонками
 * - `/tasks/:id` — детали задачи
 * - `/search` — поиск задач
 * - `/reports` — отчёты
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
      component: () => import('./module/auth/LoginPage.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('./module/auth/RegisterPage.vue'),
    },
    {
      path: '/projects',
      name: 'projects',
      component: () => import('./module/project/ProjectListPage.vue'),
    },
    {
      path: '/projects/:id',
      name: 'project-settings',
      component: () => import('./module/project/ProjectSettingsPage.vue'),
    },
    {
      path: '/projects/:id/documents',
      name: 'project-documents',
      component: () => import('./module/document/DocumentListPage.vue'),
    },
    {
      path: '/boards/:id',
      name: 'board',
      component: () => import('./module/board/BoardPage.vue'),
    },
    {
      path: '/tasks/:id',
      name: 'task-detail',
      component: () => import('./module/task/TaskDetailPage.vue'),
    },
    {
      path: '/access',
      name: 'access-control',
      component: () => import('./module/access/AccessControlPage.vue'),
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('./module/search/SearchPage.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('./module/auth/ProfilePage.vue'),
    },
    {
      path: '/reports',
      name: 'reports',
      component: () => import('./module/report/ReportsPage.vue'),
    },
  ],
})

export default router
