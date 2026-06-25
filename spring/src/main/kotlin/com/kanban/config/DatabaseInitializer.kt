package com.kanban.config

import io.r2dbc.spi.ConnectionFactory
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DatabaseInitializer(
    private val connectionFactory: ConnectionFactory
) {
    @PostConstruct
    fun initialize() {
        val populator = ResourceDatabasePopulator()
        populator.addScript(ClassPathResource("schema.sql"))
        populator.setContinueOnError(true)
        
        Mono.from(connectionFactory.create())
            .flatMap { connection ->
                populator.populate(connection)
                    .then(Mono.from(connection.close()))
            }
            .block()
    }
}
