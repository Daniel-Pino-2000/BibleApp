package com.application.bibleapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.bibleapp.data.model.StoredHeading
import com.application.bibleapp.data.model.VerseRun
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.ui.theme.ReadingStyle
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.ui.theme.scaledBy

/** Indentation per poem nesting level — enough to read as a poetry line, not just a wrapped paragraph. */
private val POEM_INDENT_STEP = 20.sp

/** Generic marker for every footnote — the API's `caller` is usually just "auto-generate", so a
 *  fixed symbol avoids the complexity of sequential per-chapter lettering for a first pass. */
private const val FOOTNOTE_MARKER = "†"

/** Comfortable line length on wide/tablet screens — text doesn't stretch edge to edge. */
private val MAX_READING_WIDTH = 640.dp

/** A stray pilcrow the plain-text fallback (no rich content) can't turn into a real break. */
private const val LEGACY_PARAGRAPH_MARKER = "¶ "

/** One or more consecutive verses that read as a single flowing paragraph. */
private data class VerseParagraph(val verses: List<VerseUI>)

/**
 * Groups verses at real paragraph boundaries only: the chapter's first verse, any verse
 * carrying a heading (a new section always starts a fresh paragraph), or a verse whose
 * own first run was marked [VerseRun.paragraphBreakBefore] by the parser. Everything else
 * continues the paragraph already in progress.
 *
 * If NO verse in the chapter carries a heading or paragraph-break marker at all — the
 * bundled KJV has no rich content whatsoever, and a downloaded translation's source text
 * may never mark paragraph starts — grouping this way would merge the *entire chapter*
 * into a single paragraph. [BibleText] scrolls to a verse by scrolling to the paragraph
 * (list item) containing it, so a single mega-paragraph makes every verse in the chapter
 * resolve to the same item — scrolling to verse 16 lands wherever verse 1 is. Falling back
 * to one verse per paragraph keeps every verse individually scrollable; a translation that
 * does have real paragraph data is unaffected.
 */
private fun groupIntoParagraphs(verses: List<VerseUI>): List<VerseParagraph> {
    val hasParagraphStructure = verses.any { verse ->
        !verse.richContent?.headings.isNullOrEmpty() ||
            verse.richContent?.runs?.firstOrNull()?.paragraphBreakBefore == true
    }
    if (!hasParagraphStructure) {
        return verses.map { VerseParagraph(listOf(it)) }
    }

    val groups = mutableListOf<MutableList<VerseUI>>()
    verses.forEach { verse ->
        val hasHeading = !verse.richContent?.headings.isNullOrEmpty()
        val startsNewParagraph = verse.richContent?.runs?.firstOrNull()?.paragraphBreakBefore == true
        if (groups.isEmpty() || hasHeading || startsNewParagraph) {
            groups += mutableListOf(verse)
        } else {
            groups.last() += verse
        }
    }
    return groups.map { VerseParagraph(it) }
}

@Composable
fun BibleText(
    verses: List<VerseUI>,
    scrollToIndex: Int,
    chapterTitle: String,
    modifier: Modifier = Modifier,
    textScale: Float = 1f,
    onFootnoteClick: (Int) -> Unit = {}
) {
    // Create a new LazyListState each time the verses list changes
    val listState = remember(verses) { androidx.compose.foundation.lazy.LazyListState() }
    val paragraphs = remember(verses) { groupIntoParagraphs(verses) }

    // Scroll to the paragraph containing the desired verse whenever verses or the
    // scroll target changes — paragraphs, not verses, are the list's item granularity.
    // +1 because the chapter title occupies list item 0, ahead of every paragraph.
    LaunchedEffect(paragraphs, scrollToIndex) {
        if (paragraphs.isEmpty()) return@LaunchedEffect
        val targetIndex = paragraphs.indexOfFirst { paragraph ->
            paragraph.verses.any { it.verse == scrollToIndex }
        }
        listState.scrollToItem((targetIndex.coerceIn(paragraphs.indices)) + 1)
    }

    // Material's "error" role is spec'd to always be a red-family tone in both light
    // and dark schemes (and under dynamic color), so it doubles as a theme-aware
    // red-letter color without a hardcoded hex that could look wrong in dark mode.
    val wordsOfJesusColor = MaterialTheme.colorScheme.error
    val footnoteColor = MaterialTheme.colorScheme.primary
    val verseNumberColor = MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = Spacing.lg, horizontal = Spacing.lg)
    ) {
        item {
            ChapterHeader(chapterTitle, textScale)
        }
        itemsIndexed(paragraphs) { index, paragraph ->
            val headings = paragraph.verses.first().richContent?.headings.orEmpty()

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Column(modifier = Modifier.widthIn(max = MAX_READING_WIDTH).fillMaxWidth()) {
                    headings.forEach { heading ->
                        HeadingText(heading, textScale)
                    }
                    Text(
                        text = paragraphAnnotatedString(
                            paragraph.verses, wordsOfJesusColor, footnoteColor, verseNumberColor, textScale, onFootnoteClick
                        ),
                        style = ReadingStyle.VerseText.scaledBy(textScale),
                        color = MaterialTheme.colorScheme.onSurface,
                        // A heading already reads as a section break on its own — only add the
                        // paragraph gap above paragraphs that start without one.
                        modifier = Modifier.padding(
                            top = if (index > 0 && headings.isEmpty()) Spacing.xl else 0.dp,
                            bottom = Spacing.xs
                        )
                    )
                }
            }
        }
    }
}

/**
 * The chapter's own title (e.g. "Genesis 1") — shown above every chapter regardless of
 * what the translation's own markup does or doesn't have, so there's always at least
 * one clear heading at the top of the reading surface. Centered within the same
 * max-width column as the verse text so it stays aligned with the body on wide screens.
 */
@Composable
private fun ChapterHeader(title: String, textScale: Float) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = MAX_READING_WIDTH).fillMaxWidth()) {
            Text(
                text = title,
                style = ReadingStyle.ChapterTitle.scaledBy(textScale),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.md)
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )
        }
    }
}

/** A section heading or Hebrew subtitle preceding the verse it introduces. */
@Composable
private fun HeadingText(heading: StoredHeading, textScale: Float) {
    if (heading.type == "hebrew_subtitle") {
        Text(
            text = heading.text,
            style = ReadingStyle.HebrewSubtitle.scaledBy(textScale),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.xs)
        )
    } else {
        Text(
            text = heading.text,
            style = ReadingStyle.SectionHeading.scaledBy(textScale),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs)
        )
    }
}

/**
 * Builds one flowing paragraph's worth of text from however many verses belong to it:
 * verse numbers render as small superscript markers inline (not line-starts), words-of-Jesus
 * runs are colored, poem-tagged runs each get their own indented line via [ParagraphStyle],
 * and a clickable superscript marker follows any run with an attached footnote. Verses with
 * no rich content at all (legacy rows) fall back to their plain text.
 */
private fun paragraphAnnotatedString(
    verses: List<VerseUI>,
    wordsOfJesusColor: Color,
    footnoteColor: Color,
    verseNumberColor: Color,
    textScale: Float,
    onFootnoteClick: (Int) -> Unit
): AnnotatedString {
    val verseNumberStyle = ReadingStyle.VerseNumber.scaledBy(textScale).toSpanStyle().copy(
        color = verseNumberColor,
        baselineShift = BaselineShift.Superscript
    )

    return buildAnnotatedString {
        var isFirstRunInParagraph = true

        verses.forEach { verse ->
            val runs = verse.richContent?.runs

            if (runs.isNullOrEmpty()) {
                if (!isFirstRunInParagraph) append(" ")
                appendVerseNumber(verse.verse, verseNumberStyle)
                append(verse.text.removePrefix(LEGACY_PARAGRAPH_MARKER))
                isFirstRunInParagraph = false
                return@forEach
            }

            runs.forEachIndexed { index, run ->
                val isFirstRunOfVerse = index == 0
                val startsNewLine = run.poemLevel != null || (run.lineBreakBefore && !isFirstRunInParagraph)

                if (startsNewLine) {
                    val indent = POEM_INDENT_STEP * (run.poemLevel ?: 0)
                    withStyle(ParagraphStyle(textIndent = TextIndent(firstLine = indent, restLine = indent))) {
                        if (isFirstRunOfVerse) appendVerseNumber(verse.verse, verseNumberStyle)
                        appendRun(run, wordsOfJesusColor, footnoteColor, textScale, onFootnoteClick)
                    }
                } else {
                    if (!isFirstRunInParagraph) append(" ")
                    if (isFirstRunOfVerse) appendVerseNumber(verse.verse, verseNumberStyle)
                    appendRun(run, wordsOfJesusColor, footnoteColor, textScale, onFootnoteClick)
                }
                isFirstRunInParagraph = false
            }
        }
    }
}

private fun AnnotatedString.Builder.appendVerseNumber(verseNumber: Int?, style: SpanStyle) {
    withStyle(style) { append("${verseNumber ?: ""}") }
    append(" ")
}

private fun AnnotatedString.Builder.appendRun(
    run: VerseRun,
    wordsOfJesusColor: Color,
    footnoteColor: Color,
    textScale: Float,
    onFootnoteClick: (Int) -> Unit
) {
    if (run.isWordsOfJesus) {
        withStyle(SpanStyle(color = wordsOfJesusColor)) { append(run.text) }
    } else {
        append(run.text)
    }

    val noteId = run.footnoteId ?: return
    withLink(LinkAnnotation.Clickable(tag = "footnote-$noteId") { onFootnoteClick(noteId) }) {
        withStyle(
            SpanStyle(
                color = footnoteColor,
                fontSize = 11.sp * textScale,
                baselineShift = BaselineShift.Superscript
            )
        ) {
            append(FOOTNOTE_MARKER)
        }
    }
}
