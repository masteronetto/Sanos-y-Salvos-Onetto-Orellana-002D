package com.sanosysalvos.bff.client

import com.sanosysalvos.bff.config.UserServiceClientProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class UserServiceClient(
    restClientBuilder: RestClient.Builder,
    userServiceClientProperties: UserServiceClientProperties,
) {
    private val restClient = restClientBuilder
        .baseUrl(userServiceClientProperties.baseUrl)
        .build()

    fun listUsers(authHeader: String): List<Map<String, Any?>> = restClient.get()
        .uri("/api/v1/users/admin/list")
        .header("Authorization", authHeader)
        .retrieve()
        .body(object : ParameterizedTypeReference<List<Map<String, Any?>>>() {})
        ?: emptyList()
}