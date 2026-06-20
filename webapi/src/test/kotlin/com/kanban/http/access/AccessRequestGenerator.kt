package com.kanban.http.access

import com.kanban.access.AccessHandler
import java.util.UUID

internal object AccessRequestGenerator {
    fun createGroupRequest(): AccessHandler.CreateGroupRequest =
        AccessHandler.CreateGroupRequest(
            name = "Group ${UUID.randomUUID().toString().take(6)}",
            description = "Description ${UUID.randomUUID().toString().take(6)}",
        )

    fun createGroupRequestWithoutDescription(): AccessHandler.CreateGroupRequest =
        AccessHandler.CreateGroupRequest(
            name = "Group ${UUID.randomUUID().toString().take(6)}",
            description = null,
        )

    fun updateGroupBody(): AccessHandler.UpdateGroupBody =
        AccessHandler.UpdateGroupBody(
            name = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun addMemberBody(): AccessHandler.AddMemberRequest =
        AccessHandler.AddMemberRequest(
            groupId = "group-${UUID.randomUUID()}",
            userId = "user-${UUID.randomUUID()}",
        )

    fun createPermissionRequest(): AccessHandler.CreatePermissionRequest =
        AccessHandler.CreatePermissionRequest(
            resource = "project",
            action = "read",
            targetId = null,
        )

    fun createPermissionRequestWithTarget(): AccessHandler.CreatePermissionRequest =
        AccessHandler.CreatePermissionRequest(
            resource = "project",
            action = "write",
            targetId = "target-${UUID.randomUUID()}",
        )

    fun grantPermissionBody(): AccessHandler.GrantPermissionRequest =
        AccessHandler.GrantPermissionRequest(
            groupId = "group-${UUID.randomUUID()}",
            permissionId = "perm-${UUID.randomUUID()}",
        )

    fun findPermissionsRequest(): AccessHandler.FindPermissionsRequest =
        AccessHandler.FindPermissionsRequest(
            resource = "project",
            targetId = null,
        )
}
