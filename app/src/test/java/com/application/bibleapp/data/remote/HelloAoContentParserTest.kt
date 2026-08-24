package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.VerseRun
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HelloAoContentParserTest {

    private fun parseContent(jsonArray: String): List<JsonElement> =
        Json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(JsonElement.serializer()), jsonArray)

    @Test
    fun `extracts plain verse text`() {
        val content = parseContent(
            """[{"type":"verse","number":1,"content":["In the beginning God created the heavens and the earth."]}]"""
        )

        val verses = HelloAoContentParser.extractVerses(content)

        assertEquals(1, verses.size)
        assertEquals(1, verses[0].number)
        assertEquals("In the beginning God created the heavens and the earth.", verses[0].plainText)
        assertEquals(listOf(VerseRun(text = "In the beginning God created the heavens and the earth.")), verses[0].richContent.runs)
    }

    @Test
    fun `chapter-level line_break marks only the immediately following verse as a new paragraph`() {
        // Real shape from BSB Genesis 1 — BSB never bakes a pilcrow into verse text; this
        // standalone content[] entry is its only paragraph-break signal.
        val content = parseContent(
            """
            [
                {"type":"line_break"},
                {"type":"verse","number":1,"content":["Text one."]},
                {"type":"verse","number":2,"content":["Text two."]}
            ]
            """.trimIndent()
        )

        val verses = HelloAoContentParser.extractVerses(content)

        assertEquals(listOf(1 to "Text one.", 2 to "Text two."), verses.map { it.number to it.plainText })
        assertTrue("verse right after line_break starts a new paragraph", verses[0].richContent.runs[0].paragraphBreakBefore)
        assertFalse("line_break must not carry over past the verse that claims it", verses[1].richContent.runs[0].paragraphBreakBefore)
    }

    @Test
    fun `flattens footnote markers and line breaks out of verse text`() {
        // Real shape from BSB Genesis 1:3 — a footnote reference splits the sentence.
        val content = parseContent(
            """
            [{"type":"verse","number":3,"content":[
                "And God said, \"Let there be light,\"",
                {"noteId":0},
                "and there was light."
            ]}]
            """.trimIndent()
        )

        val verses = HelloAoContentParser.extractVerses(content)

        assertEquals(1, verses.size)
        assertEquals(3, verses[0].number)
        assertEquals("And God said, \"Let there be light,\" and there was light.", verses[0].plainText)
    }

    @Test
    fun `joins poem-formatted fragments and preserves poem level per run`() {
        // Real shape from BSB Psalm 3:1 — poem lines are {text, poem} objects, not plain strings.
        val content = parseContent(
            """
            [{"type":"verse","number":1,"content":[
                {"text":"O LORD, how my foes have increased!","poem":1},
                {"text":"How many rise up against me!","poem":2}
            ]}]
            """.trimIndent()
        )

        val verses = HelloAoContentParser.extractVerses(content)

        assertEquals("O LORD, how my foes have increased! How many rise up against me!", verses[0].plainText)
        assertEquals(listOf(1, 2), verses[0].richContent.runs.map { it.poemLevel })
    }

    @Test
    fun `wordsOfJesus is captured per run without leaking into plain text`() {
        val content = parseContent(
            """
            [{"type":"verse","number":1,"content":[
                "Jesus said, ",
                {"text":"Let the little children come to me.","wordsOfJesus":true}
            ]}]
            """.trimIndent()
        )

        val verses = HelloAoContentParser.extractVerses(content)
        val runs = verses[0].richContent.runs

        assertEquals("Jesus said, Let the little children come to me.", verses[0].plainText)
        assertFalse(runs[0].isWordsOfJesus)
        assertTrue(runs[1].isWordsOfJesus)
    }

    @Test
    fun `lineBreak markers set lineBreakBefore on the following run`() {
        val content = parseContent(
            """
            [{"type":"verse","number":5,"content":[
                "God called the light \"day,\"",
                {"lineBreak":true},
                "and the darkness he called \"night.\""
            ]}]
            """.trimIndent()
        )

        val runs = HelloAoContentParser.extractVerses(content)[0].richContent.runs

        assertFalse(runs[0].lineBreakBefore)
        assertTrue(runs[1].lineBreakBefore)
    }

    @Test
    fun `heading and hebrew_subtitle entries attach to the next verse and reset afterward`() {
        // Real shape from BSB Psalm 3.
        val content = parseContent(
            """
            [
                {"type":"heading","content":["Deliver Me, O LORD!"]},
                {"type":"hebrew_subtitle","content":["A Psalm of David, when he fled from his son Absalom."]},
                {"type":"line_break"},
                {"type":"verse","number":1,"content":["O LORD, how my foes have increased!"]},
                {"type":"verse","number":2,"content":["Many say of me."]}
            ]
            """.trimIndent()
        )

        val verses = HelloAoContentParser.extractVerses(content)

        assertEquals(2, verses[0].richContent.headings.size)
        assertEquals("heading", verses[0].richContent.headings[0].type)
        assertEquals("Deliver Me, O LORD!", verses[0].richContent.headings[0].text)
        assertEquals("hebrew_subtitle", verses[0].richContent.headings[1].type)
        assertTrue("headings must not carry over to the next verse", verses[1].richContent.headings.isEmpty())
    }

    @Test
    fun `a verse with only markers and no text is dropped`() {
        val content = parseContent(
            """[{"type":"verse","number":1,"content":[{"noteId":0}]}]"""
        )

        assertTrue(HelloAoContentParser.extractVerses(content).isEmpty())
    }

    @Test
    fun `noteId marker attaches to the immediately preceding run, not later ones`() {
        // Real shape from BSB Genesis 1:3.
        val content = parseContent(
            """
            [{"type":"verse","number":3,"content":[
                "And God said, \"Let there be light,\"",
                {"noteId":0},
                "and there was light."
            ]}]
            """.trimIndent()
        )

        val runs = HelloAoContentParser.extractVerses(content)[0].richContent.runs

        assertEquals(2, runs.size)
        assertEquals(0, runs[0].footnoteId)
        assertEquals(null, runs[1].footnoteId)
    }

    @Test
    fun `extractFootnotes maps DTOs to plain records and drops entries with no verse reference`() {
        val footnotes = listOf(
            HelloAoFootnoteDto(noteId = 0, text = "Cited in 2 Corinthians 4:6", caller = "+", reference = HelloAoFootnoteReferenceDto(1, 3)),
            HelloAoFootnoteDto(noteId = 1, text = "No reference, should be dropped", caller = "+", reference = null)
        )

        val result = HelloAoContentParser.extractFootnotes(footnotes)

        assertEquals(1, result.size)
        assertEquals(0, result[0].noteId)
        assertEquals(3, result[0].verse)
        assertEquals("+", result[0].caller)
        assertEquals("Cited in 2 Corinthians 4:6", result[0].text)
    }
}
