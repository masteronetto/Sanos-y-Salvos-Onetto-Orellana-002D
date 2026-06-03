package com.sanosysalvos.user.api

import com.sanosysalvos.contracts.ApiEnvelope
import com.sanosysalvos.contracts.AuthResponse
import com.sanosysalvos.contracts.UserLoginRequest
import com.sanosysalvos.contracts.UserRegistrationRequest
import com.sanosysalvos.user.client.XanoAuthClient
import com.sanosysalvos.user.client.XanoUserClient
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserHealthController(
    private val xanoAuthClient: XanoAuthClient,
    private val xanoUserClient: XanoUserClient,
) {

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf(
        "service" to "user-service",
        "status" to "up",
    )

    @PostMapping("/register")
    fun register(@RequestBody request: UserRegistrationRequest): ApiEnvelope<AuthResponse> = try {
        val authResponse = xanoAuthClient.register(request)
        ApiEnvelope(
            success = true,
            message = "User registered",
            data = authResponse,
        )
    } catch (e: Exception) {
        ApiEnvelope(
            success = false,
            message = "Registration failed: ${e.message}",
            data = null,
        )
    }

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequest): ApiEnvelope<AuthResponse> = try {
        val authResponse = xanoAuthClient.login(request)
        ApiEnvelope(
            success = true,
            message = "User authenticated",
            data = authResponse,
        )
    } catch (e: Exception) {
        ApiEnvelope(
            success = false,
            message = "Authentication failed: ${e.message}",
            data = null,
        )
    }

    @GetMapping("/admin/list")
    fun listUsersForAdmin(
        @RequestHeader("Authorization", required = false) authHeader: String?,
    ): List<Map<String, Any>> {
        val token = authHeader
            ?.removePrefix("Bearer")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Authorization header requerido")

        return xanoUserClient.listUsers(token).map { user ->
            mapOf(
                "id" to user.id,
                "fullName" to user.fullName,
                "email" to user.email,
                "role" to user.role.name,
            )
        }
    }

}
