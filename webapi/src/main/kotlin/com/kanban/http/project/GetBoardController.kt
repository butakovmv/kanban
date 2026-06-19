package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения доски с колонками.
 * Обрабатывает только запрос `GET /api/v1/boards/{id}`.
 *
 * @property handler обработчик запросов досок
 */
@RestController
@RequestMapping("/api/v1/boards/{id}")
internal class GetBoardController(
    private val handler: BoardHandler,
) {
    /**
     * Возвращает доску вместе с её колонками.
     *
     * @param id идентификатор доски
     * @return 200 с представлением доски, или 404 если доска не найдена
     */
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = BoardHandler.GetBoardRequest(boardId = id)
        val result = handler.get(request)
        return when (result) {
            is BoardHandler.GetBoardResult.Success ->
                ResponseEntity.ok(result.view)
            BoardHandler.GetBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
