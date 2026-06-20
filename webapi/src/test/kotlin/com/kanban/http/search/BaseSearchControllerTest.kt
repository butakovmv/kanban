package com.kanban.http.search

import com.kanban.search.SearchHandler
import com.kanban.search.SearchOperation
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient

internal abstract class BaseSearchControllerTest {
    protected lateinit var searchOperation: SearchOperation

    protected fun bindTo(controllerClass: Class<*>): WebTestClient {
        searchOperation = mockk()

        val searchHandler = SearchHandler(searchOperation = searchOperation)

        val controller =
            when (controllerClass) {
                SearchController::class.java -> SearchController(searchHandler)
                else -> throw IllegalArgumentException("Unsupported controller: $controllerClass")
            }

        return WebTestClient.bindToController(controller).build()
    }
}
