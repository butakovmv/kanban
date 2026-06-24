package com.kanban.audit

interface AuditLogRepository {
    suspend fun save(log: AuditLog): AuditLog

    suspend fun listByProjectId(
        projectId: String,
        page: Int,
        size: Int,
    ): AuditLogPage
}

data class AuditLogPage(
    val items: List<AuditLog>,
    val total: Long,
)
