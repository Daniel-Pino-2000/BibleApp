package com.application.bibleapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerseContentTest {

    @Test
    fun `round-trips through JSON encode and decode`() {
        val original = StoredVerseContent(
            headings = listOf(StoredHeading(type = "heading", text = "The Creation")),
            runs = listOf(
                VerseRun(text = "In the beginning "),
                VerseRun(text = "God created the heavens.", isWordsOfJesus = true, poemLevel = 1, lineBreakBefore = true)
            )
        )

        val decoded = decodeVerseContentOrNull(original.encodeToJson())

        assertEquals(original, decoded)
    }

    @Test
    fun `footnoteId round-trips and legacy runs default to no footnote`() {
        val original = StoredVerseContent(
            runs = listOf(
                VerseRun(text = "And God said, \"Let there be light,\"", footnoteId = 0),
                VerseRun(text = "and there was light.")
            )
        )

        val decoded = decodeVerseContentOrNull(original.encodeToJson())

        assertEquals(0, decoded?.runs?.get(0)?.footnoteId)
        assertNull(decoded?.runs?.get(1)?.footnoteId)
    }

    @Test
    fun `null raw string decodes to null`() {
        assertNull(decodeVerseContentOrNull(null))
    }

    @Test
    fun `blank raw string decodes to null`() {
        assertNull(decodeVerseContentOrNull("   "))
    }

    @Test
    fun `legacy plain text that is not JSON decodes to null rather than throwing`() {
        // Rows written before rich content existed just have plain English/etc. text here.
        assertNull(decodeVerseContentOrNull("In the beginning God created the heavens and the earth."))
    }

    @Test
    fun `malformed JSON decodes to null rather than throwing`() {
        assertNull(decodeVerseContentOrNull("{not valid json"))
    }
}
