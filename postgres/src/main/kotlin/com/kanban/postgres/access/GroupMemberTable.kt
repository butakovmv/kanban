package com.kanban.postgres.access

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("group_members")
internal data class GroupMemberTable(
    @Column("group_id")
    val groupId: String,
    @Column("user_id")
    val userId: String,
    @Column("added_at")
    val addedAt: java.time.LocalDateTime,
)
