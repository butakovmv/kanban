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

interface RawBoard {
  id: string
  project_id: string
  name: string
  position: number
  created_at: string
}

interface RawColumn {
  id: string
  board_id: string
  name: string
  position: number
  wip_limit: number | null
  created_at: string
}

interface RawBoardView {
  board: RawBoard
  columns: RawColumn[]
}

function toBoard(raw: RawBoard): Board {
  return {
    id: raw.id,
    projectId: raw.project_id,
    name: raw.name,
    position: raw.position,
    createdAt: raw.created_at,
  }
}

function toColumn(raw: RawColumn): Column {
  return {
    id: raw.id,
    boardId: raw.board_id,
    name: raw.name,
    position: raw.position,
    wipLimit: raw.wip_limit,
    createdAt: raw.created_at,
  }
}

function toBoardView(raw: RawBoardView): BoardView {
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
  return get<RawBoardView>(`/boards/${encodeURIComponent(id)}`).then(toBoardView)
}

interface BoardsListResponse {
  boards: RawBoard[]
}

/**
 * Возвращает доску по идентификатору проекта.
 * @param projectId идентификатор проекта
 * @returns доска с колонками
 */
export function getBoardByProjectId(projectId: string): Promise<BoardView> {
  return get<RawBoardView>(`/projects/${encodeURIComponent(projectId)}/board`).then(toBoardView)
}

/**
 * Возвращает список досок проекта.
 * @param projectId идентификатор проекта
 * @returns массив досок
 */
export function listBoardsByProjectId(projectId: string): Promise<Board[]> {
  return get<BoardsListResponse>(`/projects/${encodeURIComponent(projectId)}/boards`).then(
    (r) => r.boards.map(toBoard),
  )
}

/**
 * Создаёт новую доску.
 * @param request projectId, name
 * @returns созданная доска
 */
export function createBoard(request: CreateBoardRequest): Promise<Board> {
  return post<RawBoard>('/boards', {
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
  return put<RawBoard>(`/boards/${encodeURIComponent(id)}`, {
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
  return put<RawBoardView>(
    `/boards/${encodeURIComponent(boardId)}/columns/order`,
    { column_ids: columnIds },
  ).then(toBoardView)
}
