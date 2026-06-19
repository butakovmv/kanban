package com.kanban.postgres.identity

import com.kanban.identity.UserTariff
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class UserTariffMapperTest {
    @Test
    fun `should map UserTariffTable to UserTariff domain with null expiresAt`() {
        val now = LocalDateTime.now()
        val table =
            UserTariffTable(
                id = "ut-1",
                userId = "user-1",
                tariffId = "t-1",
                startsAt = now.minusDays(1),
                expiresAt = null,
                createdAt = now,
            )

        val domain = table.toDomain()

        assertEquals("ut-1", domain.id)
        assertEquals("user-1", domain.userId)
        assertEquals("t-1", domain.tariffId)
        assertEquals(now.minusDays(1).atZone(ZoneId.systemDefault()).toInstant(), domain.startsAt)
        assertNull(domain.expiresAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
    }

    @Test
    fun `should map UserTariffTable to UserTariff domain with expiresAt`() {
        val now = LocalDateTime.now()
        val table =
            UserTariffTable(
                id = "ut-2",
                userId = "user-2",
                tariffId = "t-2",
                startsAt = now,
                expiresAt = now.plusDays(30),
                createdAt = now,
            )

        val domain = table.toDomain()

        assertEquals("ut-2", domain.id)
        assertEquals("user-2", domain.userId)
        assertEquals("t-2", domain.tariffId)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
        assertEquals(now.plusDays(30).atZone(ZoneId.systemDefault()).toInstant(), domain.expiresAt)
    }

    @Test
    fun `should map UserTariff domain to UserTariffTable with null expiresAt`() {
        val now = Instant.now()
        val domain =
            UserTariff(
                id = "ut-3",
                userId = "user-3",
                tariffId = "t-3",
                startsAt = now.minusSeconds(86400),
                expiresAt = null,
                createdAt = now,
            )

        val table = domain.toTable()

        assertEquals("ut-3", table.id)
        assertEquals("user-3", table.userId)
        assertEquals("t-3", table.tariffId)
        assertNull(table.expiresAt)
    }

    @Test
    fun `should roundtrip UserTariff domain through table`() {
        val original =
            UserTariff(
                id = "roundtrip-ut",
                userId = "roundtrip-user",
                tariffId = "roundtrip-t",
                startsAt = Instant.now().minusSeconds(86400),
                expiresAt = Instant.now().plusSeconds(86400),
                createdAt = Instant.now(),
            )

        val table = original.toTable()
        val restored = table.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.tariffId, restored.tariffId)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.startsAt, restored.startsAt)
        assertEquals(original.expiresAt, restored.expiresAt)
    }
}
