package com.sanosysalvos.bff.controller

// AUTH ROLE: gateway-owner
// DELEGATES TO: Xano directly
// STATUS: active
// NOTE: BFF auth gateway does not delegate auth to user-service.

import com.sanosysalvos.bff.client.XanoAuthClient
import com.sanosysalvos.contracts.ApiEnvelope
import com.sanosysalvos.contracts.AuthResponse
import com.sanosysalvos.contracts.UserLoginRequest
import com.sanosysalvos.contracts.UserRegistrationRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bff/auth")
class AuthGatewayController(
    private val xanoAuthClient: XanoAuthClient,
) {

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
}