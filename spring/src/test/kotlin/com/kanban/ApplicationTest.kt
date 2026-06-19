package com.kanban

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ApplicationTest {
    /**
     * Проверяет корректность поднятия всего Spring-контекста.
     *
     * Тест необходим в таком виде, так как аннотация @SpringBootTest создает весь Spring-контекст приложения
     * для интеграционного тестирования и позволяет проверить работу всех бинов, настроек компонентов
     * и элементов приложения в реалистичных условиях.
     */
    @Test
    fun contextLoads() = Unit
}
