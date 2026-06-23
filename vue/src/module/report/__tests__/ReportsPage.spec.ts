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
    routes: [
      { path: '/reports', name: 'reports', component: ReportsPage },
      {
        path: '/projects/:id/reports',
        name: 'project-reports',
        component: ReportsPage,
      },
      {
        path: '/projects/:id/board',
        name: 'project-board',
        component: ReportsPage,
      },
      {
        path: '/projects/:id/documents',
        name: 'project-documents',
        component: ReportsPage,
      },
      {
        path: '/projects/:id/settings',
        name: 'project-settings',
        component: ReportsPage,
      },
    ],
  })
}

describe('ReportsPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(api.getCfd).mockResolvedValue([])
    vi.mocked(api.getLeadTime).mockResolvedValue([])
  })

  it('renders the tabs', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    await nextTick()
    const tabs = wrapper.findAll('.reports-page__tab')
    expect(tabs).toHaveLength(2)
    expect(tabs[0].text()).toBe('CFD Chart')
    expect(tabs[1].text()).toBe('Lead Time')
  })

  it('shows CFD section by default with a Load button', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    await nextTick()
    expect(wrapper.find('.reports-page__load-btn').exists()).toBe(true)
    expect(wrapper.find('.reports-page__empty').exists()).toBe(true)
  })

  it('switches to Lead Time tab on click', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/reports')
    await router.isReady()

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    await nextTick()
    await wrapper.findAll('.reports-page__tab')[1].trigger('click')
    await nextTick()

    expect(wrapper.find('.reports-page__tab--active').text()).toBe('Lead Time')
  })

  it('loads CFD data and renders an SVG chart', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/reports')
    await router.isReady()

    const cfdData = [
      { date: '2024-01-01', columnId: 'c1', columnName: 'To do', count: 5 },
      { date: '2024-01-01', columnId: 'c2', columnName: 'Done', count: 3 },
      { date: '2024-01-02', columnId: 'c1', columnName: 'To do', count: 4 },
    ]
    vi.mocked(api.getCfd).mockResolvedValue(cfdData)

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    await nextTick()

    await wrapper.find('.reports-page__load-btn').trigger('click')
    await flushPromises()
    await nextTick()

    expect(wrapper.find('svg').exists()).toBe(true)
  })

  it('loads lead time data and renders chart', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/reports')
    await router.isReady()

    const ltData = [
      { taskId: 't1', leadTimeHours: 24 },
      { taskId: 't2', leadTimeHours: 48 },
    ]
    vi.mocked(api.getLeadTime).mockResolvedValue(ltData)

    const wrapper = mount(ReportsPage, {
      global: { plugins: [router] },
    })

    await nextTick()

    await wrapper.findAll('.reports-page__tab')[1].trigger('click')
    await nextTick()

    await wrapper.find('.reports-page__load-btn').trigger('click')
    await flushPromises()
    await nextTick()

    expect(wrapper.find('svg').exists()).toBe(true)
  })
})
