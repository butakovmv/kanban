package com.kanban.http.project

import com.kanban.project.BoardHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/boards/{id}")
internal class DeleteBoardController(
    private val handler: BoardHandler,
) {
    @DeleteMapping
    suspend fun delete(@PathVariable("id") id: String): ResponseEntity<*> {
        val result = handler.delete(boardId = id)
        return when (result) {
            BoardHandler.DeleteBoardResult.Success ->
                ResponseEntity.status(HttpStatus.NO_CONTENT).build<Any>()
            BoardHandler.DeleteBoardResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
