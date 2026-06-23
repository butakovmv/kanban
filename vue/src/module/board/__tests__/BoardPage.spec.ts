import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import BoardPage from '../BoardPage.vue'
import ProjectListPage from '../../project/ProjectListPage.vue'
import ProjectSettingsPage from '../../project/ProjectSettingsPage.vue'
import TaskDetailPage from '../../task/TaskDetailPage.vue'
import { useBoardStore } from '../store'
import { useTaskStore } from '../../task/store'
import * as api from '../api'
import * as taskApi from '../../task/api'
import { boardGenerator } from './boardGenerator'
import { taskGenerator } from '../../task/__tests__/taskGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof import('../api')>('../api')
  return {
    ...actual,
    getBoard: vi.fn(),
    getBoardByProjectId: vi.fn(),
    createBoard: vi.fn(),
    updateBoard: vi.fn(),
    deleteBoard: vi.fn(),
    reorderColumns: vi.fn(),
  }
})

vi.mock('../../task/api', async () => {
  const actual = await vi.importActual<typeof import('../../task/api')>('../../task/api')
  return {
    ...actual,
    listTasks: vi.fn(),
    getTask: vi.fn(),
    createTask: vi.fn(),
    moveTask: vi.fn(),
    archiveTask: vi.fn(),
    deleteTask: vi.fn(),
  }
})

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/projects', name: 'projects', component: ProjectListPage },
      {
        path: '/projects/:id/board',
        name: 'project-board',
        component: BoardPage,
      },
      {
        path: '/projects/:id/documents',
        name: 'project-documents',
        component: ProjectListPage,
      },
      {
        path: '/projects/:id/reports',
        name: 'project-reports',
        component: ProjectListPage,
      },
      {
        path: '/projects/:id/settings',
        name: 'project-settings',
        component: ProjectSettingsPage,
      },
      {
        path: '/tasks/:id',
        name: 'task-detail',
        component: TaskDetailPage,
      },
    ],
  })
}

describe('BoardPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(api.getBoardByProjectId).mockResolvedValue(
      boardGenerator.boardView({
        board: boardGenerator.board({ id: 'b-1' }),
        columns: [],
      }),
    )
    vi.mocked(taskApi.listTasks).mockResolvedValue([])
  })

  it('loads and renders the board with its columns from the api', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint 1' })
    const cols = [
      boardGenerator.column({ id: 'c-1', name: 'Todo', position: 0, boardId: 'b-1' }),
      boardGenerator.column({ id: 'c-2', name: 'Done', position: 1, boardId: 'b-1' }),
    ]
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: cols })

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(api.getBoardByProjectId).toHaveBeenCalledWith('p-1')
    const columns = wrapper.findAllComponents({ name: 'Column' })
    expect(columns).toHaveLength(2)
  })

  it('renders a loading state before the board is loaded', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    let resolveLoad: (value: api.BoardView) => void = () => {}
    vi.mocked(api.getBoardByProjectId).mockReturnValue(
      new Promise((resolve) => {
        resolveLoad = resolve
      }),
    )

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await nextTick()
    expect(wrapper.find('.board__loading').exists()).toBe(true)

    resolveLoad(boardGenerator.boardView())
    await flushPromises()
  })

  it('renders an error state when loading fails', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    vi.mocked(api.getBoardByProjectId).mockRejectedValue(new Error('Network error'))

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    const errorEl = wrapper.find('.board__error')
    expect(errorEl.exists()).toBe(true)
    expect(errorEl.text()).toBe('Network error')
  })

  it('renders an empty state when board has no columns', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Empty' })
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: [] })

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(wrapper.find('.board__empty').exists()).toBe(true)
  })

  it('toggles the swimlanes flag when the button is clicked', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint 1' })
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: [] })

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    const toggle = wrapper.find('.board__swimlanes-toggle')
    expect(toggle.text()).toContain('off')
    expect(toggle.attributes('aria-pressed')).toBe('false')

    await toggle.trigger('click')
    expect(toggle.text()).toContain('on')
    expect(toggle.attributes('aria-pressed')).toBe('true')

    await toggle.trigger('click')
    expect(toggle.text()).toContain('off')
    expect(toggle.attributes('aria-pressed')).toBe('false')
  })

  it('exposes an Add column button', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint 1' })
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: [] })

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(wrapper.find('.board__add-column').exists()).toBe(true)
  })

  it('renders the board name in the store and uses columns count from the store', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint X' })
    const cols = [
      boardGenerator.column({ id: 'c-1', name: 'Todo', position: 0, boardId: 'b-1' }),
      boardGenerator.column({ id: 'c-2', name: 'In progress', position: 1, boardId: 'b-1' }),
      boardGenerator.column({ id: 'c-3', name: 'Done', position: 2, boardId: 'b-1' }),
    ]
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: cols })

    const store = useBoardStore()
    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(store.currentBoard?.name).toBe('Sprint X')
    expect(store.columns).toHaveLength(3)
    expect(wrapper.findAllComponents({ name: 'Column' })).toHaveLength(3)
  })

  it('loads tasks for the board and renders them grouped by column', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint 1' })
    const cols = [
      boardGenerator.column({ id: 'c-1', name: 'Todo', position: 0, boardId: 'b-1' }),
      boardGenerator.column({ id: 'c-2', name: 'Done', position: 1, boardId: 'b-1' }),
    ]
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: cols })

    const tasks = [
      taskGenerator.task({ id: 't-1', columnId: 'c-1', position: 0, title: 'First' }),
      taskGenerator.task({ id: 't-2', columnId: 'c-1', position: 1, title: 'Second' }),
      taskGenerator.task({ id: 't-3', columnId: 'c-2', position: 0, title: 'Third' }),
    ]
    vi.mocked(taskApi.listTasks).mockResolvedValue(tasks)

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(taskApi.listTasks).toHaveBeenCalledWith('b-1', false)
    const cards = wrapper.findAllComponents({ name: 'TaskCard' })
    expect(cards).toHaveLength(3)
  })

  it('opens the create-task modal when a column Add task button is clicked', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint 1' })
    const cols = [
      boardGenerator.column({ id: 'c-1', name: 'Todo', position: 0, boardId: 'b-1' }),
    ]
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: cols })
    vi.mocked(taskApi.listTasks).mockResolvedValue([])

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(wrapper.findComponent({ name: 'CreateTaskModal' }).exists()).toBe(false)

    const addTaskBtn = wrapper.find('.column__add')
    expect(addTaskBtn.exists()).toBe(true)
    await addTaskBtn.trigger('click')
    await nextTick()

    expect(wrapper.findComponent({ name: 'CreateTaskModal' }).exists()).toBe(true)
  })

  it('creates a task and closes the modal when the modal emits submit', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint 1' })
    const cols = [
      boardGenerator.column({ id: 'c-1', name: 'Todo', position: 0, boardId: 'b-1' }),
    ]
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: cols })
    vi.mocked(taskApi.listTasks).mockResolvedValue([])

    const created = taskGenerator.task({ id: 't-new', columnId: 'c-1', title: 'Created' })
    vi.mocked(taskApi.createTask).mockResolvedValue(created)

    const taskStore = useTaskStore()
    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    await wrapper.find('.column__add').trigger('click')
    await nextTick()

    const modal = wrapper.findComponent({ name: 'CreateTaskModal' })
    await modal.vm.$emit('submit', {
      boardId: 'b-1',
      columnId: 'c-1',
      title: 'Created',
    })
    await flushPromises()
    await nextTick()

    expect(taskApi.createTask).toHaveBeenCalledWith({
      boardId: 'b-1',
      columnId: 'c-1',
      title: 'Created',
    })
    expect(taskStore.tasks).toContainEqual(created)
    expect(wrapper.findComponent({ name: 'CreateTaskModal' }).exists()).toBe(false)
  })

  it('moves a task to another column when the column receives a drop', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/board')
    await router.isReady()

    const board = boardGenerator.board({ id: 'b-1', name: 'Sprint 1' })
    const cols = [
      boardGenerator.column({ id: 'c-1', name: 'Todo', position: 0, boardId: 'b-1' }),
      boardGenerator.column({ id: 'c-2', name: 'Done', position: 1, boardId: 'b-1' }),
    ]
    vi.mocked(api.getBoardByProjectId).mockResolvedValue({ board, columns: cols })

    const tasks = [taskGenerator.task({ id: 't-1', columnId: 'c-1', position: 0 })]
    vi.mocked(taskApi.listTasks).mockResolvedValue(tasks)
    const moved = taskGenerator.task({ id: 't-1', columnId: 'c-2', position: 0 })
    vi.mocked(taskApi.moveTask).mockResolvedValue(moved)

    const wrapper = mount(BoardPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    const columnComponents = wrapper.findAllComponents({ name: 'Column' })
    expect(columnComponents).toHaveLength(2)
    const targetColumn = columnComponents[1]
    expect(targetColumn).toBeDefined()
    await targetColumn!.vm.$emit('move-task', { taskId: 't-1', columnId: 'c-2' })
    await flushPromises()

    expect(taskApi.moveTask).toHaveBeenCalledWith('t-1', { columnId: 'c-2', position: 0 })
  })
})
