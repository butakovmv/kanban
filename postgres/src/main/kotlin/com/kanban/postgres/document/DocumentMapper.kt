package com.kanban.postgres.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import java.time.ZoneId

/**
 * Маппинг табличной сущности [DocumentTable] в доменную сущность [Document].
 * Преобразует сырые типы (String, LocalDateTime) в типы предметной области
 * (DocumentId, ProjectId, Instant).
 */
internal fun DocumentTable.toDomain(): Document =
    Document(
        id = DocumentId(id),
        projectId = ProjectId(projectId),
        title = title,
        description = description,
        fileName = fileName,
        contentType = contentType,
        sizeBytes = sizeBytes,
        storageKey = storageKey,
        version = version,
        uploadedBy = uploadedBy,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [Document] в табличную сущность [DocumentTable].
 * Обратное преобразование типов предметной области в сырые типы для сохранения в БД.
 */
internal fun Document.toTable(): DocumentTable =
    DocumentTable(
        id = id.value,
        projectId = projectId.value,
        title = title,
        description = description,
        fileName = fileName,
        contentType = contentType,
        sizeBytes = sizeBytes,
        storageKey = storageKey,
        version = version,
        uploadedBy = uploadedBy,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
