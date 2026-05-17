package com.example.fisgon.presentation.register

import com.example.fisgon.domain.entity.User

data class RegisterUiState(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registeredUser: User? = null,
    val generatedToken: String? = null,
    val jwtToken: String? = null
)
