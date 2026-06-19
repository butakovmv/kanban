package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения списка проектов владельца.
 * Обрабатывает только запрос `GET /api/v1/projects?owner_id=...`.
 *
 * @property handler обработчик запросов проектов
 */
@RestController
@RequestMapping("/api/v1/projects")
internal class ListProjectsController(
    private val handler: ProjectHandler,
) {
    /**
     * Возвращает список проектов пользователя.
     *
     * @param ownerId идентификатор владельца
     * @return 200 со списком проектов
     */
    @GetMapping
    suspend fun list(
        @RequestParam("owner_id") ownerId: String,
    ): ResponseEntity<*> {
        val request = ProjectHandler.ListProjectsRequest(ownerId = ownerId)
        val result = handler.list(request)
        return when (result) {
            is ProjectHandler.ListProjectsResult.Success ->
                ResponseEntity.ok(mapOf("projects" to result.projects))
        }
    }
}
