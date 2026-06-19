package com.kanban

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/**
 * Проверяет корректность поднятия всего Spring-контекста приложения.
 *
 * Тест необходим в таком виде, так как аннотация @SpringBootTest создает весь Spring-контекст приложения
 * для интеграционного тестирования и позволяет проверить работу всех бинов, настроек компонентов
 * и элементов приложения в реалистичных условиях.
 *
 * На данный момент тест отключен (@Disabled), потому что для поднятия полного контекста необходимы
 * реализации usecase-операций (RegisterUserOperation, LoginWithPasswordOperation и т.д.),
 * которые зависят от production-реализаций репозиториев, JWT-провайдера и других компонентов,
 * ещё не реализованных в проекте. Тест будет включен после завершения соответствующих фаз TODO.md.
 */
@SpringBootTest
@Disabled("Требуются production-реализации зависимостей: репозитории, JWT, PasswordHasher")
class ApplicationTest {
    @Test
    fun contextLoads() = Unit
}
