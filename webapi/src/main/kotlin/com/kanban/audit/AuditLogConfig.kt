package com.kanban.audit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class AuditLogConfig {
    @Bean
    fun auditLogHandler(
        listAuditLogOperation: ListAuditLogOperation,
        logAuditEventOperation: LogAuditEventOperation,
    ): AuditLogHandler =
        AuditLogHandler(
            listAuditLogOperation = listAuditLogOperation,
            logAuditEventOperation = logAuditEventOperation,
        )
}
