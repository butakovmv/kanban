package com.kanban

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Главный класс Spring Boot приложения.
 * Содержит точку входа и конфигурацию авто-сканирования компонентов.
 */
@SpringBootApplication
class Application

/**
 * Точка входа в приложение.
 * Запускает Spring Boot контекст с переданными аргументами командной строки.
 *
 * @param args аргументы командной строки
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
