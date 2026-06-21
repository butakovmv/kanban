package com.kanban.postgres.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.project.Column
import com.kanban.project.ColumnRepository
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
internal class ColumnRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var boardGenerator: BoardGenerator
    private lateinit var columnGenerator: ColumnGenerator
    private lateinit var columnRepository: ColumnRepository

    @BeforeEach
    fun setUp() {
        projectGenerator = ProjectGenerator(db)
        boardGenerator = BoardGenerator(db)
        columnGenerator = ColumnGenerator(db)
        columnRepository = ColumnRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            columnGenerator.deleteAll()
            boardGenerator.deleteAll()
            projectGenerator.deleteAll()
        }

    @Test
    fun `should save new column and find by id`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val now = Instant.now()
            val column =
                Column(
                    id = ColumnId("00000000-0000-0000-0000-000000000106"),
                    boardId = BoardId(boardId),
                    name = "To Do",
                    position = 0,
                    wipLimit = 5,
                    createdAt = now,
                )

            val saved = columnRepository.save(column)

            assertEquals("00000000-0000-0000-0000-000000000106", saved.id.value)

            val found = columnRepository.findById("00000000-0000-0000-0000-000000000106")
            assertNotNull(found)
            assertEquals("00000000-0000-0000-0000-000000000106", found.id.value)
            assertEquals(boardId, found.boardId.value)
            assertEquals("To Do", found.name)
            assertEquals(0, found.position)
            assertEquals(5, found.wipLimit)
        }

    @Test
    fun `should save column with null wip limit`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val now = Instant.now()
            val column =
                Column(
                    id = ColumnId("00000000-0000-0000-0000-000000000117"),
                    boardId = BoardId(boardId),
                    name = "Backlog",
                    position = 0,
                    wipLimit = null,
                    createdAt = now,
                )

            columnRepository.save(column)

            val found = columnRepository.findById("00000000-0000-0000-0000-000000000117")
            assertNotNull(found)
            assertNull(found.wipLimit)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = columnRepository.findById("00000000-0000-0000-0000-000000000094")
            assertNull(found)
        }

    @Test
    fun `should list columns by board id ordered by position`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val firstId = columnGenerator.createAndInsert(boardId = boardId, name = "First", position = 0)
            val secondId = columnGenerator.createAndInsert(boardId = boardId, name = "Second", position = 1)
            val thirdId = columnGenerator.createAndInsert(boardId = boardId, name = "Third", position = 2)

            val list = columnRepository.listByBoardId(boardId)

            assertEquals(3, list.size)
            assertEquals(firstId, list[0].id.value)
            assertEquals(secondId, list[1].id.value)
            assertEquals(thirdId, list[2].id.value)
            assertEquals(listOf(0, 1, 2), list.map { it.position })
        }

    @Test
    fun `should return empty list when board has no columns`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val list = columnRepository.listByBoardId(boardId)
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should update existing column`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val columnId = columnGenerator.createAndInsert(boardId = boardId, name = "Old Name", position = 0)
            val existing = columnRepository.findById(columnId)!!
            val updated = existing.copy(name = "New Name", position = 3, wipLimit = 10)

            val saved = columnRepository.save(updated)

            assertEquals("New Name", saved.name)
            val reloaded = columnRepository.findById(columnId)
            assertNotNull(reloaded)
            assertEquals("New Name", reloaded.name)
            assertEquals(3, reloaded.position)
            assertEquals(10, reloaded.wipLimit)
        }

    @Test
    fun `should delete column by id`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val columnId = columnGenerator.createAndInsert(boardId = boardId)
            assertNotNull(columnRepository.findById(columnId))

            columnRepository.delete(columnId)

            assertNull(columnRepository.findById(columnId))
        }

    @Test
    fun `should reorder columns by updating positions`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val firstId = columnGenerator.createAndInsert(boardId = boardId, name = "To Do", position = 0)
            val secondId = columnGenerator.createAndInsert(boardId = boardId, name = "In Progress", position = 1)
            val thirdId = columnGenerator.createAndInsert(boardId = boardId, name = "Done", position = 2)

            val current = columnRepository.listByBoardId(boardId)
            val reordered =
                listOf(
                    current.first { it.id.value == thirdId },
                    current.first { it.id.value == firstId },
                    current.first { it.id.value == secondId },
                )

            columnRepository.updatePositions(reordered)

            val reloaded = columnRepository.listByBoardId(boardId)
            assertEquals(listOf(thirdId, firstId, secondId), reloaded.map { it.id.value })
            assertEquals(listOf(0, 1, 2), reloaded.map { it.position })
        }

    @Test
    fun `should handle empty list in updatePositions`() =
        runTest {
            columnRepository.updatePositions(emptyList())
        }
}
