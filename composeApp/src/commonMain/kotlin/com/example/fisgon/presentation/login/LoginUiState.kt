package com.example.fisgon.presentation.login

import com.example.fisgon.domain.entity.User

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedUser: User? = null,
    val generatedToken: String? = null
)
