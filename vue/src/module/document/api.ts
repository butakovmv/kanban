import { get, post, put, del } from '../../fetch'

export interface Document {
  id: string
  projectId: string
  path: string
  title: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface DocumentDetail extends Document {
  content: string
}

export interface CreateDocumentRequest {
  projectId: string
  path: string
  title: string
  content: string
  description?: string | null
}

export interface UpdateDocumentRequest {
  path?: string
  title?: string
  content?: string
  description?: string | null
}

interface RawDocument {
  id: string
  project_id: string
  path: string
  title: string
  description: string | null
  created_at: string | number
  updated_at: string | number
}

interface RawDocumentDetail {
  id: string
  project_id: string
  path: string
  title: string
  content: string
  description: string | null
  created_at: string | number
  updated_at: string | number
}

interface RawDocumentsResponse {
  documents: RawDocument[]
}

function toDocument(raw: RawDocument): Document {
  return {
    id: raw.id,
    projectId: raw.project_id,
    path: raw.path,
    title: raw.title,
    description: raw.description,
    createdAt: typeof raw.created_at === 'string' ? raw.created_at : new Date(raw.created_at * 1000).toISOString(),
    updatedAt: typeof raw.updated_at === 'string' ? raw.updated_at : new Date(raw.updated_at * 1000).toISOString(),
  }
}

function toDocumentDetail(raw: RawDocumentDetail): DocumentDetail {
  return {
    id: raw.id,
    projectId: raw.project_id,
    path: raw.path,
    title: raw.title,
    content: raw.content,
    description: raw.description,
    createdAt: typeof raw.created_at === 'string' ? raw.created_at : new Date(raw.created_at * 1000).toISOString(),
    updatedAt: typeof raw.updated_at === 'string' ? raw.updated_at : new Date(raw.updated_at * 1000).toISOString(),
  }
}

export function listDocuments(projectId: string): Promise<Document[]> {
  return get<RawDocumentsResponse>(
    `/projects/${encodeURIComponent(projectId)}/documents`,
  ).then((response) => response.documents.map(toDocument))
}

export function getDocument(id: string): Promise<DocumentDetail> {
  return get<RawDocumentDetail>(`/documents/${encodeURIComponent(id)}`).then(toDocumentDetail)
}

export function createDocument(request: CreateDocumentRequest): Promise<Document> {
  const body: Record<string, unknown> = {
    project_id: request.projectId,
    path: request.path,
    title: request.title,
    content: request.content,
  }
  if (request.description !== undefined) {
    body['description'] = request.description
  }
  return post<RawDocument>('/documents', body).then(toDocument)
}

export function updateDocument(id: string, request: UpdateDocumentRequest): Promise<DocumentDetail> {
  const body: Record<string, unknown> = {}
  if (request.path !== undefined) body['path'] = request.path
  if (request.title !== undefined) body['title'] = request.title
  if (request.content !== undefined) body['content'] = request.content
  if (request.description !== undefined) body['description'] = request.description
  return put<RawDocumentDetail>(`/documents/${encodeURIComponent(id)}`, body).then(toDocumentDetail)
}

export function deleteDocument(id: string): Promise<void> {
  return del<void>(`/documents/${encodeURIComponent(id)}`)
}
