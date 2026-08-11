package com.application.bibleapp.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.bibleapp.data.model.StoredHeading
import com.application.bibleapp.data.model.VerseRun
import com.application.bibleapp.data.model.VerseUI

/** Indentation per poem nesting level — enough to read as a poetry line, not just a wrapped paragraph. */
private val POEM_INDENT_STEP = 20.sp

/** Generic marker for every footnote — the API's `caller` is usually just "auto-generate", so a
 *  fixed symbol avoids the complexity of sequential per-chapter lettering for a first pass. */
private const val FOOTNOTE_MARKER = "†"

@Composable
fun BibleText(
    verses: List<VerseUI>,
    scrollToIndex: Int,
    modifier: Modifier = Modifier,
    onFootnoteClick: (Int) -> Unit = {}
) {
    // Create a new LazyListState each time the verses list changes
    val listState = remember(verses) { androidx.compose.foundation.lazy.LazyListState() }

    // Scroll to the desired verse whenever verses or scroll index change
    LaunchedEffect(verses.size, scrollToIndex) {
        val index = (scrollToIndex - 1).coerceIn(verses.indices)
        listState.scrollToItem(index)
    }

    // Material's "error" role is spec'd to always be a red-family tone in both light
    // and dark schemes (and under dynamic color), so it doubles as a theme-aware
    // red-letter color without a hardcoded hex that could look wrong in dark mode.
    val wordsOfJesusColor = MaterialTheme.colorScheme.error
    val footnoteColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 5.dp, horizontal = 5.dp)
    ) {
        items(verses) { verse ->
            verse.richContent?.headings?.forEach { heading ->
                HeadingText(heading)
            }
            Text(text = verseAnnotatedString(verse, wordsOfJesusColor, footnoteColor, onFootnoteClick))
        }
    }
}

/** A section heading or Hebrew subtitle preceding the verse it introduces. */
@Composable
private fun HeadingText(heading: StoredHeading) {
    if (heading.type == "hebrew_subtitle") {
        Text(
            text = heading.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
    } else {
        Text(
            text = heading.text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
    }
}

/**
 * Builds "<number> <text>" with words-of-Jesus runs colored, poem lines broken onto
 * their own indented line, and a clickable superscript marker after any run with an
 * attached footnote — falling back to plain text for legacy rows.
 *
 * Each poem-tagged run (or any run with an explicit line break before it) gets its
 * own [ParagraphStyle] — Compose treats a ParagraphStyle span as a hard paragraph
 * break, so this both breaks the line and applies the indent in one step. A run
 * with no poem level and no line break just continues the current paragraph,
 * space-separated, same as plain prose.
 */
private fun verseAnnotatedString(
    verse: VerseUI,
    wordsOfJesusColor: Color,
    footnoteColor: Color,
    onFootnoteClick: (Int) -> Unit
): AnnotatedString {
    val runs = verse.richContent?.runs
    if (runs.isNullOrEmpty()) {
        return AnnotatedString("${verse.verse} ${verse.text}")
    }
    return buildAnnotatedString {
        runs.forEachIndexed { index, run ->
            val numberPrefix = if (index == 0) "${verse.verse} " else ""
            val startsNewLine = run.poemLevel != null || (run.lineBreakBefore && index > 0)

            if (startsNewLine) {
                val indent = POEM_INDENT_STEP * (run.poemLevel ?: 0)
                withStyle(ParagraphStyle(textIndent = TextIndent(firstLine = indent, restLine = indent))) {
                    append(numberPrefix)
                    appendRun(run, wordsOfJesusColor, footnoteColor, onFootnoteClick)
                }
            } else {
                if (index > 0) append(" ")
                append(numberPrefix)
                appendRun(run, wordsOfJesusColor, footnoteColor, onFootnoteClick)
            }
        }
    }
}

private fun AnnotatedString.Builder.appendRun(
    run: VerseRun,
    wordsOfJesusColor: Color,
    footnoteColor: Color,
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
                fontSize = 11.sp,
                baselineShift = BaselineShift.Superscript
            )
        ) {
            append(FOOTNOTE_MARKER)
        }
    }
}
