import { ref } from 'vue'
import * as boardApi from '../module/board/api'

export function useBoards(projectId: () => string | undefined) {
  const boards = ref<boardApi.Board[]>([])
  const boardsLoading = ref(false)
  const boardsError = ref<string | null>(null)

  async function loadBoards() {
    const id = projectId()
    if (id === undefined) return
    boardsLoading.value = true
    boardsError.value = null
    try {
      boards.value = await boardApi.listBoardsByProjectId(id)
    } catch (e: unknown) {
      boardsError.value = e instanceof Error ? e.message : 'Failed to load boards'
      boards.value = []
    } finally {
      boardsLoading.value = false
    }
  }

  return {
    boards,
    boardsLoading,
    boardsError,
    loadBoards,
  }
}
