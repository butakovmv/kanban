package com.kanban.postgres.task

import com.kanban.task.FileAttachment
import com.kanban.task.FileAttachmentRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [FileAttachmentRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование прикрепления
 * и выполняет INSERT или UPDATE соответственно.
 */
@Repository
internal class FileAttachmentRepositoryImpl(
    private val db: DatabaseClient,
) : FileAttachmentRepository {
    /**
     * Сохранение прикрепления файла (создание или обновление).
     * @param file доменная сущность прикрепления
     * @return сохранённое прикрепление
     */
    override suspend fun save(file: FileAttachment): FileAttachment {
        val z = ZoneId.systemDefault()
        val uploadedAt = file.uploadedAt.atZone(z).toLocalDateTime()
        if (findById(file.id.value) != null) {
            updateFileAttachment(file, uploadedAt)
        } else {
            insertFileAttachment(file, uploadedAt)
        }
        return file
    }

    /**
     * Обновление существующей записи прикрепления файла в таблице `file_attachments`.
     * @param file доменная сущность прикрепления с обновлёнными данными
     * @param uploadedAt метка времени загрузки
     */
    private suspend fun updateFileAttachment(
        file: FileAttachment,
        uploadedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                UPDATE file_attachments SET
                    task_id = :taskId, file_name = :fileName,
                    content_type = :contentType, size_bytes = :sizeBytes,
                    storage_key = :storageKey, uploaded_by = :uploadedBy,
                    uploaded_at = :uploadedAt
                WHERE id = :id
                """,
            ).bind("id", UUID.fromString(file.id.value))
            .bind("taskId", UUID.fromString(file.taskId.value))
            .bind("fileName", file.fileName)
            .bind("contentType", file.contentType)
            .bind("sizeBytes", file.sizeBytes)
            .bind("storageKey", file.storageKey)
            .bind("uploadedBy", file.uploadedBy)
            .bind("uploadedAt", uploadedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи прикрепления файла в таблицу `file_attachments`.
     * @param file доменная сущность прикрепления для сохранения
     * @param uploadedAt метка времени загрузки
     */
    private suspend fun insertFileAttachment(
        file: FileAttachment,
        uploadedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO file_attachments (id, task_id, file_name, content_type, size_bytes, storage_key, uploaded_by, uploaded_at)
                VALUES (:id, :taskId, :fileName, :contentType, :sizeBytes, :storageKey, :uploadedBy, :uploadedAt)
                """,
            ).bind("id", UUID.fromString(file.id.value))
            .bind("taskId", UUID.fromString(file.taskId.value))
            .bind("fileName", file.fileName)
            .bind("contentType", file.contentType)
            .bind("sizeBytes", file.sizeBytes)
            .bind("storageKey", file.storageKey)
            .bind("uploadedBy", file.uploadedBy)
            .bind("uploadedAt", uploadedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск прикрепления файла по идентификатору.
     * @param id строковый идентификатор прикрепления
     * @return [FileAttachment] или null, если прикрепление не найдено
     */
    override suspend fun findById(id: String): FileAttachment? =
        db
            .sql("SELECT * FROM file_attachments WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .map { row, _ -> row.toFileAttachment() }
            .one()
            .awaitFirstOrNull()

    /**
     * Получение списка прикреплений указанной задачи, упорядоченных по дате загрузки.
     * @param taskId идентификатор задачи
     * @return список [FileAttachment] задачи
     */
    override suspend fun listByTaskId(taskId: String): List<FileAttachment> =
        db
            .sql("SELECT * FROM file_attachments WHERE task_id = :taskId ORDER BY uploaded_at")
            .bind("taskId", UUID.fromString(taskId))
            .map { row, _ -> row.toFileAttachment() }
            .all()
            .collectList()
            .awaitSingle()

    /**
     * Удаление прикрепления файла по идентификатору.
     * @param id идентификатор прикрепления
     */
    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM file_attachments WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [FileAttachment].
     * @param row строка результата запроса
     * @return доменная сущность [FileAttachment]
     */
    private fun io.r2dbc.spi.Row.toFileAttachment(): FileAttachment {
        val table =
            FileAttachmentTable(
                id = get("id", String::class.java)!!,
                taskId = get("task_id", String::class.java)!!,
                fileName = get("file_name", String::class.java)!!,
                contentType = get("content_type", String::class.java)!!,
                sizeBytes = (get("size_bytes", java.lang.Long::class.java) ?: 0L) as Long,
                storageKey = get("storage_key", String::class.java)!!,
                uploadedBy = get("uploaded_by", String::class.java)!!,
                uploadedAt = get("uploaded_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
