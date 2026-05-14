package com.example.fisgon.backend.routes

import com.example.fisgon.backend.db.Reports
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
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

fun Route.reportRoutes() {
    authenticate("auth-jwt") {
        route("/reports") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val request = call.receive<ReportCreateRequest>()
                val reportId = UUID.randomUUID()
                val now = Instant.now()
                val createdAt = LocalDateTime.ofInstant(now, ZoneOffset.UTC)

                transaction {
                    Reports.insert { row ->
                        row[id] = reportId
                        row[Reports.userId] = UUID.fromString(userId)
                        row[description] = request.description
                        row[severity] = request.severity
                        row[latitude] = request.latitude
                        row[longitude] = request.longitude
                        row[Reports.createdAt] = createdAt
                    }
                }

                call.respond(
                    HttpStatusCode.Created,
                    ReportResponse(
                        id = reportId.toString(),
                        userId = userId,
                        description = request.description,
                        latitude = request.latitude,
                        longitude = request.longitude,
                        severity = request.severity,
                        createdAt = createdAt.toString()
                    )
                )
            }
        }
    }
}
