package com.application.bibleapp.server.db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

/**
 * One refresh-token "session" per row. The raw token itself is never stored - only a SHA-256
 * hash of it (see auth/RefreshTokenIssuer.kt) - so a database leak alone can't be used to
 * impersonate a session; the real token has to be presented to be matched against its hash.
 *
 * `userId` cascades on delete: removing a user (account deletion) automatically removes all
 * of their sessions too, enforced by the database rather than relying on application code to
 * remember to clean them up.
 */
object RefreshTokens : Table("refresh_tokens") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")

    // Set when a token is rotated away (refresh) or explicitly revoked (logout). Not read by
    // any endpoint that just issues tokens, but present now so refresh-token rotation
    // (detecting reuse of an already-used token, per the contract's design decisions) doesn't
    // require a schema migration later.
    val revokedAt = timestamp("revoked_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
