package com.sanosysalvos.bff.controller

import com.sanosysalvos.bff.client.XanoAuthClient
import com.sanosysalvos.contracts.ApiEnvelope
import com.sanosysalvos.contracts.AuthResponse
import com.sanosysalvos.contracts.UserLoginRequest
import com.sanosysalvos.contracts.UserRegistrationRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthGatewayControllerTest {
    private val xanoAuthClient = mockk<XanoAuthClient>()
    private val controller = AuthGatewayController(xanoAuthClient)

    @Test
    fun `register returns success envelope when xano register succeeds`() {
        val request = UserRegistrationRequest("Test", "test@example.com", "1234567890", "password")
        val authResponse = AuthResponse("id", com.sanosysalvos.contracts.UserRole.USER, "token")
        every { xanoAuthClient.register(request) } returns authResponse

        val result = controller.register(request)

        assertEquals(true, result.success)
        assertEquals(authResponse, result.data)
        assertEquals("User registered", result.message)
    }

    @Test
    fun `login returns failure envelope when xano login throws`() {
        val request = UserLoginRequest("test@example.com", "password")
        every { xanoAuthClient.login(request) } throws RuntimeException("invalid")

        val result = controller.login(request)

        assertEquals(false, result.success)
        assertEquals(null, result.data)
        assertEquals("Authentication failed: invalid", result.message)
    }
}
