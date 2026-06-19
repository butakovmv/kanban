package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер создания доски.
 * Обрабатывает только запрос `POST /api/v1/boards`.
 *
 * @property handler обработчик запросов досок
 */
@RestController
@RequestMapping("/api/v1/boards")
internal class CreateBoardController(
    private val handler: BoardHandler,
) {
    /**
     * Создаёт новую доску в проекте.
     *
     * @param request данные для создания доски
     * @return 201 с созданной доской и колонками, или 400 при ошибке
     */
    @PostMapping
    suspend fun create(
        @RequestBody request: BoardHandler.CreateBoardRequest,
    ): ResponseEntity<*> {
        val result = handler.create(request)
        return when (result) {
            is BoardHandler.CreateBoardResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.view)
            is BoardHandler.CreateBoardResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
