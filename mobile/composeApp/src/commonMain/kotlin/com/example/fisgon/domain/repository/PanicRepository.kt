package com.example.fisgon.domain.repository

import com.example.fisgon.domain.entity.PanicAlert
import kotlinx.coroutines.flow.SharedFlow

interface PanicRepository {
    val alerts: SharedFlow<PanicAlert>
    suspend fun connect(token: String)
    suspend fun sendPanic(latitude: Double, longitude: Double)
    suspend fun updateLocation(latitude: Double, longitude: Double)
    suspend fun disconnect()
}
