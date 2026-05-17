package com.example.fisgon.backend.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object Users : UUIDTable("users") {
    val email = varchar("email", 255).nullable()
    val nombre = varchar("nombre", 120).default("")
    val apellido = varchar("apellido", 120).default("")
    val passwordHash = varchar("password_hash", 255).nullable()
    val passwordSalt = varchar("password_salt", 255).nullable()
    val createdAt = datetime("created_at")
}

object AuthSessions : UUIDTable("auth_sessions", "session_uuid") {
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at")
    val expiresAt = datetime("expires_at")
}

object Reports : UUIDTable("reports") {
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val description = text("description").nullable()
    val severity = integer("severity")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val createdAt = datetime("created_at")
}

object Geofences : UUIDTable("geofences") {
    val name = varchar("name", 255)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val radiusMeters = double("radius_meters")
    val severity = integer("severity").default(1)
    val createdAt = datetime("created_at")
}
