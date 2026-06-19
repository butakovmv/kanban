package com.kanban.postgres.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import com.kanban.identity.User
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class UserMapperTest {
    @Test
    fun `should map UserTable to User domain`() {
        val now = LocalDateTime.now()
        val table =
            UserTable(
                id = "test-id",
                email = "test@kanban.test",
                passwordHash = "hash123",
                displayName = "Test User",
                totpSecret = null,
                totpEnabled = false,
                createdAt = now,
                updatedAt = now,
            )

        val domain = table.toDomain()

        assertEquals("test-id", domain.id.value)
        assertEquals("test@kanban.test", domain.email.value)
        assertEquals("hash123", domain.passwordHash.value)
        assertEquals("Test User", domain.displayName)
        assertNull(domain.totpSecret)
        assertEquals(false, domain.totpEnabled)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.updatedAt)
    }

    @Test
    fun `should map User domain to UserTable`() {
        val now = Instant.now()
        val domain =
            User(
                id = UserId("test-id"),
                email = Email("test@kanban.test"),
                passwordHash = PasswordHash("hash123"),
                displayName = "Test User",
                totpSecret = "secret",
                totpEnabled = true,
                createdAt = now,
                updatedAt = now,
            )

        val table = domain.toTable()

        assertEquals("test-id", table.id)
        assertEquals("test@kanban.test", table.email)
        assertEquals("hash123", table.passwordHash)
        assertEquals("Test User", table.displayName)
        assertEquals("secret", table.totpSecret)
        assertEquals(true, table.totpEnabled)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.updatedAt)
    }

    @Test
    fun `should roundtrip User domain through table`() {
        val original =
            User(
                id = UserId("roundtrip-id"),
                email = Email("roundtrip@kanban.test"),
                passwordHash = PasswordHash("round-hash"),
                displayName = "Round Trip",
                totpSecret = null,
                totpEnabled = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        val table = original.toTable()
        val restored = table.toDomain()

        assertEquals(original.id.value, restored.id.value)
        assertEquals(original.email.value, restored.email.value)
        assertEquals(original.passwordHash.value, restored.passwordHash.value)
        assertEquals(original.displayName, restored.displayName)
        assertEquals(original.totpSecret, restored.totpSecret)
        assertEquals(original.totpEnabled, restored.totpEnabled)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
    }
}
