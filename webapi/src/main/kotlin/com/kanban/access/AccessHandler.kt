package com.kanban.access

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@Suppress("LongParameterList", "TooManyFunctions")
internal class AccessHandler(
    private val createGroupOperation: CreateGroupOperation,
    private val getGroupOperation: GetGroupOperation,
    private val listGroupsOperation: ListGroupsOperation,
    private val updateGroupOperation: UpdateGroupOperation,
    private val deleteGroupOperation: DeleteGroupOperation,
    private val addMemberOperation: AddMemberOperation,
    private val removeMemberOperation: RemoveMemberOperation,
    private val listMembersOperation: ListMembersOperation,
    private val listUserGroupsOperation: ListUserGroupsOperation,
    private val createPermissionOperation: CreatePermissionOperation,
    private val deletePermissionOperation: DeletePermissionOperation,
    private val findPermissionsOperation: FindPermissionsOperation,
    private val grantPermissionOperation: GrantPermissionOperation,
    private val revokePermissionOperation: RevokePermissionOperation,
    private val listGroupPermissionsOperation: ListGroupPermissionsOperation,
    private val checkPermissionOperation: CheckPermissionOperation,
) {
    suspend fun createGroup(request: CreateGroupRequest): CreateGroupResult {
        val result =
            createGroupOperation.execute(
                CreateGroupOperation.Arg(
                    name = request.name,
                    description = request.description,
                ),
            )
        return when (result) {
            is CreateGroupOperation.Result.Success ->
                CreateGroupResult.Success(group = result.group.toResponse())
            is CreateGroupOperation.Result.Failure ->
                CreateGroupResult.Failure(reason = result.reason)
        }
    }

    suspend fun getGroup(request: GetGroupRequest): GetGroupResult {
        val result =
            getGroupOperation.execute(
                GetGroupOperation.Arg(groupId = request.groupId),
            )
        return when (result) {
            is GetGroupOperation.Result.Success ->
                GetGroupResult.Success(group = result.group.toResponse())
            GetGroupOperation.Result.NotFound -> GetGroupResult.NotFound
        }
    }

    suspend fun listGroups(): ListGroupsResult {
        val result =
            listGroupsOperation.execute(ListGroupsOperation.Arg)
        return when (result) {
            is ListGroupsOperation.Result.Success ->
                ListGroupsResult.Success(
                    groups = result.groups.map { it.toResponse() },
                )
        }
    }

    suspend fun updateGroup(request: UpdateGroupRequest): UpdateGroupResult {
        val result =
            updateGroupOperation.execute(
                UpdateGroupOperation.Arg(
                    groupId = request.groupId,
                    name = request.name,
                    description = request.description,
                ),
            )
        return when (result) {
            is UpdateGroupOperation.Result.Success ->
                UpdateGroupResult.Success(group = result.group.toResponse())
            UpdateGroupOperation.Result.NotFound -> UpdateGroupResult.NotFound
            is UpdateGroupOperation.Result.Failure ->
                UpdateGroupResult.Failure(reason = result.reason)
        }
    }

    suspend fun deleteGroup(request: DeleteGroupRequest): DeleteGroupResult {
        val result =
            deleteGroupOperation.execute(
                DeleteGroupOperation.Arg(groupId = request.groupId),
            )
        return when (result) {
            DeleteGroupOperation.Result.Success -> DeleteGroupResult.Success
            DeleteGroupOperation.Result.NotFound -> DeleteGroupResult.NotFound
        }
    }

    suspend fun addMember(request: AddMemberRequest): AddMemberResult {
        val result =
            addMemberOperation.execute(
                AddMemberOperation.Arg(
                    groupId = request.groupId,
                    userId = request.userId,
                ),
            )
        return when (result) {
            AddMemberOperation.Result.Success -> AddMemberResult.Success
            is AddMemberOperation.Result.Failure ->
                AddMemberResult.Failure(reason = result.reason)
        }
    }

    suspend fun removeMember(request: RemoveMemberRequest): RemoveMemberResult {
        val result =
            removeMemberOperation.execute(
                RemoveMemberOperation.Arg(
                    groupId = request.groupId,
                    userId = request.userId,
                ),
            )
        return when (result) {
            RemoveMemberOperation.Result.Success -> RemoveMemberResult.Success
            is RemoveMemberOperation.Result.Failure ->
                RemoveMemberResult.Failure(reason = result.reason)
        }
    }

    suspend fun listMembers(request: ListMembersRequest): ListMembersResult {
        val result =
            listMembersOperation.execute(
                ListMembersOperation.Arg(groupId = request.groupId),
            )
        return when (result) {
            is ListMembersOperation.Result.Success ->
                ListMembersResult.Success(
                    members = result.members.map { it.toResponse() },
                )
        }
    }

    suspend fun listUserGroups(request: ListUserGroupsRequest): ListUserGroupsResult {
        val result =
            listUserGroupsOperation.execute(
                ListUserGroupsOperation.Arg(userId = request.userId),
            )
        return when (result) {
            is ListUserGroupsOperation.Result.Success ->
                ListUserGroupsResult.Success(
                    groups = result.groups.map { it.toResponse() },
                )
        }
    }

    suspend fun createPermission(request: CreatePermissionRequest): CreatePermissionResult {
        val result =
            createPermissionOperation.execute(
                CreatePermissionOperation.Arg(
                    resource = request.resource,
                    action = request.action,
                    targetId = request.targetId,
                ),
            )
        return when (result) {
            is CreatePermissionOperation.Result.Success ->
                CreatePermissionResult.Success(
                    permission = result.permission.toResponse(),
                )
            is CreatePermissionOperation.Result.Failure ->
                CreatePermissionResult.Failure(reason = result.reason)
        }
    }

    suspend fun deletePermission(request: DeletePermissionRequest): DeletePermissionResult {
        val result =
            deletePermissionOperation.execute(
                DeletePermissionOperation.Arg(permissionId = request.permissionId),
            )
        return when (result) {
            DeletePermissionOperation.Result.Success -> DeletePermissionResult.Success
            DeletePermissionOperation.Result.NotFound -> DeletePermissionResult.NotFound
        }
    }

    suspend fun findPermissions(request: FindPermissionsRequest): FindPermissionsResult {
        val result =
            findPermissionsOperation.execute(
                FindPermissionsOperation.Arg(
                    resource = request.resource,
                    targetId = request.targetId,
                ),
            )
        return when (result) {
            is FindPermissionsOperation.Result.Success ->
                FindPermissionsResult.Success(
                    permissions = result.permissions.map { it.toResponse() },
                )
        }
    }

    suspend fun grantPermission(request: GrantPermissionRequest): GrantPermissionResult {
        val result =
            grantPermissionOperation.execute(
                GrantPermissionOperation.Arg(
                    groupId = request.groupId,
                    permissionId = request.permissionId,
                ),
            )
        return when (result) {
            GrantPermissionOperation.Result.Success -> GrantPermissionResult.Success
            is GrantPermissionOperation.Result.Failure ->
                GrantPermissionResult.Failure(reason = result.reason)
        }
    }

    suspend fun revokePermission(request: RevokePermissionRequest): RevokePermissionResult {
        val result =
            revokePermissionOperation.execute(
                RevokePermissionOperation.Arg(
                    groupId = request.groupId,
                    permissionId = request.permissionId,
                ),
            )
        return when (result) {
            RevokePermissionOperation.Result.Success -> RevokePermissionResult.Success
            is RevokePermissionOperation.Result.Failure ->
                RevokePermissionResult.Failure(reason = result.reason)
        }
    }

    suspend fun listGroupPermissions(request: ListGroupPermissionsRequest): ListGroupPermissionsResult {
        val result =
            listGroupPermissionsOperation.execute(
                ListGroupPermissionsOperation.Arg(groupId = request.groupId),
            )
        return when (result) {
            is ListGroupPermissionsOperation.Result.Success ->
                ListGroupPermissionsResult.Success(
                    permissions = result.permissions.map { it.toResponse() },
                )
        }
    }

    suspend fun checkPermission(request: CheckPermissionRequest): CheckPermissionResult {
        val result =
            checkPermissionOperation.execute(
                CheckPermissionOperation.Arg(
                    userId = request.userId,
                    resource = request.resource,
                    action = request.action,
                    targetId = request.targetId,
                ),
            )
        return when (result) {
            CheckPermissionOperation.Result.Allowed ->
                CheckPermissionResult.Success(
                    CheckPermissionResponse(allowed = true, reason = null),
                )
            is CheckPermissionOperation.Result.Denied ->
                CheckPermissionResult.Success(
                    CheckPermissionResponse(allowed = false, reason = result.reason),
                )
        }
    }

    data class CreateGroupRequest(
        val name: String,
        val description: String?,
    )

    data class GetGroupRequest(
        @JsonProperty("group_id")
        val groupId: String,
    )

    data class UpdateGroupBody(
        val name: String?,
        val description: String?,
    )

    data class UpdateGroupRequest(
        @JsonProperty("group_id")
        val groupId: String,
        val name: String?,
        val description: String?,
    )

    data class DeleteGroupRequest(
        @JsonProperty("group_id")
        val groupId: String,
    )

    data class AddMemberRequest(
        @JsonProperty("group_id")
        val groupId: String,
        @JsonProperty("user_id")
        val userId: String,
    )

    data class RemoveMemberRequest(
        @JsonProperty("group_id")
        val groupId: String,
        @JsonProperty("user_id")
        val userId: String,
    )

    data class ListMembersRequest(
        @JsonProperty("group_id")
        val groupId: String,
    )

    data class ListUserGroupsRequest(
        @JsonProperty("user_id")
        val userId: String,
    )

    data class CreatePermissionRequest(
        val resource: String,
        val action: String,
        @JsonProperty("target_id")
        val targetId: String?,
    )

    data class DeletePermissionRequest(
        @JsonProperty("permission_id")
        val permissionId: String,
    )

    data class FindPermissionsRequest(
        val resource: String,
        @JsonProperty("target_id")
        val targetId: String?,
    )

    data class GrantPermissionRequest(
        @JsonProperty("group_id")
        val groupId: String,
        @JsonProperty("permission_id")
        val permissionId: String,
    )

    data class RevokePermissionRequest(
        @JsonProperty("group_id")
        val groupId: String,
        @JsonProperty("permission_id")
        val permissionId: String,
    )

    data class ListGroupPermissionsRequest(
        @JsonProperty("group_id")
        val groupId: String,
    )

    data class CheckPermissionRequest(
        @JsonProperty("user_id")
        val userId: String,
        val resource: String,
        val action: String,
        @JsonProperty("target_id")
        val targetId: String?,
    )

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

    sealed interface CreateGroupResult {
        data class Success(
            val group: GroupResponse,
        ) : CreateGroupResult

        data class Failure(
            val reason: String,
        ) : CreateGroupResult
    }

    sealed interface GetGroupResult {
        data class Success(
            val group: GroupResponse,
        ) : GetGroupResult

        data object NotFound : GetGroupResult
    }

    sealed interface ListGroupsResult {
        data class Success(
            val groups: List<GroupResponse>,
        ) : ListGroupsResult
    }

    sealed interface UpdateGroupResult {
        data class Success(
            val group: GroupResponse,
        ) : UpdateGroupResult

        data object NotFound : UpdateGroupResult

        data class Failure(
            val reason: String,
        ) : UpdateGroupResult
    }

    sealed interface DeleteGroupResult {
        data object Success : DeleteGroupResult

        data object NotFound : DeleteGroupResult
    }

    sealed interface AddMemberResult {
        data object Success : AddMemberResult

        data class Failure(
            val reason: String,
        ) : AddMemberResult
    }

    sealed interface RemoveMemberResult {
        data object Success : RemoveMemberResult

        data class Failure(
            val reason: String,
        ) : RemoveMemberResult
    }

    sealed interface ListMembersResult {
        data class Success(
            val members: List<MemberResponse>,
        ) : ListMembersResult
    }

    sealed interface ListUserGroupsResult {
        data class Success(
            val groups: List<GroupResponse>,
        ) : ListUserGroupsResult
    }

    sealed interface CreatePermissionResult {
        data class Success(
            val permission: PermissionResponse,
        ) : CreatePermissionResult

        data class Failure(
            val reason: String,
        ) : CreatePermissionResult
    }

    sealed interface DeletePermissionResult {
        data object Success : DeletePermissionResult

        data object NotFound : DeletePermissionResult
    }

    sealed interface FindPermissionsResult {
        data class Success(
            val permissions: List<PermissionResponse>,
        ) : FindPermissionsResult
    }

    sealed interface GrantPermissionResult {
        data object Success : GrantPermissionResult

        data class Failure(
            val reason: String,
        ) : GrantPermissionResult
    }

    sealed interface RevokePermissionResult {
        data object Success : RevokePermissionResult

        data class Failure(
            val reason: String,
        ) : RevokePermissionResult
    }

    sealed interface ListGroupPermissionsResult {
        data class Success(
            val permissions: List<PermissionResponse>,
        ) : ListGroupPermissionsResult
    }

    sealed interface CheckPermissionResult {
        data class Success(
            val response: CheckPermissionResponse,
        ) : CheckPermissionResult
    }

    private fun Group.toResponse(): GroupResponse =
        GroupResponse(
            id = id.value,
            name = name,
            description = description,
            createdAt = createdAt,
        )

    private fun GroupMember.toResponse(): MemberResponse =
        MemberResponse(
            groupId = groupId.value,
            userId = userId,
            addedAt = addedAt,
        )

    private fun Permission.toResponse(): PermissionResponse =
        PermissionResponse(
            id = id.value,
            resource = resource,
            action = action,
            targetId = targetId,
            createdAt = createdAt,
        )
}
