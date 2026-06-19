package com.kanban.identity

/**
 * Провайдер хеширования паролей.
 * Предоставляет методы для хеширования пароля и проверки соответствия пароля хешу.
 */
interface PasswordHasher {
    /**
     * Хеширует пароль.
     *
     * @param password пароль в открытом виде
     * @return хеш пароля
     */
    fun hash(password: String): String

    /**
     * Проверяет соответствие пароля хешу.
     *
     * @param password пароль в открытом виде
     * @param hash хеш пароля
     * @return true, если пароль соответствует хешу
     */
    fun verify(
        password: String,
        hash: String,
    ): Boolean
}
