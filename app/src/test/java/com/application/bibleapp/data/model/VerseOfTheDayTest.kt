package com.application.bibleapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerseOfTheDayTest {

    private fun verse(number: Int) =
        VerseUI(id = number, text = "v$number text", bookId = 43, chapter = 3, verse = number)

    private val john3 = (14..23).map { verse(it) }

    @Test
    fun `single verse resolves to just its own text and a plain reference`() {
        val ref = DailyVerseRef(bookId = 43, chapter = 3, startVerse = 16, endVerse = null)

        val result = resolveDailyVerseUI("John", ref, john3)

        assertEquals("John 3:16", result?.reference)
        assertEquals("v16 text", result?.text)
    }

    @Test
    fun `verse range concatenates every verse's text in order and shows a dashed reference`() {
        val ref = DailyVerseRef(bookId = 43, chapter = 3, startVerse = 20, endVerse = 21)

        val result = resolveDailyVerseUI("John", ref, john3)

        assertEquals("John 3:20-21", result?.reference)
        assertEquals("v20 text v21 text", result?.text)
    }

    @Test
    fun `a range still resolves correctly even if the chapter list is out of order`() {
        val ref = DailyVerseRef(bookId = 43, chapter = 3, startVerse = 20, endVerse = 22)

        val result = resolveDailyVerseUI("John", ref, john3.shuffled())

        assertEquals("v20 text v21 text v22 text", result?.text)
    }

    @Test
    fun `a range where endVerse equals startVerse is treated as a single verse`() {
        val ref = DailyVerseRef(bookId = 43, chapter = 3, startVerse = 16, endVerse = 16)

        val result = resolveDailyVerseUI("John", ref, john3)

        assertEquals("John 3:16", result?.reference)
        assertEquals("v16 text", result?.text)
    }

    @Test
    fun `missing start verse in the currently selected translation resolves to null`() {
        // e.g. the daily reference is Old Testament but the active translation is NT-only.
        val ref = DailyVerseRef(bookId = 43, chapter = 3, startVerse = 99, endVerse = null)

        val result = resolveDailyVerseUI("John", ref, john3)

        assertNull(result)
    }
}
