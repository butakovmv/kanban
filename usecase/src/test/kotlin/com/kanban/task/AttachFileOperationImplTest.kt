package com.kanban.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AttachFileOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val fileAttachmentRepository = mockk<FileAttachmentRepository>()
    private val fileStorage = mockk<FileStorage>()
    private val operation =
        AttachFileOperationImpl(taskRepository, fileAttachmentRepository, fileStorage)

    private val sampleTask =
        Task(
            id = TaskId("task-1"),
            boardId = BoardId("board-1"),
            columnId = ColumnId("column-1"),
            title = "Task",
            description = null,
            assigneeId = null,
            position = 0,
            dueDate = null,
            archived = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should upload file and save attachment`() =
        runTest {
            val content = "hello".toByteArray()
            coEvery { taskRepository.findById("task-1") } returns sampleTask
            coEvery { fileStorage.upload(any(), any(), any()) } answers { firstArg() }
            coEvery { fileAttachmentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    AttachFileOperation.Arg(
                        taskId = "task-1",
                        fileName = "doc.txt",
                        contentType = "text/plain",
                        sizeBytes = 5L,
                        content = content,
                        uploadedBy = "user-1",
                    ),
                )

            val success = assertIs<AttachFileOperation.Result.Success>(result)
            assertEquals("doc.txt", success.file.fileName)
            assertEquals("text/plain", success.file.contentType)
            assertEquals(5L, success.file.sizeBytes)
            assertEquals("user-1", success.file.uploadedBy)
            assertEquals(TaskId("task-1"), success.file.taskId)
            assertEquals(content.size.toLong(), success.file.sizeBytes)
            assert(success.file.storageKey.startsWith("tasks/task-1/"))

            coVerify { taskRepository.findById("task-1") }
            coVerify {
                fileStorage.upload(
                    key = match { it.startsWith("tasks/task-1/") && it.endsWith("doc.txt") },
                    content = content,
                    contentType = "text/plain",
                )
            }
            coVerify { fileAttachmentRepository.save(any()) }
        }

    @Test
    fun `should use storage key returned by FileStorage`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask
            coEvery { fileStorage.upload(any(), any(), any()) } returns "bucket/path/file.bin"
            coEvery { fileAttachmentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    AttachFileOperation.Arg(
                        taskId = "task-1",
                        fileName = "file.bin",
                        contentType = "application/octet-stream",
                        sizeBytes = 0L,
                        content = ByteArray(0),
                        uploadedBy = "user-1",
                    ),
                )

            val success = assertIs<AttachFileOperation.Result.Success>(result)
            assertEquals("bucket/path/file.bin", success.file.storageKey)
        }

    @Test
    fun `should fail when task not found`() =
        runTest {
            coEvery { taskRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    AttachFileOperation.Arg(
                        taskId = "missing",
                        fileName = "doc.txt",
                        contentType = "text/plain",
                        sizeBytes = 0L,
                        content = ByteArray(0),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<AttachFileOperation.Result.Failure>(result)
            assertEquals("Task not found", failure.reason)

            coVerify { taskRepository.findById("missing") }
            coVerify(inverse = true) { fileStorage.upload(any(), any(), any()) }
            coVerify(inverse = true) { fileAttachmentRepository.save(any()) }
        }

    @Test
    fun `should fail when file name is blank`() =
        runTest {
            val result =
                operation.execute(
                    AttachFileOperation.Arg(
                        taskId = "task-1",
                        fileName = "  ",
                        contentType = "text/plain",
                        sizeBytes = 0L,
                        content = ByteArray(0),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<AttachFileOperation.Result.Failure>(result)
            assertEquals("File name must not be blank", failure.reason)

            coVerify(inverse = true) { taskRepository.findById(any()) }
            coVerify(inverse = true) { fileStorage.upload(any(), any(), any()) }
        }

    @Test
    fun `should fail when content type is blank`() =
        runTest {
            val result =
                operation.execute(
                    AttachFileOperation.Arg(
                        taskId = "task-1",
                        fileName = "doc.txt",
                        contentType = "  ",
                        sizeBytes = 0L,
                        content = ByteArray(0),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<AttachFileOperation.Result.Failure>(result)
            assertEquals("Content type must not be blank", failure.reason)
        }

    @Test
    fun `should fail when size is negative`() =
        runTest {
            val result =
                operation.execute(
                    AttachFileOperation.Arg(
                        taskId = "task-1",
                        fileName = "doc.txt",
                        contentType = "text/plain",
                        sizeBytes = -1L,
                        content = ByteArray(0),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<AttachFileOperation.Result.Failure>(result)
            assertEquals("File size must not be negative", failure.reason)
        }
}
