package com.application.bibleapp.components

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.VerseUI

@Composable
fun BibleText(
    verses: List<VerseUI>,
    scrollToIndex: Int,
    modifier: Modifier = Modifier
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

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 5.dp, horizontal = 5.dp)
    ) {
        items(verses) { verse ->
            Text(text = verseAnnotatedString(verse, wordsOfJesusColor))
        }
    }
}

/** Builds "<number> <text>" with words-of-Jesus runs colored, falling back to plain text for legacy rows. */
private fun verseAnnotatedString(verse: VerseUI, wordsOfJesusColor: Color): AnnotatedString {
    val runs = verse.richContent?.runs
    if (runs.isNullOrEmpty()) {
        return AnnotatedString("${verse.verse} ${verse.text}")
    }
    return buildAnnotatedString {
        append("${verse.verse} ")
        runs.forEachIndexed { index, run ->
            if (index > 0) append(" ")
            if (run.isWordsOfJesus) {
                withStyle(SpanStyle(color = wordsOfJesusColor)) { append(run.text) }
            } else {
                append(run.text)
            }
        }
    }
}
