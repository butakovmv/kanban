package com.kanban.http.access

import java.util.UUID

internal object AccessRequestGenerator {
    fun createGroupBody(): CreateGroupController.CreateGroupBody =
        CreateGroupController.CreateGroupBody(
            name = "Group ${UUID.randomUUID().toString().take(6)}",
            description = "Description ${UUID.randomUUID().toString().take(6)}",
        )

    fun createGroupBodyWithoutDescription(): CreateGroupController.CreateGroupBody =
        CreateGroupController.CreateGroupBody(
            name = "Group ${UUID.randomUUID().toString().take(6)}",
            description = null,
        )

    fun updateGroupBody(): UpdateGroupController.UpdateGroupBody =
        UpdateGroupController.UpdateGroupBody(
            name = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun addMemberBody(): AddMemberController.AddMemberBody =
        AddMemberController.AddMemberBody(
            userId = "user-${UUID.randomUUID()}",
        )

    fun createPermissionBody(): CreatePermissionController.CreatePermissionBody =
        CreatePermissionController.CreatePermissionBody(
            resource = "project",
            action = "read",
            targetId = null,
        )

    fun createPermissionBodyWithTarget(): CreatePermissionController.CreatePermissionBody =
        CreatePermissionController.CreatePermissionBody(
            resource = "project",
            action = "write",
            targetId = "target-${UUID.randomUUID()}",
        )

    fun grantPermissionBody(): GrantPermissionController.GrantPermissionBody =
        GrantPermissionController.GrantPermissionBody(
            permissionId = "perm-${UUID.randomUUID()}",
        )
}
