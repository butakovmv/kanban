package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/boards/{id}/columns/order")
internal class ReorderColumnsController(
    private val handler: BoardHandler,
) {
    data class ReorderColumnsBody(
        @JsonProperty("column_ids")
        val columnIds: List<String>,
    )

    @PutMapping
    suspend fun reorder(
        @PathVariable("id") id: String,
        @RequestBody body: ReorderColumnsBody,
    ): ResponseEntity<*> {
        val result = handler.reorderColumns(
            boardId = id,
            columnIds = body.columnIds,
        )
        return when (result) {
            is BoardHandler.ReorderColumnsResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "columns" to result.columns.map { c ->
                            ColumnResponse(
                                id = c.id, boardId = c.boardId, name = c.name,
                                position = c.position, wipLimit = c.wipLimit, createdAt = c.createdAt,
                            )
                        },
                    ),
                )
            BoardHandler.ReorderColumnsResult.BoardNotFound ->
                ResponseEntity.notFound().build<Any>()
            BoardHandler.ReorderColumnsResult.InvalidColumns ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("reason" to "Invalid column set"))
        }
    }
}
