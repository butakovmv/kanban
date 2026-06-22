import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    get: vi.fn(),
  }
})

describe('search api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('sends GET to /search with query param', async () => {
    const mockResponse = { results: [], total: 0 }
    vi.mocked(fetchModule.get).mockResolvedValue(mockResponse)

    const result = await api.searchTasks({ q: 'test', page: 1, size: 10 })

    expect(fetchModule.get).toHaveBeenCalledWith(
      '/search?q=test&page=1&size=10',
    )
    expect(result).toEqual(mockResponse)
  })

  it('encodes query value', async () => {
    vi.mocked(fetchModule.get).mockResolvedValue({ results: [], total: 0 })

    await api.searchTasks({ q: 'hello world' })

    expect(fetchModule.get).toHaveBeenCalledWith(
      '/search?q=hello+world',
    )
  })

  it('includes optional filters as snake_case params', async () => {
    vi.mocked(fetchModule.get).mockResolvedValue({ results: [], total: 0 })

    await api.searchTasks({
      q: 'bug',
      projectId: 'proj-1',
      boardId: 'board-2',
      status: 'open',
      priority: 'high',
      assigneeId: 'user-3',
      dueDateFrom: '2025-01-01',
      dueDateTo: '2025-12-31',
      page: 2,
      size: 5,
    })

    expect(fetchModule.get).toHaveBeenCalledWith(
      '/search?q=bug&project_id=proj-1&board_id=board-2&status=open&priority=high&assignee_id=user-3&due_date_from=2025-01-01&due_date_to=2025-12-31&page=2&size=5',
    )
  })

  it('omits undefined optional params', async () => {
    vi.mocked(fetchModule.get).mockResolvedValue({ results: [], total: 0 })

    await api.searchTasks({ q: 'test' })

    const callUrl = vi.mocked(fetchModule.get).mock.calls[0][0] as string
    expect(callUrl).toBe('/search?q=test')
  })
})
