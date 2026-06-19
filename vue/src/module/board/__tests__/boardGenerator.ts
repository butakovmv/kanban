import type { Board, BoardView, Column } from '../api'

/**
 * Генератор тестовых данных для board-модуля.
 * Создаёт случайные сущности для тестов api.ts и store.ts.
 */
export const boardGenerator = {
  board(overrides: Partial<Board> = {}): Board {
    const id = `board-${Math.random().toString(36).slice(2, 10)}`
    return {
      id,
      projectId: `project-${Math.random().toString(36).slice(2, 8)}`,
      name: `Board ${Math.random().toString(36).slice(2, 6)}`,
      position: 0,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  column(overrides: Partial<Column> = {}): Column {
    const id = `column-${Math.random().toString(36).slice(2, 10)}`
    return {
      id,
      boardId: `board-${Math.random().toString(36).slice(2, 8)}`,
      name: `Column ${Math.random().toString(36).slice(2, 6)}`,
      position: 0,
      wipLimit: null,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  columns(count: number, boardId?: string): Column[] {
    return Array.from({ length: count }, (_, i) => this.column({ position: i, boardId }))
  },

  boardView(overrides: Partial<BoardView> = {}): BoardView {
    const board = overrides.board ?? this.board()
    const columns = overrides.columns ?? this.columns(2, board.id)
    return { board, columns }
  },
}
