package com.example.fisgon.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nombre: String? = null,
    val apellido: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val nombre: String? = null,
    val apellido: String? = null,
    val anonymousUsername: String? = null
)

@Serializable
data class AuthResponse(
    val user: UserResponse,
    val token: String,
    val expiresAt: String
)

@Serializable
data class ReportCreateRequest(
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val severity: Int = 1
)

@Serializable
data class ReportResponse(
    val id: String,
    val userId: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val severity: Int,
    val createdAt: String
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

@Serializable
data class WsMessage(
    val type: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userId: String? = null,
    val timestamp: String? = null
)

@Serializable
data class GeofenceDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val severity: Int = 1
)

@Serializable
data class GeofenceCreateRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val severity: Int = 1
)
