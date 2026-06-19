package com.kanban.http.task

import com.kanban.task.DeleteFileOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера удаления файла.
 * Проверяют корректность кодов ответа.
 */
internal class DeleteFileControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeleteFileController::class.java)
    }

    @Test
    fun `should delete file and return 204`() {
        val id = "file-${UUID.randomUUID()}"

        coEvery {
            deleteFileOperation.execute(any())
        } returns DeleteFileOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/files/$id")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify {
            deleteFileOperation.execute(match { it.fileId == id })
        }
    }

    @Test
    fun `should return 404 when file not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            deleteFileOperation.execute(any())
        } returns DeleteFileOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/files/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
