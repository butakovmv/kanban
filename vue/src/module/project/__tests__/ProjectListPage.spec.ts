import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import ProjectListPage from '../ProjectListPage.vue'
import ProjectSettingsPage from '../ProjectSettingsPage.vue'
import { useProjectStore } from '../store'
import { useAuthStore } from '../../auth/store'
import * as api from '../api'
import { projectGenerator } from './projectGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof import('../api')>('../api')
  return {
    ...actual,
    listProjects: vi.fn(),
    getProject: vi.fn(),
    createProject: vi.fn(),
    updateProject: vi.fn(),
    deleteProject: vi.fn(),
  }
})

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/projects', name: 'projects', component: ProjectListPage },
      {
        path: '/projects/:id',
        name: 'project-board',
        component: ProjectSettingsPage,
      },
    ],
  })
}

describe('ProjectListPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(api.listProjects).mockResolvedValue([])
  })

  it('renders projects from the store and a create button', async () => {
    const router = createTestRouter()
    await router.push('/projects')
    await router.isReady()

    const wrapper = mount(ProjectListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    const store = useProjectStore()
    store.projects = projectGenerator.projects(2)
    await nextTick()

    expect(wrapper.find('h1').text()).toBe('Projects')
    const items = wrapper.findAll('.project-list__item')
    expect(items).toHaveLength(2)
    expect(wrapper.find('.project-list__create-btn').exists()).toBe(true)
  })

  it('calls createProject with the typed values', async () => {
    const router = createTestRouter()
    await router.push('/projects')
    await router.isReady()

    const authStore = useAuthStore()
    authStore.user = {
      id: 'real-user',
      email: 'u@kanban.test',
      displayName: 'U',
    }

    const created = projectGenerator.project({ name: 'Created Project' })
    vi.mocked(api.createProject).mockResolvedValue(created)

    const wrapper = mount(ProjectListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    await wrapper.find('.project-list__create-btn').trigger('click')
    await nextTick()

    expect(wrapper.find('.project-list__form').exists()).toBe(true)

    await wrapper.find('input[type="text"]').setValue('Created Project')
    await wrapper.find('textarea').setValue('My description')
    await nextTick()
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(api.createProject).toHaveBeenCalledWith({
      ownerId: 'real-user',
      name: 'Created Project',
      description: 'My description',
    })
  })

  it('renders an empty state when there are no projects', async () => {
    const router = createTestRouter()
    await router.push('/projects')
    await router.isReady()

    const wrapper = mount(ProjectListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    expect(wrapper.find('.project-list__empty').exists()).toBe(true)
  })

  it('uses the authenticated user id when available', async () => {
    const router = createTestRouter()
    await router.push('/projects')
    await router.isReady()

    const authStore = useAuthStore()
    authStore.user = {
      id: 'real-user',
      email: 'u@kanban.test',
      displayName: 'U',
    }

    const created = projectGenerator.project()
    vi.mocked(api.createProject).mockResolvedValue(created)

    const wrapper = mount(ProjectListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    await wrapper.find('.project-list__create-btn').trigger('click')
    await nextTick()
    await wrapper.find('input[type="text"]').setValue('Some project')
    await nextTick()
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(api.createProject).toHaveBeenCalledWith(
      expect.objectContaining({ ownerId: 'real-user' }),
    )
  })
})
