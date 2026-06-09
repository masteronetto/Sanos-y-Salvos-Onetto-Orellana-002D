package com.sanosysalvos.user.api

// AUTH ROLE: auth-owner
// CALLS XANO FOR AUTH: yes
// STATUS: redundant
// NOTE: This controller exposes active user-service auth endpoints that call XanoAuthClient directly.
// SAFE TO REMOVE: only after UserHealthController auth endpoints are deleted
//                 and confirmed no traffic in staging/prod.

import com.sanosysalvos.user.client.XanoUserClient
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserHealthController(
    private val xanoUserClient: XanoUserClient,
) {

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf(
        "service" to "user-service",
        "status" to "up",
    )

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
