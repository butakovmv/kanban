package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер удаления проекта.
 * Обрабатывает только запрос `DELETE /api/v1/projects/{id}`.
 *
 * @property handler обработчик запросов проектов
 */
@RestController
@RequestMapping("/api/v1/projects/{id}")
internal class DeleteProjectController(
    private val handler: ProjectHandler,
) {
    /**
     * Удаляет проект по идентификатору.
     *
     * @param id идентификатор проекта
     * @return 204 при успешном удалении, или 404 если проект не найден
     */
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = ProjectHandler.DeleteProjectRequest(projectId = id)
        val result = handler.delete(request)
        return when (result) {
            ProjectHandler.DeleteProjectResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            ProjectHandler.DeleteProjectResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
