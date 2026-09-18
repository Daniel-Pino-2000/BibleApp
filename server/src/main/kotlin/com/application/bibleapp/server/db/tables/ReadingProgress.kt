package com.application.bibleapp.server.db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Where a user last left off reading - a singleton per user, not a list (there's exactly one
 * "current position", per docs/api_contract.md decision 7). Using `userId` itself as the
 * primary key (rather than a separate generated id) enforces "at most one row per user" at
 * the database level, matching that design directly instead of relying on application code to
 * remember to keep it that way.
 */
object ReadingProgress : Table("reading_progress") {
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val versionId = text("version_id")
    val bookId = integer("book_id")
    val chapter = integer("chapter")
    val verse = integer("verse")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
