package com.kanban.postgres.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import com.kanban.identity.User
import java.time.ZoneId

internal fun UserTable.toDomain(): User =
    User(
        id = UserId(id),
        email = Email(email),
        passwordHash = PasswordHash(passwordHash),
        displayName = displayName,
        totpSecret = totpSecret,
        totpEnabled = totpEnabled,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

internal fun User.toTable(): UserTable =
    UserTable(
        id = id.value,
        email = email.value,
        passwordHash = passwordHash.value,
        displayName = displayName,
        totpSecret = totpSecret,
        totpEnabled = totpEnabled,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
