package com.kanban.http

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Kanban API")
                .version("v1")
                .description("API for Kanban project management application")
                .contact(
                    Contact()
                        .name("Kanban Team"),
                ),
        )
        .components(
            Components()
                .addSchemas(
                    "ErrorResponse",
                    io.swagger.v3.oas.models.media.Schema<Any>()
                        .type("object")
                        .description("Error response in RFC 7807 Problem Detail format")
                        .addProperty("type", io.swagger.v3.oas.models.media.Schema<Any>().type("string").description("URI reference").example("/errors/bad-request"))
                        .addProperty("title", io.swagger.v3.oas.models.media.Schema<Any>().type("string").description("Short summary").example("Bad Request"))
                        .addProperty("status", io.swagger.v3.oas.models.media.Schema<Any>().type("integer").description("HTTP status code").example(400))
                        .addProperty("detail", io.swagger.v3.oas.models.media.Schema<Any>().type("string").description("Detailed error message").example("Missing required field"))
                        .addProperty("instance", io.swagger.v3.oas.models.media.Schema<Any>().type("string").description("Request path").example("/api/v1/tasks"))
                        .addProperty("requestId", io.swagger.v3.oas.models.media.Schema<Any>().type("string").description("Request trace ID").example("abc-123"))
                )
                .addResponses(
                    "400BadRequest",
                    ApiResponse()
                        .description("Bad Request - Invalid request body, query parameters, or path variables")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType()
                                    .example(
                                        """
                                        {
                                            "type": "/errors/bad-request",
                                            "title": "Bad Request",
                                            "status": 400,
                                            "detail": "Missing required field",
                                            "instance": "/api/v1/...",
                                            "requestId": "abc-123"
                                        }
                                        """,
                                    ),
                            ),
                        ),
                )
                .addResponses(
                    "401Unauthorized",
                    ApiResponse()
                        .description("Unauthorized - Invalid or missing authentication")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType()
                                    .example(
                                        """
                                        {
                                            "type": "/errors/unauthorized",
                                            "title": "Unauthorized",
                                            "status": 401,
                                            "detail": "Invalid credentials",
                                            "instance": "/api/v1/auth/login",
                                            "requestId": "abc-123"
                                        }
                                        """,
                                    ),
                            ),
                        ),
                )
                .addResponses(
                    "403Forbidden",
                    ApiResponse()
                        .description("Forbidden - Access denied")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType()
                                    .example(
                                        """
                                        {
                                            "type": "/errors/forbidden",
                                            "title": "Forbidden",
                                            "status": 403,
                                            "detail": "Access denied",
                                            "instance": "/api/v1/...",
                                            "requestId": "abc-123"
                                        }
                                        """,
                                    ),
                            ),
                        ),
                )
                .addResponses(
                    "404NotFound",
                    ApiResponse()
                        .description("Not Found - Resource does not exist")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType()
                                    .example(
                                        """
                                        {
                                            "type": "/errors/not-found",
                                            "title": "Not Found",
                                            "status": 404,
                                            "detail": "Resource not found",
                                            "instance": "/api/v1/...",
                                            "requestId": "abc-123"
                                        }
                                        """,
                                    ),
                            ),
                        ),
                )
                .addResponses(
                    "409Conflict",
                    ApiResponse()
                        .description("Conflict - Resource already exists")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType()
                                    .example(
                                        """
                                        {
                                            "type": "/errors/conflict",
                                            "title": "Conflict",
                                            "status": 409,
                                            "detail": "Resource already exists",
                                            "instance": "/api/v1/...",
                                            "requestId": "abc-123"
                                        }
                                        """,
                                    ),
                            ),
                        ),
                )
                .addResponses(
                    "500InternalError",
                    ApiResponse()
                        .description("Internal Server Error - Unexpected server error")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType()
                                    .example(
                                        """
                                        {
                                            "type": "/errors/internal",
                                            "title": "Internal Error",
                                            "status": 500,
                                            "detail": "An unexpected error occurred",
                                            "instance": "/api/v1/...",
                                            "requestId": "abc-123"
                                        }
                                        """,
                                    ),
                            ),
                        ),
                ),
        )
}
