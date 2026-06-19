package com.kanban.http.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import com.kanban.task.AttachFileOperation
import com.kanban.task.FileAttachment
import io.mockk.coEvery
import java.time.Instant
import java.util.Base64
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера прикрепления файла.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class AttachFileControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(AttachFileController::class.java)
    }

    @Test
    fun `should attach file and return 201`() {
        val taskId = "task-${UUID.randomUUID()}"
        val body = RequestGenerator.attachFileBody()
        val rawBytes = Base64.getDecoder().decode(body.contentBase64)
        val file =
            FileAttachment(
                id = FileAttachmentId("new-file-id"),
                taskId = TaskId(taskId),
                fileName = body.fileName,
                contentType = body.contentType,
                sizeBytes = rawBytes.size.toLong(),
                storageKey = "files/${UUID.randomUUID()}",
                uploadedBy = body.uploadedBy,
                uploadedAt = Instant.now(),
            )

        coEvery {
            attachFileOperation.execute(any())
        } returns AttachFileOperation.Result.Success(file = file)

        webClient
            .post()
            .uri("/api/v1/tasks/$taskId/files")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-file-id")
            .jsonPath("$.task_id")
            .isEqualTo(taskId)
            .jsonPath("$.file_name")
            .isEqualTo(body.fileName)
            .jsonPath("$.content_type")
            .isEqualTo(body.contentType)
            .jsonPath("$.size_bytes")
            .isEqualTo(rawBytes.size)
            .jsonPath("$.uploaded_by")
            .isEqualTo(body.uploadedBy)
    }

    @Test
    fun `should return 400 on failure`() {
        val taskId = "task-${UUID.randomUUID()}"
        val body = RequestGenerator.attachFileBody()

        coEvery {
            attachFileOperation.execute(any())
        } returns AttachFileOperation.Result.Failure("Task not found")

        webClient
            .post()
            .uri("/api/v1/tasks/$taskId/files")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Task not found")
    }
}
