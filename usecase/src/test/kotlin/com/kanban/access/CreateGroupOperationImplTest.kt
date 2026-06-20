package com.kanban.access

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateGroupOperationImplTest {
    private val groupRepository = mockk<GroupRepository>()
    private val operation = CreateGroupOperationImpl(groupRepository)

    @Test
    fun `should create group with trimmed name and preserve description as-is`() =
        runTest {
            coEvery { groupRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateGroupOperation.Arg(
                        name = "  Admins  ",
                        description = "Site administrators",
                    ),
                )

            val success = assertIs<CreateGroupOperation.Result.Success>(result)
            assertEquals("Admins", success.group.name)
            assertEquals("Site administrators", success.group.description)
            val groupId = success.group.id.value
            assert(groupId.isNotBlank())

            coVerify { groupRepository.save(any()) }
        }

    @Test
    fun `should preserve description with surrounding whitespace`() =
        runTest {
            coEvery { groupRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateGroupOperation.Arg(
                        name = "Editors",
                        description = "  Editors team  ",
                    ),
                )

            val success = assertIs<CreateGroupOperation.Result.Success>(result)
            assertEquals("Editors", success.group.name)
            assertEquals("  Editors team  ", success.group.description)
        }

    @Test
    fun `should create group with null description`() =
        runTest {
            coEvery { groupRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateGroupOperation.Arg(
                        name = "Editors",
                        description = null,
                    ),
                )

            val success = assertIs<CreateGroupOperation.Result.Success>(result)
            assertEquals("Editors", success.group.name)
            assertEquals(null, success.group.description)
        }

    @Test
    fun `should fail when name is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreateGroupOperation.Arg(
                        name = "   ",
                        description = null,
                    ),
                )

            val failure = assertIs<CreateGroupOperation.Result.Failure>(result)
            assertEquals("Name must not be blank", failure.reason)

            coVerify(inverse = true) { groupRepository.save(any()) }
        }
}
