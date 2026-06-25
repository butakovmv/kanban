package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.project.BoardHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{projectId}/boards")
internal class ListProjectBoardsController(
    private val handler: BoardHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("projectId") projectId: String,
    ): ResponseEntity<*> {
        val result = handler.listByProjectId(projectId = projectId)
        return when (result) {
            is BoardHandler.ListBoardsResult.Success ->
                ResponseEntity.ok(
                    BoardsListResponse(
                        boards =
                            result.boards.map { b ->
                                BoardResponse(
                                    id = b.id,
                                    projectId = b.projectId,
                                    name = b.name,
                                    position = b.position,
                                    createdAt = b.createdAt,
                                )
                            },
                    ),
                )
        }
    }
}

internal data class BoardsListResponse(
    @JsonProperty("boards")
    val boards: List<BoardResponse>,
)
