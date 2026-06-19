package com.kanban.postgres.task

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

/**
 * Параметры создания тестового прикрепления файла в БД.
 * Используется для удобной передачи опций в [FileAttachmentGenerator.createAndInsert]
 * без превышения лимита на количество параметров функции.
 */
internal data class FileAttachmentSpec(
    val taskId: String,
    val fileName: String = "file-${UUID.randomUUID().toString().take(8)}.txt",
    val contentType: String = "text/plain",
    val sizeBytes: Long = 1024L,
    val storageKey: String = "tasks/${UUID.randomUUID()}/$fileName",
    val uploadedBy: String = UUID.randomUUID().toString(),
)

internal class FileAttachmentGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(spec: FileAttachmentSpec): String {
        val id = UUID.randomUUID().toString()
        db
            .sql(
                """
                INSERT INTO file_attachments (id, task_id, file_name, content_type, size_bytes, storage_key, uploaded_by, uploaded_at)
                VALUES (:id, :taskId, :fileName, :contentType, :sizeBytes, :storageKey, :uploadedBy, :uploadedAt)
                """,
            ).bind("id", id)
            .bind("taskId", spec.taskId)
            .bind("fileName", spec.fileName)
            .bind("contentType", spec.contentType)
            .bind("sizeBytes", spec.sizeBytes)
            .bind("storageKey", spec.storageKey)
            .bind("uploadedBy", spec.uploadedBy)
            .bind("uploadedAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM file_attachments")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
