package com.kanban.search

interface SearchRepository {
    suspend fun search(criteria: SearchCriteria): List<SearchResult>

    suspend fun count(criteria: SearchCriteria): Long
}
