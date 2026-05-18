package com.example.fisgon.backend.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val host = config.propertyOrNull("db.host")?.getString() ?: "localhost"
        val port = config.propertyOrNull("db.port")?.getString()?.toIntOrNull() ?: 5432
        val name = config.propertyOrNull("db.name")?.getString() ?: "fisgon"
        val jdbcUrl = config.propertyOrNull("db.jdbcUrl")?.getString()
            ?: "jdbc:postgresql://$host:$port/$name"
        val user = config.propertyOrNull("db.user")?.getString() ?: "fisgon"
        val password = config.propertyOrNull("db.password")?.getString() ?: "fisgon"
        val driver = config.propertyOrNull("db.driver")?.getString() ?: "org.postgresql.Driver"
        val maximumPoolSize = config.propertyOrNull("db.maxPoolSize")?.getString()?.toInt() ?: 10

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            this.driverClassName = driver
            // Máximo de conexiones simultáneas; solo se abren bajo carga real.
            this.maximumPoolSize = maximumPoolSize
            // Conexiones mínimas en reposo: 2 bastan para responder rápido sin saturar la BD.
            // HikariCP escala hasta maximumPoolSize cuando hay más requests concurrentes.
            this.minimumIdle = 2
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            // Cierra conexiones idle que sobren tras 3 minutos de inactividad
            // (solo aplica cuando el pool tiene más de minimumIdle conexiones abiertas).
            this.idleTimeout = 180_000
            // Recicla cada conexión a los 8 minutos para evitar conexiones obsoletas.
            this.maxLifetime = 480_000
            // Ping cada 60 s para mantener vivas las conexiones mínimas.
            // Evita que el servidor PostgreSQL cierre conexiones idle (~2 min en Coolify).
            this.keepaliveTime = 60_000
            // Si no hay conexión disponible en 10 s, lanza excepción en vez de colgar.
            this.connectionTimeout = 10_000
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        Flyway.configure()
            .dataSource(jdbcUrl, user, password)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}
