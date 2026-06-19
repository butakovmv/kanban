package com.kanban.postgres.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import com.kanban.document.DocumentRepository
import com.kanban.postgres.project.ProjectGenerator
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
internal class DocumentRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var documentGenerator: DocumentGenerator
    private lateinit var documentRepository: DocumentRepository

    private lateinit var projectId: String
    private lateinit var otherProjectId: String

    @BeforeEach
    fun setUp() =
        runTest {
            projectGenerator = ProjectGenerator(db)
            documentGenerator = DocumentGenerator(db)
            documentRepository = DocumentRepositoryImpl(db)
            projectId = projectGenerator.createAndInsert()
            otherProjectId = projectGenerator.createAndInsert()
        }

    @AfterEach
    fun tearDown() =
        runTest {
            documentGenerator.deleteAll()
            projectGenerator.deleteAll()
        }

    @Test
    fun `should save new document and find by id`() =
        runTest {
            val now = Instant.now()
            val document =
                Document(
                    id = DocumentId("new-doc-id"),
                    projectId = ProjectId(projectId),
                    title = "Specification",
                    description = "Project spec",
                    fileName = "spec.pdf",
                    contentType = "application/pdf",
                    sizeBytes = 4096L,
                    storageKey = "projects/$projectId/new-doc-id/spec.pdf",
                    version = 1,
                    uploadedBy = "user-1",
                    createdAt = now,
                    updatedAt = now,
                )

            val saved = documentRepository.save(document)

            assertEquals("new-doc-id", saved.id.value)
            val found = documentRepository.findById("new-doc-id")
            assertNotNull(found)
            assertEquals("new-doc-id", found.id.value)
            assertEquals(projectId, found.projectId.value)
            assertEquals("Specification", found.title)
            assertEquals("Project spec", found.description)
            assertEquals("spec.pdf", found.fileName)
            assertEquals("application/pdf", found.contentType)
            assertEquals(4096L, found.sizeBytes)
            assertEquals(1, found.version)
            assertEquals("user-1", found.uploadedBy)
        }

    @Test
    fun `should save document with null description`() =
        runTest {
            val now = Instant.now()
            val document =
                Document(
                    id = DocumentId("null-desc-id"),
                    projectId = ProjectId(projectId),
                    title = "No description",
                    description = null,
                    fileName = "file.txt",
                    contentType = "text/plain",
                    sizeBytes = 1024L,
                    storageKey = "projects/$projectId/null-desc-id/file.txt",
                    version = 1,
                    uploadedBy = "user-1",
                    createdAt = now,
                    updatedAt = now,
                )

            documentRepository.save(document)

            val found = documentRepository.findById("null-desc-id")
            assertNotNull(found)
            assertNull(found.description)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = documentRepository.findById("unknown-doc-id")
            assertNull(found)
        }

    @Test
    fun `should update existing document`() =
        runTest {
            val docId =
                documentGenerator.createAndInsert(
                    DocumentSpec(
                        projectId = projectId,
                        title = "Old title",
                        fileName = "old.pdf",
                        sizeBytes = 100L,
                        version = 1,
                    ),
                )
            val existing = documentRepository.findById(docId)!!
            val updated =
                existing.copy(
                    title = "New title",
                    description = "Added description",
                    fileName = "new.pdf",
                    contentType = "application/pdf",
                    sizeBytes = 200L,
                    version = 2,
                )

            val saved = documentRepository.save(updated)

            assertEquals("New title", saved.title)
            val reloaded = documentRepository.findById(docId)
            assertNotNull(reloaded)
            assertEquals("New title", reloaded.title)
            assertEquals("Added description", reloaded.description)
            assertEquals("new.pdf", reloaded.fileName)
            assertEquals(200L, reloaded.sizeBytes)
            assertEquals(2, reloaded.version)
        }

    @Test
    fun `should clear nullable description on update`() =
        runTest {
            val docId =
                documentGenerator.createAndInsert(
                    DocumentSpec(
                        projectId = projectId,
                        description = "Old description",
                    ),
                )
            val existing = documentRepository.findById(docId)!!
            val updated = existing.copy(description = null)

            documentRepository.save(updated)

            val reloaded = documentRepository.findById(docId)
            assertNotNull(reloaded)
            assertNull(reloaded.description)
        }

    @Test
    fun `should list documents by project id ordered by updated_at desc`() =
        runTest {
            val baseTime = LocalDateTime.now(ZoneId.systemDefault())
            val firstId =
                documentGenerator.createAndInsert(
                    DocumentSpec(
                        projectId = projectId,
                        title = "First",
                        createdAt = baseTime,
                        updatedAt = baseTime,
                    ),
                )
            val secondId =
                documentGenerator.createAndInsert(
                    DocumentSpec(
                        projectId = projectId,
                        title = "Second",
                        createdAt = baseTime,
                        updatedAt = baseTime.plusMinutes(1),
                    ),
                )
            val thirdId =
                documentGenerator.createAndInsert(
                    DocumentSpec(
                        projectId = projectId,
                        title = "Third",
                        createdAt = baseTime,
                        updatedAt = baseTime.plusMinutes(2),
                    ),
                )
            documentGenerator.createAndInsert(
                DocumentSpec(
                    projectId = otherProjectId,
                    title = "Other project",
                    createdAt = baseTime.plusMinutes(3),
                    updatedAt = baseTime.plusMinutes(3),
                ),
            )

            val list = documentRepository.listByProjectId(projectId)

            assertEquals(3, list.size)
            assertEquals(listOf(thirdId, secondId, firstId), list.map { it.id.value })
            assertEquals(listOf("Third", "Second", "First"), list.map { it.title })
        }

    @Test
    fun `should return empty list when project has no documents`() =
        runTest {
            val list = documentRepository.listByProjectId(projectId)
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should not include documents from other projects`() =
        runTest {
            documentGenerator.createAndInsert(DocumentSpec(projectId = projectId, title = "Mine"))
            documentGenerator.createAndInsert(DocumentSpec(projectId = otherProjectId, title = "Other"))

            val list = documentRepository.listByProjectId(projectId)

            assertEquals(1, list.size)
            assertEquals("Mine", list.first().title)
        }

    @Test
    fun `should delete document by id`() =
        runTest {
            val docId = documentGenerator.createAndInsert(DocumentSpec(projectId = projectId))
            assertNotNull(documentRepository.findById(docId))

            documentRepository.delete(docId)

            assertNull(documentRepository.findById(docId))
        }

    @Test
    fun `should preserve large size bytes`() =
        runTest {
            val largeSize = 5_000_000_000L
            val docId =
                documentGenerator.createAndInsert(
                    DocumentSpec(projectId = projectId, fileName = "big.bin", sizeBytes = largeSize),
                )

            val found = documentRepository.findById(docId)
            assertNotNull(found)
            assertEquals(largeSize, found.sizeBytes)
        }

    @Test
    fun `should roundtrip timestamps through database`() =
        runTest {
            val nowLdt = LocalDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MICROS)
            val docId =
                documentGenerator.createAndInsert(
                    DocumentSpec(projectId = projectId, createdAt = nowLdt, updatedAt = nowLdt),
                )

            val found = documentRepository.findById(docId)
            assertNotNull(found)
            assertEquals(nowLdt.atZone(ZoneId.systemDefault()).toInstant(), found.createdAt)
            assertEquals(nowLdt.atZone(ZoneId.systemDefault()).toInstant(), found.updatedAt)
        }
}
