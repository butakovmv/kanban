import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import LoginPage from '../LoginPage.vue'

function createTestRouter(initialRoute = '/login') {
  const r = createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/login', name: 'login', component: LoginPage },
      { path: '/board', component: { template: '<div>Board</div>' } },
      { path: '/register', component: { template: '<div>Register</div>' } },
    ],
  })
  return r
}

describe('LoginPage', () => {
  it('renders login form with email, password fields and submit button', async () => {
    const router = createTestRouter()
    await router.push('/login')
    await router.isReady()

    const wrapper = mount(LoginPage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('h1').text()).toBe('Login')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').text()).toBe('Login')
  })

  it('shows error message when login fails', async () => {
    const router = createTestRouter()
    await router.push('/login')
    await router.isReady()

    const wrapper = mount(LoginPage, {
      global: { plugins: [router] },
    })

    vi.spyOn(router, 'push').mockRejectedValue(new Error('Login failed'))

    await wrapper.find('input[type="email"]').setValue('test@kanban.test')
    await wrapper.find('input[type="password"]').setValue('wrong')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(router.push).toHaveBeenCalledWith('/board')
    expect(wrapper.find('.login__error').exists()).toBe(true)
    expect(wrapper.find('.login__error').text()).toBe('Login failed')
  })

  it('has a link to register page', async () => {
    const router = createTestRouter()
    await router.push('/login')
    await router.isReady()

    const wrapper = mount(LoginPage, {
      global: { plugins: [router] },
    })

    const registerLink = wrapper.find('a[href="/register"]')
    expect(registerLink.exists()).toBe(true)
    expect(registerLink.text()).toBe('Register')
  })
})
