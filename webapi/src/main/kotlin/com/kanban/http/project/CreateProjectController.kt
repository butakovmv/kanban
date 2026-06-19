package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер создания проекта.
 * Обрабатывает только запрос `POST /api/v1/projects`.
 *
 * @property handler обработчик запросов проектов
 */
@RestController
@RequestMapping("/api/v1/projects")
internal class CreateProjectController(
    private val handler: ProjectHandler,
) {
    /**
     * Создаёт новый проект.
     *
     * @param request данные для создания проекта
     * @return 201 с созданным проектом, или 400 при ошибке
     */
    @PostMapping
    suspend fun create(
        @RequestBody request: ProjectHandler.CreateProjectRequest,
    ): ResponseEntity<*> {
        val result = handler.create(request)
        return when (result) {
            is ProjectHandler.CreateProjectResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.project)
            is ProjectHandler.CreateProjectResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
