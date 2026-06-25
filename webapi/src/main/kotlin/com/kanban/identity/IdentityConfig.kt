package com.kanban.identity

import com.kanban.audit.LogAuditEventOperation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация доменных обработчиков auth.
 * Регистрирует [AuthHandler] как Spring-бин, связывая его с usecase-операциями.
 */
@Configuration
internal class IdentityConfig {
    /**
     * Создаёт обработчик auth-запросов.
     *
     * @param registerUserOperation операция регистрации
     * @param loginWithPasswordOperation операция входа
     * @param refreshTokenOperation операция обновления токена
     * @param logoutOperation операция выхода
     * @return экземпляр [AuthHandler]
     */
    @Bean
    fun authHandler(
        registerUserOperation: RegisterUserOperation,
        loginWithPasswordOperation: LoginWithPasswordOperation,
        refreshTokenOperation: RefreshTokenOperation,
        logoutOperation: LogoutOperation,
        logAuditEventOperation: LogAuditEventOperation,
    ): AuthHandler =
        AuthHandler(
            registerUserOperation = registerUserOperation,
            loginWithPasswordOperation = loginWithPasswordOperation,
            refreshTokenOperation = refreshTokenOperation,
            logoutOperation = logoutOperation,
            logAuditEventOperation = logAuditEventOperation,
        )

    /**
     * Создаёт обработчик запросов восстановления пароля.
     *
     * @param recoveryOperation операция восстановления
     * @return экземпляр [RecoveryHandler]
     */
    @Bean
    fun recoveryHandler(recoveryOperation: RecoveryOperation): RecoveryHandler =
        RecoveryHandler(
            recoveryOperation = recoveryOperation,
        )

    @Bean
    fun profileHandler(getCurrentUserTariffOperation: GetCurrentUserTariffOperation): ProfileHandler =
        ProfileHandler(
            getCurrentUserTariffOperation = getCurrentUserTariffOperation,
        )

    @Bean
    fun userHandler(findUsersOperation: FindUsersOperation): UserHandler = UserHandler(findUsersOperation = findUsersOperation)
}
