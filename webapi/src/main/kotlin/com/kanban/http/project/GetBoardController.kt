package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{projectId}/board")
internal class GetBoardController(
    private val handler: BoardHandler,
) {
    @GetMapping
    suspend fun get(@PathVariable("projectId") projectId: String): ResponseEntity<*> {
        val result = handler.getByProjectId(projectId = projectId)
        return when (result) {
            is BoardHandler.GetBoardResult.Success -> {
                val v = result.view
                ResponseEntity.ok(
                    BoardViewResponse(
                        board = BoardResponse(
                            id = v.board.id, projectId = v.board.projectId, name = v.board.name,
                            position = v.board.position, createdAt = v.board.createdAt,
                        ),
                        columns = v.columns.map { c ->
                            ColumnResponse(
                                id = c.id, projectId = c.projectId, name = c.name,
                                position = c.position, wipLimit = c.wipLimit, createdAt = c.createdAt,
                            )
                        },
                    ),
                )
            }
            BoardHandler.GetBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
