package com.sanosysalvos.user.api

import com.sanosysalvos.contracts.ApiEnvelope
import com.sanosysalvos.contracts.AuthResponse
import com.sanosysalvos.contracts.DeviceTokenRequest
import com.sanosysalvos.contracts.UserLoginRequest
import com.sanosysalvos.contracts.UserRegistrationRequest
import com.sanosysalvos.contracts.UserRole
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserHealthController {

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf(
        "service" to "user-service",
        "status" to "up",
    )

    @PostMapping("/register")
    fun register(@RequestBody request: UserRegistrationRequest): ApiEnvelope<AuthResponse> = ApiEnvelope(
        success = true,
        message = "User registered",
        data = AuthResponse(
            userId = request.email,
            role = UserRole.USER,
            token = "token-user-${request.email}",
        ),
    )

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequest): ApiEnvelope<AuthResponse> = ApiEnvelope(
        success = true,
        message = "User authenticated",
        data = AuthResponse(
            userId = request.email,
            role = UserRole.USER,
            token = "token-user-${request.email}",
        ),
    )

    @PutMapping("/{userId}/role/{role}")
    fun assignRole(
        @PathVariable userId: String,
        @PathVariable role: UserRole,
    ): ApiEnvelope<String> = ApiEnvelope(
        success = true,
        message = "Role $role assigned",
        data = userId,
    )

    @PostMapping("/device-token")
    fun registerDeviceToken(@RequestBody request: DeviceTokenRequest): ApiEnvelope<DeviceTokenRequest> = ApiEnvelope(
        success = true,
        message = "Device token stored",
        data = request,
    )
}
