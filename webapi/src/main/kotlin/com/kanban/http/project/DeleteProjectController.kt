package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{id}")
internal class DeleteProjectController(
    private val handler: ProjectHandler,
) {
    @DeleteMapping
    suspend fun delete(@PathVariable("id") id: String): ResponseEntity<*> {
        val result = handler.delete(projectId = id)
        return when (result) {
            ProjectHandler.DeleteProjectResult.Success ->
                ResponseEntity.status(HttpStatus.NO_CONTENT).build<Any>()
            ProjectHandler.DeleteProjectResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
