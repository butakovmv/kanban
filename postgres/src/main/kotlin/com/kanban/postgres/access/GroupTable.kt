package com.kanban.postgres.access

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("groups")
internal data class GroupTable(
    @Id
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: java.time.LocalDateTime,
)
