package com.kanban.audit

data class AuditLog(
    val id: String,
    val projectId: String?,
    val documentId: String?,
    val userId: String,
    val action: String,
    val details: String?,
    val createdAt: java.time.Instant,
)
