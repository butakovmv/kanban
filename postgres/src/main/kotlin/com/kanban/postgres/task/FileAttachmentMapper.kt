package com.kanban.postgres.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import com.kanban.task.FileAttachment
import java.time.ZoneId

/**
 * Маппинг табличной сущности [FileAttachmentTable] в доменную сущность [FileAttachment].
 * Преобразует сырые типы (String, LocalDateTime) в типы предметной области
 * (FileAttachmentId, TaskId, Instant).
 */
internal fun FileAttachmentTable.toDomain(): FileAttachment =
    FileAttachment(
        id = FileAttachmentId(id),
        taskId = TaskId(taskId),
        fileName = fileName,
        contentType = contentType,
        sizeBytes = sizeBytes,
        storageKey = storageKey,
        uploadedBy = uploadedBy,
        uploadedAt = uploadedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [FileAttachment] в табличную сущность [FileAttachmentTable].
 * Обратное преобразование типов предметной области в сырые типы для сохранения в БД.
 */
internal fun FileAttachment.toTable(): FileAttachmentTable =
    FileAttachmentTable(
        id = id.value,
        taskId = taskId.value,
        fileName = fileName,
        contentType = contentType,
        sizeBytes = sizeBytes,
        storageKey = storageKey,
        uploadedBy = uploadedBy,
        uploadedAt = uploadedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
