import type {
  CreateDocumentRequest,
  Document,
  ReplaceContentRequest,
  UpdateDocumentRequest,
} from '../api'

/**
 * Кодирует строку в base64 через браузерный `btoa`.
 * @param value строка
 * @returns base64
 */
function toBase64(value: string): string {
  return btoa(unescape(encodeURIComponent(value)))
}

/**
 * Генератор тестовых данных для document-модуля.
 * Создаёт случайные сущности для тестов api.ts и store.ts.
 */
export const documentGenerator = {
  document(overrides: Partial<Document> = {}): Document {
    const id = `doc-${Math.random().toString(36).slice(2, 10)}`
    return {
      id,
      projectId: `project-${Math.random().toString(36).slice(2, 8)}`,
      title: `Document ${Math.random().toString(36).slice(2, 6)}`,
      description: `Description for document ${id}`,
      fileName: `file-${Math.random().toString(36).slice(2, 6)}.txt`,
      contentType: 'text/plain',
      sizeBytes: 2048,
      storageKey: `storage/${id}`,
      version: 1,
      uploadedBy: `user-${Math.random().toString(36).slice(2, 8)}`,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      updatedAt: new Date('2025-01-02T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  documents(count: number, projectId?: string): Document[] {
    return Array.from({ length: count }, () => this.document({ projectId }))
  },

  createRequest(overrides: Partial<CreateDocumentRequest> = {}): CreateDocumentRequest {
    return {
      projectId: `project-${Math.random().toString(36).slice(2, 8)}`,
      title: `New document ${Math.random().toString(36).slice(2, 6)}`,
      description: 'Test description',
      fileName: 'test.txt',
      contentType: 'text/plain',
      contentBase64: toBase64('hello world'),
      uploadedBy: `user-${Math.random().toString(36).slice(2, 8)}`,
      ...overrides,
    }
  },

  updateRequest(overrides: Partial<UpdateDocumentRequest> = {}): UpdateDocumentRequest {
    return {
      title: `Updated ${Math.random().toString(36).slice(2, 6)}`,
      ...overrides,
    }
  },

  replaceRequest(overrides: Partial<ReplaceContentRequest> = {}): ReplaceContentRequest {
    return {
      contentBase64: toBase64('new content'),
      newFileName: 'replaced.txt',
      newContentType: 'text/plain',
      ...overrides,
    }
  },
}
