package com.kanban.postgres.task

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `file_attachments` — хранит метаданные файлов, прикреплённых к задачам.
 * Содержимое файла располагается во внешнем хранилище; здесь хранится только ключ объекта.
 */
@Table("file_attachments")
internal data class FileAttachmentTable(
    @Id
    val id: String,
    @Column("task_id")
    val taskId: String,
    @Column("file_name")
    val fileName: String,
    @Column("content_type")
    val contentType: String,
    @Column("size_bytes")
    val sizeBytes: Long,
    @Column("storage_key")
    val storageKey: String,
    @Column("uploaded_by")
    val uploadedBy: String,
    @Column("uploaded_at")
    val uploadedAt: java.time.LocalDateTime,
)
