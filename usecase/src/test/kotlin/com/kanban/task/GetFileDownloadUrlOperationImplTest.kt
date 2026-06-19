package com.kanban.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetFileDownloadUrlOperationImplTest {
    private val fileAttachmentRepository = mockk<FileAttachmentRepository>()
    private val fileStorage = mockk<FileStorage>()
    private val operation = GetFileDownloadUrlOperationImpl(fileAttachmentRepository, fileStorage)

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
    fun `should return presigned url for existing file`() =
        runTest {
            val expectedUrl = "https://storage.example.com/presigned/file-1"
            coEvery { fileAttachmentRepository.findById("file-1") } returns sampleFile
            coEvery {
                fileStorage.getDownloadUrl("tasks/task-1/file-1/doc.txt", 15.minutes)
            } returns expectedUrl

            val result = operation.execute(GetFileDownloadUrlOperation.Arg(fileId = "file-1"))

            val success = assertIs<GetFileDownloadUrlOperation.Result.Success>(result)
            assertEquals(expectedUrl, success.url)

            coVerify { fileAttachmentRepository.findById("file-1") }
            coVerify {
                fileStorage.getDownloadUrl("tasks/task-1/file-1/doc.txt", 15.minutes)
            }
        }

    @Test
    fun `should return NotFound when file not found`() =
        runTest {
            coEvery { fileAttachmentRepository.findById("missing") } returns null

            val result = operation.execute(GetFileDownloadUrlOperation.Arg(fileId = "missing"))

            assertIs<GetFileDownloadUrlOperation.Result.NotFound>(result)

            coVerify { fileAttachmentRepository.findById("missing") }
            coVerify(inverse = true) { fileStorage.getDownloadUrl(any(), any()) }
        }
}
