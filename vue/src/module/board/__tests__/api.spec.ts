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
      const rawBoard = boardGenerator.rawBoard({ id: 'b-1', name: 'Sprint 1' })
      const rawColumns = [
        boardGenerator.rawColumn({ id: 'c-1', name: 'Todo', position: 0, board_id: 'b-1' }),
        boardGenerator.rawColumn({ id: 'c-2', name: 'Done', position: 1, board_id: 'b-1' }),
      ]
      const raw = boardGenerator.rawBoardView({ board: rawBoard, columns: rawColumns })
      vi.mocked(fetchModule.get).mockResolvedValue(raw)

      const result = await api.getBoard('b-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/boards/b-1')
      expect(result.board.id).toBe('b-1')
      expect(result.board.projectId).toBe(rawBoard.project_id)
      expect(result.board.name).toBe('Sprint 1')
      expect(result.columns).toHaveLength(2)
      expect(result.columns[0].id).toBe('c-1')
      expect(result.columns[0].boardId).toBe('b-1')
      expect(result.columns[1].id).toBe('c-2')
      expect(result.columns[1].boardId).toBe('b-1')
    })

    it('encodes id with special characters', async () => {
      const raw = boardGenerator.rawBoardView()
      vi.mocked(fetchModule.get).mockResolvedValue(raw)

      await api.getBoard('id with/slash')

      expect(fetchModule.get).toHaveBeenCalledWith('/boards/id%20with%2Fslash')
    })
  })

  describe('createBoard', () => {
    it('sends POST to /boards with snake_case body and returns the board', async () => {
      const raw = boardGenerator.rawBoard({ id: 'new-id', project_id: 'p-1', name: 'Sprint 2' })
      vi.mocked(fetchModule.post).mockResolvedValue(raw)

      const result = await api.createBoard({ projectId: 'p-1', name: 'Sprint 2' })

      expect(fetchModule.post).toHaveBeenCalledWith('/boards', {
        project_id: 'p-1',
        name: 'Sprint 2',
      })
      expect(result.id).toBe('new-id')
      expect(result.projectId).toBe('p-1')
      expect(result.name).toBe('Sprint 2')
    })
  })

  describe('updateBoard', () => {
    it('sends PUT to /boards/{id} with name and returns the updated board', async () => {
      const raw = boardGenerator.rawBoard({ id: 'b-1', name: 'Renamed' })
      vi.mocked(fetchModule.put).mockResolvedValue(raw)

      const result = await api.updateBoard('b-1', { name: 'Renamed' })

      expect(fetchModule.put).toHaveBeenCalledWith('/boards/b-1', { name: 'Renamed' })
      expect(result.id).toBe('b-1')
      expect(result.name).toBe('Renamed')
      expect(result.projectId).toBe(raw.project_id)
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
      const rawBoard = boardGenerator.rawBoard({ id: 'b-1' })
      const rawColumns = [
        boardGenerator.rawColumn({ id: 'c-2', position: 0, board_id: 'b-1' }),
        boardGenerator.rawColumn({ id: 'c-1', position: 1, board_id: 'b-1' }),
      ]
      const raw = boardGenerator.rawBoardView({ board: rawBoard, columns: rawColumns })
      vi.mocked(fetchModule.put).mockResolvedValue(raw)

      const result = await api.reorderColumns('b-1', ['c-2', 'c-1'])

      expect(fetchModule.put).toHaveBeenCalledWith('/boards/b-1/columns/order', {
        column_ids: ['c-2', 'c-1'],
      })
      expect(result.board.id).toBe('b-1')
      expect(result.columns).toHaveLength(2)
      expect(result.columns[0].id).toBe('c-2')
      expect(result.columns[1].id).toBe('c-1')
    })
  })
})
