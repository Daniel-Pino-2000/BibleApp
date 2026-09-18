package com.application.bibleapp.server.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A single verse reference within a Bible version - e.g. book 43 (John), chapter 3, verse 16.
 *
 * Shared by Highlights and Notes, which each cover a *list* of these rather than a contiguous
 * range: a user can select several verses at once (contiguous or not, even across chapters)
 * and save them as one highlight/note. See docs/api_contract.md, "Notes & Highlights"
 * decisions 9-10 for why.
 */
@Serializable
data class VerseLocation(
    val bookId: Int,
    val chapter: Int,
    val verse: Int
)

/**
 * How `verses` is actually stored: as a JSON-encoded string in a single text column, rather
 * than a jsonb column type or a child table. This is an explicit, documented simplification
 * (see api_contract.md's open items) - it works correctly and keeps the schema simple; a
 * child table would only be worth the extra complexity if a query needed to filter/join on
 * individual verses at the database level, which none of these endpoints do.
 */
fun List<VerseLocation>.toJson(): String = Json.encodeToString(this)

fun String.toVerseLocations(): List<VerseLocation> = Json.decodeFromString(this)
