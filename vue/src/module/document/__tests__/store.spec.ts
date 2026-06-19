import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDocumentStore } from '../store'
import * as api from '../api'
import { documentGenerator } from './documentGenerator'

/**
 * Кодирует строку в base64 через браузерный `btoa`.
 * @param value строка
 * @returns base64
 */
function toBase64(value: string): string {
  return btoa(unescape(encodeURIComponent(value)))
}

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    listDocuments: vi.fn(),
    getDocument: vi.fn(),
    createDocument: vi.fn(),
    updateDocument: vi.fn(),
    replaceDocumentContent: vi.fn(),
    deleteDocument: vi.fn(),
    getDocumentDownloadUrl: vi.fn(),
  }
})

describe('document store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty state', () => {
    const store = useDocumentStore()
    expect(store.documents).toEqual([])
    expect(store.currentDocument).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.hasDocuments).toBe(false)
  })

  describe('loadDocuments', () => {
    it('populates documents on success', async () => {
      const documents = documentGenerator.documents(3, 'p-1')
      vi.mocked(api.listDocuments).mockResolvedValue(documents)

      const store = useDocumentStore()
      const success = await store.loadDocuments('p-1')

      expect(success).toBe(true)
      expect(store.documents).toEqual(documents)
      expect(store.hasDocuments).toBe(true)
      expect(api.listDocuments).toHaveBeenCalledWith('p-1')
    })

    it('sets error and clears documents on failure', async () => {
      vi.mocked(api.listDocuments).mockRejectedValue(new Error('Network error'))

      const store = useDocumentStore()
      const success = await store.loadDocuments('p-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Network error')
      expect(store.documents).toEqual([])
    })

    it('uses generic error message for non-Error rejection', async () => {
      vi.mocked(api.listDocuments).mockRejectedValue('plain string')

      const store = useDocumentStore()
      const success = await store.loadDocuments('p-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Failed to load documents')
    })
  })

  describe('loadDocument', () => {
    it('populates currentDocument on success', async () => {
      const document = documentGenerator.document({ id: 'd-1' })
      vi.mocked(api.getDocument).mockResolvedValue(document)

      const store = useDocumentStore()
      const success = await store.loadDocument('d-1')

      expect(success).toBe(true)
      expect(store.currentDocument).toEqual(document)
    })

    it('sets error and clears currentDocument on failure', async () => {
      vi.mocked(api.getDocument).mockRejectedValue(new Error('Not found'))

      const store = useDocumentStore()
      const success = await store.loadDocument('d-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Not found')
      expect(store.currentDocument).toBeNull()
    })
  })

  describe('createDocument', () => {
    it('appends the new document and returns it on success', async () => {
      const existing = documentGenerator.document({ id: 'd-1' })
      const created = documentGenerator.document({ id: 'd-2' })
      vi.mocked(api.createDocument).mockResolvedValue(created)

      const store = useDocumentStore()
      store.documents = [existing]

      const request = documentGenerator.createRequest()
      const result = await store.createDocument(request)

      expect(result).toEqual(created)
      expect(store.documents).toEqual([existing, created])
    })

    it('sets error and returns null on failure', async () => {
      vi.mocked(api.createDocument).mockRejectedValue(new Error('Validation failed'))

      const store = useDocumentStore()
      const result = await store.createDocument(documentGenerator.createRequest())

      expect(result).toBeNull()
      expect(store.error).toBe('Validation failed')
    })
  })

  describe('updateDocument', () => {
    it('replaces the document in the list and returns it on success', async () => {
      const original = documentGenerator.document({ id: 'd-1', title: 'Old' })
      const updated = documentGenerator.document({ id: 'd-1', title: 'New' })
      const other = documentGenerator.document({ id: 'd-2' })
      vi.mocked(api.updateDocument).mockResolvedValue(updated)

      const store = useDocumentStore()
      store.documents = [original, other]

      const result = await store.updateDocument('d-1', { title: 'New' })

      expect(result).toEqual(updated)
      expect(store.documents).toEqual([updated, other])
    })

    it('updates currentDocument when it matches the updated id', async () => {
      const updated = documentGenerator.document({ id: 'd-1', title: 'Renamed' })
      vi.mocked(api.updateDocument).mockResolvedValue(updated)

      const store = useDocumentStore()
      store.currentDocument = documentGenerator.document({ id: 'd-1', title: 'Old' })

      await store.updateDocument('d-1', { title: 'Renamed' })

      expect(store.currentDocument).toEqual(updated)
    })

    it('does not touch currentDocument when ids differ', async () => {
      const updated = documentGenerator.document({ id: 'd-1' })
      const current = documentGenerator.document({ id: 'd-2' })
      vi.mocked(api.updateDocument).mockResolvedValue(updated)

      const store = useDocumentStore()
      store.currentDocument = current

      await store.updateDocument('d-1', { title: 'Whatever' })

      expect(store.currentDocument).toEqual(current)
    })

    it('sets error and returns null on failure', async () => {
      vi.mocked(api.updateDocument).mockRejectedValue(new Error('Server error'))

      const store = useDocumentStore()
      const result = await store.updateDocument('d-1', { title: 'X' })

      expect(result).toBeNull()
      expect(store.error).toBe('Server error')
    })
  })

  describe('replaceDocumentContent', () => {
    it('replaces the document in the list and currentDocument on success', async () => {
      const original = documentGenerator.document({ id: 'd-1', version: 1 })
      const updated = documentGenerator.document({ id: 'd-1', version: 2 })
      vi.mocked(api.replaceDocumentContent).mockResolvedValue(updated)

      const store = useDocumentStore()
      store.documents = [original]
      store.currentDocument = original

      const result = await store.replaceDocumentContent('d-1', {
        contentBase64: toBase64('x'),
      })

      expect(result).toEqual(updated)
      expect(store.documents[0]?.version).toBe(2)
      expect(store.currentDocument?.version).toBe(2)
    })

    it('sets error and returns null on failure', async () => {
      vi.mocked(api.replaceDocumentContent).mockRejectedValue(new Error('Quota exceeded'))

      const store = useDocumentStore()
      const result = await store.replaceDocumentContent('d-1', {
        contentBase64: toBase64('x'),
      })

      expect(result).toBeNull()
      expect(store.error).toBe('Quota exceeded')
    })
  })

  describe('deleteDocument', () => {
    it('removes the document from the list and returns true on success', async () => {
      const document = documentGenerator.document({ id: 'd-1' })
      const other = documentGenerator.document({ id: 'd-2' })
      vi.mocked(api.deleteDocument).mockResolvedValue(undefined)

      const store = useDocumentStore()
      store.documents = [document, other]

      const success = await store.deleteDocument('d-1')

      expect(success).toBe(true)
      expect(store.documents).toEqual([other])
    })

    it('clears currentDocument when matching id is deleted', async () => {
      vi.mocked(api.deleteDocument).mockResolvedValue(undefined)

      const store = useDocumentStore()
      store.currentDocument = documentGenerator.document({ id: 'd-1' })

      await store.deleteDocument('d-1')

      expect(store.currentDocument).toBeNull()
    })

    it('sets error and returns false on failure', async () => {
      vi.mocked(api.deleteDocument).mockRejectedValue(new Error('Forbidden'))

      const store = useDocumentStore()
      const success = await store.deleteDocument('d-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Forbidden')
    })
  })

  describe('getDownloadUrl', () => {
    it('returns the URL on success', async () => {
      vi.mocked(api.getDocumentDownloadUrl).mockResolvedValue({ url: '/api/v1/x' })

      const store = useDocumentStore()
      const url = await store.getDownloadUrl('d-1')

      expect(url).toBe('/api/v1/x')
    })

    it('returns null and sets error on failure', async () => {
      vi.mocked(api.getDocumentDownloadUrl).mockRejectedValue(new Error('Not found'))

      const store = useDocumentStore()
      const url = await store.getDownloadUrl('d-1')

      expect(url).toBeNull()
      expect(store.error).toBe('Not found')
    })
  })

  describe('clearCurrent / clearDocuments', () => {
    it('clearCurrent resets currentDocument to null', () => {
      const store = useDocumentStore()
      store.currentDocument = documentGenerator.document()

      store.clearCurrent()

      expect(store.currentDocument).toBeNull()
    })

    it('clearDocuments resets the documents list', () => {
      const store = useDocumentStore()
      store.documents = documentGenerator.documents(3)

      store.clearDocuments()

      expect(store.documents).toEqual([])
      expect(store.hasDocuments).toBe(false)
    })
  })
})
