package com.kanban.access

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
    data class GroupData(
        val id: String,
        val name: String,
        val description: String?,
        val createdAt: Instant,
    )

    data class MemberData(
        val groupId: String,
        val userId: String,
        val addedAt: Instant,
    )

    data class PermissionData(
        val id: String,
        val resource: String,
        val action: String,
        val targetId: String?,
        val createdAt: Instant,
    )

    suspend fun createGroup(
        name: String,
        description: String?,
    ): CreateGroupResult {
        val result =
            createGroupOperation.execute(
                CreateGroupOperation.Arg(
                    name = name,
                    description = description,
                ),
            )
        return when (result) {
            is CreateGroupOperation.Result.Success ->
                CreateGroupResult.Success(group = result.group.toData())
            is CreateGroupOperation.Result.Failure ->
                CreateGroupResult.Failure(reason = result.reason)
        }
    }

    suspend fun getGroup(groupId: String): GetGroupResult {
        val result =
            getGroupOperation.execute(
                GetGroupOperation.Arg(groupId = groupId),
            )
        return when (result) {
            is GetGroupOperation.Result.Success ->
                GetGroupResult.Success(group = result.group.toData())
            GetGroupOperation.Result.NotFound -> GetGroupResult.NotFound
        }
    }

    suspend fun listGroups(): ListGroupsResult {
        val result =
            listGroupsOperation.execute(ListGroupsOperation.Arg)
        return when (result) {
            is ListGroupsOperation.Result.Success ->
                ListGroupsResult.Success(
                    groups = result.groups.map { it.toData() },
                )
        }
    }

    suspend fun updateGroup(
        groupId: String,
        name: String?,
        description: String?,
    ): UpdateGroupResult {
        val result =
            updateGroupOperation.execute(
                UpdateGroupOperation.Arg(
                    groupId = groupId,
                    name = name,
                    description = description,
                ),
            )
        return when (result) {
            is UpdateGroupOperation.Result.Success ->
                UpdateGroupResult.Success(group = result.group.toData())
            UpdateGroupOperation.Result.NotFound -> UpdateGroupResult.NotFound
            is UpdateGroupOperation.Result.Failure ->
                UpdateGroupResult.Failure(reason = result.reason)
        }
    }

    suspend fun deleteGroup(groupId: String): DeleteGroupResult {
        val result =
            deleteGroupOperation.execute(
                DeleteGroupOperation.Arg(groupId = groupId),
            )
        return when (result) {
            DeleteGroupOperation.Result.Success -> DeleteGroupResult.Success
            DeleteGroupOperation.Result.NotFound -> DeleteGroupResult.NotFound
        }
    }

    suspend fun addMember(
        groupId: String,
        userId: String,
    ): AddMemberResult {
        val result =
            addMemberOperation.execute(
                AddMemberOperation.Arg(
                    groupId = groupId,
                    userId = userId,
                ),
            )
        return when (result) {
            AddMemberOperation.Result.Success -> AddMemberResult.Success
            is AddMemberOperation.Result.Failure ->
                AddMemberResult.Failure(reason = result.reason)
        }
    }

    suspend fun removeMember(
        groupId: String,
        userId: String,
    ): RemoveMemberResult {
        val result =
            removeMemberOperation.execute(
                RemoveMemberOperation.Arg(
                    groupId = groupId,
                    userId = userId,
                ),
            )
        return when (result) {
            RemoveMemberOperation.Result.Success -> RemoveMemberResult.Success
            is RemoveMemberOperation.Result.Failure ->
                RemoveMemberResult.Failure(reason = result.reason)
        }
    }

    suspend fun listMembers(groupId: String): ListMembersResult {
        val result =
            listMembersOperation.execute(
                ListMembersOperation.Arg(groupId = groupId),
            )
        return when (result) {
            is ListMembersOperation.Result.Success ->
                ListMembersResult.Success(
                    members = result.members.map { it.toData() },
                )
        }
    }

    suspend fun listUserGroups(userId: String): ListUserGroupsResult {
        val result =
            listUserGroupsOperation.execute(
                ListUserGroupsOperation.Arg(userId = userId),
            )
        return when (result) {
            is ListUserGroupsOperation.Result.Success ->
                ListUserGroupsResult.Success(
                    groups = result.groups.map { it.toData() },
                )
        }
    }

    suspend fun createPermission(
        resource: String,
        action: String,
        targetId: String?,
    ): CreatePermissionResult {
        val result =
            createPermissionOperation.execute(
                CreatePermissionOperation.Arg(
                    resource = resource,
                    action = action,
                    targetId = targetId,
                ),
            )
        return when (result) {
            is CreatePermissionOperation.Result.Success ->
                CreatePermissionResult.Success(
                    permission = result.permission.toData(),
                )
            is CreatePermissionOperation.Result.Failure ->
                CreatePermissionResult.Failure(reason = result.reason)
        }
    }

    suspend fun deletePermission(permissionId: String): DeletePermissionResult {
        val result =
            deletePermissionOperation.execute(
                DeletePermissionOperation.Arg(permissionId = permissionId),
            )
        return when (result) {
            DeletePermissionOperation.Result.Success -> DeletePermissionResult.Success
            DeletePermissionOperation.Result.NotFound -> DeletePermissionResult.NotFound
        }
    }

    suspend fun findPermissions(
        resource: String,
        targetId: String?,
    ): FindPermissionsResult {
        val result =
            findPermissionsOperation.execute(
                FindPermissionsOperation.Arg(
                    resource = resource,
                    targetId = targetId,
                ),
            )
        return when (result) {
            is FindPermissionsOperation.Result.Success ->
                FindPermissionsResult.Success(
                    permissions = result.permissions.map { it.toData() },
                )
        }
    }

    suspend fun grantPermission(
        groupId: String,
        permissionId: String,
    ): GrantPermissionResult {
        val result =
            grantPermissionOperation.execute(
                GrantPermissionOperation.Arg(
                    groupId = groupId,
                    permissionId = permissionId,
                ),
            )
        return when (result) {
            GrantPermissionOperation.Result.Success -> GrantPermissionResult.Success
            is GrantPermissionOperation.Result.Failure ->
                GrantPermissionResult.Failure(reason = result.reason)
        }
    }

    suspend fun revokePermission(
        groupId: String,
        permissionId: String,
    ): RevokePermissionResult {
        val result =
            revokePermissionOperation.execute(
                RevokePermissionOperation.Arg(
                    groupId = groupId,
                    permissionId = permissionId,
                ),
            )
        return when (result) {
            RevokePermissionOperation.Result.Success -> RevokePermissionResult.Success
            is RevokePermissionOperation.Result.Failure ->
                RevokePermissionResult.Failure(reason = result.reason)
        }
    }

    suspend fun listGroupPermissions(groupId: String): ListGroupPermissionsResult {
        val result =
            listGroupPermissionsOperation.execute(
                ListGroupPermissionsOperation.Arg(groupId = groupId),
            )
        return when (result) {
            is ListGroupPermissionsOperation.Result.Success ->
                ListGroupPermissionsResult.Success(
                    permissions = result.permissions.map { it.toData() },
                )
        }
    }

    suspend fun checkPermission(
        userId: String,
        resource: String,
        action: String,
        targetId: String?,
    ): CheckPermissionResult {
        val result =
            checkPermissionOperation.execute(
                CheckPermissionOperation.Arg(
                    userId = userId,
                    resource = resource,
                    action = action,
                    targetId = targetId,
                ),
            )
        return when (result) {
            CheckPermissionOperation.Result.Allowed ->
                CheckPermissionResult.Success(allowed = true, reason = null)
            is CheckPermissionOperation.Result.Denied ->
                CheckPermissionResult.Success(allowed = false, reason = result.reason)
        }
    }

    sealed interface CreateGroupResult {
        data class Success(
            val group: GroupData,
        ) : CreateGroupResult

        data class Failure(
            val reason: String,
        ) : CreateGroupResult
    }

    sealed interface GetGroupResult {
        data class Success(
            val group: GroupData,
        ) : GetGroupResult

        data object NotFound : GetGroupResult
    }

    sealed interface ListGroupsResult {
        data class Success(
            val groups: List<GroupData>,
        ) : ListGroupsResult
    }

    sealed interface UpdateGroupResult {
        data class Success(
            val group: GroupData,
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
            val members: List<MemberData>,
        ) : ListMembersResult
    }

    sealed interface ListUserGroupsResult {
        data class Success(
            val groups: List<GroupData>,
        ) : ListUserGroupsResult
    }

    sealed interface CreatePermissionResult {
        data class Success(
            val permission: PermissionData,
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
            val permissions: List<PermissionData>,
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
            val permissions: List<PermissionData>,
        ) : ListGroupPermissionsResult
    }

    sealed interface CheckPermissionResult {
        data class Success(
            val allowed: Boolean,
            val reason: String?,
        ) : CheckPermissionResult
    }

    private fun Group.toData(): GroupData =
        GroupData(
            id = id.value,
            name = name,
            description = description,
            createdAt = createdAt,
        )

    private fun GroupMember.toData(): MemberData =
        MemberData(
            groupId = groupId.value,
            userId = userId,
            addedAt = addedAt,
        )

    private fun Permission.toData(): PermissionData =
        PermissionData(
            id = id.value,
            resource = resource,
            action = action,
            targetId = targetId,
            createdAt = createdAt,
        )
}
