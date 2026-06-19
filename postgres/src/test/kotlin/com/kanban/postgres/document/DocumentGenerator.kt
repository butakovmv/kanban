package com.kanban.postgres.document

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

/**
 * Параметры создания тестового документа в БД.
 * Используется для удобной передачи опций в [DocumentGenerator.createAndInsert]
 * без превышения лимита на количество параметров функции.
 */
internal data class DocumentSpec(
    val projectId: String,
    val title: String = "Document-${UUID.randomUUID().toString().take(8)}",
    val description: String? = null,
    val fileName: String = "file-${UUID.randomUUID().toString().take(8)}.txt",
    val contentType: String = "text/plain",
    val sizeBytes: Long = 1024L,
    val storageKey: String = "projects/$projectId/${UUID.randomUUID()}/$fileName",
    val version: Int = 1,
    val uploadedBy: String = UUID.randomUUID().toString(),
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

internal class DocumentGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(spec: DocumentSpec): String {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val createdAt = spec.createdAt ?: now
        val updatedAt = spec.updatedAt ?: now
        val base =
            db
                .sql(
                    """
                    INSERT INTO documents (id, project_id, title, description, file_name,
                        content_type, size_bytes, storage_key, version, uploaded_by,
                        created_at, updated_at)
                    VALUES (:id, :projectId, :title, :description, :fileName,
                        :contentType, :sizeBytes, :storageKey, :version, :uploadedBy,
                        :createdAt, :updatedAt)
                    """,
                ).bind("id", id)
                .bind("projectId", spec.projectId)
                .bind("title", spec.title)
                .bind("fileName", spec.fileName)
                .bind("contentType", spec.contentType)
                .bind("sizeBytes", spec.sizeBytes)
                .bind("storageKey", spec.storageKey)
                .bind("version", spec.version)
                .bind("uploadedBy", spec.uploadedBy)
                .bind("createdAt", createdAt)
                .bind("updatedAt", updatedAt)
        val withDescription =
            if (spec.description != null) {
                base.bind("description", spec.description)
            } else {
                base.bindNull("description", String::class.java)
            }
        withDescription
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM documents")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
