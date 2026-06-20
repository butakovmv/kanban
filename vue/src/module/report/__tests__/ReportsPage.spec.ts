import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import ReportsPage from '../ReportsPage.vue'
import * as api from '../api'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof import('../api')>('../api')
  return {
    ...actual,
    getCfd: vi.fn(),
    getLeadTime: vi.fn(),
  }
})

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [{ path: '/reports', name: 'reports', component: ReportsPage }],
  })
}

describe('ReportsPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(api.getCfd).mockResolvedValue([])
    vi.mocked(api.getLeadTime).mockResolvedValue([])
  })

  it('renders the page header and tabs', async () => {
    const router = createTestRouter()
    await router.push('/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('h1').text()).toBe('Reports')
    const tabs = wrapper.findAll('.reports-page__tab')
    expect(tabs).toHaveLength(2)
    expect(tabs[0].text()).toBe('CFD Chart')
    expect(tabs[1].text()).toBe('Lead Time')
  })

  it('shows CFD section by default with a Load button', async () => {
    const router = createTestRouter()
    await router.push('/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    expect(wrapper.find('.reports-page__load-btn').exists()).toBe(true)
    expect(wrapper.find('.reports-page__empty').exists()).toBe(true)
  })

  it('switches to Lead Time tab on click', async () => {
    const router = createTestRouter()
    await router.push('/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    const tabs = wrapper.findAll('.reports-page__tab')
    await tabs[1].trigger('click')
    await nextTick()

    expect(wrapper.find('.reports-page__tab--active').text()).toBe('Lead Time')
  })

  it('loads CFD data and renders an SVG chart', async () => {
    vi.mocked(api.getCfd).mockResolvedValue([
      { date: '2025-01-01', columnId: 'c1', columnName: 'To Do', count: 3 },
      { date: '2025-01-02', columnId: 'c1', columnName: 'To Do', count: 5 },
    ])

    const router = createTestRouter()
    await router.push('/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    await wrapper.find('.reports-page__load-btn').trigger('click')
    await flushPromises()

    expect(api.getCfd).toHaveBeenCalled()
    const svg = wrapper.find('.reports-page__svg')
    expect(svg.exists()).toBe(true)
  })

  it('loads lead time data and renders chart', async () => {
    vi.mocked(api.getLeadTime).mockResolvedValue([
      { date: '2025-01-15', taskId: 't-1', taskTitle: 'Bug', leadTimeHours: 10 },
    ])

    const router = createTestRouter()
    await router.push('/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    const tabs = wrapper.findAll('.reports-page__tab')
    await tabs[1].trigger('click')
    await nextTick()

    await wrapper.find('.reports-page__load-btn').trigger('click')
    await flushPromises()

    expect(api.getLeadTime).toHaveBeenCalled()
    expect(wrapper.find('.reports-page__avg').exists()).toBe(true)
  })
})
