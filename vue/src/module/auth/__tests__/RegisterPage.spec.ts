import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import RegisterPage from '../RegisterPage.vue'

function createTestRouter() {
  const r = createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/register', name: 'register', component: RegisterPage },
      { path: '/login', component: { template: '<div>Login</div>' } },
    ],
  })
  return r
}

describe('RegisterPage', () => {
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

  it('shows error message when registration fails', async () => {
    const router = createTestRouter()
    await router.push('/register')
    await router.isReady()

    const wrapper = mount(RegisterPage, {
      global: { plugins: [router] },
    })

    vi.spyOn(router, 'push').mockRejectedValue(new Error('Registration failed'))

    await wrapper.find('input[type="text"]').setValue('New User')
    await wrapper.find('input[type="email"]').setValue('new@kanban.test')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(router.push).toHaveBeenCalledWith('/login')
    const errorEl = wrapper.find('.register__error')
    expect(errorEl.exists()).toBe(true)
    expect(errorEl.text()).toBe('Registration failed')
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
