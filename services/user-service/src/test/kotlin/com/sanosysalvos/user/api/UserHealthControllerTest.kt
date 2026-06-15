package com.sanosysalvos.user.api

import com.sanosysalvos.user.client.XanoUserClient
import com.sanosysalvos.contracts.UserProfile
import com.sanosysalvos.contracts.UserRole
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UserHealthControllerTest {
    private val xanoUserClient = mockk<XanoUserClient>()
    private val controller = UserHealthController(xanoUserClient)

    @Test
    fun `health returns service up status`() {
        assertEquals(mapOf("service" to "user-service", "status" to "up"), controller.health())
    }

    @Test
    fun `listUsersForAdmin returns users when authorized`() {
        val user = UserProfile("1", "Test User", "test@example.com", null, UserRole.ADMIN)
        every { xanoUserClient.listUsers("token") } returns listOf(user)

        val result = controller.listUsersForAdmin("Bearer token")

        assertEquals(1, result.size)
        assertEquals("Test User", result.first()["fullName"])
    }

    @Test
    fun `listUsersForAdmin throws when authorization header is missing`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.listUsersForAdmin(null)
        }
    }
}
