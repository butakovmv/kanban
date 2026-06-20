package com.kanban.access

import com.kanban.common.GroupId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateGroupOperationImplTest {
    private val groupRepository = mockk<GroupRepository>()
    private val operation = UpdateGroupOperationImpl(groupRepository)

    private val sampleGroup =
        Group(
            id = GroupId("group-1"),
            name = "Old Name",
            description = "Old Desc",
            createdAt = Instant.now(),
        )

    @Test
    fun `should update name and description`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { groupRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateGroupOperation.Arg(
                        groupId = "group-1",
                        name = "  New Name  ",
                        description = "New Desc",
                    ),
                )

            val success = assertIs<UpdateGroupOperation.Result.Success>(result)
            assertEquals("New Name", success.group.name)
            assertEquals("New Desc", success.group.description)
            assertEquals(GroupId("group-1"), success.group.id)

            coVerify { groupRepository.findById("group-1") }
            coVerify { groupRepository.save(any()) }
        }

    @Test
    fun `should keep existing values when arguments are null`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { groupRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateGroupOperation.Arg(
                        groupId = "group-1",
                        name = null,
                        description = null,
                    ),
                )

            val success = assertIs<UpdateGroupOperation.Result.Success>(result)
            assertEquals(sampleGroup.name, success.group.name)
            assertEquals(sampleGroup.description, success.group.description)
        }

    @Test
    fun `should update only name when description is null`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { groupRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateGroupOperation.Arg(
                        groupId = "group-1",
                        name = "Only Name",
                        description = null,
                    ),
                )

            val success = assertIs<UpdateGroupOperation.Result.Success>(result)
            assertEquals("Only Name", success.group.name)
            assertEquals(sampleGroup.description, success.group.description)
        }

    @Test
    fun `should return NotFound when group not found`() =
        runTest {
            coEvery { groupRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    UpdateGroupOperation.Arg(
                        groupId = "missing",
                        name = "X",
                        description = null,
                    ),
                )

            assertIs<UpdateGroupOperation.Result.NotFound>(result)
            coVerify { groupRepository.findById("missing") }
            coVerify(inverse = true) { groupRepository.save(any()) }
        }

    @Test
    fun `should fail when name is blank`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup

            val result =
                operation.execute(
                    UpdateGroupOperation.Arg(
                        groupId = "group-1",
                        name = "   ",
                        description = null,
                    ),
                )

            val failure = assertIs<UpdateGroupOperation.Result.Failure>(result)
            assertEquals("Name must not be blank", failure.reason)
            coVerify { groupRepository.findById("group-1") }
            coVerify(inverse = true) { groupRepository.save(any()) }
        }
}
