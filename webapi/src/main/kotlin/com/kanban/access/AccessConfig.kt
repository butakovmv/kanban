package com.kanban.access

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class AccessConfig {
    @Bean
    @Suppress("LongParameterList")
    fun accessHandler(
        createGroup: CreateGroupOperation,
        getGroup: GetGroupOperation,
        listGroups: ListGroupsOperation,
        updateGroup: UpdateGroupOperation,
        deleteGroup: DeleteGroupOperation,
        addMember: AddMemberOperation,
        removeMember: RemoveMemberOperation,
        listMembers: ListMembersOperation,
        listUserGroups: ListUserGroupsOperation,
        createPermission: CreatePermissionOperation,
        deletePermission: DeletePermissionOperation,
        findPermissions: FindPermissionsOperation,
        grantPermission: GrantPermissionOperation,
        revokePermission: RevokePermissionOperation,
        listGroupPermissions: ListGroupPermissionsOperation,
        checkPermission: CheckPermissionOperation,
    ): AccessHandler =
        AccessHandler(
            createGroupOperation = createGroup,
            getGroupOperation = getGroup,
            listGroupsOperation = listGroups,
            updateGroupOperation = updateGroup,
            deleteGroupOperation = deleteGroup,
            addMemberOperation = addMember,
            removeMemberOperation = removeMember,
            listMembersOperation = listMembers,
            listUserGroupsOperation = listUserGroups,
            createPermissionOperation = createPermission,
            deletePermissionOperation = deletePermission,
            findPermissionsOperation = findPermissions,
            grantPermissionOperation = grantPermission,
            revokePermissionOperation = revokePermission,
            listGroupPermissionsOperation = listGroupPermissions,
            checkPermissionOperation = checkPermission,
        )
}
