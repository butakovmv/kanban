import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'
import { boardGenerator } from './boardGenerator'

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
  }
})

describe('board api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getBoard', () => {
    it('sends GET to /boards/{id} and returns board with columns', async () => {
      const view = boardGenerator.boardView({
        board: boardGenerator.board({ id: 'b-1', name: 'Sprint 1' }),
        columns: [
          boardGenerator.column({ id: 'c-1', name: 'Todo', position: 0, boardId: 'b-1' }),
          boardGenerator.column({ id: 'c-2', name: 'Done', position: 1, boardId: 'b-1' }),
        ],
      })
      vi.mocked(fetchModule.get).mockResolvedValue(view)

      const result = await api.getBoard('b-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/boards/b-1')
      expect(result).toEqual(view)
      expect(result.columns).toHaveLength(2)
    })

    it('encodes id with special characters', async () => {
      const view = boardGenerator.boardView()
      vi.mocked(fetchModule.get).mockResolvedValue(view)

      await api.getBoard('id with/slash')

      expect(fetchModule.get).toHaveBeenCalledWith('/boards/id%20with%2Fslash')
    })
  })

  describe('createBoard', () => {
    it('sends POST to /boards with snake_case body and returns the board', async () => {
      const created = boardGenerator.board({
        id: 'new-id',
        projectId: 'p-1',
        name: 'Sprint 2',
      })
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      const result = await api.createBoard({ projectId: 'p-1', name: 'Sprint 2' })

      expect(fetchModule.post).toHaveBeenCalledWith('/boards', {
        project_id: 'p-1',
        name: 'Sprint 2',
      })
      expect(result).toEqual(created)
    })
  })

  describe('updateBoard', () => {
    it('sends PUT to /boards/{id} with name and returns the updated board', async () => {
      const updated = boardGenerator.board({ id: 'b-1', name: 'Renamed' })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      const result = await api.updateBoard('b-1', { name: 'Renamed' })

      expect(fetchModule.put).toHaveBeenCalledWith('/boards/b-1', { name: 'Renamed' })
      expect(result).toEqual(updated)
    })
  })

  describe('deleteBoard', () => {
    it('sends DELETE to /boards/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deleteBoard('b-99')

      expect(fetchModule.del).toHaveBeenCalledWith('/boards/b-99')
    })
  })

  describe('reorderColumns', () => {
    it('sends PUT to /boards/{id}/columns/order with column_ids body', async () => {
      const view = boardGenerator.boardView({
        board: boardGenerator.board({ id: 'b-1' }),
        columns: [
          boardGenerator.column({ id: 'c-2', position: 0, boardId: 'b-1' }),
          boardGenerator.column({ id: 'c-1', position: 1, boardId: 'b-1' }),
        ],
      })
      vi.mocked(fetchModule.put).mockResolvedValue(view)

      const result = await api.reorderColumns('b-1', ['c-2', 'c-1'])

      expect(fetchModule.put).toHaveBeenCalledWith('/boards/b-1/columns/order', {
        column_ids: ['c-2', 'c-1'],
      })
      expect(result).toEqual(view)
    })
  })
})
