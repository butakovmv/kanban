import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import RegisterPage from '../RegisterPage.vue'
import { useAuthStore } from '../store'

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/register', name: 'register', component: RegisterPage },
      { path: '/login', component: { template: '<div>Login</div>' } },
    ],
  })
}

describe('RegisterPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders register form with name, email, password fields and submit button', async () => {
    const router = createTestRouter()
    await router.push('/register')
    await router.isReady()

    const wrapper = mount(RegisterPage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('h1').text()).toBe('Register')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').text()).toBe('Register')
  })

  it('navigates to login on successful registration', async () => {
    const router = createTestRouter()
    await router.push('/register')
    await router.isReady()

    const authStore = useAuthStore()
    vi.spyOn(authStore, 'register').mockResolvedValue(true)
    vi.spyOn(router, 'push').mockResolvedValue(undefined)

    const wrapper = mount(RegisterPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('input[type="text"]').setValue('New User')
    await wrapper.find('input[type="email"]').setValue('new@kanban.test')
    await wrapper.find('input[type="password"]').setValue('pwd')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(authStore.register).toHaveBeenCalledWith({
      email: 'new@kanban.test',
      password: 'pwd',
      displayName: 'New User',
    })
    expect(router.push).toHaveBeenCalledWith('/login')
  })

  it('shows error message when registration fails', async () => {
    const router = createTestRouter()
    await router.push('/register')
    await router.isReady()

    const authStore = useAuthStore()
    authStore.error = 'Email already registered'

    const wrapper = mount(RegisterPage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('.register__error').exists()).toBe(true)
    expect(wrapper.find('.register__error').text()).toBe('Email already registered')
  })

  it('does not navigate when registration fails', async () => {
    const router = createTestRouter()
    await router.push('/register')
    await router.isReady()

    const authStore = useAuthStore()
    vi.spyOn(authStore, 'register').mockResolvedValue(false)
    const pushSpy = vi.spyOn(router, 'push').mockResolvedValue(undefined)

    const wrapper = mount(RegisterPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('input[type="text"]').setValue('User')
    await wrapper.find('input[type="email"]').setValue('existing@kanban.test')
    await wrapper.find('input[type="password"]').setValue('pwd')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(authStore.register).toHaveBeenCalled()
    expect(pushSpy).not.toHaveBeenCalled()
  })

  it('has a link to login page', async () => {
    const router = createTestRouter()
    await router.push('/register')
    await router.isReady()

    const wrapper = mount(RegisterPage, {
      global: { plugins: [router] },
    })

    const loginLink = wrapper.find('a[href="/login"]')
    expect(loginLink.exists()).toBe(true)
    expect(loginLink.text()).toBe('Login')
  })
})
