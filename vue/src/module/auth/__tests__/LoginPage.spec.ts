import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import LoginPage from '../LoginPage.vue'
import { useAuthStore } from '../store'

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/login', name: 'login', component: LoginPage },
      { path: '/board', component: { template: '<div>Board</div>' } },
      { path: '/register', component: { template: '<div>Register</div>' } },
    ],
  })
}

describe('LoginPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

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

  it('navigates to board on successful login', async () => {
    const router = createTestRouter()
    await router.push('/login')
    await router.isReady()

    const authStore = useAuthStore()
    vi.spyOn(authStore, 'login').mockResolvedValue(true)
    vi.spyOn(router, 'push').mockResolvedValue(undefined)

    const wrapper = mount(LoginPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('input[type="email"]').setValue('test@kanban.test')
    await wrapper.find('input[type="password"]').setValue('correct')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(authStore.login).toHaveBeenCalledWith({
      email: 'test@kanban.test',
      password: 'correct',
    })
    expect(router.push).toHaveBeenCalledWith('/board')
  })

  it('shows error message when login fails', async () => {
    const router = createTestRouter()
    await router.push('/login')
    await router.isReady()

    const authStore = useAuthStore()
    authStore.error = 'Invalid email or password'

    const wrapper = mount(LoginPage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('.login__error').exists()).toBe(true)
    expect(wrapper.find('.login__error').text()).toBe('Invalid email or password')
  })

  it('does not navigate when login fails', async () => {
    const router = createTestRouter()
    await router.push('/login')
    await router.isReady()

    const authStore = useAuthStore()
    vi.spyOn(authStore, 'login').mockResolvedValue(false)
    const pushSpy = vi.spyOn(router, 'push').mockResolvedValue(undefined)

    const wrapper = mount(LoginPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('input[type="email"]').setValue('test@kanban.test')
    await wrapper.find('input[type="password"]').setValue('wrong')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(authStore.login).toHaveBeenCalled()
    expect(pushSpy).not.toHaveBeenCalled()
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
