package com.example.fisgon.presentation.panic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fisgon.domain.repository.LocationRepository
import com.example.fisgon.domain.repository.PanicRepository
import com.example.fisgon.domain.usecase.SendPanicUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PanicViewModel(
    private val panicRepository: PanicRepository,
    private val locationRepository: LocationRepository,
    private val token: String
) : ViewModel() {

    private val sendPanicUseCase = SendPanicUseCase(panicRepository)

    private val _uiState = MutableStateFlow(PanicUiState())
    val uiState: StateFlow<PanicUiState> = _uiState.asStateFlow()

    private var wsJob: Job? = null

    init {
        locationRepository.startTracking()
        observeLocation()
        connectWebSocket()
    }

    private fun observeLocation() {
        viewModelScope.launch {
            locationRepository.location.collect { loc ->
                _uiState.update { it.copy(currentLocation = loc) }
                if (loc != null) {
                    panicRepository.updateLocation(loc.latitude, loc.longitude)
                }
            }
        }
    }

    private fun connectWebSocket() {
        wsJob = viewModelScope.launch {
            runCatching {
                _uiState.update { it.copy(isConnected = true) }
                panicRepository.connect(token)
            }.onFailure { e ->
                _uiState.update { it.copy(isConnected = false, error = e.message) }
            }
            _uiState.update { it.copy(isConnected = false) }
        }

        viewModelScope.launch {
            panicRepository.alerts.collect { alert ->
                _uiState.update { it.copy(incomingAlert = alert) }
            }
        }
    }

    fun onPanicButtonPressed() {
        val loc = _uiState.value.currentLocation
        if (loc == null) {
            _uiState.update { it.copy(noLocationError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingPanic = true) }
            runCatching { sendPanicUseCase(loc.latitude, loc.longitude) }
            _uiState.update { it.copy(isSendingPanic = false, panicSentConfirmation = true) }
        }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(incomingAlert = null) }
    }

    fun dismissPanicConfirmation() {
        _uiState.update { it.copy(panicSentConfirmation = false) }
    }

    fun dismissNoLocationError() {
        _uiState.update { it.copy(noLocationError = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        locationRepository.stopTracking()
        wsJob?.cancel()
        viewModelScope.launch { panicRepository.disconnect() }
    }
}
