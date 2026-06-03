package com.sanosysalvos.user.client

import com.sanosysalvos.contracts.AuthResponse
import com.sanosysalvos.contracts.UserLoginRequest
import com.sanosysalvos.contracts.UserProfile
import com.sanosysalvos.contracts.UserRegistrationRequest
import com.sanosysalvos.contracts.UserRole
import com.sanosysalvos.common.PrefixedIdGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.postForObject
import org.slf4j.LoggerFactory

@Suppress("UNCHECKED_CAST")
@Component
class XanoUserClient(
    private val restTemplate: RestTemplate,
    @Value("\${xano.api.baseUrl:https://x8ki-letl-twmt.n7.xano.io}") val baseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(XanoUserClient::class.java)
    
    fun register(request: UserRegistrationRequest): AuthResponse {
        if (request.role == UserRole.COLLABORATOR && request.collaboratorType == null) {
            error("collaboratorType is required when role is COLLABORATOR")
        }

        val url = "$baseUrl/api:sanos-y-salvos-auth/register"
        val payload = mutableMapOf<String, Any?>()
        payload["fullName"] = request.fullName
        payload["email"] = request.email
        payload["phone"] = request.phone
        payload["password"] = request.password
        request.role?.let { payload["role"] = it.name }
        request.collaboratorType?.let { payload["collaboratorType"] = it.name }
        val response = restTemplate.postForObject<Map<String, Any>>(url, payload)
        return parseAuthResponse(response)
    }

    fun login(request: UserLoginRequest): AuthResponse {
        val url = "$baseUrl/api:sanos-y-salvos-auth/login"
        val response = restTemplate.postForObject<Map<String, Any>>(url, request)
        return parseAuthResponse(response)
    }

    fun getMe(token: String): UserProfile {
        val url = "$baseUrl/api:sanos-y-salvos-auth/me"
        val headers = HttpHeaders().apply {
            add("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(url, HttpMethod.GET, entity, Map::class.java).body
        @Suppress("UNCHECKED_CAST")
        val typedResponse = response as? Map<String, Any>
        return parseUserProfile(typedResponse)
    }

    fun getById(id: String, token: String): UserProfile {
        val url = "$baseUrl/api:sanos-y-salvos-auth/users/$id"
        val headers = HttpHeaders().apply {
            add("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(url, HttpMethod.GET, entity, Map::class.java).body
        @Suppress("UNCHECKED_CAST")
        val typedResponse = response as? Map<String, Any>
        return parseUserProfile(typedResponse)
    }

    fun listUsers(token: String): List<UserProfile> {
        val headers = HttpHeaders().apply {
            add("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity<Void>(headers)

        val candidateUrls = listOf(
            "$baseUrl/api:sanos-y-salvos-auth/users/list",
            "$baseUrl/api:sanos-y-salvos-auth/users",
        )

        candidateUrls.forEach { url ->
            try {
                val response = restTemplate.exchange<Any>(url, HttpMethod.GET, entity).body
                val users = parseUsersList(response)
                if (users.isNotEmpty()) {
                    return users
                }
            } catch (e: Exception) {
                logger.warn("listUsers attempt failed for {}: {}", url, e.message)
            }
        }

        logger.warn("Could not fetch users from Xano using known endpoints; returning empty list")
        return emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseUsersList(response: Any?): List<UserProfile> {
        val listData = when (response) {
            is List<*> -> response.filterIsInstance<Map<String, Any>>()
            is Map<*, *> -> {
                val data = response["data"]
                if (data is List<*>) data.filterIsInstance<Map<String, Any>>() else emptyList()
            }
            else -> emptyList()
        }

        return listData.map { parseUserProfile(it) }
    }

    fun updateUser(id: String, profile: UserProfile, token: String): UserProfile {
        val url = "$baseUrl/api:sanos-y-salvos-auth/users/$id"
        val headers = HttpHeaders().apply {
            add("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(profile, headers)
        val response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map::class.java).body
        @Suppress("UNCHECKED_CAST")
        val typedResponse = response as? Map<String, Any>
        return parseUserProfile(typedResponse)
    }

    fun deleteUser(id: String, token: String): String {
        val url = "$baseUrl/api:sanos-y-salvos-auth/users/$id"
        val headers = HttpHeaders().apply {
            add("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity<Void>(headers)
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void::class.java)
        return id
    }

    fun refresh(refreshToken: String): AuthResponse {
        val url = "$baseUrl/api:sanos-y-salvos-auth/refresh"
        val body = mapOf("refreshToken" to refreshToken)
        val response = restTemplate.postForObject<Map<String, Any>>(url, body)
        return parseAuthResponse(response)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseAuthResponse(response: Map<String, Any>?): AuthResponse {
        val data = (response?.get("data") as? Map<String, Any>) ?: response ?: emptyMap()
        return AuthResponse(
            userId = data["userId"]?.toString() ?: data["uid"]?.toString() ?: data["id"]?.toString() ?: "",
            role = UserRole.valueOf((data["role"]?.toString() ?: "USER").uppercase()),
            token = data["token"]?.toString() ?: "",
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseUserProfile(response: Map<String, Any>?): UserProfile {
        val data = (response?.get("data") as? Map<String, Any>) ?: response ?: emptyMap()
        return UserProfile(
            id = data["id"]?.toString() ?: "",
            fullName = data["fullName"]?.toString() ?: "",
            email = data["email"]?.toString() ?: "",
            phone = data["phone"]?.toString(),
            role = UserRole.valueOf((data["role"]?.toString() ?: "USER").uppercase()),
        )
    }
}
