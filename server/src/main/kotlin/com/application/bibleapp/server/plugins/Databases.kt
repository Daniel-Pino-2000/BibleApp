package com.application.bibleapp.server.plugins

import com.application.bibleapp.server.db.tables.Highlights
import com.application.bibleapp.server.db.tables.Notes
import com.application.bibleapp.server.db.tables.ReadingProgress
import com.application.bibleapp.server.db.tables.RefreshTokens
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
    // query can run, so a separate standalone connectivity check would be redundant.
    // Users must be created before the tables that reference it via a foreign key.
    transaction(database) {
        SchemaUtils.create(Users, RefreshTokens, Highlights, Notes, ReadingProgress)
    }

    // For anything beyond a hobby-scale schema, prefer a real migration tool (e.g. Flyway)
    // over SchemaUtils.create so schema changes are versioned instead of inferred at startup.
}
