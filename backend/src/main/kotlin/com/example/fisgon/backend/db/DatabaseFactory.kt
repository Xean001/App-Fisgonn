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
            this.maximumPoolSize = maximumPoolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
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
