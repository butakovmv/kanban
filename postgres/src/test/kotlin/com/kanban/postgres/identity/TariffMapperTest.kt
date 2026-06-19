package com.kanban.postgres.identity

import com.kanban.common.TariffId
import com.kanban.identity.Tariff
import com.kanban.identity.TariffLimits
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class TariffMapperTest {
    @Test
    fun `should map TariffTable to Tariff domain`() {
        val now = LocalDateTime.now()
        val table =
            TariffTable(
                id = "t-1",
                name = "Premium",
                maxProjects = 10,
                maxBoardsPerProject = 20,
                maxTasksPerBoard = 100,
                maxFileSizeMb = 50,
                maxStorageMb = 1000,
                createdAt = now,
            )

        val domain = table.toDomain()

        assertEquals("t-1", domain.id.value)
        assertEquals("Premium", domain.name)
        assertEquals(10, domain.limits.maxProjects)
        assertEquals(20, domain.limits.maxBoardsPerProject)
        assertEquals(100, domain.limits.maxTasksPerBoard)
        assertEquals(50, domain.limits.maxFileSizeMb)
        assertEquals(1000, domain.limits.maxStorageMb)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
    }

    @Test
    fun `should map Tariff domain to TariffTable`() {
        val now = Instant.now()
        val domain =
            Tariff(
                id = TariffId("t-2"),
                name = "Free",
                limits =
                    TariffLimits(
                        maxProjects = 2,
                        maxBoardsPerProject = 3,
                        maxTasksPerBoard = 10,
                        maxFileSizeMb = 5,
                        maxStorageMb = 100,
                    ),
                createdAt = now,
            )

        val table = domain.toTable()

        assertEquals("t-2", table.id)
        assertEquals("Free", table.name)
        assertEquals(2, table.maxProjects)
        assertEquals(3, table.maxBoardsPerProject)
        assertEquals(10, table.maxTasksPerBoard)
        assertEquals(5, table.maxFileSizeMb)
        assertEquals(100, table.maxStorageMb)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.createdAt)
    }

    @Test
    fun `should roundtrip Tariff domain through table`() {
        val original =
            Tariff(
                id = TariffId("roundtrip-t"),
                name = "Roundtrip",
                limits =
                    TariffLimits(
                        maxProjects = 5,
                        maxBoardsPerProject = 10,
                        maxTasksPerBoard = 50,
                        maxFileSizeMb = 25,
                        maxStorageMb = 500,
                    ),
                createdAt = Instant.now(),
            )

        val table = original.toTable()
        val restored = table.toDomain()

        assertEquals(original.id.value, restored.id.value)
        assertEquals(original.name, restored.name)
        assertEquals(original.limits.maxProjects, restored.limits.maxProjects)
        assertEquals(original.limits.maxBoardsPerProject, restored.limits.maxBoardsPerProject)
        assertEquals(original.limits.maxTasksPerBoard, restored.limits.maxTasksPerBoard)
        assertEquals(original.limits.maxFileSizeMb, restored.limits.maxFileSizeMb)
        assertEquals(original.limits.maxStorageMb, restored.limits.maxStorageMb)
        assertEquals(original.createdAt, restored.createdAt)
    }
}
