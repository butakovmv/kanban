import { get, post, put, del } from '../../fetch'

/**
 * Проект, возвращаемый сервером.
 */
export interface Project {
  id: string
  ownerId: string
  name: string
  description: string | null
  createdAt: string
  updatedAt: string
}

/**
 * Параметры запроса создания проекта.
 */
export interface CreateProjectRequest {
  ownerId: string
  name: string
  description?: string | null
}

/**
 * Параметры запроса обновления проекта.
 */
export interface UpdateProjectRequest {
  name?: string
  description?: string | null
}

/**
 * Ответ сервера со списком проектов.
 */
interface ProjectListResponse {
  items: Project[]
  total: number
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление проекта.
 */
function toProject(raw: Project): Project {
  return {
    id: raw.id,
    ownerId: raw.ownerId,
    name: raw.name,
    description: raw.description,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

/**
 * Возвращает список проектов пользователя.
 * @param ownerId идентификатор владельца
 * @returns массив проектов
 */
export function listProjects(ownerId: string): Promise<Project[]> {
  return get<ProjectListResponse>(`/projects?owner_id=${encodeURIComponent(ownerId)}`).then(
    (response) => response.items.map(toProject),
  )
}

/**
 * Возвращает детальную информацию о проекте.
 * @param id идентификатор проекта
 */
export function getProject(id: string): Promise<Project> {
  return get<Project>(`/projects/${encodeURIComponent(id)}`).then(toProject)
}

/**
 * Создаёт новый проект.
 * @param request ownerId, name, description?
 * @returns созданный проект
 */
export function createProject(request: CreateProjectRequest): Promise<Project> {
  const body: Record<string, unknown> = {
    owner_id: request.ownerId,
    name: request.name,
  }
  if (request.description !== undefined) {
    body['description'] = request.description
  }
  return post<Project>('/projects', body).then(toProject)
}

/**
 * Обновляет проект.
 * @param id идентификатор проекта
 * @param request name?, description?
 * @returns обновлённый проект
 */
export function updateProject(id: string, request: UpdateProjectRequest): Promise<Project> {
  const body: Record<string, unknown> = {}
  if (request.name !== undefined) {
    body['name'] = request.name
  }
  if (request.description !== undefined) {
    body['description'] = request.description
  }
  return put<Project>(`/projects/${encodeURIComponent(id)}`, body).then(toProject)
}

/**
 * Удаляет проект.
 * @param id идентификатор проекта
 */
export function deleteProject(id: string): Promise<void> {
  return del<void>(`/projects/${encodeURIComponent(id)}`)
}
