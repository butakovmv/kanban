package com.kanban.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import java.time.Instant

/**
 * Сущность прикреплённого к задаче файла.
 * Хранит метаданные файла и ключ хранения во внешнем хранилище (MinIO/S3).
 *
 * @property id уникальный идентификатор прикрепления
 * @property taskId идентификатор задачи, к которой прикреплён файл
 * @property fileName исходное имя файла
 * @property contentType MIME-тип содержимого файла
 * @property sizeBytes размер файла в байтах
 * @property storageKey ключ (путь) объекта во внешнем хранилище
 * @property uploadedBy идентификатор пользователя, загрузившего файл
 * @property uploadedAt дата и время загрузки
 */
data class FileAttachment(
    val id: FileAttachmentId,
    val taskId: TaskId,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    val uploadedBy: String,
    val uploadedAt: Instant,
)
