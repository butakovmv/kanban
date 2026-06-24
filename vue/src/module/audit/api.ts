import { get } from '../../fetch'

export interface AuditEntry {
  id: string
  projectId: string
  documentId: string | null
  userId: string
  action: string
  details: string | null
  createdAt: string
}

export interface AuditLogResponse {
  items: AuditEntry[]
  total: number
}

export function listAuditLog(
  projectId: string,
  page: number = 1,
  size: number = 20,
): Promise<AuditLogResponse> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  return get<AuditLogResponse>(
    `/projects/${encodeURIComponent(projectId)}/audit-log?${params.toString()}`,
  )
}
