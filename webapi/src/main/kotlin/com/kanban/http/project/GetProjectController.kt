package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения проекта по идентификатору.
 * Обрабатывает только запрос `GET /api/v1/projects/{id}`.
 *
 * @property handler обработчик запросов проектов
 */
@RestController
@RequestMapping("/api/v1/projects/{id}")
internal class GetProjectController(
    private val handler: ProjectHandler,
) {
    /**
     * Возвращает проект по его идентификатору.
     *
     * @param id идентификатор проекта
     * @return 200 с проектом, или 404 если проект не найден
     */
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = ProjectHandler.GetProjectRequest(projectId = id)
        val result = handler.get(request)
        return when (result) {
            is ProjectHandler.GetProjectResult.Success ->
                ResponseEntity.ok(result.project)
            ProjectHandler.GetProjectResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
