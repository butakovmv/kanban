package com.kanban.search

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class SearchConfig {
    @Bean
    @Suppress("MaxLineLength")
    fun searchHandler(searchOperation: SearchOperation): SearchHandler = SearchHandler(searchOperation = searchOperation)
}
