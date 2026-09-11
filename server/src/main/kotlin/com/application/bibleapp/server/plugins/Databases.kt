package com.application.bibleapp.server.plugins

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val storageConfig = environment.config.config("storage")

    val database = Database.connect(
        url = storageConfig.property("jdbcUrl").getString(),
        driver = storageConfig.property("driverClassName").getString(),
        user = storageConfig.property("user").getString(),
        password = storageConfig.property("password").getString()
    )

    // TEMPORARY: Database.connect() is lazy and won't actually open a connection until a
    // query runs. This forces one immediately so a bad URL/credentials fail loudly at
    // startup instead of silently on the first real request. Remove once the Users table
    // (and the real SchemaUtils.create call below) makes this redundant.
    transaction(database) {
        exec("SELECT 1")
    }

    // TODO: once your Exposed tables exist (e.g. under db/tables), create/verify the
    // schema on startup, for example:
    //
    // transaction {
    //     SchemaUtils.create(Users)
    // }
    //
    // For anything beyond a hobby-scale schema, prefer a real migration tool
    // (e.g. Flyway) over SchemaUtils.create so changes are versioned.
}
