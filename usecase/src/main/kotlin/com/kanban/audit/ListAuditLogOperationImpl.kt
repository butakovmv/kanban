package com.kanban.audit

internal class ListAuditLogOperationImpl(
    private val auditLogRepository: AuditLogRepository,
) : ListAuditLogOperation {
    override suspend fun execute(arg: ListAuditLogOperation.Arg): ListAuditLogOperation.Result {
        val page = auditLogRepository.listByProjectId(
            projectId = arg.projectId,
            page = arg.page,
            size = arg.size,
        )
        return ListAuditLogOperation.Result.Success(
            items = page.items,
            total = page.total,
        )
    }
}
