package com.application.bibleapp.data.model

import androidx.room.PrimaryKey

data class BibleVerse(
    val id: Int?,
    val text: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val richContent: StoredVerseContent? = null // null for the bundled KJV table and pre-rich-content downloads
)

/**
 * Map BibleVerse to VerseUI for UI display
 */
// VerseExtensions.kt
fun BibleVerse.toUI(): VerseUI = VerseUI(
    id = id,
    text = text,
    richContent = richContent,
    bookId = bookId,
    chapter = chapter,
    verse = verse,
    isUserVerse = false,
    isHighlighted = false,
    highlightColor = 0x00000000
)

