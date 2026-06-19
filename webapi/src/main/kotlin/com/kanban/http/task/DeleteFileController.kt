package com.kanban.http.task

import com.kanban.task.FileHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер удаления прикреплённого файла.
 * Обрабатывает только запрос `DELETE /api/v1/files/{id}`.
 *
 * @property handler обработчик запросов прикреплённых файлов
 */
@RestController
@RequestMapping("/api/v1/files/{id}")
internal class DeleteFileController(
    private val handler: FileHandler,
) {
    /**
     * Удаляет прикреплённый файл по идентификатору.
     *
     * @param id идентификатор прикрепления
     * @return 204 при успешном удалении, или 404 если файл не найден
     */
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = FileHandler.DeleteFileRequest(fileId = id)
        val result = handler.delete(request)
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
