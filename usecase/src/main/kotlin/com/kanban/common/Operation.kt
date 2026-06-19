package com.kanban.common

interface Operation<in A, out R> {
    suspend fun execute(arg: A): R
}
