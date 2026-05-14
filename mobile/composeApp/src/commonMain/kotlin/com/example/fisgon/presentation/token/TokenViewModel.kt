package com.example.fisgon.presentation.token

import androidx.lifecycle.ViewModel
import com.example.fisgon.domain.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TokenViewModel(
    val user: User,
    val generatedToken: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TokenUiState())
    val uiState: StateFlow<TokenUiState> = _uiState.asStateFlow()

    fun onTokenChange(value: String) {
        _uiState.update { it.copy(enteredToken = value.take(6), isError = false) }
    }

    fun verify() {
        if (_uiState.value.enteredToken == generatedToken) {
            _uiState.update { it.copy(isVerified = true) }
        } else {
            _uiState.update { it.copy(isError = true) }
        }
    }
}
