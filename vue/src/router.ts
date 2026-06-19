import { createRouter, createWebHistory } from 'vue-router'

/**
 * Маршрутизатор приложения с history-режимом.
 *
 * Доступные маршруты:
 * - `/` — редирект на `/login`
 * - `/login` — страница входа
 * - `/register` — страница регистрации
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
  ],
})

export default router
