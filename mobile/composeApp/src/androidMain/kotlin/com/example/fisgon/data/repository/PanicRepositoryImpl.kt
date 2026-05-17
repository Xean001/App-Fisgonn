package com.example.fisgon.data.repository

import com.example.fisgon.data.remote.ApiConfig
import com.example.fisgon.domain.entity.PanicAlert
import com.example.fisgon.domain.repository.PanicRepository
import com.example.fisgon.shared.model.WsMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PanicRepositoryImpl : PanicRepository {

    private val client = HttpClient(CIO) { install(WebSockets) }
    private val json = Json { ignoreUnknownKeys = true }

    private val _alerts = MutableSharedFlow<PanicAlert>(extraBufferCapacity = 16)
    override val alerts: SharedFlow<PanicAlert> = _alerts.asSharedFlow()

    // Renamed from 'outgoing' to avoid shadowing Ktor session's own 'outgoing: SendChannel<Frame>'
    private val sendQueue = Channel<String>(Channel.BUFFERED)

    override suspend fun connect(token: String) {
        client.webSocket(
            urlString = "${ApiConfig.WS_URL}/ws/panic",
            request = { url.parameters.append("token", token) }
        ) {
            val sender = launch {
                while (isActive) {
                    val result = sendQueue.receiveCatching()
                    if (result.isClosed) break
                    val msg = result.getOrNull() ?: continue
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
        sendQueue.trySend(json.encodeToString(WsMessage("panic", latitude, longitude)))
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double) {
        sendQueue.trySend(json.encodeToString(WsMessage("location_update", latitude, longitude)))
    }

    override suspend fun disconnect() {
        sendQueue.close()
        client.close()
    }
}
