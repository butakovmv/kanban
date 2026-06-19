package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер обновления проекта.
 * Обрабатывает только запрос `PUT /api/v1/projects/{id}`.
 *
 * @property handler обработчик запросов проектов
 */
@RestController
@RequestMapping("/api/v1/projects/{id}")
internal class UpdateProjectController(
    private val handler: ProjectHandler,
) {
    /**
     * Обновляет поля проекта.
     *
     * @param id идентификатор проекта
     * @param body данные для обновления
     * @return 200 с обновлённым проектом, или 404 если проект не найден
     */
    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: ProjectHandler.UpdateProjectBody,
    ): ResponseEntity<*> {
        val request =
            ProjectHandler.UpdateProjectRequest(
                projectId = id,
                name = body.name,
                description = body.description,
            )
        val result = handler.update(request)
        return when (result) {
            is ProjectHandler.UpdateProjectResult.Success ->
                ResponseEntity.ok(result.project)
            ProjectHandler.UpdateProjectResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
