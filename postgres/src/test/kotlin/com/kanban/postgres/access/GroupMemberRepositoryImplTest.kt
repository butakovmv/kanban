package com.kanban.postgres.access

import com.kanban.access.GroupMemberRepository
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class GroupMemberRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var generator: AccessGenerator
    private lateinit var repository: GroupMemberRepository

    private lateinit var groupId: String
    private lateinit var otherGroupId: String

    @BeforeEach
    fun setUp() =
        runTest {
            generator = AccessGenerator(db)
            repository = GroupMemberRepositoryImpl(db)
            groupId = generator.insertGroup(name = "Group A")
            otherGroupId = generator.insertGroup(name = "Group B")
        }

    @AfterEach
    fun tearDown() =
        runTest {
            generator.deleteAll()
        }

    @Test
    fun `should add member and list members`() =
        runTest {
            val userId = UUID.randomUUID().toString()
            generator.addMember(groupId, userId)

            val members = repository.listMembers(groupId)
            assertEquals(1, members.size)
            assertEquals(groupId, members.first().groupId.value)
            assertEquals(userId, members.first().userId)
        }

    @Test
    fun `should list multiple members`() =
        runTest {
            val userA = UUID.randomUUID().toString()
            val userB = UUID.randomUUID().toString()
            generator.addMember(groupId, userA)
            generator.addMember(groupId, userB)

            val members = repository.listMembers(groupId)
            assertEquals(2, members.size)
            assertTrue(members.any { it.userId == userA })
            assertTrue(members.any { it.userId == userB })
        }

    @Test
    fun `should return empty list when group has no members`() =
        runTest {
            val members = repository.listMembers(groupId)
            assertTrue(members.isEmpty())
        }

    @Test
    fun `should not include members from other groups`() =
        runTest {
            val userId = UUID.randomUUID().toString()
            generator.addMember(groupId, userId)

            val members = repository.listMembers(otherGroupId)
            assertTrue(members.isEmpty())
        }

    @Test
    fun `should check isMember returns true when user is in group`() =
        runTest {
            val userId = UUID.randomUUID().toString()
            generator.addMember(groupId, userId)

            val result = repository.isMember(groupId, userId)
            assertTrue(result)
        }

    @Test
    fun `should check isMember returns false when user is not in group`() =
        runTest {
            val result = repository.isMember(groupId, UUID.randomUUID().toString())
            assertFalse(result)
        }

    @Test
    fun `should remove member`() =
        runTest {
            val userId = UUID.randomUUID().toString()
            generator.addMember(groupId, userId)
            assertTrue(repository.isMember(groupId, userId))

            repository.removeMember(groupId, userId)

            assertFalse(repository.isMember(groupId, userId))
        }

    @Test
    fun `should list groups for user`() =
        runTest {
            val userId = UUID.randomUUID().toString()
            generator.addMember(groupId, userId)
            generator.addMember(otherGroupId, userId)

            val groups = repository.listGroupsForUser(userId)
            assertEquals(2, groups.size)
            assertTrue(groups.any { it.id.value == groupId })
            assertTrue(groups.any { it.id.value == otherGroupId })
        }

    @Test
    fun `should return empty list for user not in any group`() =
        runTest {
            val groups = repository.listGroupsForUser(UUID.randomUUID().toString())
            assertTrue(groups.isEmpty())
        }

    @Test
    fun `should delete all members by group`() =
        runTest {
            val userA = UUID.randomUUID().toString()
            val userB = UUID.randomUUID().toString()
            generator.addMember(groupId, userA)
            generator.addMember(groupId, userB)
            generator.addMember(otherGroupId, userA)

            repository.deleteAllByGroup(groupId)

            assertTrue(repository.listMembers(groupId).isEmpty())
            assertEquals(1, repository.listMembers(otherGroupId).size)
        }
}
