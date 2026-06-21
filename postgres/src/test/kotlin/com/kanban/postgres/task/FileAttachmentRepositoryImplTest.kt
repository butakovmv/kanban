package com.kanban.postgres.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import com.kanban.postgres.project.BoardGenerator
import com.kanban.postgres.project.ColumnGenerator
import com.kanban.postgres.project.ProjectGenerator
import com.kanban.task.FileAttachment
import com.kanban.task.FileAttachmentRepository
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
internal class FileAttachmentRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var boardGenerator: BoardGenerator
    private lateinit var columnGenerator: ColumnGenerator
    private lateinit var taskGenerator: TaskGenerator
    private lateinit var fileAttachmentGenerator: FileAttachmentGenerator
    private lateinit var fileAttachmentRepository: FileAttachmentRepository

    private lateinit var taskId: String
    private lateinit var otherTaskId: String

    @BeforeEach
    fun setUp() =
        runTest {
            projectGenerator = ProjectGenerator(db)
            boardGenerator = BoardGenerator(db)
            columnGenerator = ColumnGenerator(db)
            taskGenerator = TaskGenerator(db)
            fileAttachmentGenerator = FileAttachmentGenerator(db)
            fileAttachmentRepository = FileAttachmentRepositoryImpl(db)
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val columnId = columnGenerator.createAndInsert(boardId = boardId)
            taskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId))
            otherTaskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId))
        }

    @AfterEach
    fun tearDown() =
        runTest {
            fileAttachmentGenerator.deleteAll()
            taskGenerator.deleteAll()
            columnGenerator.deleteAll()
            boardGenerator.deleteAll()
            projectGenerator.deleteAll()
        }

    @Test
    fun `should save new file attachment and find by id`() =
        runTest {
            val now = Instant.now()
            val attachment =
                FileAttachment(
                    id = FileAttachmentId("00000000-0000-0000-0000-000000000113"),
                    taskId = TaskId(taskId),
                    fileName = "doc.pdf",
                    contentType = "application/pdf",
                    sizeBytes = 4096L,
                    storageKey = "tasks/abc/doc.pdf",
                    uploadedBy = "00000000-0000-0000-0000-000000000001",
                    uploadedAt = now,
                )

            val saved = fileAttachmentRepository.save(attachment)

            assertEquals("00000000-0000-0000-0000-000000000113", saved.id.value)
            val found = fileAttachmentRepository.findById("00000000-0000-0000-0000-000000000113")
            assertNotNull(found)
            assertEquals("00000000-0000-0000-0000-000000000113", found.id.value)
            assertEquals(taskId, found.taskId.value)
            assertEquals("doc.pdf", found.fileName)
            assertEquals("application/pdf", found.contentType)
            assertEquals(4096L, found.sizeBytes)
            assertEquals("tasks/abc/doc.pdf", found.storageKey)
            assertEquals("00000000-0000-0000-0000-000000000001", found.uploadedBy)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = fileAttachmentRepository.findById("00000000-0000-0000-0000-000000000105")
            assertNull(found)
        }

    @Test
    fun `should update existing file attachment`() =
        runTest {
            val attachmentId =
                fileAttachmentGenerator.createAndInsert(
                    FileAttachmentSpec(taskId = taskId, fileName = "old.bin", sizeBytes = 100L),
                )
            val existing = fileAttachmentRepository.findById(attachmentId)!!
            val updated = existing.copy(fileName = "new.bin", sizeBytes = 200L)

            val saved = fileAttachmentRepository.save(updated)

            assertEquals("new.bin", saved.fileName)
            assertEquals(200L, saved.sizeBytes)
            val reloaded = fileAttachmentRepository.findById(attachmentId)
            assertNotNull(reloaded)
            assertEquals("new.bin", reloaded.fileName)
            assertEquals(200L, reloaded.sizeBytes)
        }

    @Test
    fun `should list file attachments by task id ordered by uploaded_at`() =
        runTest {
            val firstId =
                fileAttachmentGenerator.createAndInsert(
                    FileAttachmentSpec(taskId = taskId, fileName = "first.txt"),
                )
            val secondId =
                fileAttachmentGenerator.createAndInsert(
                    FileAttachmentSpec(taskId = taskId, fileName = "second.txt"),
                )
            val thirdId =
                fileAttachmentGenerator.createAndInsert(
                    FileAttachmentSpec(taskId = taskId, fileName = "third.txt"),
                )
            fileAttachmentGenerator.createAndInsert(FileAttachmentSpec(taskId = otherTaskId, fileName = "other.txt"))

            val list = fileAttachmentRepository.listByTaskId(taskId)

            assertEquals(3, list.size)
            assertEquals(listOf(firstId, secondId, thirdId), list.map { it.id.value })
        }

    @Test
    fun `should return empty list when task has no files`() =
        runTest {
            val list = fileAttachmentRepository.listByTaskId(taskId)
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should not include attachments from other tasks`() =
        runTest {
            fileAttachmentGenerator.createAndInsert(FileAttachmentSpec(taskId = taskId, fileName = "mine.txt"))
            fileAttachmentGenerator.createAndInsert(FileAttachmentSpec(taskId = otherTaskId, fileName = "other.txt"))

            val list = fileAttachmentRepository.listByTaskId(taskId)

            assertEquals(1, list.size)
            assertEquals("mine.txt", list.first().fileName)
        }

    @Test
    fun `should delete file attachment by id`() =
        runTest {
            val attachmentId = fileAttachmentGenerator.createAndInsert(FileAttachmentSpec(taskId = taskId))
            assertNotNull(fileAttachmentRepository.findById(attachmentId))

            fileAttachmentRepository.delete(attachmentId)

            assertNull(fileAttachmentRepository.findById(attachmentId))
        }

    @Test
    fun `should preserve large size bytes`() =
        runTest {
            val largeSize = 5_000_000_000L
            val attachmentId =
                fileAttachmentGenerator.createAndInsert(
                    FileAttachmentSpec(taskId = taskId, fileName = "big.bin", sizeBytes = largeSize),
                )

            val found = fileAttachmentRepository.findById(attachmentId)
            assertNotNull(found)
            assertEquals(largeSize, found.sizeBytes)
        }
}
