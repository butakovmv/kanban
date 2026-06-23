package com.kanban.postgres.document

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("documents")
internal data class DocumentTable(
    @Id
    val id: String,
    val projectId: String,
    val path: String,
    val title: String,
    val content: String,
    val description: String?,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime,
)
