package com.kanban.postgres.access

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("groups")
internal data class GroupTable(
    @Id
    val id: String,
    @Column("name")
    val name: String,
    @Column("description")
    val description: String?,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
)
