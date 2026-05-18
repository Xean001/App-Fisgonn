package com.example.fisgon.domain.entity

data class User(
    val id: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val anonymousUsername: String = ""
)
