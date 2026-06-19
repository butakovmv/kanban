import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as documentApi from './api'

/**
 * Pinia-хранилище состояния документов проекта.
 * Управляет списком документов, текущим выбранным документом
 * и действиями загрузки, создания, обновления, замены содержимого
 * и удаления документов, а также получения URL для скачивания.
 */
export const useDocumentStore = defineStore('document', () => {
  const documents = ref<documentApi.Document[]>([])
  const currentDocument = ref<documentApi.Document | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const hasDocuments = computed(() => documents.value.length > 0)

  /**
   * Загружает список документов проекта.
   * @param projectId идентификатор проекта
   * @returns true при успешной загрузке, false при ошибке
   */
  async function loadDocuments(projectId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      documents.value = await documentApi.listDocuments(projectId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load documents'
      documents.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Загружает документ по идентификатору и сохраняет его в currentDocument.
   * @param id идентификатор документа
   * @returns true при успешной загрузке, false при ошибке
   */
  async function loadDocument(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      currentDocument.value = await documentApi.getDocument(id)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load document'
      currentDocument.value = null
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Создаёт новый документ и добавляет его в локальный список.
   * @param request параметры создания
   * @returns созданный документ или null при ошибке
   */
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
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Обновляет метаданные документа и заменяет его в локальном списке.
   * @param id идентификатор документа
   * @param request title?, description?
   * @returns обновлённый документ или null при ошибке
   */
  async function updateDocument(
    id: string,
    request: documentApi.UpdateDocumentRequest,
  ): Promise<documentApi.Document | null> {
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
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Заменяет содержимое документа и обновляет его в локальном списке.
   * @param id идентификатор документа
   * @param request contentBase64, newFileName?, newContentType?
   * @returns обновлённый документ или null при ошибке
   */
  async function replaceDocumentContent(
    id: string,
    request: documentApi.ReplaceContentRequest,
  ): Promise<documentApi.Document | null> {
    loading.value = true
    error.value = null
    try {
      const updated = await documentApi.replaceDocumentContent(id, request)
      documents.value = documents.value.map((d) => (d.id === id ? updated : d))
      if (currentDocument.value !== null && currentDocument.value.id === id) {
        currentDocument.value = updated
      }
      return updated
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to replace document content'
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Удаляет документ и убирает его из локального списка.
   * @param id идентификатор документа
   * @returns true при успешном удалении, false при ошибке
   */
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
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Запрашивает presigned URL для скачивания документа.
   * @param id идентификатор документа
   * @returns URL или null при ошибке
   */
  async function getDownloadUrl(id: string): Promise<string | null> {
    error.value = null
    try {
      const response = await documentApi.getDocumentDownloadUrl(id)
      return response.url
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to get download URL'
      return null
    }
  }

  /**
   * Сбрасывает состояние текущего выбранного документа.
   */
  function clearCurrent(): void {
    currentDocument.value = null
  }

  /**
   * Сбрасывает список документов (например, при переходе на другой проект).
   */
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
    replaceDocumentContent,
    deleteDocument,
    getDownloadUrl,
    clearCurrent,
    clearDocuments,
  }
})
