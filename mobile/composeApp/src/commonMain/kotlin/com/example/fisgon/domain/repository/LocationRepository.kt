package com.example.fisgon.domain.repository

import com.example.fisgon.domain.entity.LocationData
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val location: StateFlow<LocationData?>
    fun startTracking()
    fun stopTracking()
}
