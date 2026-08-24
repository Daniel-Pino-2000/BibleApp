package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.Footnote
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
 * line breaks (stage 2), section headings (stage 3), and footnote markers
 * (stage 4) — see docs at https://bible.helloao.org/docs/reference/.
 *
 * Headings/hebrew_subtitle are chapter-level entries that precede whichever
 * verse comes next; they're captured on [StoredVerseContent.headings] here.
 *
 * Pure/no Android dependency, so it's plain-JVM testable.
 */
object HelloAoContentParser {

    /**
     * Walks one chapter's `content[]` in order, accumulating "heading"/"hebrew_subtitle"
     * entries until the next "verse" entry claims them. A verse with markers but no
     * actual text (e.g. only a stray `{"noteId":N}`) is dropped — there's nothing to
     * store or render for it.
     *
     * A chapter-level (not inline) `{"type":"line_break"}` entry is a translation's
     * *structured* paragraph/stanza break — the alternative to baking a `¶` into verse
     * text (see [extractParagraphBreak]). Some translations (e.g. BSB) use this instead
     * of pilcrows and never emit a pilcrow at all, so it's tracked the same way pending
     * headings are: it marks whichever verse comes next as starting a new paragraph,
     * same effect as [VerseRun.paragraphBreakBefore] set from a pilcrow.
     */
    fun extractVerses(content: List<JsonElement>): List<ParsedVerse> {
        val verses = mutableListOf<ParsedVerse>()
        val pendingHeadings = mutableListOf<StoredHeading>()
        var pendingParagraphBreak = false

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

                "line_break" -> pendingParagraphBreak = true

                "verse" -> {
                    val number = obj["number"]?.jsonPrimitive?.intOrNull ?: return@forEach
                    val runs = buildRuns(obj["content"]?.jsonArray ?: JsonArray(emptyList()))
                        .let { parsedRuns ->
                            if (pendingParagraphBreak && parsedRuns.isNotEmpty()) {
                                parsedRuns.toMutableList().also {
                                    it[0] = it[0].copy(paragraphBreakBefore = true)
                                }
                            } else {
                                parsedRuns
                            }
                        }
                    pendingParagraphBreak = false
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

                else -> {} // anything else carries no info we need beyond what's already in runs
            }
        }
        return verses
    }

    /** Maps a chapter's footnotes[] to plain records keyed by noteId, dropping any without a verse reference. */
    fun extractFootnotes(footnotes: List<HelloAoFootnoteDto>): List<Footnote> =
        footnotes.mapNotNull { dto ->
            val verse = dto.reference?.verse ?: return@mapNotNull null
            Footnote(noteId = dto.noteId, verse = verse, caller = dto.caller, text = dto.text)
        }

    private fun flattenStringArray(items: JsonArray?): String =
        items.orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            .joinToString(" ").replace(Regex("\\s+"), " ").trim()

    private const val PARAGRAPH_MARKER = '¶'

    /**
     * The API has no structured paragraph field — a pilcrow baked into the start of a
     * run's own text is the only signal a translation gives that this run begins a new
     * paragraph. Strips the glyph (and the space that follows it) out of the visible
     * text and reports whether it was there, so the renderer can turn it into a real
     * paragraph break instead of a stray character.
     */
    private fun extractParagraphBreak(rawText: String): Pair<String, Boolean> {
        val trimmed = rawText.trimStart()
        return if (trimmed.startsWith(PARAGRAPH_MARKER)) {
            trimmed.removePrefix(PARAGRAPH_MARKER.toString()).trimStart() to true
        } else {
            rawText to false
        }
    }

    /**
     * A verse's own content[] mixes plain strings with objects like
     * {"text":..., "poem":N, "wordsOfJesus":true}, {"lineBreak":true},
     * {"heading":...}, and {"noteId":N}. A {"noteId":N} marker has no text of
     * its own — it attaches to whichever run immediately precedes it (that's
     * where it appears in the source, e.g. Genesis 1:3's
     * "...Let there be light," {"noteId":0} "and there was light."), so the
     * footnote indicator renders right after the text it annotates. Inline
     * {"heading":...} markers are dropped — rare, and not distinguishable
     * from section headings in the UI.
     */
    private fun buildRuns(items: JsonArray): List<VerseRun> {
        val runs = mutableListOf<VerseRun>()
        var pendingLineBreak = false

        items.forEach { item ->
            when (item) {
                is JsonPrimitive -> {
                    val rawText = item.contentOrNull
                    if (!rawText.isNullOrEmpty()) {
                        val (text, paragraphBreak) = extractParagraphBreak(rawText)
                        if (text.isNotEmpty()) {
                            runs += VerseRun(
                                text = text,
                                lineBreakBefore = pendingLineBreak,
                                paragraphBreakBefore = paragraphBreak
                            )
                            pendingLineBreak = false
                        }
                    }
                }
                is JsonObject -> {
                    when {
                        item["lineBreak"]?.jsonPrimitive?.booleanOrNull == true -> pendingLineBreak = true
                        item["noteId"] != null -> {
                            val noteId = item["noteId"]?.jsonPrimitive?.intOrNull
                            if (noteId != null && runs.isNotEmpty()) {
                                runs[runs.lastIndex] = runs.last().copy(footnoteId = noteId)
                            }
                        }
                        item["text"] != null -> {
                            val rawText = item["text"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                            val (text, paragraphBreak) = extractParagraphBreak(rawText)
                            runs += VerseRun(
                                text = text,
                                isWordsOfJesus = item["wordsOfJesus"]?.jsonPrimitive?.booleanOrNull == true,
                                poemLevel = item["poem"]?.jsonPrimitive?.intOrNull,
                                lineBreakBefore = pendingLineBreak,
                                paragraphBreakBefore = paragraphBreak
                            )
                            pendingLineBreak = false
                        }
                    }
                }
                else -> {}
            }
        }
        return runs
    }
}
