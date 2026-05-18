package com.example.fisgon.backend.routes

import com.example.fisgon.backend.db.Users
import com.example.fisgon.backend.db.AuthSessions
import com.example.fisgon.backend.security.JwtConfig
import com.example.fisgon.backend.security.PasswordHasher
import com.example.fisgon.backend.util.AnonymousUsername
import org.jetbrains.exposed.exceptions.ExposedSQLException
import com.example.fisgon.shared.model.AuthResponse
import com.example.fisgon.shared.model.LoginRequest
import com.example.fisgon.shared.model.RegisterRequest
import com.example.fisgon.shared.model.UserResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

fun Route.authRoutes(jwtConfig: JwtConfig) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val email = request.email.trim().lowercase()
            if (email.isBlank() || request.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val existing = transaction {
                Users.selectAll().where { Users.email eq email }.limit(1).count() > 0
            }
            if (existing) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            val userId = UUID.randomUUID()
            val salt = PasswordHasher.newSalt()
            val hash = PasswordHasher.hash(request.password, salt)
            val now = Instant.now()
            val createdAt = LocalDateTime.ofInstant(now, ZoneOffset.UTC)

            // Genera el username anónimo en el backend (única fuente de la
            // lógica). La BD garantiza unicidad con el índice UNIQUE; si choca,
            // se regenera y reintenta.
            var anonymousUsername = ""
            var inserted = false
            var attempt = 0
            while (!inserted && attempt < 6) {
                attempt++
                val candidate = AnonymousUsername.next()
                try {
                    transaction {
                        Users.insert { row ->
                            row[id] = userId
                            row[Users.email] = email
                            row[Users.nombre] = request.nombre ?: "Usuario"
                            row[Users.apellido] = request.apellido ?: ""
                            row[passwordHash] = hash
                            row[passwordSalt] = salt
                            row[Users.anonymousUsername] = candidate
                            row[Users.createdAt] = createdAt
                        }
                    }
                    anonymousUsername = candidate
                    inserted = true
                } catch (e: ExposedSQLException) {
                    if (e.message?.contains("users_anon_username_uq") == true) {
                        continue // colisión de username anónimo: reintentar
                    }
                    throw e
                }
            }
            if (!inserted) {
                call.respond(HttpStatusCode.InternalServerError)
                return@post
            }

            val expiresAt = jwtConfig.expiresAt(now)
            val sessionUuid = UUID.randomUUID()

            transaction {
                AuthSessions.insert { row ->
                    row[id] = sessionUuid
                    row[AuthSessions.userId] = userId
                    row[AuthSessions.createdAt] = createdAt
                    row[AuthSessions.expiresAt] = LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC)
                }
            }

            val token = jwtConfig.createToken(sessionUuid.toString(), now)
            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    user = UserResponse(
                        id = userId.toString(),
                        email = email,
                        nombre = request.nombre ?: "Usuario",
                        apellido = request.apellido ?: "",
                        anonymousUsername = anonymousUsername
                    ),
                    token = token,
                    expiresAt = expiresAt.toString()
                )
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val email = request.email.trim().lowercase()
            if (email.isBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val row = transaction {
                Users.selectAll().where { Users.email eq email }.singleOrNull()
            }
            val hash = row?.get(Users.passwordHash)
            val salt = row?.get(Users.passwordSalt)
            if (row == null || hash.isNullOrBlank() || salt.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            if (!PasswordHasher.verify(request.password, salt, hash)) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val userId = row[Users.id].value
            val now = Instant.now()
            val expiresAt = jwtConfig.expiresAt(now)
            val createdAt = LocalDateTime.ofInstant(now, ZoneOffset.UTC)
            val sessionUuid = UUID.randomUUID()

            transaction {
                AuthSessions.insert { session ->
                    session[id] = sessionUuid
                    session[AuthSessions.userId] = userId
                    session[AuthSessions.createdAt] = createdAt
                    session[AuthSessions.expiresAt] = LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC)
                }
            }

            val token = jwtConfig.createToken(sessionUuid.toString(), now)

            call.respond(
                AuthResponse(
                    user = UserResponse(
                        id = userId.toString(),
                        email = email,
                        nombre = row[Users.nombre].ifBlank { "Usuario" },
                        apellido = row[Users.apellido],
                        anonymousUsername = row[Users.anonymousUsername]
                    ),
                    token = token,
                    expiresAt = expiresAt.toString()
                )
            )
        }
    }
}
