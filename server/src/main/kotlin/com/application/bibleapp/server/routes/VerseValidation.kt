package com.application.bibleapp.server.routes

import com.application.bibleapp.server.models.VerseLocation

private const val MAX_VERSES = 500

/**
 * Shared by Highlights and Notes, since both cover a `verses: List<VerseLocation>` with the
 * same rules (docs/api_contract.md decisions 9 and 17). Returns a human-readable message
 * describing the first problem found, or null if the list is valid.
 */
fun validateVerses(verses: List<VerseLocation>): String? = when {
    verses.isEmpty() -> "verses must include at least one entry"
    verses.size > MAX_VERSES -> "verses must include at most $MAX_VERSES entries"
    verses.any { it.bookId !in 1..66 } -> "bookId must be between 1 and 66"
    verses.any { it.chapter < 1 } -> "chapter must be 1 or greater"
    verses.any { it.verse < 1 } -> "verse must be 1 or greater"
    else -> null
}
