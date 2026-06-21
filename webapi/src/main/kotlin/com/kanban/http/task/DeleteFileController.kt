package com.kanban.http.task

import com.kanban.task.FileHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/files/{id}")
internal class DeleteFileController(
    private val handler: FileHandler,
) {
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.delete(fileId = id)
        return when (result) {
            FileHandler.DeleteFileResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            FileHandler.DeleteFileResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
