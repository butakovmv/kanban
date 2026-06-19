import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import ProjectListPage from '../ProjectListPage.vue'
import ProjectSettingsPage from '../ProjectSettingsPage.vue'
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
        name: 'project-settings',
        component: ProjectSettingsPage,
      },
    ],
  })
}

describe('ProjectSettingsPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.spyOn(window, 'confirm').mockReturnValue(false)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders project name and description loaded from the api', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1')
    await router.isReady()

    const project = projectGenerator.project({
      id: 'p-1',
      name: 'Loaded project',
      description: 'Loaded description',
    })
    vi.mocked(api.getProject).mockResolvedValue(project)

    const wrapper = mount(ProjectSettingsPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    const nameInput = wrapper.find('input[type="text"]').element as HTMLInputElement
    expect(nameInput.value).toBe('Loaded project')
    const descTextarea = wrapper.find('textarea').element as HTMLTextAreaElement
    expect(descTextarea.value).toBe('Loaded description')
  })

  it('calls updateProject and disables the save button after saving', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1')
    await router.isReady()

    const project = projectGenerator.project({
      id: 'p-1',
      name: 'Original',
      description: 'Original desc',
    })
    vi.mocked(api.getProject).mockResolvedValue(project)
    const updated = projectGenerator.project({
      id: 'p-1',
      name: 'Renamed',
      description: 'New desc',
    })
    vi.mocked(api.updateProject).mockResolvedValue(updated)

    const wrapper = mount(ProjectSettingsPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    await wrapper.find('input[type="text"]').setValue('Renamed')
    await wrapper.find('textarea').setValue('New desc')
    await nextTick()
    const saveButton = wrapper.find('.project-settings__actions button[type="submit"]')
    expect((saveButton.element as HTMLButtonElement).disabled).toBe(false)
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(api.updateProject).toHaveBeenCalledWith('p-1', {
      name: 'Renamed',
      description: 'New desc',
    })
    expect((saveButton.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('asks for confirmation and navigates back after delete', async () => {
    vi.mocked(window.confirm).mockReturnValue(true)
    const router = createTestRouter()
    await router.push('/projects/p-1')
    await router.isReady()
    const pushSpy = vi.spyOn(router, 'push').mockResolvedValue(undefined)

    const project = projectGenerator.project({ id: 'p-1', name: 'To delete' })
    vi.mocked(api.getProject).mockResolvedValue(project)
    vi.mocked(api.deleteProject).mockResolvedValue(undefined)

    const wrapper = mount(ProjectSettingsPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    await wrapper.find('.project-settings__delete').trigger('click')
    await flushPromises()
    await nextTick()

    expect(window.confirm).toHaveBeenCalled()
    expect(api.deleteProject).toHaveBeenCalledWith('p-1')
    expect(pushSpy).toHaveBeenCalledWith({ name: 'projects' })
  })

  it('does not delete when user cancels the confirmation', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1')
    await router.isReady()

    const project = projectGenerator.project({ id: 'p-1', name: 'Keep me' })
    vi.mocked(api.getProject).mockResolvedValue(project)

    const wrapper = mount(ProjectSettingsPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    await wrapper.find('.project-settings__delete').trigger('click')
    await flushPromises()
    await nextTick()

    expect(window.confirm).toHaveBeenCalled()
    expect(api.deleteProject).not.toHaveBeenCalled()
  })

  it('shows a not-found state when the project cannot be loaded', async () => {
    const router = createTestRouter()
    await router.push('/projects/missing')
    await router.isReady()

    vi.mocked(api.getProject).mockRejectedValue(new Error('Not found'))

    const wrapper = mount(ProjectSettingsPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(wrapper.find('.project-settings__not-found').exists()).toBe(true)
  })
})
