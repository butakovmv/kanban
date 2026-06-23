package com.kanban.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import java.time.Instant

data class Document(
    val id: DocumentId,
    val projectId: ProjectId,
    val path: String,
    val title: String,
    val content: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
