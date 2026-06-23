package com.kanban.postgres.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import java.time.ZoneId

internal fun DocumentTable.toDomain(): Document =
    Document(
        id = DocumentId(id),
        projectId = ProjectId(projectId),
        path = path,
        title = title,
        content = content,
        description = description,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

internal fun Document.toTable(): DocumentTable =
    DocumentTable(
        id = id.value,
        projectId = projectId.value,
        path = path,
        title = title,
        content = content,
        description = description,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
