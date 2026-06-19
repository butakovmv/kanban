import type { CreateProjectRequest, Project, UpdateProjectRequest } from '../api'

/**
 * Генератор тестовых данных для project-модуля.
 * Создаёт случайные сущности для тестов api.ts и store.ts.
 */
export const projectGenerator = {
  project(overrides: Partial<Project> = {}): Project {
    const id = `project-${Math.random().toString(36).slice(2, 10)}`
    const ownerId = `owner-${Math.random().toString(36).slice(2, 8)}`
    const name = `Project ${Math.random().toString(36).slice(2, 6)}`
    return {
      id,
      ownerId,
      name,
      description: `Description for ${name}`,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      updatedAt: new Date('2025-01-02T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  projects(count: number): Project[] {
    return Array.from({ length: count }, () => this.project())
  },

  createRequest(overrides: Partial<CreateProjectRequest> = {}): CreateProjectRequest {
    return {
      ownerId: `owner-${Math.random().toString(36).slice(2, 8)}`,
      name: `Project ${Math.random().toString(36).slice(2, 6)}`,
      description: 'Test description',
      ...overrides,
    }
  },

  updateRequest(overrides: Partial<UpdateProjectRequest> = {}): UpdateProjectRequest {
    return {
      name: `Updated ${Math.random().toString(36).slice(2, 6)}`,
      ...overrides,
    }
  },
}
