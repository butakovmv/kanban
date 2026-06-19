package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер архивирования доски.
 * Обрабатывает только запрос `POST /api/v1/boards/{id}/archive`.
 *
 * @property handler обработчик запросов досок
 */
@RestController
@RequestMapping("/api/v1/boards/{id}/archive")
internal class ArchiveBoardController(
    private val handler: BoardHandler,
) {
    /**
     * Архивирует доску.
     *
     * @param id идентификатор доски
     * @return 204 при успешном архивировании, или 404 если доска не найдена
     */
    @PostMapping
    suspend fun archive(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = BoardHandler.ArchiveBoardRequest(boardId = id)
        val result = handler.archive(request)
        return when (result) {
            BoardHandler.ArchiveBoardResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            BoardHandler.ArchiveBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
