package com.kanban.task

import com.kanban.common.TestFactory

internal object TaskGenerator {
    fun title() = TestFactory.string("task")

    fun description() = TestFactory.string("desc")

    fun commentText() = TestFactory.string("comment")
}
