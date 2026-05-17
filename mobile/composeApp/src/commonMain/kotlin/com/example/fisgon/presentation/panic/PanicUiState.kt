package com.example.fisgon.presentation.panic

import com.example.fisgon.domain.entity.LocationData
import com.example.fisgon.domain.entity.PanicAlert

data class PanicUiState(
    val isConnected: Boolean = false,
    val isSendingPanic: Boolean = false,
    val currentLocation: LocationData? = null,
    val incomingAlert: PanicAlert? = null,
    val panicSentConfirmation: Boolean = false,
    val noLocationError: Boolean = false,
    val error: String? = null
)
