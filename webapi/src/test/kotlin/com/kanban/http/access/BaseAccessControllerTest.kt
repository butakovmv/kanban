package com.kanban.http.access

import com.kanban.access.AccessHandler
import com.kanban.access.AddMemberOperation
import com.kanban.access.CheckPermissionOperation
import com.kanban.access.CreateGroupOperation
import com.kanban.access.CreatePermissionOperation
import com.kanban.access.DeleteGroupOperation
import com.kanban.access.DeletePermissionOperation
import com.kanban.access.FindPermissionsOperation
import com.kanban.access.GetGroupOperation
import com.kanban.access.GrantPermissionOperation
import com.kanban.access.ListGroupPermissionsOperation
import com.kanban.access.ListGroupsOperation
import com.kanban.access.ListMembersOperation
import com.kanban.access.ListUserGroupsOperation
import com.kanban.access.RemoveMemberOperation
import com.kanban.access.RevokePermissionOperation
import com.kanban.access.UpdateGroupOperation
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient

internal abstract class BaseAccessControllerTest {
    protected lateinit var createGroupOperation: CreateGroupOperation
    protected lateinit var getGroupOperation: GetGroupOperation
    protected lateinit var listGroupsOperation: ListGroupsOperation
    protected lateinit var updateGroupOperation: UpdateGroupOperation
    protected lateinit var deleteGroupOperation: DeleteGroupOperation
    protected lateinit var addMemberOperation: AddMemberOperation
    protected lateinit var removeMemberOperation: RemoveMemberOperation
    protected lateinit var listMembersOperation: ListMembersOperation
    protected lateinit var listUserGroupsOperation: ListUserGroupsOperation
    protected lateinit var createPermissionOperation: CreatePermissionOperation
    protected lateinit var deletePermissionOperation: DeletePermissionOperation
    protected lateinit var findPermissionsOperation: FindPermissionsOperation
    protected lateinit var grantPermissionOperation: GrantPermissionOperation
    protected lateinit var revokePermissionOperation: RevokePermissionOperation
    protected lateinit var listGroupPermissionsOperation: ListGroupPermissionsOperation
    protected lateinit var checkPermissionOperation: CheckPermissionOperation

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    protected fun bindTo(controllerClass: Class<*>): WebTestClient {
        createGroupOperation = mockk()
        getGroupOperation = mockk()
        listGroupsOperation = mockk()
        updateGroupOperation = mockk()
        deleteGroupOperation = mockk()
        addMemberOperation = mockk()
        removeMemberOperation = mockk()
        listMembersOperation = mockk()
        listUserGroupsOperation = mockk()
        createPermissionOperation = mockk()
        deletePermissionOperation = mockk()
        findPermissionsOperation = mockk()
        grantPermissionOperation = mockk()
        revokePermissionOperation = mockk()
        listGroupPermissionsOperation = mockk()
        checkPermissionOperation = mockk()

        val handler =
            AccessHandler(
                createGroupOperation = createGroupOperation,
                getGroupOperation = getGroupOperation,
                listGroupsOperation = listGroupsOperation,
                updateGroupOperation = updateGroupOperation,
                deleteGroupOperation = deleteGroupOperation,
                addMemberOperation = addMemberOperation,
                removeMemberOperation = removeMemberOperation,
                listMembersOperation = listMembersOperation,
                listUserGroupsOperation = listUserGroupsOperation,
                createPermissionOperation = createPermissionOperation,
                deletePermissionOperation = deletePermissionOperation,
                findPermissionsOperation = findPermissionsOperation,
                grantPermissionOperation = grantPermissionOperation,
                revokePermissionOperation = revokePermissionOperation,
                listGroupPermissionsOperation = listGroupPermissionsOperation,
                checkPermissionOperation = checkPermissionOperation,
            )

        val controller =
            when (controllerClass) {
                CreateGroupController::class.java -> CreateGroupController(handler)
                GetGroupController::class.java -> GetGroupController(handler)
                ListGroupsController::class.java -> ListGroupsController(handler)
                UpdateGroupController::class.java -> UpdateGroupController(handler)
                DeleteGroupController::class.java -> DeleteGroupController(handler)
                AddMemberController::class.java -> AddMemberController(handler)
                RemoveMemberController::class.java -> RemoveMemberController(handler)
                ListMembersController::class.java -> ListMembersController(handler)
                ListUserGroupsController::class.java -> ListUserGroupsController(handler)
                CreatePermissionController::class.java -> CreatePermissionController(handler)
                DeletePermissionController::class.java -> DeletePermissionController(handler)
                FindPermissionsController::class.java -> FindPermissionsController(handler)
                GrantPermissionController::class.java -> GrantPermissionController(handler)
                RevokePermissionController::class.java -> RevokePermissionController(handler)
                ListGroupPermissionsController::class.java -> ListGroupPermissionsController(handler)
                CheckPermissionController::class.java -> CheckPermissionController(handler)
                else -> throw IllegalArgumentException("Unsupported controller: $controllerClass")
            }

        return WebTestClient.bindToController(controller).build()
    }
}
