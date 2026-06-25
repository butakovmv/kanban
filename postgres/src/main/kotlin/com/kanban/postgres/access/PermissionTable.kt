package com.kanban.postgres.access

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("permissions")
internal data class PermissionTable(
    @Id
    val id: String,
    @Column("resource")
    val resource: String,
    @Column("action")
    val action: String,
    @Column("target_id")
    val targetId: String?,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
)
