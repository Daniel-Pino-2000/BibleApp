package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.utils.IdUtils.generateVerseId
import kotlinx.serialization.Serializable

@Serializable
data class ApiVerseDto(
    val verse: String,
    val text: String
)

// Mapper extension
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

