import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useBoardStore } from '../store'
import * as api from '../api'
import { boardGenerator } from './boardGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    getBoard: vi.fn(),
    createBoard: vi.fn(),
    updateBoard: vi.fn(),
    deleteBoard: vi.fn(),
    reorderColumns: vi.fn(),
  }
})

describe('board store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty state', () => {
    const store = useBoardStore()
    expect(store.currentBoard).toBeNull()
    expect(store.columns).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.hasBoard).toBe(false)
    expect(store.hasColumns).toBe(false)
  })

  describe('loadBoard', () => {
    it('populates currentBoard and columns on success', async () => {
      const view = boardGenerator.boardView({
        board: boardGenerator.board({ id: 'b-1' }),
        columns: boardGenerator.columns(3, 'b-1'),
      })
      vi.mocked(api.getBoard).mockResolvedValue(view)

      const store = useBoardStore()
      const success = await store.loadBoard('b-1')

      expect(success).toBe(true)
      expect(store.currentBoard).toEqual(view.board)
      expect(store.columns).toEqual(view.columns)
      expect(store.hasBoard).toBe(true)
      expect(store.hasColumns).toBe(true)
      expect(api.getBoard).toHaveBeenCalledWith('b-1')
    })

    it('sets error and clears state on failure', async () => {
      vi.mocked(api.getBoard).mockRejectedValue(new Error('Not found'))

      const store = useBoardStore()
      const success = await store.loadBoard('missing')

      expect(success).toBe(false)
      expect(store.error).toBe('Not found')
      expect(store.currentBoard).toBeNull()
      expect(store.columns).toEqual([])
    })

    it('uses generic error message for non-Error rejection', async () => {
      vi.mocked(api.getBoard).mockRejectedValue('plain string')

      const store = useBoardStore()
      const success = await store.loadBoard('b-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Failed to load board')
    })

    it('toggles loading state during load', async () => {
      let resolveLoad: (value: api.BoardView) => void = () => {}
      vi.mocked(api.getBoard).mockReturnValue(
        new Promise((resolve) => {
          resolveLoad = resolve
        }),
      )

      const store = useBoardStore()
      const promise = store.loadBoard('b-1')

      expect(store.loading).toBe(true)

      resolveLoad(boardGenerator.boardView())
      await promise

      expect(store.loading).toBe(false)
    })
  })

  describe('createBoard', () => {
    it('returns the created board on success', async () => {
      const created = boardGenerator.board({ id: 'b-1', name: 'New' })
      vi.mocked(api.createBoard).mockResolvedValue(created)

      const store = useBoardStore()
      const result = await store.createBoard({ projectId: 'p-1', name: 'New' })

      expect(result).toEqual(created)
      expect(store.currentBoard).toBeNull()
    })

    it('sets error and returns null on failure', async () => {
      vi.mocked(api.createBoard).mockRejectedValue(new Error('Validation failed'))

      const store = useBoardStore()
      const result = await store.createBoard({ projectId: 'p-1', name: 'X' })

      expect(result).toBeNull()
      expect(store.error).toBe('Validation failed')
    })
  })

  describe('updateBoard', () => {
    it('updates currentBoard when ids match and returns true', async () => {
      const original = boardGenerator.board({ id: 'b-1', name: 'Old' })
      const updated = boardGenerator.board({ id: 'b-1', name: 'New' })
      vi.mocked(api.updateBoard).mockResolvedValue(updated)

      const store = useBoardStore()
      store.currentBoard = original

      const success = await store.updateBoard('b-1', { name: 'New' })

      expect(success).toBe(true)
      expect(store.currentBoard).toEqual(updated)
    })

    it('does not touch currentBoard when ids differ', async () => {
      const updated = boardGenerator.board({ id: 'b-1' })
      const current = boardGenerator.board({ id: 'b-2' })
      vi.mocked(api.updateBoard).mockResolvedValue(updated)

      const store = useBoardStore()
      store.currentBoard = current

      await store.updateBoard('b-1', { name: 'X' })

      expect(store.currentBoard).toEqual(current)
    })

    it('sets error and returns false on failure', async () => {
      vi.mocked(api.updateBoard).mockRejectedValue(new Error('Server error'))

      const store = useBoardStore()
      const success = await store.updateBoard('b-1', { name: 'X' })

      expect(success).toBe(false)
      expect(store.error).toBe('Server error')
    })
  })

  describe('deleteBoard', () => {
    it('removes the currentBoard on success and returns true', async () => {
      vi.mocked(api.deleteBoard).mockResolvedValue(undefined)

      const store = useBoardStore()
      store.currentBoard = boardGenerator.board({ id: 'b-1' })
      store.columns = boardGenerator.columns(2, 'b-1')

      const success = await store.deleteBoard('b-1')

      expect(success).toBe(true)
      expect(store.currentBoard).toBeNull()
      expect(store.columns).toEqual([])
    })

    it('leaves state intact when ids differ', async () => {
      vi.mocked(api.deleteBoard).mockResolvedValue(undefined)

      const store = useBoardStore()
      const current = boardGenerator.board({ id: 'b-1' })
      store.currentBoard = current

      const success = await store.deleteBoard('b-2')

      expect(success).toBe(true)
      expect(store.currentBoard).toEqual(current)
    })

    it('sets error and returns false on failure', async () => {
      vi.mocked(api.deleteBoard).mockRejectedValue(new Error('Forbidden'))

      const store = useBoardStore()
      const success = await store.deleteBoard('b-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Forbidden')
    })
  })

  describe('reorderColumns', () => {
    it('updates currentBoard and columns from server response', async () => {
      const view = boardGenerator.boardView({
        board: boardGenerator.board({ id: 'b-1' }),
        columns: [
          boardGenerator.column({ id: 'c-2', position: 0, boardId: 'b-1' }),
          boardGenerator.column({ id: 'c-1', position: 1, boardId: 'b-1' }),
        ],
      })
      vi.mocked(api.reorderColumns).mockResolvedValue(view)

      const store = useBoardStore()
      const success = await store.reorderColumns('b-1', ['c-2', 'c-1'])

      expect(success).toBe(true)
      expect(store.columns.map((c) => c.id)).toEqual(['c-2', 'c-1'])
    })

    it('sets error and returns false on failure', async () => {
      vi.mocked(api.reorderColumns).mockRejectedValue(new Error('Conflict'))

      const store = useBoardStore()
      const success = await store.reorderColumns('b-1', ['c-2', 'c-1'])

      expect(success).toBe(false)
      expect(store.error).toBe('Conflict')
    })
  })

  describe('clearCurrent', () => {
    it('resets currentBoard and columns', () => {
      const store = useBoardStore()
      store.currentBoard = boardGenerator.board()
      store.columns = boardGenerator.columns(2)

      store.clearCurrent()

      expect(store.currentBoard).toBeNull()
      expect(store.columns).toEqual([])
    })
  })
})
