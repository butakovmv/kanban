package com.kanban.postgres

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing

/**
 * Конфигурация R2DBC для модуля Postgres.
 * Включает аудит R2DBC-сущностей с помощью @EnableR2dbcAuditing.
 */
@Configuration
@EnableR2dbcAuditing
class R2dbcConfig
