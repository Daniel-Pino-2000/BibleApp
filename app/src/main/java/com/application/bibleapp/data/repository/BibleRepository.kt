package com.application.bibleapp.data.repository

import android.content.Context
import com.application.bibleapp.data.local.BibleDatabaseManager
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class BibleRepository(private val context: Context) {

    // Cache for recently accessed chapters: key = bookId to chapter
    private val chapterCache = mutableMapOf<Pair<Int, Int>, List<VerseUI>>()

    /**
     * Get a chapter as VerseUI list.
     * Cached if already loaded.
     */
    suspend fun getChapter(bookId: Int, chapter: Int): List<VerseUI> {
        val key = bookId to chapter
        return chapterCache.getOrPut(key) {
            withContext(Dispatchers.IO) {   // <- runs database query on a background thread
                BibleDatabaseManager.getVersesByChapter(context, bookId, chapter)
                    .map { it.toUI() }
            }
        }
    }
}

/**
 * Map BibleVerse to VerseUI for UI display
 */
fun BibleVerse.toUI(): VerseUI = VerseUI(
    id = id,
    text = text,
    bookId = bookId,
    chapter = chapter,
    verse = verse,
    isUserVerse = false,
    isHighlighted = false,
    highlightColor = 0x00000000
)
