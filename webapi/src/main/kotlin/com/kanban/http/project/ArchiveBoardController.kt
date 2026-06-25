package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{projectId}/board/archive")
internal class ArchiveBoardController(
    private val handler: BoardHandler,
) {
    @PostMapping
    suspend fun archive(
        @PathVariable("projectId") projectId: String,
    ): ResponseEntity<*> {
        val result = handler.archiveByProjectId(projectId = projectId)
        return when (result) {
            BoardHandler.ArchiveBoardResult.Success ->
                ResponseEntity.status(HttpStatus.NO_CONTENT).build<Any>()
            BoardHandler.ArchiveBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
