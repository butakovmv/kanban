package com.kanban.document

interface DocumentRepository {
    suspend fun save(document: Document): Document

    suspend fun findById(id: String): Document?

    suspend fun listByProjectId(projectId: String): List<Document>

    suspend fun delete(id: String)
}
