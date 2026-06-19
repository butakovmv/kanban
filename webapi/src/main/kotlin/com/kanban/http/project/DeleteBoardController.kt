package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер удаления доски.
 * Обрабатывает только запрос `DELETE /api/v1/boards/{id}`.
 *
 * @property handler обработчик запросов досок
 */
@RestController
@RequestMapping("/api/v1/boards/{id}")
internal class DeleteBoardController(
    private val handler: BoardHandler,
) {
    /**
     * Удаляет доску по идентификатору.
     *
     * @param id идентификатор доски
     * @return 204 при успешном удалении, или 404 если доска не найдена
     */
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = BoardHandler.DeleteBoardRequest(boardId = id)
        val result = handler.delete(request)
        return when (result) {
            BoardHandler.DeleteBoardResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            BoardHandler.DeleteBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
