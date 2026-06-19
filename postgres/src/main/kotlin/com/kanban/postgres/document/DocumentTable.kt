package com.kanban.postgres.document

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `documents` — хранит метаданные документов проектов.
 * Каждый документ принадлежит проекту, имеет заголовок, опциональное описание, имя файла,
 * MIME-тип, размер, ключ объекта во внешнем хранилище, номер версии содержимого,
 * идентификатор загрузившего пользователя и метки времени создания/обновления.
 */
@Table("documents")
internal data class DocumentTable(
    @Id
    val id: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    val version: Int,
    val uploadedBy: String,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime,
)
