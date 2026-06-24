import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import TaskDetailPage from '../TaskDetailPage.vue'
import BoardPage from '../../board/BoardPage.vue'
import { useTaskStore } from '../store'
import * as api from '../api'
import { taskGenerator } from './taskGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof import('../api')>('../api')
  return {
    ...actual,
    getTask: vi.fn(),
    listComments: vi.fn(),
    listFiles: vi.fn(),
    updateTask: vi.fn(),
    archiveTask: vi.fn(),
    deleteTask: vi.fn(),
  }
})

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/boards/:id',
        name: 'board',
        component: BoardPage,
      },
      {
        path: '/tasks/:id',
        name: 'task-detail',
        component: TaskDetailPage,
      },
    ],
  })
}

describe('TaskDetailPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.spyOn(window, 'confirm').mockReturnValue(false)
    vi.mocked(api.listComments).mockResolvedValue([])
    vi.mocked(api.listFiles).mockResolvedValue([])
  })

  it('loads and renders the task data and sections', async () => {
    const router = createTestRouter()
    await router.push('/tasks/t-1')
    await router.isReady()

    const task = taskGenerator.task({
      id: 't-1',
      title: 'Detail task',
      description: 'Detail desc',
      dueDate: '2025-12-31',
    })
    const comments = taskGenerator.comments(1, 't-1')
    const files = taskGenerator.files(1, 't-1')
    vi.mocked(api.getTask).mockResolvedValue(task)
    vi.mocked(api.listComments).mockResolvedValue(comments)
    vi.mocked(api.listFiles).mockResolvedValue(files)

    const wrapper = mount(TaskDetailPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(api.getTask).toHaveBeenCalledWith('t-1')
    expect(wrapper.find('.task-detail__title').text()).toBe('Detail task')
    expect(wrapper.text()).toContain('Description')
    expect(wrapper.text()).toContain('2025-12-31')
    expect(wrapper.find('.comments__list').exists()).toBe(true)
    expect(wrapper.find('.files__list').exists()).toBe(true)
  })

  it('enters edit mode and saves the updated task', async () => {
    const router = createTestRouter()
    await router.push('/tasks/t-1')
    await router.isReady()

    const task = taskGenerator.task({ id: 't-1', title: 'Original' })
    const updated = taskGenerator.task({ id: 't-1', title: 'Updated title' })
    vi.mocked(api.getTask).mockResolvedValue(task)
    vi.mocked(api.updateTask).mockResolvedValue(updated)

    const wrapper = mount(TaskDetailPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    await wrapper.find('.task-detail__action').trigger('click')
    const input = wrapper.find('input.task-detail__input')
    await input.setValue('Updated title')
    await wrapper.find('.task-detail__edit-form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(api.updateTask).toHaveBeenCalledWith(
      't-1',
      expect.objectContaining({ title: 'Updated title' }),
    )
  })

  it('confirms and navigates back when deleting the task', async () => {
    const router = createTestRouter()
    await router.push('/tasks/t-1')
    await router.isReady()
    const pushSpy = vi.spyOn(router, 'push').mockResolvedValue(undefined)

    const task = taskGenerator.task({ id: 't-1', projectId: 'p-1' })
    vi.mocked(api.getTask).mockResolvedValue(task)
    vi.mocked(api.deleteTask).mockResolvedValue(undefined)

    const wrapper = mount(TaskDetailPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    const deleteButton = wrapper
      .findAll('.task-detail__action')
      .find((b) => b.text() === 'Delete')
    expect(deleteButton).toBeDefined()
    await deleteButton!.trigger('click')
    await nextTick()

    expect(wrapper.find('.task-detail__confirm').exists()).toBe(true)

    const yes = wrapper
      .findAll('.task-detail__action')
      .find((b) => b.text() === 'Yes')
    expect(yes).toBeDefined()
    await yes!.trigger('click')
    await flushPromises()
    await nextTick()

    expect(api.deleteTask).toHaveBeenCalledWith('t-1')
    expect(pushSpy).toHaveBeenCalledWith('/projects/p-1')
  })

  it('shows a not-found state when the task cannot be loaded', async () => {
    const router = createTestRouter()
    await router.push('/tasks/missing')
    await router.isReady()

    vi.mocked(api.getTask).mockRejectedValue(new Error('not found'))

    const wrapper = mount(TaskDetailPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(wrapper.find('.task-detail__not-found').exists()).toBe(true)
  })

  it('loads comments and files alongside the task', async () => {
    const router = createTestRouter()
    await router.push('/tasks/t-1')
    await router.isReady()

    const task = taskGenerator.task({ id: 't-1' })
    vi.mocked(api.getTask).mockResolvedValue(task)

    const store = useTaskStore()
    const wrapper = mount(TaskDetailPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(api.listComments).toHaveBeenCalledWith('t-1')
    expect(api.listFiles).toHaveBeenCalledWith('t-1')
    expect(store.comments).toEqual([])
    expect(store.files).toEqual([])
    expect(wrapper.find('.comments__empty').exists()).toBe(true)
    expect(wrapper.find('.files__empty').exists()).toBe(true)
  })
})
