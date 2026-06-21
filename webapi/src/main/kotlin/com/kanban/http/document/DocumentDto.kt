package com.kanban.http.document

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class DocumentDownloadUrlResponse(
    val url: String,
)

data class DocumentResponse(
    val id: String,
    @JsonProperty("project_id")
    val projectId: String,
    val title: String,
    val description: String?,
    @JsonProperty("file_name")
    val fileName: String,
    @JsonProperty("content_type")
    val contentType: String,
    @JsonProperty("size_bytes")
    val sizeBytes: Long,
    @JsonProperty("storage_key")
    val storageKey: String,
    val version: Int,
    @JsonProperty("uploaded_by")
    val uploadedBy: String,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
)
