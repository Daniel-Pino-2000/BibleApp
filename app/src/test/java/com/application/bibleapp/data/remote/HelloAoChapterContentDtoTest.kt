package com.application.bibleapp.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class HelloAoChapterContentDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes footnotes as a sibling of content, matching the live complete_json shape`() {
        // Exact shape confirmed live against GET .../eng_kjv/complete.json — footnotes
        // sits alongside "content" inside the chapter object, not inside each verse.
        val raw = """
        {"number":1,"content":[
            {"type":"verse","number":4,"content":["And God saw the light, that it was good: and God divided the light from the darkness.",{"noteId":0}]}
        ],"footnotes":[
            {"noteId":0,"caller":"+","text":"1.4 the light from…: Heb. between the light and between the darkness","reference":{"chapter":1,"verse":4}}
        ]}
        """.trimIndent()

        val decoded = json.decodeFromString(HelloAoChapterContentDto.serializer(), raw)

        assertEquals(1, decoded.content.size)
        assertEquals(1, decoded.footnotes.size)
        assertEquals(0, decoded.footnotes[0].noteId)
        assertEquals("1.4 the light from…: Heb. between the light and between the darkness", decoded.footnotes[0].text)
        assertEquals(4, decoded.footnotes[0].reference?.verse)
    }

    @Test
    fun `footnotes defaults to empty for chapters with none`() {
        val raw = """{"number":1,"content":[]}"""

        val decoded = json.decodeFromString(HelloAoChapterContentDto.serializer(), raw)

        assertEquals(0, decoded.footnotes.size)
    }
}
