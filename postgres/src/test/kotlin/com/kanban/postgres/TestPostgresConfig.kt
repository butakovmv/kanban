package com.kanban.postgres

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@SpringBootConfiguration
@EnableAutoConfiguration
class TestPostgresConfig {
    @Bean
    fun schemaInitializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        initializer.setDatabasePopulator(
            ResourceDatabasePopulator(ClassPathResource("db/schema-h2.sql")),
        )
        return initializer
    }
}
