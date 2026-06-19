import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as projectApi from './api'

/**
 * Pinia-хранилище состояния проектов.
 * Управляет списком проектов, текущим выбранным проектом
 * и действиями загрузки, создания, обновления и удаления.
 */
export const useProjectStore = defineStore('project', () => {
  const projects = ref<projectApi.Project[]>([])
  const currentProject = ref<projectApi.Project | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const hasProjects = computed(() => projects.value.length > 0)

  /**
   * Загружает список проектов пользователя.
   * @param ownerId идентификатор владельца
   * @returns true при успешной загрузке, false при ошибке
   */
  async function loadProjects(ownerId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      projects.value = await projectApi.listProjects(ownerId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load projects'
      projects.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Загружает детальную информацию о проекте.
   * @param id идентификатор проекта
   * @returns true при успешной загрузке, false при ошибке
   */
  async function loadProject(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      currentProject.value = await projectApi.getProject(id)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load project'
      currentProject.value = null
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Создаёт новый проект.
   * @param request параметры создания
   * @returns созданный проект или null при ошибке
   */
  async function createProject(
    request: projectApi.CreateProjectRequest,
  ): Promise<projectApi.Project | null> {
    loading.value = true
    error.value = null
    try {
      const project = await projectApi.createProject(request)
      projects.value = [...projects.value, project]
      return project
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to create project'
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Обновляет проект.
   * @param id идентификатор проекта
   * @param request параметры обновления
   * @returns true при успешном обновлении, false при ошибке
   */
  async function updateProject(
    id: string,
    request: projectApi.UpdateProjectRequest,
  ): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const updated = await projectApi.updateProject(id, request)
      projects.value = projects.value.map((p) => (p.id === id ? updated : p))
      if (currentProject.value !== null && currentProject.value.id === id) {
        currentProject.value = updated
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to update project'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Удаляет проект.
   * @param id идентификатор проекта
   * @returns true при успешном удалении, false при ошибке
   */
  async function deleteProject(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await projectApi.deleteProject(id)
      projects.value = projects.value.filter((p) => p.id !== id)
      if (currentProject.value !== null && currentProject.value.id === id) {
        currentProject.value = null
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to delete project'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Сбрасывает состояние текущего выбранного проекта.
   */
  function clearCurrent(): void {
    currentProject.value = null
  }

  return {
    projects,
    currentProject,
    loading,
    error,
    hasProjects,
    loadProjects,
    loadProject,
    createProject,
    updateProject,
    deleteProject,
    clearCurrent,
  }
})
