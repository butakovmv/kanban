import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'
import { documentGenerator } from './documentGenerator'

/**
 * Кодирует строку в base64 через браузерный `btoa`.
 * @param value строка
 * @returns base64
 */
function toBase64(value: string): string {
  return btoa(unescape(encodeURIComponent(value)))
}

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
  }
})

describe('document api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listDocuments', () => {
    it('sends GET to /projects/{id}/documents and returns the mapped list', async () => {
      const documents = documentGenerator.documents(3, 'p-1')
      vi.mocked(fetchModule.get).mockResolvedValue(documents)

      const result = await api.listDocuments('p-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/projects/p-1/documents')
      expect(result).toEqual(documents)
      expect(result).toHaveLength(3)
    })

    it('encodes special characters in projectId', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue([])

      await api.listDocuments('p with/slash')

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/projects/p%20with%2Fslash/documents',
      )
    })
  })

  describe('getDocument', () => {
    it('sends GET to /documents/{id} and returns the document', async () => {
      const document = documentGenerator.document({ id: 'd-42' })
      vi.mocked(fetchModule.get).mockResolvedValue(document)

      const result = await api.getDocument('d-42')

      expect(fetchModule.get).toHaveBeenCalledWith('/documents/d-42')
      expect(result).toEqual(document)
    })
  })

  describe('createDocument', () => {
    it('sends POST to /documents with snake_case body and returns the document', async () => {
      const request = documentGenerator.createRequest({
        projectId: 'p-1',
        title: 'New doc',
        description: 'Desc',
        fileName: 'file.txt',
        contentType: 'text/plain',
        contentBase64: toBase64('hi'),
        uploadedBy: 'u-1',
      })
      const created = documentGenerator.document({
        id: 'new-id',
        title: 'New doc',
        description: 'Desc',
      })
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      const result = await api.createDocument(request)

      expect(fetchModule.post).toHaveBeenCalledWith('/documents', {
        project_id: 'p-1',
        title: 'New doc',
        description: 'Desc',
        file_name: 'file.txt',
        content_type: 'text/plain',
        content_base64: toBase64('hi'),
        uploaded_by: 'u-1',
      })
      expect(result).toEqual(created)
    })

    it('omits description key when not provided', async () => {
      const request = documentGenerator.createRequest({ description: undefined })
      vi.mocked(fetchModule.post).mockResolvedValue(documentGenerator.document())

      await api.createDocument(request)

      const callArgs = vi.mocked(fetchModule.post).mock.calls[0]
      expect(callArgs).toBeDefined()
      const body = callArgs![1] as Record<string, unknown>
      expect(body).not.toHaveProperty('description')
    })

    it('passes null description explicitly when provided', async () => {
      const request = documentGenerator.createRequest({ description: null })
      vi.mocked(fetchModule.post).mockResolvedValue(documentGenerator.document())

      await api.createDocument(request)

      expect(fetchModule.post).toHaveBeenCalledWith(
        '/documents',
        expect.objectContaining({ description: null }),
      )
    })
  })

  describe('updateDocument', () => {
    it('sends PUT to /documents/{id} with only provided fields', async () => {
      const updated = documentGenerator.document({ title: 'Renamed' })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      const result = await api.updateDocument('d-1', { title: 'Renamed' })

      expect(fetchModule.put).toHaveBeenCalledWith('/documents/d-1', {
        title: 'Renamed',
      })
      expect(result).toEqual(updated)
    })

    it('sends description key with null when explicitly set', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(documentGenerator.document())

      await api.updateDocument('d-1', { description: null })

      expect(fetchModule.put).toHaveBeenCalledWith('/documents/d-1', {
        description: null,
      })
    })

    it('sends empty body when no fields provided', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(documentGenerator.document())

      await api.updateDocument('d-1', {})

      expect(fetchModule.put).toHaveBeenCalledWith('/documents/d-1', {})
    })
  })

  describe('replaceDocumentContent', () => {
    it('sends PUT to /documents/{id}/content with base64 and optional fields', async () => {
      const updated = documentGenerator.document({
        fileName: 'replaced.txt',
        version: 2,
      })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      const result = await api.replaceDocumentContent('d-1', {
        contentBase64: toBase64('new'),
        newFileName: 'replaced.txt',
        newContentType: 'text/plain',
      })

      expect(fetchModule.put).toHaveBeenCalledWith('/documents/d-1/content', {
        content_base64: toBase64('new'),
        new_file_name: 'replaced.txt',
        new_content_type: 'text/plain',
      })
      expect(result).toEqual(updated)
    })

    it('omits optional file name and content type when not provided', async () => {
      vi.mocked(fetchModule.put).mockResolvedValue(documentGenerator.document())

      await api.replaceDocumentContent('d-1', {
        contentBase64: toBase64('x'),
      })

      const body = vi.mocked(fetchModule.put).mock.calls[0]?.[1] as Record<string, unknown>
      expect(body).not.toHaveProperty('new_file_name')
      expect(body).not.toHaveProperty('new_content_type')
      expect(body).toEqual({
        content_base64: toBase64('x'),
      })
    })
  })

  describe('deleteDocument', () => {
    it('sends DELETE to /documents/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deleteDocument('d-99')

      expect(fetchModule.del).toHaveBeenCalledWith('/documents/d-99')
    })
  })

  describe('getDocumentDownloadUrl', () => {
    it('sends GET to /documents/{id}/download and returns the url', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ url: '/api/v1/documents/d-1/raw' })

      const result = await api.getDocumentDownloadUrl('d-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/documents/d-1/download')
      expect(result).toEqual({ url: '/api/v1/documents/d-1/raw' })
    })

    it('encodes special characters in id', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ url: '/api/v1/x' })

      await api.getDocumentDownloadUrl('a/b c')

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/documents/a%2Fb%20c/download',
      )
    })
  })
})
