package com.example.fisgon.data.repository

import com.example.fisgon.data.remote.ApiConfig
import com.example.fisgon.domain.entity.PanicAlert
import com.example.fisgon.domain.repository.PanicRepository
import com.example.fisgon.shared.model.WsMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PanicRepositoryImpl : PanicRepository {

    private val client = HttpClient(Darwin) { install(WebSockets) }
    private val json = Json { ignoreUnknownKeys = true }

    private val _alerts = MutableSharedFlow<PanicAlert>(extraBufferCapacity = 16)
    override val alerts: SharedFlow<PanicAlert> = _alerts.asSharedFlow()

    // MutableSharedFlow works on K/N unlike Channel receive APIs
    private val sendBus = MutableSharedFlow<String>(extraBufferCapacity = 64)

    override suspend fun connect(token: String) {
        client.webSocket(
            urlString = "${ApiConfig.WS_URL}/ws/panic",
            request = { url.parameters.append("token", token) }
        ) {
            val sender = launch {
                sendBus.collect { msg ->
                    runCatching { send(msg) }
                }
            }
            try {
                for (frame in incoming) {
                    if (frame !is Frame.Text) continue
                    val msg = runCatching {
                        json.decodeFromString<WsMessage>(frame.readText())
                    }.getOrNull() ?: continue
                    if (msg.type == "panic_alert") {
                        val uid = msg.userId ?: continue
                        _alerts.emit(
                            PanicAlert(
                                userId = uid,
                                latitude = msg.latitude,
                                longitude = msg.longitude,
                                timestamp = msg.timestamp ?: ""
                            )
                        )
                    }
                }
            } finally {
                sender.cancel()
            }
        }
    }

    override suspend fun sendPanic(latitude: Double, longitude: Double) {
        sendBus.tryEmit(json.encodeToString(WsMessage("panic", latitude, longitude)))
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double) {
        sendBus.tryEmit(json.encodeToString(WsMessage("location_update", latitude, longitude)))
    }

    override suspend fun disconnect() {
        client.close()
    }
}
