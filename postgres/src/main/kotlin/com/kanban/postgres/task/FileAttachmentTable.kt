package com.kanban.postgres.task

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `file_attachments` — хранит метаданные файлов, прикреплённых к задачам.
 * Содержимое файла располагается во внешнем хранилище; здесь хранится только ключ объекта.
 */
@Table("file_attachments")
internal data class FileAttachmentTable(
    @Id
    val id: String,
    val taskId: String,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    val uploadedBy: String,
    val uploadedAt: java.time.LocalDateTime,
)
