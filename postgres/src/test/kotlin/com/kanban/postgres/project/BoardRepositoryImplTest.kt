package com.kanban.postgres.project

import com.kanban.common.BoardId
import com.kanban.common.ProjectId
import com.kanban.project.Board
import com.kanban.project.BoardRepository
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class BoardRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var boardGenerator: BoardGenerator
    private lateinit var boardRepository: BoardRepository

    @BeforeEach
    fun setUp() {
        projectGenerator = ProjectGenerator(db)
        boardGenerator = BoardGenerator(db)
        boardRepository = BoardRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            boardGenerator.deleteAll()
            projectGenerator.deleteAll()
        }

    @Test
    fun `should save new board and find by id`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val now = Instant.now()
            val board =
                Board(
                    id = BoardId("new-board-id"),
                    projectId = ProjectId(projectId),
                    name = "Main Board",
                    position = 0,
                    createdAt = now,
                )

            val saved = boardRepository.save(board)

            assertEquals("new-board-id", saved.id.value)

            val found = boardRepository.findById("new-board-id")
            assertNotNull(found)
            assertEquals("new-board-id", found.id.value)
            assertEquals(projectId, found.projectId.value)
            assertEquals("Main Board", found.name)
            assertEquals(0, found.position)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = boardRepository.findById("unknown-board-id")
            assertNull(found)
        }

    @Test
    fun `should list boards by project id ordered by position`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val firstId = boardGenerator.createAndInsert(projectId = projectId, name = "First", position = 0)
            val secondId = boardGenerator.createAndInsert(projectId = projectId, name = "Second", position = 1)
            val thirdId = boardGenerator.createAndInsert(projectId = projectId, name = "Third", position = 2)

            val list = boardRepository.listByProjectId(projectId)

            assertEquals(3, list.size)
            assertEquals(firstId, list[0].id.value)
            assertEquals(secondId, list[1].id.value)
            assertEquals(thirdId, list[2].id.value)
            assertEquals(listOf(0, 1, 2), list.map { it.position })
        }

    @Test
    fun `should return empty list when project has no boards`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val list = boardRepository.listByProjectId(projectId)
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should update existing board`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId, name = "Old Name", position = 0)
            val existing = boardRepository.findById(boardId)!!
            val updated = existing.copy(name = "New Name", position = 5)

            val saved = boardRepository.save(updated)

            assertEquals("New Name", saved.name)
            val reloaded = boardRepository.findById(boardId)
            assertNotNull(reloaded)
            assertEquals("New Name", reloaded.name)
            assertEquals(5, reloaded.position)
        }

    @Test
    fun `should preserve archived flag on update`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            boardRepository.archive(boardId)

            val existing = boardRepository.findById(boardId)!!
            val updated = existing.copy(name = "Renamed After Archive")

            boardRepository.save(updated)

            val reloaded = boardRepository.findById(boardId)
            assertNotNull(reloaded)
            assertEquals("Renamed After Archive", reloaded.name)
        }

    @Test
    fun `should delete board by id`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            assertNotNull(boardRepository.findById(boardId))

            boardRepository.delete(boardId)

            assertNull(boardRepository.findById(boardId))
        }

    @Test
    fun `should archive board by id`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            assertNotNull(boardRepository.findById(boardId))

            boardRepository.archive(boardId)

            val count =
                db
                    .sql("SELECT COUNT(*) AS cnt FROM boards WHERE id = :id AND archived = TRUE")
                    .bind("id", boardId)
                    .map { row, _ -> (row.get("cnt", java.lang.Long::class.java) ?: 0L) as Long }
                    .one()
                    .awaitSingle()
            assertEquals(1L, count)
        }
}
