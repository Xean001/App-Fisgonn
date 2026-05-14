package com.example.fisgon.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.repository.AuthRepository
import com.example.fisgon.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun togglePasswordVisibility() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onLogin() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(LoginCredentials(state.email, state.password))
                .onSuccess { session ->
                    val verificationCode = generateVerificationCode()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loggedUser = session.user,
                            generatedToken = verificationCode,
                            jwtToken = session.token
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onRegister() {
        _uiState.update { it.copy(navigateToRegister = true) }
    }

    fun onRegisterNavigated() {
        _uiState.update { it.copy(navigateToRegister = false) }
    }

    fun onGoogleLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.loginWithGoogle()
                .onSuccess { session ->
                    val verificationCode = generateVerificationCode()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loggedUser = session.user,
                            generatedToken = verificationCode,
                            jwtToken = session.token
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}

private fun generateVerificationCode(): String =
    Random.nextInt(100000, 999999).toString()
