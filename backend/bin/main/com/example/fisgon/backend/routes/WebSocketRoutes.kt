package com.example.fisgon.backend.routes

import com.example.fisgon.backend.db.AuthSessions
import com.example.fisgon.backend.security.JwtConfig
import com.example.fisgon.shared.model.WsMessage
import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private data class PanicSession(
    val session: DefaultWebSocketServerSession,
    val userId: String,
    @Volatile var latitude: Double = 0.0,
    @Volatile var longitude: Double = 0.0
)

private val activeSessions = ConcurrentHashMap<String, PanicSession>()

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6_371_000.0
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val dPhi = Math.toRadians(lat2 - lat1)
    val dLambda = Math.toRadians(lon2 - lon1)
    val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun Route.webSocketRoutes(jwtConfig: JwtConfig) {
    webSocket("/ws/panic") {
        val token = call.request.queryParameters["token"]
        val userId = token?.let { t ->
            runCatching {
                val decoded = jwtConfig.verifier.verify(t)
                val sessionUuid = decoded.getClaim("sessionUuid").asString()
                val now = LocalDateTime.now(ZoneOffset.UTC)
                transaction {
                    AuthSessions.selectAll()
                        .where { AuthSessions.id eq UUID.fromString(sessionUuid) }
                        .singleOrNull()
                        ?.takeIf { it[AuthSessions.expiresAt].isAfter(now) }
                        ?.get(AuthSessions.userId)
                        ?.toString()
                }
            }.getOrNull()
        }

        if (userId == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
            return@webSocket
        }

        val sessionId = UUID.randomUUID().toString()
        val panicSession = PanicSession(session = this, userId = userId)
        activeSessions[sessionId] = panicSession

        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val msg = runCatching {
                    Json.decodeFromString<WsMessage>(frame.readText())
                }.getOrNull() ?: continue

                when (msg.type) {
                    "location_update" -> {
                        panicSession.latitude = msg.latitude
                        panicSession.longitude = msg.longitude
                    }
                    "panic" -> {
                        val alert = WsMessage(
                            type = "panic_alert",
                            userId = userId,
                            latitude = msg.latitude,
                            longitude = msg.longitude,
                            timestamp = LocalDateTime.now(ZoneOffset.UTC).toString()
                        )
                        val alertJson = Json.encodeToString(alert)
                        activeSessions.values
                            .filter { it !== panicSession }
                            .filter { s ->
                                // Si no tiene ubicación (0,0) recibe todo; si tiene, solo dentro de 5km
                                (s.latitude == 0.0 && s.longitude == 0.0) ||
                                    haversineMeters(s.latitude, s.longitude, msg.latitude, msg.longitude) <= 5_000
                            }
                            .forEach { s -> launch { runCatching { s.session.send(alertJson) } } }
                    }
                }
            }
        } finally {
            activeSessions.remove(sessionId)
        }
    }
}
