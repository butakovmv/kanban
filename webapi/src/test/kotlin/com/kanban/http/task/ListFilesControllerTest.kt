package com.kanban.http.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import com.kanban.task.FileAttachment
import com.kanban.task.ListFilesOperation
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения списка файлов.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class ListFilesControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListFilesController::class.java)
    }

    @Test
    fun `should return 200 with files list`() {
        val taskId = "task-${UUID.randomUUID()}"
        val files =
            listOf(
                FileAttachment(
                    id = FileAttachmentId("file-1"),
                    taskId = TaskId(taskId),
                    fileName = "doc.pdf",
                    contentType = "application/pdf",
                    sizeBytes = 1024L,
                    storageKey = "files/1",
                    uploadedBy = "user-1",
                    uploadedAt = Instant.now(),
                ),
                FileAttachment(
                    id = FileAttachmentId("file-2"),
                    taskId = TaskId(taskId),
                    fileName = "image.png",
                    contentType = "image/png",
                    sizeBytes = 2048L,
                    storageKey = "files/2",
                    uploadedBy = "user-2",
                    uploadedAt = Instant.now(),
                ),
            )

        coEvery {
            listFilesOperation.execute(any())
        } returns ListFilesOperation.Result.Success(files = files)

        webClient
            .get()
            .uri("/api/v1/tasks/$taskId/files")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.files.length()")
            .isEqualTo(2)
            .jsonPath("$.files[0].id")
            .isEqualTo("file-1")
            .jsonPath("$.files[0].task_id")
            .isEqualTo(taskId)
            .jsonPath("$.files[0].file_name")
            .isEqualTo("doc.pdf")
            .jsonPath("$.files[1].id")
            .isEqualTo("file-2")
    }

    @Test
    fun `should return 200 with empty list when no files`() {
        val taskId = "task-${UUID.randomUUID()}"

        coEvery {
            listFilesOperation.execute(any())
        } returns ListFilesOperation.Result.Success(files = emptyList())

        webClient
            .get()
            .uri("/api/v1/tasks/$taskId/files")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.files.length()")
            .isEqualTo(0)
    }
}
