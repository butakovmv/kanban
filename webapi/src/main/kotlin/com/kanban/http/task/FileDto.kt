package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class FileAttachmentResponse(
    val id: String,
    @JsonProperty("task_id")
    val taskId: String,
    @JsonProperty("file_name")
    val fileName: String,
    @JsonProperty("content_type")
    val contentType: String,
    @JsonProperty("size_bytes")
    val sizeBytes: Long,
    @JsonProperty("storage_key")
    val storageKey: String,
    @JsonProperty("uploaded_by")
    val uploadedBy: String,
    @JsonProperty("uploaded_at")
    val uploadedAt: Instant,
)

data class DownloadUrlResponse(
    val url: String,
)
