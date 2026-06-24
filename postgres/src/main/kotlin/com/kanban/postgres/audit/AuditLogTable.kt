package com.kanban.postgres.audit

import com.kanban.audit.AuditLog
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("audit_log")
internal data class AuditLogTable(
    @Id val id: String,
    val projectId: String?,
    val documentId: String?,
    val userId: String,
    val action: String,
    val details: String?,
    val createdAt: java.time.LocalDateTime,
) {
    fun toDomain(): AuditLog = AuditLog(
        id = id,
        projectId = projectId,
        documentId = documentId,
        userId = userId,
        action = action,
        details = details,
        createdAt = createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant(),
    )

    companion object {
        fun fromDomain(log: AuditLog): AuditLogTable = AuditLogTable(
            id = log.id,
            projectId = log.projectId,
            documentId = log.documentId,
            userId = log.userId,
            action = log.action,
            details = log.details,
            createdAt = log.createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
        )
    }
}
