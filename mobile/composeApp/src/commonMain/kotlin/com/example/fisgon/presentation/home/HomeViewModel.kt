package com.example.fisgon.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fisgon.data.remote.ApiConfig
import com.example.fisgon.data.remote.createHttpClient
import com.example.fisgon.domain.entity.User
import com.example.fisgon.presentation.map.IncidentMarker
import com.example.fisgon.shared.model.CategoryResponse
import com.example.fisgon.shared.model.ReportCreateRequest
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    val currentUser: User,
    private val token: String
) : ViewModel() {

    private val client = createHttpClient()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadCategories() }

    private fun loadCategories() {
        viewModelScope.launch {
            runCatching {
                client.get("${ApiConfig.BASE_URL}/categories").body<List<CategoryResponse>>()
            }.onSuccess { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun onTitleChange(v: String)       = _uiState.update { it.copy(formTitle = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(formDescription = v) }
    fun onCategoryMenuToggle()         = _uiState.update { it.copy(categoryMenuExpanded = !it.categoryMenuExpanded) }
    fun onCategoryMenuDismiss()        = _uiState.update { it.copy(categoryMenuExpanded = false) }

    fun onCategorySelected(cat: CategoryResponse) =
        _uiState.update { it.copy(selectedCategory = cat, categoryMenuExpanded = false) }

    fun onMarkLocation(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) {
            _uiState.update { it.copy(noGpsError = true) }
            return
        }
        val s = _uiState.value
        val title = s.formTitle.ifBlank { s.selectedCategory?.localizedName() ?: "Incidente" }

        // Añade marcador visual local
        _uiState.update {
            it.copy(markers = it.markers + IncidentMarker(latitude, longitude, title))
        }

        // Envía al backend
        viewModelScope.launch {
            runCatching {
                client.post("${ApiConfig.BASE_URL}/reports") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        ReportCreateRequest(
                            title       = s.formTitle.ifBlank { null },
                            description = s.formDescription.ifBlank { null },
                            categoryId  = s.selectedCategory?.id,
                            latitude    = latitude,
                            longitude   = longitude
                        )
                    )
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        formTitle = "", formDescription = "",
                        selectedCategory = null,
                        markerAdded = true, noGpsError = false, reportError = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(markerAdded = true, reportError = e.message)
                }
            }
        }
    }

    fun dismissMarkerAdded() = _uiState.update { it.copy(markerAdded = false, reportError = null) }
    fun dismissNoGps()       = _uiState.update { it.copy(noGpsError = false) }
}
