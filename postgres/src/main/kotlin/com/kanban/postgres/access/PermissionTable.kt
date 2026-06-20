package com.kanban.postgres.access

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("permissions")
internal data class PermissionTable(
    @Id
    val id: String,
    val resource: String,
    val action: String,
    val targetId: String?,
    val createdAt: java.time.LocalDateTime,
)
