package com.kanban.postgres.access

import org.springframework.data.relational.core.mapping.Table

@Table("group_members")
internal data class GroupMemberTable(
    val groupId: String,
    val userId: String,
    val addedAt: java.time.LocalDateTime,
)
