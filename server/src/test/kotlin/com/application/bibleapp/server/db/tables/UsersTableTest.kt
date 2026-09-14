package com.application.bibleapp.server.db.tables

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO: this points directly at the local dev Postgres container so it's easy to run right
// now. Once more tables exist it's worth switching to a dedicated test database (H2 in-memory,
// or Testcontainers) instead of sharing the dev DB — see the roadmap PDF, Section 2.
class UsersTableTest {

    private val database = Database.connect(
        url = "jdbc:postgresql://localhost:5433/bibleapp",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "postgres"
    )

    @BeforeTest
    fun createSchema() = transaction(database) {
        SchemaUtils.create(Users)
    }

    @AfterTest
    fun dropSchema() = transaction(database) {
        // Users.email is unique, so leftover rows from a previous run would break the next
        // one's insert — dropping the table after each test keeps them independent.
        SchemaUtils.drop(Users)
    }

    @Test
    fun insertedUserCanBeReadBackByEmail() = transaction(database) {
        val insertedId = Users.insert {
            it[email] = "test@example.com"
            it[passwordHash] = "hashed-password"
            it[createdAt] = Instant.now()
        }[Users.id]

        val row = Users.selectAll().where { Users.email eq "test@example.com" }.single()

        assertEquals(insertedId, row[Users.id])
        assertEquals("test@example.com", row[Users.email])
        assertEquals("hashed-password", row[Users.passwordHash])
    }
}
