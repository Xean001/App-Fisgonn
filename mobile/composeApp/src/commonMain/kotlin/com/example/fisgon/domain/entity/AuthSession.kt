package com.example.fisgon.domain.entity

data class AuthSession(
    val user: User,
    val token: String
)
