package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.utils.IdUtils.generateVerseId
import kotlinx.serialization.Serializable

@Serializable
data class ApiVerseDto(
    val verse: String,
    val text: String
)

// Mapper extensions
fun ApiVerseDto.toBibleVerse(
    bookId: Int,
    chapter: Int
): BibleVerse = BibleVerse(
    id = null,                  // API doesn’t provide DB ID
    bookId = bookId,            // pass numeric book ID
    chapter = chapter,          // pass chapter
    verse = verse.toIntOrNull() ?: 0,
    text = text
)

fun ApiVerseDto.toUIVerse(bookId: Int, chapter: Int): VerseUI = VerseUI(
    id = null,
    bookId = bookId,
    chapter = chapter,
    verse = verse.toIntOrNull() ?: 0,
    text = text
)


