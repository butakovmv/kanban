import { get, post, put, del } from '../../fetch'

/**
 * Доска, возвращаемая сервером.
 */
export interface Board {
  id: string
  projectId: string
  name: string
  position: number
  createdAt: string
}

/**
 * Колонка на доске.
 */
export interface Column {
  id: string
  boardId: string
  name: string
  position: number
  wipLimit: number | null
  createdAt: string
}

/**
 * Ответ сервера с доской и её колонками.
 */
export interface BoardView {
  board: Board
  columns: Column[]
}

/**
 * Параметры запроса создания доски.
 */
export interface CreateBoardRequest {
  projectId: string
  name: string
}

/**
 * Параметры запроса обновления доски.
 */
export interface UpdateBoardRequest {
  name: string
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление доски.
 */
function toBoard(raw: Board): Board {
  return {
    id: raw.id,
    projectId: raw.projectId,
    name: raw.name,
    position: raw.position,
    createdAt: raw.createdAt,
  }
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление колонки.
 */
function toColumn(raw: Column): Column {
  return {
    id: raw.id,
    boardId: raw.boardId,
    name: raw.name,
    position: raw.position,
    wipLimit: raw.wipLimit,
    createdAt: raw.createdAt,
  }
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление доски с колонками.
 */
function toBoardView(raw: BoardView): BoardView {
  return {
    board: toBoard(raw.board),
    columns: raw.columns.map(toColumn),
  }
}

/**
 * Возвращает доску и её колонки по идентификатору.
 * @param id идентификатор доски
 * @returns доска с массивом колонок
 */
export function getBoard(id: string): Promise<BoardView> {
  return get<BoardView>(`/boards/${encodeURIComponent(id)}`).then(toBoardView)
}

/**
 * Создаёт новую доску.
 * @param request projectId, name
 * @returns созданная доска
 */
export function createBoard(request: CreateBoardRequest): Promise<Board> {
  return post<Board>('/boards', {
    project_id: request.projectId,
    name: request.name,
  }).then(toBoard)
}

/**
 * Обновляет доску.
 * @param id идентификатор доски
 * @param request name
 * @returns обновлённая доска
 */
export function updateBoard(id: string, request: UpdateBoardRequest): Promise<Board> {
  return put<Board>(`/boards/${encodeURIComponent(id)}`, {
    name: request.name,
  }).then(toBoard)
}

/**
 * Удаляет доску.
 * @param id идентификатор доски
 */
export function deleteBoard(id: string): Promise<void> {
  return del<void>(`/boards/${encodeURIComponent(id)}`)
}

/**
 * Сохраняет порядок колонок на доске.
 * @param boardId идентификатор доски
 * @param columnIds упорядоченный список идентификаторов колонок
 * @returns доска с обновлённым порядком колонок
 */
export function reorderColumns(boardId: string, columnIds: string[]): Promise<BoardView> {
  return put<BoardView>(
    `/boards/${encodeURIComponent(boardId)}/columns/order`,
    { column_ids: columnIds },
  ).then(toBoardView)
}
