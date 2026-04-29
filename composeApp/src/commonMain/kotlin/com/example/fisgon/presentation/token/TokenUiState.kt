package com.example.fisgon.presentation.token

data class TokenUiState(
    val enteredToken: String = "",
    val isError: Boolean = false,
    val isVerified: Boolean = false
)
