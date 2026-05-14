package com.example.fisgon.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenPayload(
    val userId: String,
    val issuedAt: Long,
    val expiresAt: Long
)
