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

/**
 * Преобразует snake_case-ответ сервера в camelCase представление документа.
 */
function toDocument(raw: Document): Document {
  return {
    id: raw.id,
    projectId: raw.projectId,
    title: raw.title,
    description: raw.description,
    fileName: raw.fileName,
    contentType: raw.contentType,
    sizeBytes: raw.sizeBytes,
    storageKey: raw.storageKey,
    version: raw.version,
    uploadedBy: raw.uploadedBy,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

/**
 * Возвращает список документов проекта.
 * @param projectId идентификатор проекта
 * @returns массив документов
 */
export function listDocuments(projectId: string): Promise<Document[]> {
  return get<Document[]>(
    `/projects/${encodeURIComponent(projectId)}/documents`,
  ).then((items) => items.map(toDocument))
}

/**
 * Возвращает документ по идентификатору.
 * @param id идентификатор документа
 */
export function getDocument(id: string): Promise<Document> {
  return get<Document>(`/documents/${encodeURIComponent(id)}`).then(toDocument)
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
  return post<Document>('/documents', body).then(toDocument)
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
  return put<Document>(`/documents/${encodeURIComponent(id)}`, body).then(toDocument)
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
