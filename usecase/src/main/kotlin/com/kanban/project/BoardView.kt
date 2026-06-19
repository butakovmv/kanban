package com.kanban.project

/**
 * DTO представления доски: доска вместе с упорядоченным списком её колонок.
 * Используется в операциях, возвращающих полное состояние доски.
 *
 * @property board сущность доски
 * @property columns список колонок доски, упорядоченный по позиции
 */
data class BoardView(
    val board: Board,
    val columns: List<Column>,
)
