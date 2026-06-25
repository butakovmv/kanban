import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import TaskCard from '../TaskCard.vue'
import type { Column } from '../../board/api'
import { taskGenerator } from './taskGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof import('../api')>('../api')
  return {
    ...actual,
    moveTask: vi.fn(),
    updateTask: vi.fn(),
    archiveTask: vi.fn(),
    deleteTask: vi.fn(),
  }
})

import * as api from '../api'

const columns: Column[] = [
  {
    id: 'c-1',
    boardId: 'b-1',
    name: 'Todo',
    position: 0,
    wipLimit: null,
    createdAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'c-2',
    boardId: 'b-1',
    name: 'Done',
    position: 1,
    wipLimit: null,
    createdAt: '2025-01-01T00:00:00Z',
  },
]

describe('TaskCard', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders the task title and description preview', () => {
    const task = taskGenerator.task({
      title: 'Write tests',
      description: 'Implement unit tests for the task module',
    })

    const wrapper = mount(TaskCard, {
      props: { task, columns },
    })

    expect(wrapper.find('.task-card__title').text()).toBe('Write tests')
    expect(wrapper.find('.task-card__description').text()).toContain(
      'Implement unit tests for the task module',
    )
  })

  it('emits open event with the task id on click', async () => {
    const task = taskGenerator.task({ id: 't-1', title: 'Open me' })

    const wrapper = mount(TaskCard, {
      props: { task, columns },
    })

    await wrapper.find('.task-card__title').trigger('click')

    expect(wrapper.emitted('open')).toBeTruthy()
    expect(wrapper.emitted('open')?.[0]).toEqual(['t-1'])
  })

  it('allows editing the title and calls updateTask', async () => {
    const task = taskGenerator.task({ id: 't-1', title: 'Old' })
    const updated = taskGenerator.task({ id: 't-1', title: 'New' })
    vi.mocked(api.updateTask).mockResolvedValue(updated)

    const wrapper = mount(TaskCard, {
      props: { task, columns },
    })

    await wrapper.find('.task-card__edit').trigger('click')
    const input = wrapper.find('input.task-card__edit-input')
    await input.setValue('New')
    await wrapper.find('.task-card__edit-form').trigger('submit')

    expect(api.updateTask).toHaveBeenCalledWith('t-1', { title: 'New' })
  })

  it('confirms and deletes the task when the user accepts', async () => {
    const task = taskGenerator.task({ id: 't-1', archived: true })
    vi.mocked(api.deleteTask).mockResolvedValue(undefined)

    const wrapper = mount(TaskCard, {
      props: { task, columns },
    })

    const deleteButton = wrapper.find('.task-card__delete-btn')
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')

    expect(wrapper.find('.task-card__title-row--confirm').exists()).toBe(true)

    const yes = wrapper.find('.task-card__confirm-yes')
    expect(yes.exists()).toBe(true)
    await yes.trigger('click')

    expect(api.deleteTask).toHaveBeenCalledWith('t-1')
  })

  it('renders nothing for description preview when description is null', () => {
    const task = taskGenerator.task({ description: null })

    const wrapper = mount(TaskCard, {
      props: { task, columns },
    })

    expect(wrapper.find('.task-card__description').exists()).toBe(false)
  })
})
