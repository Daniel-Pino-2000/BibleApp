package com.application.bibleapp.server.db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

/**
 * A highlight: a color applied to a set of verses (in one version), owned by one user.
 *
 * `versesJson` stores the verse list as JSON text rather than a jsonb column or a child table
 * - see models/VerseLocation.kt for why. `deletedAt` makes deletion soft: a plain GET only
 * returns active highlights, but passing updatedSince also returns tombstones, so a syncing
 * device can tell "deleted elsewhere" apart from "never received yet" (docs/api_contract.md
 * decision 13).
 */
object Highlights : Table("highlights") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val versionId = text("version_id")
    val versesJson = text("verses_json")
    val color = integer("color")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
