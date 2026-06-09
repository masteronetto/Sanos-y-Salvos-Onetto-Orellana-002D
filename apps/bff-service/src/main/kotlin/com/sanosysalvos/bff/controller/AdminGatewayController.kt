package com.sanosysalvos.bff.controller

import com.sanosysalvos.bff.client.UserServiceClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bff/users")
class AdminGatewayController(
    private val userServiceClient: UserServiceClient,
) {

    @GetMapping("/admin/list")
    fun listUsersForAdmin(
        @RequestHeader("Authorization", required = false) authHeader: String?,
    ): List<Map<String, Any?>> {
        val token = authHeader
            ?.removePrefix("Bearer")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Authorization header requerido")

        return userServiceClient.listUsers("Bearer $token")
    }
}