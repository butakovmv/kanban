package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер обновления доски.
 * Обрабатывает только запрос `PUT /api/v1/boards/{id}`.
 *
 * @property handler обработчик запросов досок
 */
@RestController
@RequestMapping("/api/v1/boards/{id}")
internal class UpdateBoardController(
    private val handler: BoardHandler,
) {
    /**
     * Обновляет название доски.
     *
     * @param id идентификатор доски
     * @param body данные для обновления
     * @return 200 с обновлённой доской, или 404 если доска не найдена
     */
    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: BoardHandler.UpdateBoardBody,
    ): ResponseEntity<*> {
        val request =
            BoardHandler.UpdateBoardRequest(
                boardId = id,
                name = body.name,
            )
        val result = handler.update(request)
        return when (result) {
            is BoardHandler.UpdateBoardResult.Success ->
                ResponseEntity.ok(result.board)
            BoardHandler.UpdateBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
