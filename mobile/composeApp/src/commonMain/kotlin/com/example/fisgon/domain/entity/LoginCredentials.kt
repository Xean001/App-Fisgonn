package com.example.fisgon.domain.entity

data class LoginCredentials(
    val email: String,
    val password: String,
    val nombre: String? = null,
    val apellido: String? = null
)
