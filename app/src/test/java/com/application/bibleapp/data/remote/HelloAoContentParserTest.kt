package com.application.bibleapp.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
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

        val verses = HelloAoContentParser.extractVerseTexts(content)

        assertEquals(listOf(1 to "In the beginning God created the heavens and the earth."), verses)
    }

    @Test
    fun `skips headings and line breaks, only extracts verse entries`() {
        val content = parseContent(
            """
            [
                {"type":"heading","content":["The Creation"]},
                {"type":"line_break"},
                {"type":"verse","number":1,"content":["Text one."]},
                {"type":"hebrew_subtitle","content":["A Psalm of David."]},
                {"type":"verse","number":2,"content":["Text two."]}
            ]
            """.trimIndent()
        )

        val verses = HelloAoContentParser.extractVerseTexts(content)

        assertEquals(listOf(1 to "Text one.", 2 to "Text two."), verses)
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

        val verses = HelloAoContentParser.extractVerseTexts(content)

        assertEquals(1, verses.size)
        assertEquals(3, verses[0].first)
        assertEquals("And God said, \"Let there be light,\" and there was light.", verses[0].second)
    }

    @Test
    fun `joins poem-formatted fragments from FormattedText objects`() {
        // Real shape from BSB Psalm 3:1 — poem lines are {text, poem} objects, not plain strings.
        val content = parseContent(
            """
            [{"type":"verse","number":1,"content":[
                {"text":"O LORD, how my foes have increased!","poem":1},
                {"text":"How many rise up against me!","poem":2}
            ]}]
            """.trimIndent()
        )

        val verses = HelloAoContentParser.extractVerseTexts(content)

        assertEquals("O LORD, how my foes have increased! How many rise up against me!", verses[0].second)
    }

    @Test
    fun `a verse with only markers and no text is dropped`() {
        val content = parseContent(
            """[{"type":"verse","number":1,"content":[{"noteId":0}]}]"""
        )

        assertTrue(HelloAoContentParser.extractVerseTexts(content).isEmpty())
    }
}
