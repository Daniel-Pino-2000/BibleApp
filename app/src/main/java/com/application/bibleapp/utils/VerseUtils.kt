package com.application.bibleapp.utils

import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.remote.ApiChapterDto
import com.application.bibleapp.data.remote.ApiVerseDto

object VerseUtils {
    /**
     * Map BibleVerse to VerseUI for UI display
     */
    // VerseExtensions.kt
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

    fun ApiVerseDto.toVerseUI(bookIdMap: Map<String, Int>): VerseUI {
        val numericBookId = bookIdMap[book] ?: 0  // map book name to numeric ID
        return VerseUI(
            id = null,  // API does not provide DB id
            text = text,
            bookId = numericBookId,
            chapter = chapter.toIntOrNull(),
            verse = verse.toIntOrNull()
        )
    }

    fun ApiChapterDto.toVerseUIList(bookIdMap: Map<String, Int>): List<VerseUI> =
        data.map { it.toVerseUI(bookIdMap) }



}