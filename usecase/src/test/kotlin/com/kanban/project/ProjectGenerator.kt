package com.kanban.project

import com.kanban.common.TestFactory

internal object ProjectGenerator {
    fun name() = TestFactory.string("project")

    fun columnName() = TestFactory.string("column")

    fun boardName() = TestFactory.string("board")
}
