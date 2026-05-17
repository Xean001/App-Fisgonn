package com.example.fisgon.data.repository

import com.example.fisgon.domain.entity.PanicAlert
import com.example.fisgon.domain.repository.PanicRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NoopPanicRepository : PanicRepository {
    override val alerts: SharedFlow<PanicAlert> = MutableSharedFlow<PanicAlert>().asSharedFlow()
    override suspend fun connect(token: String) {}
    override suspend fun sendPanic(latitude: Double, longitude: Double) {}
    override suspend fun updateLocation(latitude: Double, longitude: Double) {}
    override suspend fun disconnect() {}
}
