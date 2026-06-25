package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{projectId}/board")
internal class UpdateBoardController(
    private val handler: BoardHandler,
) {
    data class UpdateBoardBody(
        val name: String?,
    )

    @PutMapping
    suspend fun update(
        @PathVariable("projectId") projectId: String,
        @RequestBody body: UpdateBoardBody,
    ): ResponseEntity<*> {
        val result = handler.updateByProjectId(projectId = projectId, name = body.name)
        return when (result) {
            is BoardHandler.UpdateBoardResult.Success -> {
                val b = result.board
                ResponseEntity.ok(
                    BoardResponse(
                        id = b.id,
                        projectId = b.projectId,
                        name = b.name,
                        position = b.position,
                        createdAt = b.createdAt,
                    ),
                )
            }
            BoardHandler.UpdateBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
