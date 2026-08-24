package com.application.bibleapp.data.model

import java.util.Calendar

/** A book/chapter/verse reference, resolved against whatever translation is active. */
data class DailyVerseRef(val bookId: Int, val chapter: Int, val verse: Int)

/**
 * A small curated set of well-known verses, picked deterministically by day-of-year
 * so the Home screen shows the same verse all day and a different one tomorrow.
 * Uses [Calendar] rather than java.time — this app's minSdk (24) predates java.time
 * without core library desugaring.
 */
object VerseOfTheDay {
    private val refs = listOf(
        DailyVerseRef(bookId = 43, chapter = 3, verse = 16),  // John 3:16
        DailyVerseRef(bookId = 19, chapter = 23, verse = 1),  // Psalm 23:1
        DailyVerseRef(bookId = 20, chapter = 3, verse = 5),   // Proverbs 3:5
        DailyVerseRef(bookId = 45, chapter = 8, verse = 28),  // Romans 8:28
        DailyVerseRef(bookId = 50, chapter = 4, verse = 13),  // Philippians 4:13
        DailyVerseRef(bookId = 6, chapter = 1, verse = 9),    // Joshua 1:9
        DailyVerseRef(bookId = 23, chapter = 41, verse = 10), // Isaiah 41:10
    )

    fun forToday(): DailyVerseRef {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return refs[dayOfYear % refs.size]
    }
}
