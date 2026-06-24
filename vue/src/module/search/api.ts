import { get } from '../../fetch'

export interface SearchResult {
  id: string
  title: string
  description: string | null
  status: string
  priority: string | null
  assigneeId: string | null
  boardId: string
  columnId: string
  projectId: string
  dueDate: string | null
  createdAt: string
  updatedAt: string
  rank: number
}

export interface SearchParams {
  q: string
  projectId?: string
  status?: string
  priority?: string
  assigneeId?: string
  dueDateFrom?: string
  dueDateTo?: string
  page?: number
  size?: number
}

interface SearchResponse {
  results: SearchResult[]
  total: number
}

function toSnakeCase(params: SearchParams): Record<string, string> {
  const q: Record<string, string> = {}
  q['q'] = params.q
  if (params.projectId !== undefined) q['project_id'] = params.projectId
  if (params.status !== undefined) q['status'] = params.status
  if (params.priority !== undefined) q['priority'] = params.priority
  if (params.assigneeId !== undefined) q['assignee_id'] = params.assigneeId
  if (params.dueDateFrom !== undefined) q['due_date_from'] = params.dueDateFrom
  if (params.dueDateTo !== undefined) q['due_date_to'] = params.dueDateTo
  if (params.page !== undefined) q['page'] = String(params.page)
  if (params.size !== undefined) q['size'] = String(params.size)
  return q
}

export function searchTasks(params: SearchParams): Promise<{ results: SearchResult[]; total: number }> {
  const query = new URLSearchParams(toSnakeCase(params)).toString()
  return get<SearchResponse>(`/search?${query}`)
}
