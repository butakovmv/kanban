import type { Board, BoardView, Column } from '../api'

function isoDate() {
  return new Date('2025-01-01T00:00:00Z').toISOString()
}

const id = () => Math.random().toString(36).slice(2, 10)
const shortId = () => Math.random().toString(36).slice(2, 8)

export const boardGenerator = {
  board(overrides: Partial<Board> = {}): Board {
    return {
      id: `board-${id()}`,
      projectId: `project-${shortId()}`,
      name: `Board ${shortId()}`,
      position: 0,
      createdAt: isoDate(),
      ...overrides,
    }
  },

  column(overrides: Partial<Column> = {}): Column {
    return {
      id: `column-${id()}`,
      boardId: `board-${shortId()}`,
      name: `Column ${shortId()}`,
      position: 0,
      wipLimit: null,
      createdAt: isoDate(),
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

  rawBoard(overrides: Partial<Record<string, unknown>> = {}): Record<string, unknown> {
    return {
      id: `board-${id()}`,
      project_id: `project-${shortId()}`,
      name: `Board ${shortId()}`,
      position: 0,
      created_at: isoDate(),
      ...overrides,
    }
  },

  rawColumn(overrides: Partial<Record<string, unknown>> = {}): Record<string, unknown> {
    return {
      id: `column-${id()}`,
      board_id: `board-${shortId()}`,
      name: `Column ${shortId()}`,
      position: 0,
      wip_limit: null,
      created_at: isoDate(),
      ...overrides,
    }
  },

  rawBoardView(overrides: Partial<Record<string, unknown>> = {}): Record<string, unknown> {
    const board = overrides.board ?? this.rawBoard()
    const columns = overrides.columns ?? [this.rawColumn({ board_id: board.id }), this.rawColumn({ board_id: board.id, position: 1 })]
    return { board, columns }
  },
}
