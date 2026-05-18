package com.example.fisgon.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MapLibreMap(
    modifier: Modifier = Modifier,
    latitude: Double? = null,
    longitude: Double? = null,
    markers: List<IncidentMarker> = emptyList()
)
