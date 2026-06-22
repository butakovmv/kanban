import { get, post, put, del } from '../../fetch'

/**
 * Документ проекта, возвращаемый сервером.
 */
export interface Document {
  id: string
  projectId: string
  title: string
  description: string | null
  fileName: string
  contentType: string
  sizeBytes: number
  storageKey: string
  version: number
  uploadedBy: string
  createdAt: string
  updatedAt: string
}

/**
 * Параметры запроса создания документа.
 */
export interface CreateDocumentRequest {
  projectId: string
  title: string
  description?: string | null
  fileName: string
  contentType: string
  contentBase64: string
  uploadedBy: string
}

/**
 * Параметры запроса обновления метаданных документа.
 */
export interface UpdateDocumentRequest {
  title?: string
  description?: string | null
}

/**
 * Параметры запроса замены содержимого документа.
 */
export interface ReplaceContentRequest {
  contentBase64: string
  newFileName?: string
  newContentType?: string
}

interface RawDocument {
  id: string
  project_id: string
  title: string
  description: string | null
  file_name: string
  content_type: string
  size_bytes: number
  storage_key: string
  version: number
  uploaded_by: string
  created_at: string | number
  updated_at: string | number
}

interface RawDocumentsResponse {
  documents: RawDocument[]
}

/**
 * Преобразует snake_case-ответ сервера в camelCase представление документа.
 */
function toDocument(raw: RawDocument): Document {
  return {
    id: raw.id,
    projectId: raw.project_id,
    title: raw.title,
    description: raw.description,
    fileName: raw.file_name,
    contentType: raw.content_type,
    sizeBytes: raw.size_bytes,
    storageKey: raw.storage_key,
    version: raw.version,
    uploadedBy: raw.uploaded_by,
    createdAt: typeof raw.created_at === 'string' ? raw.created_at : new Date(raw.created_at * 1000).toISOString(),
    updatedAt: typeof raw.updated_at === 'string' ? raw.updated_at : new Date(raw.updated_at * 1000).toISOString(),
  }
}

/**
 * Возвращает список документов проекта.
 * @param projectId идентификатор проекта
 * @returns массив документов
 */
export function listDocuments(projectId: string): Promise<Document[]> {
  return get<RawDocumentsResponse>(
    `/projects/${encodeURIComponent(projectId)}/documents`,
  ).then((response) => response.documents.map(toDocument))
}

/**
 * Возвращает документ по идентификатору.
 * @param id идентификатор документа
 */
export function getDocument(id: string): Promise<Document> {
  return get<RawDocument>(`/documents/${encodeURIComponent(id)}`).then(toDocument)
}

/**
 * Создаёт новый документ.
 * @param request projectId, title, description?, fileName, contentType, contentBase64, uploadedBy
 * @returns созданный документ
 */
export function createDocument(request: CreateDocumentRequest): Promise<Document> {
  const body: Record<string, unknown> = {
    project_id: request.projectId,
    title: request.title,
    file_name: request.fileName,
    content_type: request.contentType,
    content_base64: request.contentBase64,
    uploaded_by: request.uploadedBy,
  }
  if (request.description !== undefined) {
    body['description'] = request.description
  }
  return post<RawDocument>('/documents', body).then(toDocument)
}

/**
 * Обновляет метаданные документа (название и/или описание).
 * @param id идентификатор документа
 * @param request title?, description?
 * @returns обновлённый документ
 */
export function updateDocument(id: string, request: UpdateDocumentRequest): Promise<Document> {
  const body: Record<string, unknown> = {}
  if (request.title !== undefined) {
    body['title'] = request.title
  }
  if (request.description !== undefined) {
    body['description'] = request.description
  }
  return put<RawDocument>(`/documents/${encodeURIComponent(id)}`, body).then(toDocument)
}

/**
 * Заменяет содержимое документа новым файлом.
 * @param id идентификатор документа
 * @param request contentBase64, newFileName?, newContentType?
 * @returns обновлённый документ
 */
export function replaceDocumentContent(
  id: string,
  request: ReplaceContentRequest,
): Promise<Document> {
  const body: Record<string, unknown> = {
    content_base64: request.contentBase64,
  }
  if (request.newFileName !== undefined) {
    body['new_file_name'] = request.newFileName
  }
  if (request.newContentType !== undefined) {
    body['new_content_type'] = request.newContentType
  }
  return put<Document>(`/documents/${encodeURIComponent(id)}/content`, body).then(toDocument)
}

/**
 * Удаляет документ.
 * @param id идентификатор документа
 */
export function deleteDocument(id: string): Promise<void> {
  return del<void>(`/documents/${encodeURIComponent(id)}`)
}

/**
 * Запрашивает у сервера presigned URL для скачивания документа.
 * @param id идентификатор документа
 * @returns объект с относительным URL
 */
export function getDocumentDownloadUrl(id: string): Promise<{ url: string }> {
  return get<{ url: string }>(`/documents/${encodeURIComponent(id)}/download`)
}
