package com.kanban.postgres.identity

import com.kanban.identity.User
import com.kanban.identity.UserRepository
import kotlin.test.assertIs
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
    fun `should save and find by id`() =
        runTest {
            val userId = userGenerator.createAndInsert()

            val found = userRepository.findById(userId)

            assertNotNull(found)
            assertIs<User>(found)
        }

    @Test
    fun `should find by email`() =
        runTest {
            val email = "findme@kanban.test"
            userGenerator.createAndInsert(email = email)

            val found = userRepository.findByEmail(email)

            assertNotNull(found)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = userRepository.findById("unknown-id")
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
    fun `should update existing user`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val user = userRepository.findById(userId)!!
            val updated = user.copy(displayName = "Updated Name")

            val saved = userRepository.save(updated)

            assertNotNull(saved)
            assert(saved.displayName == "Updated Name")
        }
}
