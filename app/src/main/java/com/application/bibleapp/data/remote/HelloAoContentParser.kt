package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.StoredHeading
import com.application.bibleapp.data.model.StoredVerseContent
import com.application.bibleapp.data.model.VerseRun
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/** One verse, both as plain text (for storage/search) and as rich content (for rendering). */
data class ParsedVerse(
    val number: Int,
    val plainText: String,
    val richContent: StoredVerseContent
)

/**
 * Parses a chapter's polymorphic `content[]` array into verses, preserving
 * enough structure to render red-letter text (stage 1), poem indentation and
 * line breaks (stage 2), and section headings (stage 3) — see docs at
 * https://bible.helloao.org/docs/reference/.
 *
 * Headings/hebrew_subtitle are chapter-level entries that precede whichever
 * verse comes next; they're captured on [StoredVerseContent.headings] here
 * but only populated once a verse consumes them — stage 3 wires up rendering.
 *
 * Pure/no Android dependency, so it's plain-JVM testable.
 */
object HelloAoContentParser {

    fun extractVerses(content: List<JsonElement>): List<ParsedVerse> {
        val verses = mutableListOf<ParsedVerse>()
        val pendingHeadings = mutableListOf<StoredHeading>()

        content.forEach { element ->
            val obj = element as? JsonObject ?: return@forEach
            val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: return@forEach

            when (type) {
                "heading" -> flattenStringArray(obj["content"]?.jsonArray)
                    .takeIf { it.isNotBlank() }
                    ?.let { pendingHeadings += StoredHeading(type = "heading", text = it) }

                "hebrew_subtitle" -> flattenStringArray(obj["content"]?.jsonArray)
                    .takeIf { it.isNotBlank() }
                    ?.let { pendingHeadings += StoredHeading(type = "hebrew_subtitle", text = it) }

                "verse" -> {
                    val number = obj["number"]?.jsonPrimitive?.intOrNull ?: return@forEach
                    val runs = buildRuns(obj["content"]?.jsonArray ?: JsonArray(emptyList()))
                    val plainText = runs.joinToString(" ") { it.text }.replace(Regex("\\s+"), " ").trim()
                    if (plainText.isNotBlank()) {
                        verses += ParsedVerse(
                            number = number,
                            plainText = plainText,
                            richContent = StoredVerseContent(headings = pendingHeadings.toList(), runs = runs)
                        )
                        pendingHeadings.clear()
                    }
                }

                else -> {} // "line_break" and anything else carries no info we need beyond what's already in runs
            }
        }
        return verses
    }

    private fun flattenStringArray(items: JsonArray?): String =
        items.orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            .joinToString(" ").replace(Regex("\\s+"), " ").trim()

    /**
     * A verse's own content[] mixes plain strings with objects like
     * {"text":..., "poem":N, "wordsOfJesus":true}, {"lineBreak":true},
     * {"heading":...}, and {"noteId":N}. Footnote markers and inline headings
     * are dropped here — footnotes are stage 4's job, and inline headings
     * (rare) aren't distinguishable from section headings in the UI yet.
     */
    private fun buildRuns(items: JsonArray): List<VerseRun> {
        val runs = mutableListOf<VerseRun>()
        var pendingLineBreak = false

        items.forEach { item ->
            when (item) {
                is JsonPrimitive -> {
                    val text = item.contentOrNull
                    if (!text.isNullOrEmpty()) {
                        runs += VerseRun(text = text, lineBreakBefore = pendingLineBreak)
                        pendingLineBreak = false
                    }
                }
                is JsonObject -> {
                    when {
                        item["lineBreak"]?.jsonPrimitive?.booleanOrNull == true -> pendingLineBreak = true
                        item["text"] != null -> {
                            val text = item["text"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                            runs += VerseRun(
                                text = text,
                                isWordsOfJesus = item["wordsOfJesus"]?.jsonPrimitive?.booleanOrNull == true,
                                poemLevel = item["poem"]?.jsonPrimitive?.intOrNull,
                                lineBreakBefore = pendingLineBreak
                            )
                            pendingLineBreak = false
                        }
                        // {"noteId": N} footnote references and inline {"heading": ...} markers: no text to append
                    }
                }
                else -> {}
            }
        }
        return runs
    }
}
