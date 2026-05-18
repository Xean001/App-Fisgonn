package com.example.fisgon.presentation.home

import com.example.fisgon.shared.model.CategoryResponse

internal fun CategoryResponse.localizedName(): String = when (name) {
    "Robbery" -> "Robo"
    "Harassment" -> "Acoso"
    "No lighting" -> "Falta de alumbrado"
    "Accident" -> "Accidente"
    "Danger zone" -> "Zona peligrosa"
    "Other" -> "Otro"
    else -> name
}
