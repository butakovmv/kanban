package com.kanban.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ListFilesOperationImplTest {
    private val fileAttachmentRepository = mockk<FileAttachmentRepository>()
    private val operation = ListFilesOperationImpl(fileAttachmentRepository)

    @Test
    fun `should return files for task`() =
        runTest {
            val files =
                listOf(
                    FileAttachment(
                        id = FileAttachmentId("f-1"),
                        taskId = TaskId("task-1"),
                        fileName = "a.txt",
                        contentType = "text/plain",
                        sizeBytes = 1L,
                        storageKey = "k-1",
                        uploadedBy = "user-1",
                        uploadedAt = Instant.now(),
                    ),
                    FileAttachment(
                        id = FileAttachmentId("f-2"),
                        taskId = TaskId("task-1"),
                        fileName = "b.txt",
                        contentType = "text/plain",
                        sizeBytes = 2L,
                        storageKey = "k-2",
                        uploadedBy = "user-2",
                        uploadedAt = Instant.now(),
                    ),
                )
            coEvery { fileAttachmentRepository.listByTaskId("task-1") } returns files

            val result = operation.execute(ListFilesOperation.Arg(taskId = "task-1"))

            val success = assertIs<ListFilesOperation.Result.Success>(result)
            assertEquals(files, success.files)

            coVerify { fileAttachmentRepository.listByTaskId("task-1") }
        }

    @Test
    fun `should return empty list when task has no files`() =
        runTest {
            coEvery { fileAttachmentRepository.listByTaskId("empty") } returns emptyList()

            val result = operation.execute(ListFilesOperation.Arg(taskId = "empty"))

            val success = assertIs<ListFilesOperation.Result.Success>(result)
            assertEquals(emptyList(), success.files)
        }
}
