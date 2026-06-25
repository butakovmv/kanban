package com.kanban.postgres.document

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("documents")
internal data class DocumentTable(
    @Id
    val id: String,
    @Column("project_id")
    val projectId: String,
    @Column("path")
    val path: String,
    @Column("title")
    val title: String,
    @Column("content")
    val content: String,
    @Column("description")
    val description: String?,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
    @Column("updated_at")
    val updatedAt: java.time.LocalDateTime,
)
