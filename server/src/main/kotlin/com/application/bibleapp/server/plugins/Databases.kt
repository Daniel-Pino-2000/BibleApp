package com.application.bibleapp.server.plugins

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val storageConfig = environment.config.config("storage")

    Database.connect(
        url = storageConfig.property("jdbcUrl").getString(),
        driver = storageConfig.property("driverClassName").getString(),
        user = storageConfig.property("user").getString(),
        password = storageConfig.property("password").getString()
    )

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
