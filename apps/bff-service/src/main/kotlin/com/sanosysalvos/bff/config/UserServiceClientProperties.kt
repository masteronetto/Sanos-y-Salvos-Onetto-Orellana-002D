package com.sanosysalvos.bff.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.user-service")
data class UserServiceClientProperties(
    val baseUrl: String,
)