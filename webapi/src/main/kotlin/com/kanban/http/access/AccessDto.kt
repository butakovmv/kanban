package com.kanban.http.access

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class GroupResponse(
    val id: String,
    val name: String,
    val description: String?,
    @JsonProperty("created_at")
    val createdAt: Instant,
)

data class MemberResponse(
    @JsonProperty("group_id")
    val groupId: String,
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("added_at")
    val addedAt: Instant,
)

data class PermissionResponse(
    val id: String,
    val resource: String,
    val action: String,
    @JsonProperty("target_id")
    val targetId: String?,
    @JsonProperty("created_at")
    val createdAt: Instant,
)

data class CheckPermissionResponse(
    val allowed: Boolean,
    val reason: String?,
)
