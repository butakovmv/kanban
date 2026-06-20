import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import ProfilePage from '../ProfilePage.vue'
import { useAuthStore } from '../store'

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/profile', name: 'profile', component: ProfilePage },
      { path: '/login', component: { template: '<div>Login</div>' } },
    ],
  })
}

describe('ProfilePage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders user info from store', async () => {
    const router = createTestRouter()
    await router.push('/profile')
    await router.isReady()

    const authStore = useAuthStore()
    authStore.user = { id: 'user-1', email: 'test@kanban.test', displayName: 'Test User' }

    const wrapper = mount(ProfilePage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('h1').text()).toBe('Profile')
    expect(wrapper.text()).toContain('test@kanban.test')
    expect(wrapper.text()).toContain('Test User')
    expect(wrapper.text()).toContain('user-1')
  })

  it('shows not logged in message when no user', async () => {
    const router = createTestRouter()
    await router.push('/profile')
    await router.isReady()

    const wrapper = mount(ProfilePage, {
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('Not logged in')
  })

  it('logs out and navigates to login on button click', async () => {
    const router = createTestRouter()
    await router.push('/profile')
    await router.isReady()

    const authStore = useAuthStore()
    authStore.user = { id: 'user-1', email: 'test@kanban.test', displayName: 'Test User' }
    vi.spyOn(authStore, 'logout').mockResolvedValue(undefined)
    vi.spyOn(router, 'push').mockResolvedValue(undefined)

    const wrapper = mount(ProfilePage, {
      global: { plugins: [router] },
    })

    await wrapper.find('.profile__logout-btn').trigger('click')
    await flushPromises()
    await nextTick()

    expect(authStore.logout).toHaveBeenCalled()
    expect(router.push).toHaveBeenCalledWith('/login')
  })

  it('renders logout button', async () => {
    const router = createTestRouter()
    await router.push('/profile')
    await router.isReady()

    const authStore = useAuthStore()
    authStore.user = { id: 'user-1', email: 'test@kanban.test', displayName: 'Test User' }

    const wrapper = mount(ProfilePage, {
      global: { plugins: [router] },
    })

    const logoutBtn = wrapper.find('.profile__logout-btn')
    expect(logoutBtn.exists()).toBe(true)
    expect(logoutBtn.text()).toBe('Logout')
  })
})
