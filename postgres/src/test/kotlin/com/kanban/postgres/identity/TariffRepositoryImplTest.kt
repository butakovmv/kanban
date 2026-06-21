package com.kanban.postgres.identity

import com.kanban.identity.TariffRepository
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
internal class TariffRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var tariffGenerator: TariffGenerator
    private lateinit var tariffRepository: TariffRepository

    @BeforeEach
    fun setUp() {
        tariffGenerator = TariffGenerator(db)
        tariffRepository = TariffRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            tariffGenerator.deleteAll()
        }

    @Test
    fun `should find by id`() =
        runTest {
            val tariffId = tariffGenerator.createAndInsert(TariffParams(name = "Premium"))

            val found = tariffRepository.findById(tariffId)

            assertNotNull(found)
            assertEquals(tariffId, found.id.value)
            assertEquals("Premium", found.name)
        }

    @Test
    fun `should find by name`() =
        runTest {
            val name = "Premium"
            tariffGenerator.createAndInsert(TariffParams(name = name))

            val found = tariffRepository.findByName(name)

            assertNotNull(found)
            assertEquals(name, found.name)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = tariffRepository.findById("00000000-0000-0000-0000-000000000105")
            assertNull(found)
        }

    @Test
    fun `should return null for unknown name`() =
        runTest {
            val found = tariffRepository.findByName("NonExistent")
            assertNull(found)
        }

    @Test
    fun `should list all tariffs`() =
        runTest {
            tariffGenerator.createAndInsert(TariffParams(name = "Free"))
            tariffGenerator.createAndInsert(TariffParams(name = "Pro"))

            val all = tariffRepository.listAll()

            assertTrue(all.size >= 2)
            assertEquals("Free", all[0].name)
            assertEquals("Pro", all[1].name)
        }

    @Test
    fun `should return empty list when no tariffs`() =
        runTest {
            val all = tariffRepository.listAll()
            assertTrue(all.isEmpty())
        }
}
