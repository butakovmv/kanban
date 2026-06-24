package com.kanban.http.audit

import com.kanban.audit.AuditLogHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{projectId}/audit-log")
@Tag(name = "Audit Log", description = "Audit log operations")
internal class ListAuditLogController(
    private val handler: AuditLogHandler,
) {
    @Operation(
        summary = "Get audit log",
        description = "Retrieves audit log entries for a project with pagination",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Audit log entries",
        content = [Content(schema = Schema(implementation = AuditLogListResponse::class))],
    )
    @GetMapping
    suspend fun list(
        @Parameter(description = "Project ID") @PathVariable("projectId") projectId: String,
        @Parameter(description = "Page number", example = "1") @RequestParam(value = "page", defaultValue = "1") page: Int,
        @Parameter(description = "Page size", example = "20") @RequestParam(value = "size", defaultValue = "20") size: Int,
    ): ResponseEntity<*> {
        val result = handler.list(projectId = projectId, page = page, size = size)
        return when (result) {
            is AuditLogHandler.ListAuditLogResult.Success ->
                ResponseEntity.ok(
                    AuditLogListResponse(
                        items = result.items.map { e ->
                            AuditEntryResponse(
                                id = e.id,
                                projectId = e.projectId,
                                documentId = e.documentId,
                                userId = e.userId,
                                action = e.action,
                                details = e.details,
                                createdAt = e.createdAt,
                            )
                        },
                        total = result.total,
                    ),
                )
        }
    }
}
