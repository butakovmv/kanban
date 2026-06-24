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
 * Сырой проект из JSON-ответа сервера (snake_case).
 */
interface RawProject {
  id: string
  owner_id: string
  name: string
  description: string | null
  created_at: string
  updated_at: string
}

/**
 * Ответ сервера со списком проектов.
 */
interface ProjectListResponse {
  projects: RawProject[]
}

/**
 * Преобразует snake_case-ответ сервера в Project.
 */
function toProject(raw: RawProject): Project {
  return {
    id: raw.id,
    ownerId: raw.owner_id,
    name: raw.name,
    description: raw.description,
    createdAt: raw.created_at,
    updatedAt: raw.updated_at,
  }
}

/**
 * Возвращает список проектов пользователя.
 * @param ownerId идентификатор владельца
 * @returns массив проектов
 */
export function listProjects(ownerId: string): Promise<Project[]> {
  return get<ProjectListResponse>(`/projects?owner_id=${encodeURIComponent(ownerId)}`).then(
    (response) => response.projects.map(toProject),
  )
}

/**
 * Возвращает детальную информацию о проекте.
 * @param id идентификатор проекта
 */
export function getProject(id: string): Promise<Project> {
  return get<RawProject>(`/projects/${encodeURIComponent(id)}`).then(toProject)
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
  return post<RawProject>('/projects', body).then(toProject)
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
  return put<RawProject>(`/projects/${encodeURIComponent(id)}`, body).then(toProject)
}

/**
 * Удаляет проект.
 * @param id идентификатор проекта
 */
export function deleteProject(id: string): Promise<void> {
  return del<void>(`/projects/${encodeURIComponent(id)}`)
}

export interface ProjectMember {
  userId: string
  displayName: string
  addedAt: string
}

interface RawProjectMember {
  user_id: string
  display_name: string
  added_at: string
}

interface MemberListResponse {
  members: RawProjectMember[]
}

function toProjectMember(raw: RawProjectMember): ProjectMember {
  return { userId: raw.user_id, displayName: raw.display_name, addedAt: raw.added_at }
}

export function listProjectMembers(projectId: string): Promise<ProjectMember[]> {
  return get<MemberListResponse>(`/projects/${encodeURIComponent(projectId)}/members`).then(
    (r) => r.members.map(toProjectMember),
  )
}

export function addProjectMember(projectId: string, userId: string): Promise<void> {
  return post<void>(`/projects/${encodeURIComponent(projectId)}/members`, { user_id: userId })
}

export function removeProjectMember(projectId: string, userId: string): Promise<void> {
  return del<void>(`/projects/${encodeURIComponent(projectId)}/members?user_id=${encodeURIComponent(userId)}`)
}

export function listMemberProjects(userId: string): Promise<Project[]> {
  return get<ProjectListResponse>(`/projects/member?user_id=${encodeURIComponent(userId)}`).then(
    (r) => r.projects.map(toProject),
  )
}
