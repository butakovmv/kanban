package com.kanban.postgres.identity

import com.kanban.identity.TariffRepository
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
            val tariffId = tariffGenerator.createAndInsert()

            val found = tariffRepository.findById(tariffId)

            assertNotNull(found)
        }

    @Test
    fun `should find by name`() =
        runTest {
            val name = "Premium"
            tariffGenerator.createAndInsert(TariffParams(name = name))

            val found = tariffRepository.findByName(name)

            assertNotNull(found)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = tariffRepository.findById("unknown-id")
            assertNull(found)
        }

    @Test
    fun `should list all tariffs`() =
        runTest {
            tariffGenerator.createAndInsert(TariffParams(name = "Free"))
            tariffGenerator.createAndInsert(TariffParams(name = "Pro"))

            val all = tariffRepository.listAll()

            assertTrue(all.size >= 2)
        }
}
