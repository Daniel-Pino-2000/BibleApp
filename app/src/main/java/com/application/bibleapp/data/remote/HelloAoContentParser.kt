package com.application.bibleapp.data.remote

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Flattens a chapter's polymorphic `content[]` array into plain verse text.
 *
 * The API's content model supports rich text (headings, poem indentation,
 * red-letter/wordsOfJesus, footnote references, hebrew_subtitle superscripts —
 * see docs at https://bible.helloao.org/docs/reference/), but this app's
 * reading UI and local schema only ever displayed a single plain-text string
 * per verse. Headings and footnote markers are intentionally dropped rather
 * than smuggled into the verse text; only "verse" entries are extracted.
 *
 * Pure/no Android dependency, so it's plain-JVM testable.
 */
object HelloAoContentParser {

    /** Returns (verseNumber, plainText) pairs for every "verse" entry in [content]. */
    fun extractVerseTexts(content: List<JsonElement>): List<Pair<Int, String>> {
        val verses = mutableListOf<Pair<Int, String>>()
        content.forEach { element ->
            val obj = element as? JsonObject ?: return@forEach
            val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            if (type != "verse") return@forEach

            val number = obj["number"]?.jsonPrimitive?.intOrNull ?: return@forEach
            val text = flattenVerseContent(obj["content"]?.jsonArray ?: JsonArray(emptyList()))
            if (text.isNotBlank()) verses += number to text
        }
        return verses
    }

    /**
     * A verse's own content[] mixes plain strings with objects like
     * {"text":..., "poem":N, "wordsOfJesus":true}, {"lineBreak":true},
     * {"heading":...}, and {"noteId":N}. We only care about the readable text.
     */
    private fun flattenVerseContent(items: JsonArray): String {
        val fragments = items.mapNotNull { item ->
            when (item) {
                is JsonPrimitive -> item.contentOrNull
                is JsonObject -> item["text"]?.jsonPrimitive?.contentOrNull
                else -> null
            }
        }
        return fragments.joinToString(" ").replace(Regex("\\s+"), " ").trim()
    }
}
