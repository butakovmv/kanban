package com.kanban.project

import java.time.Instant

data class ProjectMember(
    val projectId: String,
    val userId: String,
    val displayName: String,
    val addedAt: Instant,
)
