package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер реордеринга колонок доски.
 * Обрабатывает только запрос `PUT /api/v1/boards/{id}/columns/order`.
 *
 * @property handler обработчик запросов досок
 */
@RestController
@RequestMapping("/api/v1/boards/{id}/columns/order")
internal class ReorderColumnsController(
    private val handler: BoardHandler,
) {
    /**
     * Изменяет порядок колонок на доске.
     *
     * @param id идентификатор доски
     * @param body идентификаторы колонок в новом порядке
     * @return 200 с обновлённым списком колонок, или 400/404 при ошибке
     */
    @PutMapping
    suspend fun reorder(
        @PathVariable("id") id: String,
        @RequestBody body: BoardHandler.ReorderColumnsBody,
    ): ResponseEntity<*> {
        val request =
            BoardHandler.ReorderColumnsRequest(
                boardId = id,
                columnIds = body.columnIds,
            )
        val result = handler.reorderColumns(request)
        return when (result) {
            is BoardHandler.ReorderColumnsResult.Success ->
                ResponseEntity.ok(mapOf("columns" to result.columns))
            BoardHandler.ReorderColumnsResult.BoardNotFound ->
                ResponseEntity.notFound().build<Any>()
            BoardHandler.ReorderColumnsResult.InvalidColumns ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to "Invalid column set"))
        }
    }
}
