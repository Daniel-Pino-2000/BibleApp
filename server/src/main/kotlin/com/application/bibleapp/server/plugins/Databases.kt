package com.application.bibleapp.server.plugins

import com.application.bibleapp.server.db.tables.Users
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val storageConfig = environment.config.config("storage")

    val database = Database.connect(
        url = storageConfig.property("jdbcUrl").getString(),
        driver = storageConfig.property("driverClassName").getString(),
        user = storageConfig.property("user").getString(),
        password = storageConfig.property("password").getString()
    )

    // Also proves the connection itself works: SchemaUtils.create only succeeds if a real
    // query can run, so the old standalone "SELECT 1" check is redundant now.
    transaction(database) {
        SchemaUtils.create(Users)
    }

    // TODO: add each new table here as it's created (Highlights, Notes, ReadingProgress,
    // RefreshTokens...). For anything beyond a hobby-scale schema, prefer a real migration
    // tool (e.g. Flyway) over SchemaUtils.create so changes are versioned.
}
