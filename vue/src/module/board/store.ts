import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as boardApi from './api'

/**
 * Pinia-хранилище состояния доски и её колонок.
 * Управляет текущей открытой доской, списком колонок
 * и действиями загрузки, создания, обновления, удаления и изменения порядка.
 */
export const useBoardStore = defineStore('board', () => {
  const currentBoard = ref<boardApi.Board | null>(null)
  const columns = ref<boardApi.Column[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const hasBoard = computed(() => currentBoard.value !== null)
  const hasColumns = computed(() => columns.value.length > 0)

  /**
   * Загружает доску с её колонками.
   * @param id идентификатор доски
   * @returns true при успешной загрузке, false при ошибке
   */
  async function loadBoard(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const view = await boardApi.getBoard(id)
      currentBoard.value = view.board
      columns.value = view.columns
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load board'
      currentBoard.value = null
      columns.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Создаёт новую доску.
   * @param request параметры создания
   * @returns созданная доска или null при ошибке
   */
  async function createBoard(
    request: boardApi.CreateBoardRequest,
  ): Promise<boardApi.Board | null> {
    loading.value = true
    error.value = null
    try {
      const board = await boardApi.createBoard(request)
      return board
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to create board'
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Обновляет доску.
   * @param id идентификатор доски
   * @param request параметры обновления
   * @returns true при успешном обновлении, false при ошибке
   */
  async function updateBoard(id: string, request: boardApi.UpdateBoardRequest): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const updated = await boardApi.updateBoard(id, request)
      if (currentBoard.value !== null && currentBoard.value.id === id) {
        currentBoard.value = updated
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to update board'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Удаляет доску.
   * @param id идентификатор доски
   * @returns true при успешном удалении, false при ошибке
   */
  async function deleteBoard(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await boardApi.deleteBoard(id)
      if (currentBoard.value !== null && currentBoard.value.id === id) {
        currentBoard.value = null
        columns.value = []
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to delete board'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Сохраняет порядок колонок на доске.
   * @param boardId идентификатор доски
   * @param columnIds упорядоченный список идентификаторов колонок
   * @returns true при успешном сохранении, false при ошибке
   */
  async function reorderColumns(boardId: string, columnIds: string[]): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const view = await boardApi.reorderColumns(boardId, columnIds)
      currentBoard.value = view.board
      columns.value = view.columns
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to reorder columns'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Сбрасывает состояние текущей доски.
   */
  function clearCurrent(): void {
    currentBoard.value = null
    columns.value = []
  }

  return {
    currentBoard,
    columns,
    loading,
    error,
    hasBoard,
    hasColumns,
    loadBoard,
    createBoard,
    updateBoard,
    deleteBoard,
    reorderColumns,
    clearCurrent,
  }
})
