package com.example.fisgon.presentation.home

import com.example.fisgon.domain.entity.Product

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val formCodigo: String = "",
    val formDescripcion: String = "",
    val formPrecio: String = "",
    val formMarca: String = "",
    val selectedProduct: Product? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
    // Panic / WS state (delegated to PanicViewModel, mirrored here for HomeScreen)
    val panicAlertVisible: Boolean = false,
    val panicAlertLat: Double = 0.0,
    val panicAlertLon: Double = 0.0,
    val wsConnected: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null
)
