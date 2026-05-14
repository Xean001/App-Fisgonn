package com.example.fisgon.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.time.Instant

class JwtConfig(
    private val secret: String,
    val issuer: String,
    val audience: String,
    val ttlSeconds: Long
) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createToken(userId: String, issuedAt: Instant): String {
        val expiresAt = issuedAt.plusSeconds(ttlSeconds)
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun expiresAt(issuedAt: Instant): Instant = issuedAt.plusSeconds(ttlSeconds)

    companion object {
        fun fromConfig(config: ApplicationConfig): JwtConfig {
            val secret = config.propertyOrNull("jwt.secret")?.getString() ?: "change-me"
            val issuer = config.propertyOrNull("jwt.issuer")?.getString() ?: "fisgon"
            val audience = config.propertyOrNull("jwt.audience")?.getString() ?: "fisgon-users"
            val ttlSeconds = config.propertyOrNull("jwt.ttlSeconds")?.getString()?.toLong() ?: 2592000L
            return JwtConfig(secret, issuer, audience, ttlSeconds)
        }
    }
}
