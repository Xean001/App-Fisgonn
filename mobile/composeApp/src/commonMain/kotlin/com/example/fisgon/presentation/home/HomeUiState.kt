package com.example.fisgon.presentation.home

import com.example.fisgon.presentation.map.IncidentMarker
import com.example.fisgon.shared.model.CategoryResponse

data class HomeUiState(
    val isLoading: Boolean = false,
    val formTitle: String = "",
    val formDescription: String = "",
    val categories: List<CategoryResponse> = emptyList(),
    val selectedCategory: CategoryResponse? = null,
    val categoryMenuExpanded: Boolean = false,
    val markers: List<IncidentMarker> = emptyList(),
    val noGpsError: Boolean = false,
    val markerAdded: Boolean = false,
    val reportError: String? = null
)
