package com.kanban.project

import java.time.Instant

data class ProjectMemberDto(
    val userId: String,
    val displayName: String,
    val addedAt: Instant,
)
