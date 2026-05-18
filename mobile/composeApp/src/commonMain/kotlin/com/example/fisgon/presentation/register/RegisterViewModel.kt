package com.example.fisgon.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fisgon.domain.entity.LoginCredentials
import com.example.fisgon.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) = _uiState.update { it.copy(nombre = value, errorMessage = null) }
    fun onApellidoChange(value: String) = _uiState.update { it.copy(apellido = value, errorMessage = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun toggleConfirmPasswordVisibility() = _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

    fun onRegister() {
        val state = _uiState.value
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        when {
            state.nombre.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "El nombre es requerido") }
                return
            }
            state.email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "El correo es requerido") }
                return
            }
            !emailRegex.matches(state.email.trim()) -> {
                _uiState.update { it.copy(errorMessage = "Ingresa un correo válido (ej: usuario@dominio.com)") }
                return
            }
            state.password.length < 8 -> {
                _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres") }
                return
            }
            !state.password.any { it.isLetter() } || !state.password.any { it.isDigit() } -> {
                _uiState.update { it.copy(errorMessage = "La contraseña debe incluir al menos una letra y un número") }
                return
            }
            state.password != state.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(
                LoginCredentials(
                    email = state.email.trim(),
                    password = state.password,
                    nombre = state.nombre.trim(),
                    apellido = state.apellido.trim().ifBlank { null }
                )
            )
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registeredUser = session.user,
                            generatedToken = Random.nextInt(100000, 999999).toString(),
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
