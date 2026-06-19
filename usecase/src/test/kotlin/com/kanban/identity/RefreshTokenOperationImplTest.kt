package com.kanban.identity

import com.kanban.common.AccessToken
import com.kanban.common.AuthTokens
import com.kanban.common.RefreshToken
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RefreshTokenOperationImplTest {
    private val tokenProvider = mockk<TokenProvider>()
    private val operation = RefreshTokenOperationImpl(tokenProvider)

    @Test
    fun `should refresh tokens successfully`() =
        runTest {
            val refreshToken = "valid-refresh-token"
            val newTokens =
                AuthTokens(
                    AccessToken("new-access"),
                    RefreshToken("new-refresh"),
                )
            coEvery { tokenProvider.refreshAccessToken(refreshToken) } returns newTokens

            val result = operation.execute(RefreshTokenOperation.Arg(refreshToken))

            val success = assertIs<RefreshTokenOperation.Result.Success>(result)
            assertEquals("new-access", success.tokens.accessToken.value)
            assertEquals("new-refresh", success.tokens.refreshToken.value)

            coVerify { tokenProvider.refreshAccessToken(refreshToken) }
        }

    @Test
    fun `should fail with invalid refresh token`() =
        runTest {
            val refreshToken = "invalid-refresh-token"
            coEvery { tokenProvider.refreshAccessToken(refreshToken) } returns null

            val result = operation.execute(RefreshTokenOperation.Arg(refreshToken))

            val failure = assertIs<RefreshTokenOperation.Result.Failure>(result)
            assertEquals("Invalid refresh token", failure.reason)

            coVerify { tokenProvider.refreshAccessToken(refreshToken) }
        }
}
