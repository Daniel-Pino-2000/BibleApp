package com.application.bibleapp.server.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object Users : Table("users") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}