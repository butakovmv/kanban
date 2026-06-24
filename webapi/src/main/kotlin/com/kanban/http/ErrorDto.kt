package com.kanban.http

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Error response in RFC 7807 Problem Detail format",
    example = """
        {
            "type": "/errors/bad-request",
            "title": "Bad Request",
            "status": 400,
            "detail": "Missing required field 'email'",
            "instance": "/api/v1/auth/login",
            "requestId": "abc-123-def"
        }
    """,
)
data class ErrorResponse(
    @field:Schema(
        description = "URI reference that identifies the problem type",
        example = "/errors/bad-request",
    )
    val type: String,
    @field:Schema(
        description = "Short, human-readable summary of the problem",
        example = "Bad Request",
    )
    val title: String,
    @field:Schema(
        description = "HTTP status code",
        example = "400",
    )
    val status: Int,
    @field:Schema(
        description = "Human-readable explanation of the specific problem",
        example = "Missing required field 'email'",
    )
    val detail: String,
    @field:Schema(
        description = "URI that identifies the specific occurrence of the problem",
        example = "/api/v1/auth/login",
    )
    val instance: String?,
    @field:Schema(
        description = "Request trace ID for debugging",
        example = "abc-123-def",
    )
    val requestId: String?,
)

@Schema(
    description = "Validation error details",
    example = """
        {
            "type": "/errors/validation",
            "title": "Validation Error",
            "status": 400,
            "detail": "email: must not be blank; password: must not be blank",
            "instance": "/api/v1/tasks",
            "requestId": "abc-123-def"
        }
    """,
)
data class ValidationErrorResponse(
    @field:Schema(description = "URI reference that identifies the problem type", example = "/errors/validation")
    val type: String,
    @field:Schema(description = "Short, human-readable summary", example = "Validation Error")
    val title: String,
    @field:Schema(description = "HTTP status code", example = "400")
    val status: Int,
    @field:Schema(description = "Detailed validation errors", example = "email: must not be blank; password: must not be blank")
    val detail: String,
    @field:Schema(description = "Request path", example = "/api/v1/tasks", nullable = true)
    val instance: String?,
    @field:Schema(description = "Request trace ID for debugging", example = "abc-123-def")
    val requestId: String?,
)

@Schema(
    description = "Authentication error",
    example = """
        {
            "type": "/errors/unauthorized",
            "title": "Unauthorized",
            "status": 401,
            "detail": "Invalid credentials",
            "instance": "/api/v1/auth/login",
            "requestId": "abc-123-def"
        }
    """,
)
data class UnauthorizedErrorResponse(
    @field:Schema(description = "URI reference", example = "/errors/unauthorized")
    val type: String,
    @field:Schema(description = "Short summary", example = "Unauthorized")
    val title: String,
    @field:Schema(description = "HTTP status", example = "401")
    val status: Int,
    @field:Schema(description = "Error details", example = "Invalid credentials")
    val detail: String,
    @field:Schema(description = "Request path", example = "/api/v1/auth/login", nullable = true)
    val instance: String?,
    @field:Schema(description = "Request trace ID for debugging", example = "abc-123-def")
    val requestId: String?,
)

@Schema(
    description = "Not found error",
    example = """
        {
            "type": "/errors/not-found",
            "title": "Not Found",
            "status": 404,
            "detail": "Task not found",
            "instance": "/api/v1/tasks/123",
            "requestId": "abc-123-def"
        }
    """,
)
data class NotFoundErrorResponse(
    @field:Schema(description = "URI reference", example = "/errors/not-found")
    val type: String,
    @field:Schema(description = "Short summary", example = "Not Found")
    val title: String,
    @field:Schema(description = "HTTP status", example = "404")
    val status: Int,
    @field:Schema(description = "Error details", example = "Task not found")
    val detail: String,
    @field:Schema(description = "Request path", example = "/api/v1/tasks/123", nullable = true)
    val instance: String?,
    @field:Schema(description = "Request trace ID for debugging", example = "abc-123-def")
    val requestId: String?,
)
