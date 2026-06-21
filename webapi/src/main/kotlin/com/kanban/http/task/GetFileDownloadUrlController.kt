package com.kanban.http.task

import com.kanban.task.FileHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/files/{id}/download")
internal class GetFileDownloadUrlController(
    private val handler: FileHandler,
) {
    @GetMapping
    suspend fun getDownloadUrl(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.getDownloadUrl(fileId = id)
        return when (result) {
            is FileHandler.GetFileDownloadUrlResult.Success ->
                ResponseEntity.ok(DownloadUrlResponse(url = result.url))
            FileHandler.GetFileDownloadUrlResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
