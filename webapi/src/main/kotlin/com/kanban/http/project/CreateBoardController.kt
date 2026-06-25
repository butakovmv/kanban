package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/boards")
internal class CreateBoardController(
    private val handler: BoardHandler,
) {
    data class CreateBoardBody(
        @JsonProperty("project_id")
        val projectId: String,
        val name: String,
    )

    @PostMapping
    suspend fun create(
        @RequestBody body: CreateBoardBody,
    ): ResponseEntity<*> {
        val result =
            handler.create(
                projectId = body.projectId,
                name = body.name,
            )
        return when (result) {
            is BoardHandler.CreateBoardResult.Success -> {
                val v = result.view
                ResponseEntity.status(HttpStatus.CREATED).body(
                    BoardViewResponse(
                        board =
                            BoardResponse(
                                id = v.board.id,
                                projectId = v.board.projectId,
                                name = v.board.name,
                                position = v.board.position,
                                createdAt = v.board.createdAt,
                            ),
                        columns =
                            v.columns.map { c ->
                                ColumnResponse(
                                    id = c.id,
                                    projectId = c.projectId,
                                    name = c.name,
                                    position = c.position,
                                    wipLimit = c.wipLimit,
                                    createdAt = c.createdAt,
                                )
                            },
                    ),
                )
            }
            is BoardHandler.CreateBoardResult.Failure ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("reason" to result.reason))
        }
    }
}
