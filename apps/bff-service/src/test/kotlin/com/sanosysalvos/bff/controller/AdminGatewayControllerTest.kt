package com.sanosysalvos.bff.controller

import com.sanosysalvos.bff.client.UserServiceClient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AdminGatewayControllerTest {
    private val userServiceClient = mockk<UserServiceClient>()
    private val controller = AdminGatewayController(userServiceClient)

    @Test
    fun `listUsersForAdmin returns user list when authorization header is valid`() {
        val payload = listOf(mapOf("id" to "1", "fullName" to "Test User"))
        every { userServiceClient.listUsers("Bearer token") } returns payload

        val result = controller.listUsersForAdmin("Bearer token")

        assertEquals(payload, result)
    }

    @Test
    fun `listUsersForAdmin throws when authorization header is missing`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.listUsersForAdmin(null)
        }
    }
}
