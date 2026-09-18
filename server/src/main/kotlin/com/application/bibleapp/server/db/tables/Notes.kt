package com.application.bibleapp.server.db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

/**
 * A note: free text attached to a set of verses (in one version), owned by one user.
 *
 * The column is named `content`, not `text`, purely to avoid shadowing Exposed's own
 * `Table.text(...)` column-builder function used to declare it - the contract's field name
 * (`text`) is what the API actually exposes; the database column name is an internal detail.
 * Same `versesJson`/`deletedAt` reasoning as Highlights.
 */
object Notes : Table("notes") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val versionId = text("version_id")
    val versesJson = text("verses_json")
    val content = text("content")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
