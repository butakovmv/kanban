package com.kanban.postgres.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import com.kanban.identity.User
import com.kanban.identity.UserRepository
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class UserRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var userGenerator: UserGenerator
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userGenerator = UserGenerator(db)
        userRepository = UserRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            userGenerator.deleteAll()
        }

    @Test
    fun `should save new user and find by id`() =
        runTest {
            val userId = "00000000-0000-0000-0000-000000000001"
            val user =
                User(
                    id = UserId(userId),
                    email = Email("insert@kanban.test"),
                    passwordHash = PasswordHash("hash-insert"),
                    displayName = "Insert Test",
                    totpSecret = null,
                    totpEnabled = false,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                )

            val saved = userRepository.save(user)

            assertEquals(userId, saved.id.value)
            assertEquals("insert@kanban.test", saved.email.value)

            val found = userRepository.findById(userId)
            assertNotNull(found)
            assertEquals(userId, found.id.value)
            assertEquals("insert@kanban.test", found.email.value)
            assertEquals("hash-insert", found.passwordHash.value)
            assertEquals("Insert Test", found.displayName)
        }

    @Test
    fun `should find by email`() =
        runTest {
            val email = "findme@kanban.test"
            userGenerator.createAndInsert(email = email)

            val found = userRepository.findByEmail(email)

            assertNotNull(found)
            assertEquals(email, found.email.value)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = userRepository.findById("00000000-0000-0000-0000-000000000099")
            assertNull(found)
        }

    @Test
    fun `should return null for unknown email`() =
        runTest {
            val found = userRepository.findByEmail("nonexistent@kanban.test")
            assertNull(found)
        }

    @Test
    fun `should check email existence`() =
        runTest {
            val email = "exists@kanban.test"
            userGenerator.createAndInsert(email = email)

            assertTrue(userRepository.existsByEmail(email))
        }

    @Test
    fun `should return false for non-existent email`() =
        runTest {
            assertTrue(!userRepository.existsByEmail("noone@kanban.test"))
        }

    @Test
    fun `should update existing user`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val user = userRepository.findById(userId)!!
            val updated = user.copy(displayName = "Updated Name")

            val saved = userRepository.save(updated)

            assertNotNull(saved)
            assertEquals("Updated Name", saved.displayName)

            val reloaded = userRepository.findById(userId)
            assertEquals("Updated Name", reloaded!!.displayName)
        }

    @Test
    fun `should update email`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val user = userRepository.findById(userId)!!
            val updated = user.copy(email = Email("changed@kanban.test"))

            userRepository.save(updated)

            val byNewEmail = userRepository.findByEmail("changed@kanban.test")
            assertNotNull(byNewEmail)
            assertEquals(userId, byNewEmail.id.value)
        }
}
