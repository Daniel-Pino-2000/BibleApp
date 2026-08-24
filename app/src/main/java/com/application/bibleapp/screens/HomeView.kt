package com.application.bibleapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.ReadingStyle
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.ui.theme.scaledBy
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun HomeView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onContinueReadingClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val verseOfTheDay by bibleViewModel.verseOfTheDay.collectAsState()
    val verseTextScale by bibleViewModel.verseTextScale.collectAsState()
    val currentBook by bibleViewModel.currentBook.collectAsState()
    val currentChapter by bibleViewModel.currentChapter.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        verseOfTheDay?.let { verse ->
            VerseOfTheDayCard(
                reference = verse.reference,
                text = verse.text,
                textScale = verseTextScale.multiplier
            )
        }

        HomeShortcutRow(
            icon = Icons.Filled.MenuBook,
            title = "Continue Reading",
            subtitle = "${BibleBooks.getBookById(currentBook)?.name ?: "Unknown"} $currentChapter",
            onClick = onContinueReadingClick
        )

        HomeShortcutRow(
            icon = Icons.Filled.Search,
            title = "Search the Bible",
            subtitle = "Find a verse by keyword",
            onClick = onSearchClick
        )
    }
}

@Composable
private fun VerseOfTheDayCard(reference: String, text: String, textScale: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "Verse of the Day",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = ReadingStyle.VerseText.scaledBy(textScale),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = reference,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
