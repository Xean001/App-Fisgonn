package com.example.fisgon.backend

import com.example.fisgon.backend.db.DatabaseFactory
import com.example.fisgon.backend.db.AuthSessions
import com.example.fisgon.backend.routes.authRoutes
import com.example.fisgon.backend.routes.geofenceRoutes
import com.example.fisgon.backend.routes.healthRoutes
import com.example.fisgon.backend.routes.reportRoutes
import com.example.fisgon.backend.routes.webSocketRoutes
import com.example.fisgon.backend.security.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    val jwtConfig = JwtConfig.fromConfig(environment.config)

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtConfig.verifier)
            validate { credentials ->
                val sessionUuid = credentials.payload.getClaim("sessionUuid").asString()
                val isActive = sessionUuid?.let { uuid ->
                    runCatching {
                        val now = LocalDateTime.now(ZoneOffset.UTC)
                        transaction {
                            AuthSessions.selectAll()
                                .where { AuthSessions.id eq UUID.fromString(uuid) }
                                .limit(1)
                                .singleOrNull()
                                ?.get(AuthSessions.expiresAt)
                                ?.isAfter(now) == true
                        }
                    }.getOrDefault(false)
                } == true
                if (isActive) credentials else null
            }
        }
    }

    routing {
        get("/") { call.respondText("fisgon-backend") }
        healthRoutes()
        authRoutes(jwtConfig)
        reportRoutes()
        geofenceRoutes()
        webSocketRoutes(jwtConfig)
    }
}
