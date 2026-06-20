package com.kanban.config

import com.kanban.common.AccessToken
import com.kanban.common.AuthTokens
import com.kanban.common.Operation
import com.kanban.common.RefreshToken
import com.kanban.document.DocumentStorage
import com.kanban.identity.EmailService
import com.kanban.identity.PasswordHasher
import com.kanban.identity.TokenProvider
import com.kanban.task.FileStorage
import java.security.MessageDigest
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component

@Component
internal class OperationBeanRegistrar : BeanDefinitionRegistryPostProcessor {
    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AssignableTypeFilter(Operation::class.java))

        for (candidate in scanner.findCandidateComponents("com.kanban")) {
            val beanClass = Class.forName(candidate.beanClassName)
            if (beanClass.simpleName.endsWith("Impl") && !beanClass.name.contains("test")) {
                val builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass)
                builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)
                registry.registerBeanDefinition(beanClass.name, builder.beanDefinition)
            }
        }
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) = Unit
}

@Configuration
internal class InfrastructureConfig {
    @Bean
    fun passwordHasher(): PasswordHasher =
        object : PasswordHasher {
            override fun hash(password: String): String {
                val digest = MessageDigest.getInstance("SHA-256")
                return digest.digest(password.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
            }

            override fun verify(
                password: String,
                hash: String,
            ): Boolean = hash(password) == hash
        }

    @Bean
    fun tokenProvider(): TokenProvider =
        object : TokenProvider {
            override suspend fun generateTokens(userId: String): AuthTokens =
                AuthTokens(
                    accessToken = AccessToken("access_$userId"),
                    refreshToken = RefreshToken("refresh_$userId"),
                )

            override suspend fun refreshAccessToken(refreshToken: String): AuthTokens? =
                if (refreshToken.startsWith("refresh_")) {
                    val userId = refreshToken.removePrefix("refresh_")
                    AuthTokens(
                        accessToken = AccessToken("access_$userId"),
                        refreshToken = RefreshToken("refresh_$userId"),
                    )
                } else {
                    null
                }
        }

    @Bean
    fun emailService(): EmailService =
        object : EmailService {
            override suspend fun sendRecoveryToken(
                to: String,
                recoveryToken: String,
            ) {
                println("EmailService: recovery token for $to: $recoveryToken")
            }
        }

    @Bean
    fun fileStorage(): FileStorage =
        object : FileStorage {
            override suspend fun upload(
                key: String,
                content: ByteArray,
                contentType: String,
            ): String {
                println("FileStorage: uploaded $key ($contentType, ${content.size} bytes)")
                return "/api/v1/files/$key/download"
            }

            override suspend fun getDownloadUrl(
                key: String,
                expiresIn: kotlin.time.Duration,
            ): String = "/api/v1/files/$key/download?expires=${expiresIn.inWholeSeconds}"

            override suspend fun delete(key: String) {
                println("FileStorage: deleted $key")
            }
        }

    @Bean
    fun documentStorage(): DocumentStorage =
        object : DocumentStorage {
            override suspend fun upload(
                key: String,
                content: ByteArray,
                contentType: String,
            ): String {
                println("DocumentStorage: uploaded $key ($contentType, ${content.size} bytes)")
                return "/api/v1/documents/$key/download"
            }

            override suspend fun getDownloadUrl(
                key: String,
                expiresIn: kotlin.time.Duration,
            ): String = "/api/v1/documents/$key/download?expires=${expiresIn.inWholeSeconds}"

            override suspend fun delete(key: String) {
                println("DocumentStorage: deleted $key")
            }
        }
}
