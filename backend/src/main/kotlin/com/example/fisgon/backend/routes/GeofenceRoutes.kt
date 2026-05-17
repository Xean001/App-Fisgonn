package com.example.fisgon.backend.routes

import com.example.fisgon.backend.db.Geofences
import com.example.fisgon.shared.model.GeofenceCreateRequest
import com.example.fisgon.shared.model.GeofenceDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

fun Route.geofenceRoutes() {
    authenticate("auth-jwt") {
        route("/geofences") {
            get {
                val list = transaction {
                    Geofences.selectAll().map { row ->
                        GeofenceDto(
                            id = row[Geofences.id].toString(),
                            name = row[Geofences.name],
                            latitude = row[Geofences.latitude],
                            longitude = row[Geofences.longitude],
                            radiusMeters = row[Geofences.radiusMeters],
                            severity = row[Geofences.severity]
                        )
                    }
                }
                call.respond(list)
            }

            post {
                val req = call.receive<GeofenceCreateRequest>()
                val id = UUID.randomUUID()
                val now = LocalDateTime.now(ZoneOffset.UTC)
                transaction {
                    Geofences.insert { row ->
                        row[Geofences.id] = id
                        row[name] = req.name
                        row[latitude] = req.latitude
                        row[longitude] = req.longitude
                        row[radiusMeters] = req.radiusMeters
                        row[severity] = req.severity
                        row[createdAt] = now
                    }
                }
                call.respond(
                    HttpStatusCode.Created,
                    GeofenceDto(
                        id = id.toString(),
                        name = req.name,
                        latitude = req.latitude,
                        longitude = req.longitude,
                        radiusMeters = req.radiusMeters,
                        severity = req.severity
                    )
                )
            }
        }
    }
}
