package com.example.fisgon.backend.routes

import com.example.fisgon.backend.db.AuthSessions
import com.example.fisgon.backend.db.Categories
import com.example.fisgon.backend.db.Reports
import com.example.fisgon.shared.model.CategoryResponse
import com.example.fisgon.shared.model.ReportCreateRequest
import com.example.fisgon.shared.model.ReportResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

fun Route.reportRoutes() {

    // ── Categorías (sin autenticación) ───────────────────────────
    get("/categories") {
        val categories = transaction {
            Categories.selectAll().map { row ->
                CategoryResponse(
                    id    = row[Categories.id].value.toString(),
                    name  = row[Categories.name],
                    icon  = row[Categories.icon],
                    color = row[Categories.color]
                )
            }
        }
        call.respond(categories)
    }

    // ── Reportes (requiere JWT) ──────────────────────────────────
    authenticate("auth-jwt") {
        route("/reports") {
            post {
                val principal  = call.principal<JWTPrincipal>()
                val sessionUuid = principal?.payload?.getClaim("sessionUuid")?.asString()
                val userId = sessionUuid?.let { uuid ->
                    runCatching {
                        transaction {
                            AuthSessions.selectAll()
                                .where { AuthSessions.id eq UUID.fromString(uuid) }
                                .limit(1)
                                .singleOrNull()
                                ?.get(AuthSessions.userId)
                        }
                    }.getOrNull()
                }
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val request   = call.receive<ReportCreateRequest>()
                val reportId  = UUID.randomUUID()
                val createdAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                val catUuid   = request.categoryId?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                transaction {
                    Reports.insert { row ->
                        row[id]          = reportId
                        row[Reports.userId]     = userId
                        row[title]       = request.title
                        row[description] = request.description
                        row[categoryId]  = catUuid
                        row[severity]    = request.severity
                        row[latitude]    = request.latitude
                        row[longitude]   = request.longitude
                        row[Reports.createdAt]  = createdAt
                    }
                }

                call.respond(
                    HttpStatusCode.Created,
                    ReportResponse(
                        id          = reportId.toString(),
                        userId      = userId.toString(),
                        title       = request.title,
                        description = request.description,
                        categoryId  = request.categoryId,
                        latitude    = request.latitude,
                        longitude   = request.longitude,
                        severity    = request.severity,
                        createdAt   = createdAt.toString()
                    )
                )
            }
        }
    }
}
