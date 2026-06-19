package com.kanban.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteFileOperationImplTest {
    private val fileAttachmentRepository = mockk<FileAttachmentRepository>()
    private val fileStorage = mockk<FileStorage>()
    private val operation = DeleteFileOperationImpl(fileAttachmentRepository, fileStorage)

    private val sampleFile =
        FileAttachment(
            id = FileAttachmentId("file-1"),
            taskId = TaskId("task-1"),
            fileName = "doc.txt",
            contentType = "text/plain",
            sizeBytes = 10L,
            storageKey = "tasks/task-1/file-1/doc.txt",
            uploadedBy = "user-1",
            uploadedAt = Instant.now(),
        )

    @Test
    fun `should delete file from storage and repository`() =
        runTest {
            coEvery { fileAttachmentRepository.findById("file-1") } returns sampleFile
            coEvery { fileStorage.delete("tasks/task-1/file-1/doc.txt") } returns Unit
            coEvery { fileAttachmentRepository.delete("file-1") } returns Unit

            val result = operation.execute(DeleteFileOperation.Arg(fileId = "file-1"))

            assertIs<DeleteFileOperation.Result.Success>(result)

            coVerify { fileAttachmentRepository.findById("file-1") }
            coVerify { fileStorage.delete("tasks/task-1/file-1/doc.txt") }
            coVerify { fileAttachmentRepository.delete("file-1") }
        }

    @Test
    fun `should return NotFound when file not found`() =
        runTest {
            coEvery { fileAttachmentRepository.findById("missing") } returns null

            val result = operation.execute(DeleteFileOperation.Arg(fileId = "missing"))

            assertIs<DeleteFileOperation.Result.NotFound>(result)

            coVerify { fileAttachmentRepository.findById("missing") }
            coVerify(inverse = true) { fileStorage.delete(any()) }
            coVerify(inverse = true) { fileAttachmentRepository.delete(any()) }
        }
}
