package com.kanban.audit

import java.time.Instant
import java.util.UUID

internal class LogAuditEventOperationImpl(
    private val auditLogRepository: AuditLogRepository,
) : LogAuditEventOperation {
    override suspend fun execute(arg: LogAuditEventOperation.Arg): LogAuditEventOperation.Result {
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            projectId = arg.projectId,
            documentId = arg.documentId,
            userId = arg.userId,
            action = arg.action,
            details = arg.details,
            createdAt = Instant.now(),
        )
        auditLogRepository.save(log)
        return LogAuditEventOperation.Result.Success
    }
}
