package com.kanban.document

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.Project
import com.kanban.project.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateDocumentOperationImplTest {
    private val documentRepository = mockk<DocumentRepository>()
    private val documentStorage = mockk<DocumentStorage>()
    private val projectRepository = mockk<ProjectRepository>()
    private val operation =
        CreateDocumentOperationImpl(documentRepository, documentStorage, projectRepository)

    private val sampleProject =
        Project(
            id = ProjectId("project-1"),
            ownerId = UserId("user-1"),
            name = "Project",
            description = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should upload content and create document with version 1`() =
        runTest {
            val content = "hello".toByteArray()
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery { documentStorage.upload(any(), any(), any()) } answers { firstArg() }
            coEvery { documentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateDocumentOperation.Arg(
                        projectId = "project-1",
                        title = "  Spec  ",
                        description = "Spec description",
                        fileName = "spec.pdf",
                        contentType = "application/pdf",
                        content = content,
                        uploadedBy = "user-1",
                    ),
                )

            val success = assertIs<CreateDocumentOperation.Result.Success>(result)
            assertEquals("Spec", success.document.title)
            assertEquals("Spec description", success.document.description)
            assertEquals("spec.pdf", success.document.fileName)
            assertEquals("application/pdf", success.document.contentType)
            assertEquals(content.size.toLong(), success.document.sizeBytes)
            assertEquals("user-1", success.document.uploadedBy)
            assertEquals(ProjectId("project-1"), success.document.projectId)
            assertEquals(1, success.document.version)
            assert(success.document.storageKey.startsWith("projects/project-1/documents/"))
            assert(success.document.storageKey.endsWith("spec.pdf"))
            assertEquals(success.document.createdAt, success.document.updatedAt)

            coVerify { projectRepository.findById("project-1") }
            coVerify {
                documentStorage.upload(
                    key =
                        match {
                            it.startsWith("projects/project-1/documents/") && it.endsWith("spec.pdf")
                        },
                    content = content,
                    contentType = "application/pdf",
                )
            }
            coVerify { documentRepository.save(any()) }
        }

    @Test
    fun `should use storage key returned by DocumentStorage`() =
        runTest {
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery { documentStorage.upload(any(), any(), any()) } returns "bucket/path/file.bin"
            coEvery { documentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateDocumentOperation.Arg(
                        projectId = "project-1",
                        title = "Doc",
                        description = null,
                        fileName = "file.bin",
                        contentType = "application/octet-stream",
                        content = ByteArray(3),
                        uploadedBy = "user-1",
                    ),
                )

            val success = assertIs<CreateDocumentOperation.Result.Success>(result)
            assertEquals("bucket/path/file.bin", success.document.storageKey)
        }

    @Test
    fun `should fail when project not found`() =
        runTest {
            coEvery { projectRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    CreateDocumentOperation.Arg(
                        projectId = "missing",
                        title = "Doc",
                        description = null,
                        fileName = "doc.txt",
                        contentType = "text/plain",
                        content = ByteArray(1),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<CreateDocumentOperation.Result.Failure>(result)
            assertEquals("Project not found", failure.reason)

            coVerify { projectRepository.findById("missing") }
            coVerify(inverse = true) { documentStorage.upload(any(), any(), any()) }
            coVerify(inverse = true) { documentRepository.save(any()) }
        }

    @Test
    fun `should fail when title is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreateDocumentOperation.Arg(
                        projectId = "project-1",
                        title = "   ",
                        description = null,
                        fileName = "doc.txt",
                        contentType = "text/plain",
                        content = ByteArray(1),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<CreateDocumentOperation.Result.Failure>(result)
            assertEquals("Title must not be blank", failure.reason)

            coVerify(inverse = true) { projectRepository.findById(any()) }
            coVerify(inverse = true) { documentStorage.upload(any(), any(), any()) }
        }

    @Test
    fun `should fail when file name is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreateDocumentOperation.Arg(
                        projectId = "project-1",
                        title = "Doc",
                        description = null,
                        fileName = "  ",
                        contentType = "text/plain",
                        content = ByteArray(1),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<CreateDocumentOperation.Result.Failure>(result)
            assertEquals("File name must not be blank", failure.reason)
        }

    @Test
    fun `should fail when content type is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreateDocumentOperation.Arg(
                        projectId = "project-1",
                        title = "Doc",
                        description = null,
                        fileName = "doc.txt",
                        contentType = "  ",
                        content = ByteArray(1),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<CreateDocumentOperation.Result.Failure>(result)
            assertEquals("Content type must not be blank", failure.reason)
        }

    @Test
    fun `should fail when content is empty`() =
        runTest {
            val result =
                operation.execute(
                    CreateDocumentOperation.Arg(
                        projectId = "project-1",
                        title = "Doc",
                        description = null,
                        fileName = "doc.txt",
                        contentType = "text/plain",
                        content = ByteArray(0),
                        uploadedBy = "user-1",
                    ),
                )

            val failure = assertIs<CreateDocumentOperation.Result.Failure>(result)
            assertEquals("Content must not be empty", failure.reason)
        }
}
