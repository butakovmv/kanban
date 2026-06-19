package com.kanban.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import java.time.Instant

/**
 * Сущность документа проекта.
 * Содержит метаданные документа (заголовок, описание, имя файла, тип и размер)
 * и ключ хранения содержимого во внешнем хранилище (MinIO/S3).
 * Поддерживает версионирование: каждая замена содержимого увеличивает [version].
 *
 * @property id уникальный идентификатор документа
 * @property projectId идентификатор проекта, к которому относится документ
 * @property title заголовок документа
 * @property description описание документа (опционально)
 * @property fileName исходное имя файла
 * @property contentType MIME-тип содержимого файла
 * @property sizeBytes размер файла в байтах
 * @property storageKey ключ (путь) объекта во внешнем хранилище
 * @property version номер версии содержимого документа (начиная с 1)
 * @property uploadedBy идентификатор пользователя, загрузившего документ
 * @property createdAt дата и время создания
 * @property updatedAt дата и время последнего изменения
 */
data class Document(
    val id: DocumentId,
    val projectId: ProjectId,
    val title: String,
    val description: String?,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    val version: Int,
    val uploadedBy: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
