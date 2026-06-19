package com.kanban.identity

import com.kanban.common.TestFactory

internal object UserGenerator {
    fun email() = TestFactory.email()

    fun password() = TestFactory.string("pwd")

    fun displayName() = TestFactory.string("name")
}
