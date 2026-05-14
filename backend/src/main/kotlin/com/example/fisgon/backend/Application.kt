package com.example.fisgon.backend

import com.example.fisgon.backend.db.DatabaseFactory
import com.example.fisgon.backend.routes.authRoutes
import com.example.fisgon.backend.routes.healthRoutes
import com.example.fisgon.backend.routes.reportRoutes
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

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    val jwtConfig = JwtConfig.fromConfig(environment.config)

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtConfig.verifier)
            validate { credentials ->
                val userId = credentials.payload.getClaim("userId").asString()
                if (userId.isNullOrBlank()) null else credentials
            }
        }
    }

    routing {
        get("/") { call.respondText("fisgon-backend") }
        healthRoutes()
        authRoutes(jwtConfig)
        reportRoutes()
    }
}
