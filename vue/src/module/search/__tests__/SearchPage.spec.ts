import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { setActivePinia, createPinia } from 'pinia'
import SearchPage from '../SearchPage.vue'
import * as api from '../api'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof import('../api')>('../api')
  return {
    ...actual,
    searchTasks: vi.fn(),
  }
})

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/search', name: 'search', component: SearchPage },
      { path: '/tasks/:id', name: 'task-detail', component: { template: '<div>task</div>' } },
    ],
  })
}

describe('SearchPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders the search input and header', async () => {
    const router = createTestRouter()
    await router.push('/search')
    await router.isReady()

    vi.mocked(api.searchTasks).mockResolvedValue({ results: [], total: 0 })

    const wrapper = mount(SearchPage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('h1').text()).toBe('Search Tasks')
    expect(wrapper.find('.search-page__input').exists()).toBe(true)
  })

  it('performs search on input after debounce', async () => {
    vi.useFakeTimers()
    const router = createTestRouter()
    await router.push('/search')
    await router.isReady()

    vi.mocked(api.searchTasks).mockResolvedValue({ results: [], total: 0 })

    const wrapper = mount(SearchPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('.search-page__input').setValue('test')

    vi.advanceTimersByTime(300)
    await flushPromises()

    expect(api.searchTasks).toHaveBeenCalledWith(
      expect.objectContaining({ q: 'test', page: 1, size: 10 }),
    )
    vi.useRealTimers()
  })

  it('shows empty state when no results', async () => {
    const router = createTestRouter()
    await router.push('/search')
    await router.isReady()

    vi.mocked(api.searchTasks).mockResolvedValue({ results: [], total: 0 })

    const wrapper = mount(SearchPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('.search-page__input').setValue('test')
    await new Promise((r) => setTimeout(r, 350))
    await flushPromises()

    expect(wrapper.find('.search-page__empty').exists()).toBe(true)
  })

  it('displays results when search returns data', async () => {
    const router = createTestRouter()
    await router.push('/search')
    await router.isReady()

    vi.mocked(api.searchTasks).mockResolvedValue({
      results: [
        {
          id: 'task-1',
          title: 'Fix login bug',
          description: 'The login form fails',
          status: 'open',
          priority: 'high',
          assigneeId: 'user-1',
          boardId: 'board-1',
          columnId: 'col-1',
          projectId: 'proj-1',
          dueDate: null,
          createdAt: '2025-01-01T00:00:00Z',
          updatedAt: '2025-01-02T00:00:00Z',
          rank: 1.5,
        },
      ],
      total: 1,
    })

    const wrapper = mount(SearchPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('.search-page__input').setValue('login')
    await new Promise((r) => setTimeout(r, 350))
    await flushPromises()

    expect(wrapper.find('.search-page__card-title').exists()).toBe(true)
    expect(wrapper.find('.search-page__meta').text()).toContain('1')
  })
})
