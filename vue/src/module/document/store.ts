import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as documentApi from './api'

export const useDocumentStore = defineStore('document', () => {
  const documents = ref<documentApi.Document[]>([])
  const currentDocument = ref<documentApi.DocumentDetail | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const hasDocuments = computed(() => documents.value.length > 0)

  async function loadDocuments(projectId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      documents.value = await documentApi.listDocuments(projectId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load documents'
      console.error('Document store error:', e)
      documents.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  async function loadDocument(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      currentDocument.value = await documentApi.getDocument(id)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load document'
      console.error('Document store error:', e)
      currentDocument.value = null
      return false
    } finally {
      loading.value = false
    }
  }

  async function createDocument(
    request: documentApi.CreateDocumentRequest,
  ): Promise<documentApi.Document | null> {
    loading.value = true
    error.value = null
    try {
      const created = await documentApi.createDocument(request)
      documents.value = [...documents.value, created]
      return created
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to create document'
      console.error('Document store error:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateDocument(
    id: string,
    request: documentApi.UpdateDocumentRequest,
  ): Promise<documentApi.DocumentDetail | null> {
    loading.value = true
    error.value = null
    try {
      const updated = await documentApi.updateDocument(id, request)
      documents.value = documents.value.map((d) => (d.id === id ? updated : d))
      if (currentDocument.value !== null && currentDocument.value.id === id) {
        currentDocument.value = updated
      }
      return updated
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to update document'
      console.error('Document store error:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  async function deleteDocument(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await documentApi.deleteDocument(id)
      documents.value = documents.value.filter((d) => d.id !== id)
      if (currentDocument.value !== null && currentDocument.value.id === id) {
        currentDocument.value = null
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to delete document'
      console.error('Document store error:', e)
      return false
    } finally {
      loading.value = false
    }
  }

  function clearCurrent(): void {
    currentDocument.value = null
  }

  function clearDocuments(): void {
    documents.value = []
  }

  return {
    documents,
    currentDocument,
    loading,
    error,
    hasDocuments,
    loadDocuments,
    loadDocument,
    createDocument,
    updateDocument,
    deleteDocument,
    clearCurrent,
    clearDocuments,
  }
})
